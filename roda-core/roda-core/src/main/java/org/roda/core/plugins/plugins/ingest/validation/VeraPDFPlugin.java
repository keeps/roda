/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
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

public class VeraPDFPlugin extends AbstractPlugin<AIP> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private String profile = "1b";
  private boolean hasFeatures = false;
  private long maxKbytes = 20000; // default 20000 kb
  private boolean hasPartialSuccessOnOutcome = Boolean
    .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("tools", "allplugins", "hasPartialSuccessOnOutcome"));

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
    return "PDF/A format validator";
  }

  @Override
  public String getDescription() {
    return "PDF/A format validator using VeraPDF.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
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

  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    for (AIP aip : list) {
      logger.debug("Processing AIP " + aip.getId());

      for (Representation representation : aip.getRepresentations()) {
        Map<String, Path> resourceList = new HashMap<>();
        int state = 1; // success

        try {
          logger.debug("Processing representation " + representation.getId() + " of AIP " + aip.getId());

          boolean recursive = true;
          CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), representation.getId(), recursive);

          for (File file : allFiles) {
            logger.debug("Processing file: " + file);

            if (!file.isDirectory()) {
              IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
              String fileMimetype = ifile.getFileFormat().getMimeType();
              String filePronom = ifile.getFileFormat().getPronom();
              String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());

              if ((fileFormat.equalsIgnoreCase("pdf") || fileMimetype.equals("application/pdf"))
                && ifile.getSize() < (maxKbytes * 1024)) {

                StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                Binary binary = storage.getBinary(fileStoragePath);

                // FIXME file that doesn't get deleted afterwards
                logger.debug("Running veraPDF validator on " + file.getId());
                Path veraPDFResult = VeraPDFPluginUtils.runVeraPDF(binary.getContent(), file.getId(), profile,
                  hasFeatures);

                if (veraPDFResult != null) {
                  resourceList.put(file.getId(), veraPDFResult);
                } else {
                  state = 2; // partial success or failure
                }
              }
            } else {

              logger.debug("veraPDF validation did not run on file: " + file);
            }
          }
          IOUtils.closeQuietly(allFiles);
        } catch (Throwable e) {
          logger.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          state = 0; // failure
        }

        logger.debug("Creating veraPDF event for the representation " + representation.getId());
        createEvent(resourceList, aip, representation.getId(), model, state);
      }
    }

    return null;
  }

  private void createEvent(Map<String, Path> resourceList, AIP aip, String representationId, ModelService model,
    int state) throws PluginException {
    PluginState pluginState = PluginState.SUCCESS;
    String outcomeDetails = null;
    try {
      // building the detail extension for the plugin event

      StringBuilder noteStringBuilder = new StringBuilder();
      StringBuilder detailsStringBuilder = new StringBuilder();
      noteStringBuilder.append("The following files did not pass veraPDF's validation with success: ");
      detailsStringBuilder.append("<reportWrapper>");

      for (String fileID : resourceList.keySet()) {
        Path veraPDFResult = resourceList.get(fileID);
        Binary b = (Binary) FSUtils.convertPathToResource(veraPDFResult.getParent(), veraPDFResult);

        // using XPath to discover if the validation was successful or not
        XPath xpath = XPathFactory.newInstance().newXPath();
        InputStream inputStream = b.getContent().createInputStream();
        String xmlReport = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        InputSource inputSource = new InputSource(xmlReport);
        NodeList nodes = (NodeList) xpath.evaluate("//*[@compliant='false']", inputSource, XPathConstants.NODESET);

        if (nodes.getLength() > 0) {
          noteStringBuilder.append(fileID + ", ");
          detailsStringBuilder.append(xmlReport.substring(xmlReport.indexOf('\n') + 1));
          state = 2;
        }

        IOUtils.closeQuietly(inputStream);
      }

      noteStringBuilder.setLength(noteStringBuilder.length() - 2);
      detailsStringBuilder.append("</reportWrapper>");

      // all file have passed the validation
      if (state == 1) {
        pluginState = PluginState.SUCCESS;
        noteStringBuilder.setLength(0);
        detailsStringBuilder.setLength(0);
      }
      // veraPDF plugin did not run correctly
      if (state == 0 || (state == 2 && hasPartialSuccessOnOutcome == false)) {
        pluginState = PluginState.FAILURE;
        noteStringBuilder.setLength(0);
        detailsStringBuilder.setLength(0);
      }
      // some files did not pass the verification
      if (state == 2 && hasPartialSuccessOnOutcome == true) {
        pluginState = PluginState.PARTIAL_SUCCESS;
      }

      logger.debug("The veraPDF validation on the representation " + representationId + " of AIP " + aip.getId()
        + " finished with a status: " + pluginState.name() + ".");

      outcomeDetails = noteStringBuilder.toString();
    } catch (Throwable e) {
      pluginState = PluginState.FAILURE;
      outcomeDetails = e.getMessage();
      logger.error("Error executing VeraPDF plugin: " + e.getMessage(), e);
    }
    boolean notify = false;

    try {
      // TODO fix linking identifiers
      PluginHelper.createPluginEvent(this, aip.getId(), null, null, null, model, null, null, pluginState,
        outcomeDetails, notify);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | ValidationException | AlreadyExistsException e) {
      logger.error("Error creating event: " + e.getMessage(), e);
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

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.FORMAT_VALIDATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Checked if digital signatures were valid.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Digital signatures were valid.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to validate the digital signature or invalid signature.";
  }

}
