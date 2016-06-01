/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

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
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
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
import org.roda.core.plugins.orchestrate.JobException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.verapdf.core.VeraPDFException;

public class VeraPDFPlugin<T extends Serializable> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VeraPDFPlugin.class);
  private String profile;
  private boolean hasFeatures = false;
  private boolean hasPartialSuccessOnOutcome;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE, "PDF Profile", PluginParameterType.STRING, "1b",
        VeraPDFPluginUtils.getProfileList(), true, false, "Validation of the file is always based on the profile"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION, "Job finished notification",
        PluginParameterType.STRING, "", false, false,
        "Send a notification, after finishing the process, to one or more e-mail addresses (comma separated)"));
  }

  public VeraPDFPlugin() {
    profile = "1b";
    hasFeatures = Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "verapdf", "hasFeatures"));
    hasPartialSuccessOnOutcome = Boolean.parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "tools",
      "convert", "allplugins", "hasPartialSuccessOnOutcome"));
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
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE)) {
      profile = parameters.get(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE);
    }

  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException {

    if (!list.isEmpty()) {
      if (list.get(0) instanceof AIP) {
        return executeOnAIP(index, model, storage, (List<AIP>) list);
      } else if (list.get(0) instanceof Representation) {
        return executeOnRepresentation(index, model, storage, (List<Representation>) list);
      } else if (list.get(0) instanceof File) {
        return executeOnFile(index, model, storage, (List<File>) list);
      }
    }

    return new Report();
  }

  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (AIP aip : list) {
        LOGGER.debug("Processing AIP {}", aip.getId());
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIPState.INGEST_PROCESSING);
        PluginState pluginResultState = PluginState.SUCCESS;
        PluginState reportState = PluginState.SUCCESS;

        for (Representation representation : aip.getRepresentations()) {
          List<String> resourceList = new ArrayList<String>();
          // FIXME 20160516 hsilva: see how to set initial
          // initialOutcomeObjectState
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
                  String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1,
                    ifile.getId().length());

                  if ("pdf".equalsIgnoreCase(fileFormat) || "application/pdf".equals(fileMimetype)) {
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

            IOUtils.closeQuietly(allFiles);
            if (!pluginResultState.equals(PluginState.SUCCESS)) {
              reportState = PluginState.FAILURE;
            }

          } catch (Throwable e) {
            LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
            pluginResultState = PluginState.FAILURE;
            reportState = PluginState.FAILURE;
          } finally {
            LOGGER.debug("Creating veraPDF event for the representation {}", representation.getId());
            createEvent(resourceList, aip, representation.getId(), model, index, pluginResultState, details);
          }
        }

        if (reportState.equals(PluginState.SUCCESS)) {
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
        }

        reportItem.setPluginState(reportState).setPluginDetails("VeraPDF validation on AIP");
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage,
    List<Representation> list) throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (Representation representation : list) {
        List<String> resourceList = new ArrayList<String>();
        // FIXME 20160516 hsilva: see how to set initial
        // initialOutcomeObjectState
        Report reportItem = PluginHelper.initPluginReportItem(this, representation.getId(), AIPState.INGEST_PROCESSING);
        PluginState pluginResultState = PluginState.SUCCESS;
        PluginState reportState = PluginState.SUCCESS;
        StringBuilder details = new StringBuilder();
        AIP aip = model.retrieveAIP(representation.getAipId());

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

                if ("pdf".equalsIgnoreCase(fileFormat) || "application/pdf".equals(fileMimetype)) {
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

          IOUtils.closeQuietly(allFiles);
          if (!pluginResultState.equals(PluginState.SUCCESS)) {
            reportState = PluginState.FAILURE;
          }

        } catch (Throwable e) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          pluginResultState = PluginState.FAILURE;
          reportState = PluginState.FAILURE;
        } finally {
          LOGGER.debug("Creating veraPDF event for the representation {}", representation.getId());
          createEvent(resourceList, aip, representation.getId(), model, index, pluginResultState, details);
        }

        if (reportState.equals(PluginState.SUCCESS)) {
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
        }

        reportItem.setPluginState(reportState).setPluginDetails("VeraPDF validation on Representation");
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error retrieving aip on VeraPDF representation validation");
    }

    return report;
  }

  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, List<File> list)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      try {
        for (File file : list) {
          List<String> resourceList = new ArrayList<String>();
          // FIXME 20160516 hsilva: see how to set initial
          // initialOutcomeObjectState
          Report reportItem = PluginHelper.initPluginReportItem(this, file.getId(), AIPState.INGEST_PROCESSING);
          PluginState pluginResultState = PluginState.SUCCESS;
          PluginState reportState = PluginState.SUCCESS;
          StringBuilder details = new StringBuilder();
          AIP aip = model.retrieveAIP(file.getAipId());

          LOGGER.debug("Processing file: {}", file);
          if (!file.isDirectory()) {
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());

            if ("pdf".equalsIgnoreCase(fileFormat) || "application/pdf".equals(fileMimetype)) {
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

          if (!pluginResultState.equals(PluginState.SUCCESS)) {
            reportState = PluginState.FAILURE;
          }

          createEvent(resourceList, aip, file.getRepresentationId(), model, index, pluginResultState, details);

          if (reportState.equals(PluginState.SUCCESS)) {
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } else {
            jobPluginInfo.incrementObjectsProcessedWithFailure();
          }

          reportItem.setPluginState(reportState).setPluginDetails("VeraPDF validation on File");
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
        }
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
        | IOException | IllegalArgumentException | JAXBException | VeraPDFException e) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        LOGGER.error("Could not run VeraPDF successfully");
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
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
  public Plugin<T> cloneMe() {
    return new VeraPDFPlugin<T>();
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

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_FORMAT_VALIDATION);
  }

}
