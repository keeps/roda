/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConvertPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static Logger LOGGER = LoggerFactory.getLogger(AbstractConvertPlugin.class);

  private String inputFormat;
  private String outputFormat;
  private boolean ignoreFiles = true;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT, "Input format", PluginParameterType.STRING, "",
        true, false, "Input file format to be converted."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, "Output format", PluginParameterType.STRING, "",
        true, false, "Output file format to be converted."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES, "Ignore other files",
        PluginParameterType.BOOLEAN, "true", false, false,
        "Ignore files that have a different format from the indicated."));
  }

  protected AbstractConvertPlugin() {
    super();
    inputFormat = "";
    outputFormat = "";
  }

  public void init() throws PluginException {
    // do nothing
  }

  public void shutdown() {
    // do nothing
  }

  public boolean hasPartialSuccessOnOutcome() {
    return Boolean.parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "convert", "allplugins",
      "hasPartialSuccessOnOutcome"));
  }

  public abstract List<String> getApplicableTo();

  public abstract List<String> getConvertableTo();

  public abstract Map<String, List<String>> getPronomToExtension();

  public abstract Map<String, List<String>> getMimetypeToExtension();

  public String getInputFormat() {
    return this.inputFormat;
  }

  public String getOutputFormat() {
    return this.outputFormat;
  }

  public void setInputFormat(String format) {
    this.inputFormat = format;
  }

  public void setOutputFormat(String format) {
    this.outputFormat = format;
  }

  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<PluginParameter> getParameters() {
    List<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // input image format
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT)) {
      setInputFormat(parameters.get(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT));
    }

    // output image format
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT)) {
      setOutputFormat(parameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES)) {
      ignoreFiles = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
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

  private Report executeOnAIP(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.initPluginReport(this);
    String detailExtension = "";

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (AIP aip : list) {
        LOGGER.debug("Processing AIP {}", aip.getId());
        List<String> newRepresentations = new ArrayList<String>();
        String newRepresentationID = null;
        boolean notify = true;
        PluginState reportState = PluginState.SUCCESS;
        ValidationReport validationReport = new ValidationReport();
        boolean hasNonPdfFiles = false;

        for (Representation representation : aip.getRepresentations()) {
          List<File> alteredFiles = new ArrayList<File>();
          List<File> newFiles = new ArrayList<File>();
          List<File> unchangedFiles = new ArrayList<File>();
          newRepresentationID = UUID.randomUUID().toString();
          PluginState pluginResultState = PluginState.SUCCESS;
          // FIXME 20160516 hsilva: see how to set initial
          // initialOutcomeObjectState
          Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
            IdUtils.getRepresentationId(representation), Representation.class, AIPState.ACTIVE);

          try {
            LOGGER.debug("Processing representation {}", representation);
            boolean recursive = true;
            CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
              representation.getId(), recursive);

            for (OptionalWithCause<File> oFile : allFiles) {
              if (oFile.isPresent()) {
                File file = oFile.get();
                LOGGER.debug("Processing file {}", file);
                if (!file.isDirectory()) {
                  IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
                  String fileMimetype = ifile.getFileFormat().getMimeType();
                  String filePronom = ifile.getFileFormat().getPronom();
                  String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1,
                    ifile.getId().length());
                  List<String> applicableTo = getApplicableTo();
                  List<String> convertableTo = getConvertableTo();
                  Map<String, List<String>> pronomToExtension = getPronomToExtension();
                  Map<String, List<String>> mimetypeToExtension = getMimetypeToExtension();
                  String fileInfoPath = StringUtils.join(Arrays.asList(aip.getId(), representation.getId(),
                    StringUtils.join(file.getPath(), '/'), file.getId()), '/');

                  if (doPluginExecute(fileFormat, filePronom, fileMimetype, applicableTo, convertableTo,
                    pronomToExtension, mimetypeToExtension)) {

                    fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype, applicableTo, pronomToExtension,
                      mimetypeToExtension);

                    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                    DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

                    LOGGER.debug("Running a ConvertPlugin ({} to {}) on {}", fileFormat, outputFormat, file.getId());
                    try {
                      Path pluginResult = Files.createTempFile("converted", "." + getOutputFormat());
                      String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);

                      ContentPayload payload = new FSPathContentPayload(pluginResult);

                      // create a new representation if it does not exist
                      if (!newRepresentations.contains(newRepresentationID)) {
                        LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, aip.getId());
                        boolean original = false;
                        newRepresentations.add(newRepresentationID);
                        // TODO the concrete plugin should define the
                        // representation type
                        String newRepresentationType = representation.getType();
                        model.createRepresentation(aip.getId(), newRepresentationID, original, newRepresentationType,
                          notify);
                        reportItem.setOutcomeObjectId(
                          IdUtils.getRepresentationId(representation.getAipId(), newRepresentationID));
                      }

                      String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                      File f = model.createFile(aip.getId(), newRepresentationID, file.getPath(), newFileId, payload,
                        notify);
                      alteredFiles.add(file);
                      newFiles.add(f);
                      IOUtils.closeQuietly(directAccess);

                      Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                        AIPState.ACTIVE);
                      fileReportItem.setPluginState(pluginResultState).setPluginDetails(result);
                      reportItem.addReport(fileReportItem);

                    } catch (CommandException e) {
                      detailExtension += file.getId() + ": " + e.getOutput();
                      pluginResultState = PluginState.PARTIAL_SUCCESS;
                      reportState = pluginResultState;
                      reportItem.setPluginState(pluginResultState).addPluginDetails(e.getMessage());

                      LOGGER.debug("Conversion ({} to {}) failed on file {} of representation {} from AIP {}",
                        fileFormat, outputFormat, file.getId(), representation.getId(), aip.getId());
                    }

                  } else {
                    unchangedFiles.add(file);

                    if (ignoreFiles) {
                      validationReport.addIssue(new ValidationIssue(ModelUtils.getFileStoragePath(file).toString()));
                    } else {
                      pluginResultState = PluginState.FAILURE;
                      reportState = pluginResultState;
                      hasNonPdfFiles = true;
                    }
                  }
                }
              } else {
                LOGGER.error("Cannot process AIP representation file", oFile.getCause());
              }
            }

            IOUtils.closeQuietly(allFiles);
            reportItem.setPluginState(pluginResultState);

            if (reportState.equals(PluginState.SUCCESS)) {
              if (ignoreFiles && validationReport.getIssues().size() > 0) {
                reportItem.setHtmlPluginDetails(true)
                  .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
              }
            }

            if (hasNonPdfFiles) {
              reportItem.setPluginDetails("Certain files were not ignored");
            }

            // add unchanged files to the new representation if created
            if (!alteredFiles.isEmpty()) {
              createNewFilesOnRepresentation(storage, model, unchangedFiles, newRepresentationID, notify);
            }

          } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
            | AuthorizationDeniedException | IOException | AlreadyExistsException e) {
            LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
            pluginResultState = PluginState.FAILURE;
            reportState = pluginResultState;
            reportItem.setPluginState(pluginResultState).setPluginDetails(e.getMessage());
          } finally {
            LOGGER.debug("Creating convert plugin event for the representation {}", representation.getId());
            boolean notifyEvent = false;
            createEvent(alteredFiles, newFiles, aip.getId(), newRepresentationID, model, index, outputFormat,
              pluginResultState, detailExtension, notifyEvent);
            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
          }
        }

        try {
          model.notifyAIPUpdated(aip.getId());
          jobPluginInfo.incrementObjectsProcessed(reportState);
        } catch (Exception e) {
          LOGGER.debug("Error on update AIP notify");
        }

      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  private Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage,
    List<Representation> list) throws PluginException {

    List<String> newRepresentations = new ArrayList<String>();
    String aipId = null;
    Report report = PluginHelper.initPluginReport(this);
    String detailExtension = "";

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (Representation representation : list) {
        List<File> unchangedFiles = new ArrayList<File>();
        String newRepresentationID = UUID.randomUUID().toString();
        List<File> alteredFiles = new ArrayList<File>();
        List<File> newFiles = new ArrayList<File>();
        aipId = representation.getAipId();
        PluginState reportState = PluginState.SUCCESS;
        boolean notify = true;
        // FIXME 20160516 hsilva: see how to set initial
        // initialOutcomeObjectState
        Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
          Representation.class, AIPState.ACTIVE);
        ValidationReport validationReport = new ValidationReport();
        boolean hasNonPdfFiles = false;

        try {
          LOGGER.debug("Processing representation {}", representation);
          boolean recursive = true;
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
            representation.getId(), recursive);

          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();
              LOGGER.debug("Processing file {}", file);

              if (!file.isDirectory()) {
                IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
                String fileMimetype = ifile.getFileFormat().getMimeType();
                String filePronom = ifile.getFileFormat().getPronom();
                String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);
                List<String> applicableTo = getApplicableTo();
                List<String> convertableTo = getConvertableTo();
                Map<String, List<String>> pronomToExtension = getPronomToExtension();
                Map<String, List<String>> mimetypeToExtension = getMimetypeToExtension();

                if (doPluginExecute(fileFormat, filePronom, fileMimetype, applicableTo, convertableTo,
                  pronomToExtension, mimetypeToExtension)) {

                  fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype, applicableTo, pronomToExtension,
                    mimetypeToExtension);

                  StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                  DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

                  LOGGER.debug("Running a ConvertPlugin ({} to {}) on {}", fileFormat, outputFormat, file.getId());
                  try {
                    Path pluginResult = Files.createTempFile("converted", "." + getOutputFormat());
                    String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);
                    ContentPayload payload = new FSPathContentPayload(pluginResult);

                    if (!newRepresentations.contains(newRepresentationID)) {
                      LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, aipId);
                      boolean original = false;
                      newRepresentations.add(newRepresentationID);
                      // TODO the concrete plugin should define the
                      // representation type
                      String newRepresentationType = representation.getType();
                      model.createRepresentation(aipId, newRepresentationID, original, newRepresentationType, notify);
                      reportItem.setOutcomeObjectId(
                        IdUtils.getRepresentationId(representation.getAipId(), newRepresentationID));
                    }

                    String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                    File newFile = model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload,
                      notify);
                    alteredFiles.add(file);
                    newFiles.add(newFile);
                    IOUtils.closeQuietly(directAccess);

                    Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                      AIPState.ACTIVE);
                    fileReportItem.setPluginState(PluginState.SUCCESS).setPluginDetails(result);
                    reportItem.addReport(fileReportItem);

                  } catch (CommandException e) {
                    detailExtension += file.getId() + ": " + e.getOutput();
                    reportState = PluginState.PARTIAL_SUCCESS;
                    reportItem.setPluginState(reportState).addPluginDetails(e.getMessage());

                    LOGGER.debug("Conversion ({} to {}) failed on file {} of representation {} from AIP {}", fileFormat,
                      outputFormat, file.getId(), representation.getId(), representation.getAipId());
                  }
                } else {
                  unchangedFiles.add(file);

                  if (ignoreFiles) {
                    validationReport.addIssue(new ValidationIssue(file.getId()));
                  } else {
                    reportState = PluginState.FAILURE;
                    hasNonPdfFiles = true;
                  }
                }
              }
            } else {
              LOGGER.error("Cannot process AIP representation file", oFile.getCause());
            }
          }
          IOUtils.closeQuietly(allFiles);

          reportItem.setPluginState(reportState);

          if (reportState.equals(PluginState.SUCCESS)) {
            if (ignoreFiles) {
              reportItem.setHtmlPluginDetails(true)
                .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
            }
          }

          if (hasNonPdfFiles) {
            reportItem.setPluginDetails("Certain files were not ignored");
          }

          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);

          // add unchanged files to the new representation
          if (!alteredFiles.isEmpty()) {
            createNewFilesOnRepresentation(storage, model, unchangedFiles, newRepresentationID, notify);
          }

        } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
          | AuthorizationDeniedException | IOException | AlreadyExistsException e) {
          LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
          reportState = PluginState.FAILURE;

          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
          report.addReport(reportItem);
        }

        jobPluginInfo.incrementObjectsProcessed(reportState);
        LOGGER.debug("Creating convert plugin event for the representation " + representation.getId());
        boolean notifyEvent = false;
        createEvent(alteredFiles, newFiles, aipId, newRepresentationID, model, index, outputFormat, reportState,
          detailExtension, notifyEvent);
      }

      try {
        model.notifyAIPUpdated(aipId);
      } catch (RODAException e) {
        LOGGER.error("Error running creating agent for AbstractConvertPlugin", e);
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  private Report executeOnFile(IndexService index, ModelService model, StorageService storage, List<File> list)
    throws PluginException {

    Map<String, String> changedRepresentationsOnAIPs = new HashMap<String, String>();
    boolean notify = true;
    String newRepresentationID = null;
    String newFileId = null;
    ArrayList<File> newFiles = new ArrayList<File>();
    String detailExtension = "";
    Report report = PluginHelper.initPluginReport(this);
    Report reportItem = null;
    PluginState reportState = PluginState.SUCCESS;
    PluginState pluginResultState = PluginState.SUCCESS;

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (File file : list) {
        try {
          LOGGER.debug("Processing file {}", file.getId());

          reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class, AIPState.ACTIVE);
          newRepresentationID = UUID.randomUUID().toString();
          pluginResultState = PluginState.SUCCESS;
          // FIXME 20160516 hsilva: see how to set initial
          // initialOutcomeObjectState

          if (!file.isDirectory()) {
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String filePronom = ifile.getFileFormat().getPronom();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);
            List<String> applicableTo = getApplicableTo();
            List<String> convertableTo = getConvertableTo();
            Map<String, List<String>> pronomToExtension = getPronomToExtension();
            Map<String, List<String>> mimetypeToExtension = getMimetypeToExtension();

            if (doPluginExecute(fileFormat, filePronom, fileMimetype, applicableTo, convertableTo, pronomToExtension,
              mimetypeToExtension)) {

              fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype, applicableTo, pronomToExtension,
                mimetypeToExtension);

              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
              DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

              LOGGER.debug("Running a ConvertPlugin ({} to {}) on {}", fileFormat, outputFormat, file.getId());
              try {
                Path pluginResult = Files.createTempFile("converted", "." + getOutputFormat());
                String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);

                ContentPayload payload = new FSPathContentPayload(pluginResult);
                StoragePath storagePath = ModelUtils.getRepresentationStoragePath(file.getAipId(),
                  file.getRepresentationId());

                // create a new representation if it does not exist
                LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, file.getAipId());
                boolean original = false;
                // TODO the concrete plugin should define the
                // representation type
                String newRepresentationType = RodaConstants.REPRESENTATION_TYPE_MIXED;
                model.createRepresentation(file.getAipId(), newRepresentationID, original, newRepresentationType,
                  model.getStorage(), storagePath);

                // update file on new representation
                newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                model.deleteFile(file.getAipId(), newRepresentationID, file.getPath(), file.getId(), notify);
                File f = model.createFile(file.getAipId(), newRepresentationID, file.getPath(), newFileId, payload,
                  notify);
                newFiles.add(f);
                reportItem.setOutcomeObjectId(IdUtils.getFileId(f));
                changedRepresentationsOnAIPs.put(file.getRepresentationId(), file.getAipId());

                Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                  AIPState.ACTIVE);
                fileReportItem.setPluginState(PluginState.SUCCESS).setPluginDetails(result);
                reportItem.addReport(fileReportItem);

              } catch (CommandException e) {
                detailExtension += file.getId() + ": " + e.getOutput();
                pluginResultState = PluginState.PARTIAL_SUCCESS;

                Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                  AIPState.ACTIVE);
                fileReportItem.setPluginState(PluginState.PARTIAL_SUCCESS).setPluginDetails(e.getMessage());
                reportItem.addReport(fileReportItem);

                LOGGER.debug("Conversion ({} to {}) failed on file {} of representation {} from AIP {}", fileFormat,
                  outputFormat, file.getId(), file.getRepresentationId(), file.getAipId());
              }
            } else {
              if (ignoreFiles) {
                reportItem.setPluginDetails("This file was ignored.");
              } else {
                pluginResultState = PluginState.FAILURE;
                reportItem.setPluginDetails("This file was not ignored.");
              }
            }
          }

          if (!pluginResultState.equals(PluginState.SUCCESS)) {
            reportState = PluginState.FAILURE;
          }

        } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
          | AuthorizationDeniedException | ValidationException | IOException | AlreadyExistsException e) {
          LOGGER.error("Error processing File " + file.getId() + ": " + e.getMessage(), e);
          reportState = PluginState.FAILURE;
          reportItem.setPluginDetails(e.getMessage());
        } finally {
          reportItem.setPluginState(pluginResultState);
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
        }

        jobPluginInfo.incrementObjectsProcessed(reportState);
        boolean notifyEvent = false;
        createEvent(Arrays.asList(file), newFiles, file.getAipId(), newRepresentationID, model, index, outputFormat,
          reportState, detailExtension, notifyEvent);
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  public abstract String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException;

  private void createEvent(List<File> alteredFiles, List<File> newFiles, String aipId, String newRepresentationID,
    ModelService model, IndexService index, String outputFormat, PluginState outcome, String detailExtension,
    boolean notify) throws PluginException {

    List<LinkingIdentifier> premisSourceFilesIdentifiers = new ArrayList<LinkingIdentifier>();
    List<LinkingIdentifier> premisTargetFilesIdentifiers = new ArrayList<LinkingIdentifier>();

    // building the detail for the plugin event
    StringBuilder stringBuilder = new StringBuilder();

    if (alteredFiles.isEmpty()) {
      stringBuilder
        .append("No file was successfully converted on this representation due to plugin or command line issues.");
    } else {
      for (File file : alteredFiles) {
        premisSourceFilesIdentifiers.add(PluginHelper.getLinkingIdentifier(aipId, file.getRepresentationId(),
          file.getPath(), file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
      }

      for (File file : newFiles) {
        premisTargetFilesIdentifiers.add(PluginHelper.getLinkingIdentifier(aipId, file.getRepresentationId(),
          file.getPath(), file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
      }

      stringBuilder.append("The source files were converted to a new format (." + outputFormat + ")");
    }

    // Conversion plugin did not run correctly
    if (outcome == PluginState.FAILURE
      || (outcome == PluginState.PARTIAL_SUCCESS && hasPartialSuccessOnOutcome() == false)) {
      outcome = PluginState.FAILURE;
      stringBuilder.setLength(0);
    }

    try {
      PluginHelper.createPluginEvent(this, aipId, model, index, premisSourceFilesIdentifiers,
        premisTargetFilesIdentifiers, outcome, stringBuilder.toString(), notify);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  private boolean doPluginExecute(String fileFormat, String filePronom, String fileMimetype, List<String> applicableTo,
    List<String> convertableTo, Map<String, List<String>> pronomToExtension,
    Map<String, List<String>> mimetypeToExtension) {
    if (((!getInputFormat().isEmpty() && fileFormat.equalsIgnoreCase(getInputFormat())) || (getInputFormat().isEmpty()))
      && (applicableTo.size() == 0 || (filePronom != null && pronomToExtension.containsKey(filePronom))
        || (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype))
        || (applicableTo.contains(fileFormat)))
      && (convertableTo.size() == 0 || convertableTo.contains(outputFormat)))
      return true;
    else
      return false;
  }

  private String getNewFileFormat(String fileFormat, String filePronom, String fileMimetype, List<String> applicableTo,
    Map<String, List<String>> pronomToExtension, Map<String, List<String>> mimetypeToExtension) {

    if (applicableTo.size() > 0) {
      if (filePronom != null && !filePronom.isEmpty() && pronomToExtension.get(filePronom) != null
        && !pronomToExtension.get(filePronom).contains(fileFormat)) {
        fileFormat = pronomToExtension.get(filePronom).get(0);
      } else if (fileMimetype != null && !fileMimetype.isEmpty() && mimetypeToExtension.get(fileMimetype) != null
        && !mimetypeToExtension.get(fileMimetype).contains(fileFormat)) {
        fileFormat = mimetypeToExtension.get(fileMimetype).get(0);
      }
    }

    return fileFormat;
  }

  private void createNewFilesOnRepresentation(StorageService storage, ModelService model, List<File> unchangedFiles,
    String newRepresentationID, boolean notify) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, UnsupportedOperationException, IOException, AlreadyExistsException {

    for (File f : unchangedFiles) {
      StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
      Binary binary = storage.getBinary(fileStoragePath);
      Path uriPath = Paths.get(binary.getContent().getURI());
      ContentPayload payload = new FSPathContentPayload(uriPath);
      model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, notify);
    }
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.MIGRATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Converted, if possible, files to a new format (" + outputFormat + ").";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Files were successfully converted to a new format.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "File conversion failed.";
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    list.add(File.class);
    return (List) list;
  }

}
