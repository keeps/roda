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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VeraPDFPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VeraPDFPlugin.class);
  private String profile;
  private boolean hasFeatures = false;
  private boolean hasPartialSuccessOnOutcome;

  public VeraPDFPlugin() {
    profile = "1b";
    hasFeatures = false;
    hasPartialSuccessOnOutcome = Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("tools", "allplugins", "hasPartialSuccessOnOutcome"));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "PDF/A format validator";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "PDF/A format validator using VeraPDF.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
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

  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.createPluginReport(this);

    for (AIP aip : list) {
      LOGGER.debug("Processing AIP {}", aip.getId());

      for (Representation representation : aip.getRepresentations()) {
        List<String> resourceList = new ArrayList<String>();
        // FIXME 20160324 hsilva: the report item should be at AIP level (and
        // not representation level)
        Report reportItem = PluginHelper.createPluginReportItem(this, representation.getId(), null);
        PluginState pluginResultState = PluginState.SUCCESS;
        StringBuilder details = new StringBuilder();

        try {
          LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());

          boolean recursive = true;
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
            representation.getId(), recursive);

          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();
              LOGGER.debug("Processing file: {}", file);
              if (!file.isDirectory()) {
                IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
                String fileMimetype = ifile.getFileFormat().getMimeType();
                String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());

                if ((fileFormat.equalsIgnoreCase("pdf") || fileMimetype.equals("application/pdf"))) {
                  LOGGER.debug("Running veraPDF validator on {}", file.getId());
                  StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                  DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
                  Path veraPDFResult = VeraPDFPluginUtils.runVeraPDF(directAccess.getPath(), profile, hasFeatures);

                  if (veraPDFResult != null) {
                    ContentPayload payload = new FSPathContentPayload(veraPDFResult);
                    InputStream inputStream = payload.createInputStream();
                    String xmlReport = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    IOUtils.closeQuietly(inputStream);

                    Pattern pattern = Pattern.compile("<validationReport.*?compliant=\"false\">");
                    Matcher matcher = pattern.matcher(xmlReport);

                    if (matcher.find()) {
                      resourceList.add(file.getId());
                      pluginResultState = PluginState.PARTIAL_SUCCESS;
                      details.append(xmlReport.substring(xmlReport.indexOf('\n') + 1));
                    }

                  } else {
                    pluginResultState = PluginState.PARTIAL_SUCCESS;
                  }

                  IOUtils.closeQuietly(directAccess);
                }
              }
            } else {
              LOGGER.error("Cannot process AIP representation file", oFile.getCause());
            }
          }

          reportItem.setPluginState(pluginResultState);
          IOUtils.closeQuietly(allFiles);
        } catch (Throwable e) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          pluginResultState = PluginState.FAILURE;
          reportItem.setPluginState(pluginResultState);
        } finally {
          LOGGER.debug("Creating veraPDF event for the representation {}", representation.getId());
          report.addReport(reportItem);
          createEvent(resourceList, aip, representation.getId(), model, index, pluginResultState, details);
        }
      }
    }

    return report;
  }

  private void createEvent(List<String> resourceList, AIP aip, String representationId, ModelService model,
    IndexService index, PluginState pluginState, StringBuilder details) throws PluginException {
    String outcomeDetails = null;
    try {
      // building the detail extension for the plugin event
      StringBuilder noteStringBuilder = new StringBuilder();
      StringBuilder detailsStringBuilder = new StringBuilder();
      noteStringBuilder.append("The following files did not pass veraPDF's validation with success: ");
      detailsStringBuilder.append("\n\n<reportWrapper>");

      for (String fileID : resourceList) {
        noteStringBuilder.append(fileID + ", ");
      }

      noteStringBuilder.setLength(noteStringBuilder.length() - 2);
      detailsStringBuilder.append(details);
      detailsStringBuilder.append("</reportWrapper>");
      noteStringBuilder.append(detailsStringBuilder);

      // all file have passed the validation
      if (pluginState == PluginState.SUCCESS)
        noteStringBuilder.setLength(0);

      // veraPDF plugin did not run correctly
      if (pluginState == PluginState.FAILURE
        || (pluginState == PluginState.PARTIAL_SUCCESS && hasPartialSuccessOnOutcome == false)) {
        pluginState = PluginState.FAILURE;
        noteStringBuilder.setLength(0);
      }

      outcomeDetails = noteStringBuilder.toString();

    } catch (Throwable e) {
      pluginState = PluginState.FAILURE;
      outcomeDetails = e.getMessage();
      LOGGER.error("Error executing VeraPDF plugin: " + e.getMessage(), e);
    }
    boolean notify = false;

    try {
      // TODO fix linking identifiers
      PluginHelper
        .createPluginEvent(
          this, aip.getId(), model, index, Arrays.asList(PluginHelper.getLinkingIdentifier(aip.getId(),
            representationId, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)),
          null, pluginState, outcomeDetails, notify);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.error("Error creating event: " + e.getMessage(), e);
    }

  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
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
    return "Checked if PDF files were veraPDF valid.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "PDF files were veraPDF validated.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to validate the PDF files with veraPDF.";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

}
