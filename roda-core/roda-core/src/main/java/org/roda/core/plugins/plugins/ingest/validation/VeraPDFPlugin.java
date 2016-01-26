/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class VeraPDFPlugin implements Plugin<AIP> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private String profile = "1b";
  private boolean hasFeatures = false;
  private long maxKbytes = 20000; // default 20000kb
  private boolean hasPartialSuccessOnOutcome = true; // default with partial
                                                     // success included

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "VeraPDF validation action";
  }

  @Override
  public String getDescription() {
    return "Validates PDFA format of the AIP files";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // indicates what validation profile will be used
    if (parameters.containsKey("profile")) {
      profile = parameters.get("profile");
    }

    // indicates if the final report will integrate the features information
    if (parameters.containsKey("hasFeatures")) {
      hasFeatures = Boolean.parseBoolean(parameters.get("hasFeatures"));
    }

    // indicates the maximum kbytes the files that will be processed must have
    if (parameters.containsKey("maxKbytes")) {
      maxKbytes = Long.parseLong(parameters.get("maxKbytes"));
    }

    // indicates the outcome possible types: success, partial success (if true),
    // failure
    if (parameters.containsKey("hasPartialSuccessOnOutcome")) {
      hasPartialSuccessOnOutcome = Boolean.parseBoolean(parameters.get("hasPartialSuccessOnOutcome"));
    }

  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    for (AIP aip : list) {
      logger.debug("Processing AIP " + aip.getId());

      for (String representationID : aip.getRepresentationIds()) {
        Map<String, Path> resourceList = new HashMap<>();
        int state = 1; // success

        try {
          logger.debug("Processing representation " + representationID + " of AIP " + aip.getId());

          Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
          Iterable<File> allFiles = model.listAllFiles(aip.getId(), representationID);
          for (File file : allFiles) {
            logger.debug("Processing file: " + file);

            if (!file.isDirectory()) {
              // TODO filter by file type and size
              // file.getOriginalName().endsWith(".pdf") && (file.getSize() <=
              // maxKbytes * 1024)
              StoragePath storagePath = ModelUtils.getRepresentationFilePath(file);
              Binary binary = storage.getBinary(storagePath);

              // FIXME file that doesn't get deleted afterwards
              logger.debug("Running veraPDF validator on " + file.getId());
              Path veraPDFResult = VeraPDFPluginUtils.runVeraPDF(binary.getContent().createInputStream(), file.getId(),
                profile, hasFeatures);

              if (veraPDFResult != null) {
                resourceList.put(file.getId(), veraPDFResult);
              } else {
                state = 2; // partial success or failure
              }
            } else {
              logger.debug("veraPDF validation did not run on file: " + file);
            }
          }
        } catch (Throwable e) {
          logger.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          state = 0; // failure
        }

        logger.debug("Creating veraPDF event for the representation " + representationID);
        createEvent(resourceList, aip, representationID, model, state);
      }
    }

    return null;
  }

  private void createEvent(Map<String, Path> resourceList, AIP aip, String representationID, ModelService model,
    int state) throws PluginException {

    try {
      // building the detail extension for the plugin event
      String outcome = "success";
      StringBuilder noteStringBuilder = new StringBuilder();
      StringBuilder detailsStringBuilder = new StringBuilder();
      noteStringBuilder.append("The following files did not pass veraPDF's validation with success: ");
      detailsStringBuilder.append("<reportWrapper>");

      for (String fileID : resourceList.keySet()) {
        Path veraPDFResult = resourceList.get(fileID);
        Binary b = (Binary) FSUtils.convertPathToResource(veraPDFResult.getParent(), veraPDFResult);

        // using XPath to discover if the validation was successful or not
        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource inputSource = new InputSource(b.getContent().createInputStream());
        NodeList nodes = (NodeList) xpath.evaluate("//*[@compliant='false']", inputSource, XPathConstants.NODESET);

        if (nodes.getLength() > 0) {
          noteStringBuilder.append(fileID + ", ");
          String xmlReport = IOUtils.toString(b.getContent().createInputStream(), StandardCharsets.UTF_8);
          detailsStringBuilder.append(xmlReport.substring(xmlReport.indexOf('\n') + 1));
          state = 2;
        }
      }

      noteStringBuilder.setLength(noteStringBuilder.length() - 2);
      detailsStringBuilder.append("</reportWrapper>");

      // all file have passed the validation
      if (state == 1) {
        outcome = "success";
        noteStringBuilder.setLength(0);
        detailsStringBuilder.setLength(0);
      }
      // veraPDF plugin did not run correctly
      if (state == 0 || (state == 2 && hasPartialSuccessOnOutcome == false)) {
        outcome = "failure";
        noteStringBuilder.setLength(0);
        detailsStringBuilder.setLength(0);
      }
      // some files did not pass the verification
      if (state == 2 && hasPartialSuccessOnOutcome == true) {
        outcome = "partial success";
      }

      logger.debug("The veraPDF validation on the representation " + representationID + " of AIP " + aip.getId()
        + " finished with a status: " + outcome + ".");

      // FIXME revise PREMIS generation
      PluginHelper.createPluginEventAndAgent(aip.getId(), representationID, model,
        EventPreservationObject.PRESERVATION_EVENT_TYPE_FORMAT_VALIDATION,
        "All the files from the AIP were submitted to a veraPDF validation.",
        EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_VALIDATION_TASK, "veraPDFChecker",
        Arrays.asList(representationID), outcome, noteStringBuilder.toString(), detailsStringBuilder.toString(),
        getClass().getName() + "@" + getVersion(),
        AgentPreservationObject.PRESERVATION_AGENT_TYPE_VERAPDF_CHECK_PLUGIN);

    } catch (Throwable e) {
      throw new PluginException(e.getMessage(), e);
    }

  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new VeraPDFPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
