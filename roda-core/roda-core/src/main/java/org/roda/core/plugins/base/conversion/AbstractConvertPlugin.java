/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.conversion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractAIPComponentsPlugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.base.characterization.PremisSkeletonPluginUtils;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated It will be removed in the next major version.
 */
public abstract class AbstractConvertPlugin<T extends IsRODAObject> extends AbstractAIPComponentsPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConvertPlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT, "Input format", PluginParameterType.STRING, "", true, false,
      "Input file format to be converted (check documentation for list of supported formats). If the input file format is not specified, the task will"
        + " run on all supported formats (check roda-core-formats.properties for list of supported formats)."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, "Output format", PluginParameterType.STRING, "",
        true, false, "Output file format to be converted (check documentation for list of supported formats)."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES, "Ignore other files",
        PluginParameterType.BOOLEAN, "true", false, false,
        "Do not process files that have a different format from the indicated."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP,
      PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "Outcome",
        PluginParameterType.CONVERSION)
        .withDescription(
          "A conversion can create a representation or a dissemination. Please choose which option to output")
        .build());
  }

  private String inputFormat;
  private String outputFormat;
  private boolean ignoreFiles = true;
  private boolean createDIP = false;
  private boolean hasPartialSuccessOnOutcome = false;
  private String dipTitle = "";
  private String dipDescription = "";
  private String representationType = "";

  protected AbstractConvertPlugin() {
    super();
    inputFormat = "";
    outputFormat = "";
  }

  protected Map<String, PluginParameter> getDefaultParameters() {
    return pluginParameters.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> new PluginParameter(e.getValue())));
  }

  protected List<PluginParameter> orderParameters(Map<String, PluginParameter> params) {
    List<PluginParameter> orderedList = new ArrayList<>();
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_TYPE));
    return orderedList;
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public abstract List<String> getApplicableTo();

  public abstract List<String> getConvertableTo();

  public abstract Map<String, List<String>> getPronomToExtension();

  public abstract Map<String, List<String>> getMimetypeToExtension();

  public String getInputFormat() {
    return this.inputFormat;
  }

  public void setInputFormat(String format) {
    this.inputFormat = format;
  }

  public String getOutputFormat() {
    return this.outputFormat;
  }

  public void setOutputFormat(String format) {
    this.outputFormat = format;
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
  public List<PluginParameter> getParameters() {
    return orderParameters(getDefaultParameters());
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT)) {
      setInputFormat(parameters.get(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT)) {
      setOutputFormat(parameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES)) {
      ignoreFiles = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP)) {
      String value = parameters.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP);

      Map<String, String> map = new HashMap<>();
      String[] keyValuePairs = value.split(";");
      for (String pair : keyValuePairs) {
        String[] parts = pair.split("=");
        if (parts.length == 2) {
          map.put(parts[0].trim(), parts[1].trim());
        }
      }

      if (map.get("type").equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_REPRESENTATION)) {
        createDIP = false;
        representationType = map.get("value");
      } else {
        createDIP = true;
        dipTitle = map.get("title");
        dipDescription = map.get("description");
      }
    }

    hasPartialSuccessOnOutcome = Boolean.parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "tools",
      "convert", "allplugins", "hasPartialSuccessOnOutcome"));
  }

  @Override
  protected Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, List<AIP> list, Job job) {
    for (AIP aip : list) {
      LOGGER.debug("Processing AIP {}", aip.getId());
      List<String> newRepresentations = new ArrayList<>();
      String newRepresentationID = null;
      boolean notify = true;
      PluginState reportState = PluginState.SUCCESS;
      ValidationReport validationReport = new ValidationReport();
      boolean hasUnacceptedFormatFiles = false;
      List<File> alteredFiles = new ArrayList<>();
      List<File> newFiles = new ArrayList<>();
      List<DIPFile> newDIPFiles = new ArrayList<>();
      List<File> unchangedFiles = new ArrayList<>();
      if (aip.getRepresentations() != null && !aip.getRepresentations().isEmpty()) {
        for (Representation representation : aip.getRepresentations()) {
          newRepresentationID = IdUtils.createUUID();
          PluginState pluginResultState = PluginState.SUCCESS;

          Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
            IdUtils.getRepresentationId(representation), Representation.class, AIPState.ACTIVE);
          if (createDIP) {
            reportItem.setOutcomeObjectClass(DIP.class.getName());
          }

          try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
            representation.getId(), true)) {
            LOGGER.debug("Processing representation {}", representation);

            for (OptionalWithCause<File> oFile : allFiles) {
              String newFileId = null;
              if (oFile.isPresent()) {
                File file = oFile.get();
                LOGGER.debug("Processing file {}", file);

                if (!file.isDirectory()) {
                  if (FSUtils.isManifestOfExternalFiles(file.getId())) {
                    processShallowFile(index, file, job, newRepresentationID, model, pluginResultState, reportItem,
                      notify, storage, newFileId, newDIPFiles, newFiles, reportState, report);
                  } else {
                    IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file),
                      RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
                    String fileMimetype = ifile.getFileFormat().getMimeType();
                    String filePronom = ifile.getFileFormat().getPronom();
                    String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);
                    List<String> applicableTo = getApplicableTo();
                    List<String> convertableTo = getConvertableTo();
                    Map<String, List<String>> pronomToExtension = getPronomToExtension();
                    Map<String, List<String>> mimetypeToExtension = getMimetypeToExtension();

                    if (doPluginExecute(fileFormat, filePronom, fileMimetype, applicableTo, convertableTo,
                      pronomToExtension, mimetypeToExtension)) {

                      fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype, applicableTo,
                        pronomToExtension, mimetypeToExtension);

                      StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                      DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

                      LOGGER.debug("Running a ConvertPlugin ({} to {}) on {}", fileFormat, outputFormat, file.getId());
                      try {
                        Path pluginResult = Files.createTempFile(getWorkingDirectory(), "converted",
                          "." + FilenameUtils.normalize(getOutputFormat()));
                        String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);

                        newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                        ContentPayload payload = new FSPathContentPayload(pluginResult);

                        if (createDIP) {
                          FileLink fileLink = new FileLink(file.getAipId(), file.getRepresentationId(), file.getPath(),
                            file.getId());
                          List<FileLink> links = new ArrayList<>();
                          links.add(fileLink);

                          DIP dip = new DIP();
                          dip.setId(IdUtils.createUUID());
                          dip.setFileIds(links);
                          dip.setPermissions(aip.getPermissions());
                          dip.setTitle(dipTitle);
                          dip.setDescription(dipDescription);
                          dip.setType(RodaConstants.DIP_TYPE_CONVERSION);
                          dip = model.createDIP(dip, true);
                          newRepresentationID = dip.getId();

                          DIPFile f = model.createDIPFile(newRepresentationID, file.getPath(), newFileId,
                            directAccess.getPath().toFile().length(), payload, notify);
                          newDIPFiles.add(f);
                        } else {
                          // create a new representation if it does not exist
                          if (!newRepresentations.contains(newRepresentationID)) {
                            LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID,
                              aip.getId());
                            boolean original = false;
                            newRepresentations.add(newRepresentationID);
                            String newRepresentationType = representation.getType();

                            if (StringUtils.isNotBlank(representationType)) {
                              newRepresentationType = representationType;
                            }

                            model.createRepresentation(aip.getId(), newRepresentationID, original,
                              newRepresentationType, notify, job.getUsername());
                            reportItem.setSourceAndOutcomeObjectId(reportItem.getSourceObjectId(),
                              IdUtils.getRepresentationId(representation.getAipId(), newRepresentationID));
                          }

                          File f = model.createFile(aip.getId(), newRepresentationID, file.getPath(), newFileId,
                            payload, job.getUsername(), notify);
                          newFiles.add(f);
                        }

                        alteredFiles.add(file);
                        IOUtils.closeQuietly(directAccess);

                        Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                          AIPState.ACTIVE);
                        fileReportItem.setPluginState(pluginResultState).setPluginDetails(result);
                        reportItem.addReport(fileReportItem);

                      } catch (CommandException e) {
                        pluginResultState = PluginState.FAILURE;
                        reportState = pluginResultState;
                        reportItem.setPluginState(pluginResultState).addPluginDetails(getOutputMessage(e));

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
                        hasUnacceptedFormatFiles = true;
                      }
                    }
                  }
                }
              } else {
                LOGGER.error("Cannot process AIP representation file", oFile.getCause());
              }
            }

            reportItem.setPluginState(pluginResultState);

            if (reportState.equals(PluginState.SUCCESS)) {
              if (ignoreFiles && !validationReport.getIssues().isEmpty()) {
                reportItem.setHtmlPluginDetails(true)
                  .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
              }
            }

            if (hasUnacceptedFormatFiles) {
              reportItem.setPluginDetails(
                "Source files include formats that are not accepted by this plugin and the plugin was not set to ignore these files. The conversion was not run.");
            }

            // add unchanged files to the new representation if created
            if (!alteredFiles.isEmpty() && !createDIP) {
              createNewFilesOnRepresentation(storage, model, unchangedFiles, newRepresentationID, job.getUsername(),
                notify);
            }

          } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
            | AuthorizationDeniedException | IOException | AlreadyExistsException | PluginException e) {
            LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
            pluginResultState = PluginState.FAILURE;
            reportState = pluginResultState;
            reportItem.setPluginState(pluginResultState).setPluginDetails(e.getMessage());
          } finally {
            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

            if (!createDIP) {
              try {
                Representation rep = model.retrieveRepresentation(aip.getId(), newRepresentationID);
                createPremisSkeletonOnRepresentation(model, aip.getId(), rep, job.getUsername());
              } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
                | ValidationException | IOException e) {
                LOGGER.error("Error running premis skeleton on new representation: {}", e.getMessage());
              }
            }
          }
        }
      } else {

        final Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
        reportState = PluginState.SKIPPED;
        reportItem.setPluginState(reportState);
        reportItem.setPluginDetails("Skipped because no representation was found for this AIP");
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
      try {
        LOGGER.debug("Creating convert plugin event for the AIP {}", aip.getId());
        boolean notifyEvent = false;
        createEvent(model, index, aip.getId(), null, null, null, outputFormat, reportState, alteredFiles, newFiles,
          notifyEvent, job);
        model.notifyAipUpdated(aip.getId());
        jobPluginInfo.incrementObjectsProcessed(reportState);
      } catch (PluginException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException e) {
        LOGGER.debug("Error on update AIP notify");
      }
    }

    return report;
  }

  @Override
  protected Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage,
    Report report, JobPluginInfo jobPluginInfo, List<Representation> list, Job job) throws PluginException {
    List<String> newRepresentations = new ArrayList<>();
    String aipId = null;

    for (Representation representation : list) {
      List<File> unchangedFiles = new ArrayList<>();
      String newRepresentationID = IdUtils.createUUID();
      List<File> alteredFiles = new ArrayList<>();
      List<File> newFiles = new ArrayList<>();
      List<DIPFile> newDIPFiles = new ArrayList<>();
      aipId = representation.getAipId();
      PluginState reportState = PluginState.SUCCESS;
      PluginState pluginResultState = PluginState.SUCCESS;
      boolean notify = true;

      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
        Representation.class, AIPState.ACTIVE);
      if (createDIP) {
        reportItem.setOutcomeObjectClass(DIP.class.getName());
      }

      ValidationReport validationReport = new ValidationReport();
      boolean hasNonPdfFiles = false;

      try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
        representation.getId(), true)) {
        LOGGER.debug("Processing representation {}", representation);

        for (OptionalWithCause<File> oFile : allFiles) {
          String newFileId = null;
          if (oFile.isPresent()) {
            File file = oFile.get();
            LOGGER.debug("Processing file {}", file);

            if (!file.isDirectory()) {
              if (FSUtils.isManifestOfExternalFiles(file.getId())) {
                processShallowFile(index, file, job, newRepresentationID, model, pluginResultState, reportItem, notify,
                  storage, newFileId, newDIPFiles, newFiles, reportState, report);
              } else {
                IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file),
                  RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
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
                    Path pluginResult = Files.createTempFile(getWorkingDirectory(), "converted",
                      "." + FilenameUtils.normalize(getOutputFormat()));
                    String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);
                    ContentPayload payload = new FSPathContentPayload(pluginResult);

                    if (!newRepresentations.contains(newRepresentationID)) {
                      LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, aipId);
                      boolean original = false;
                      newRepresentations.add(newRepresentationID);

                      if (createDIP) {
                        FileLink fileLink = new FileLink(file.getAipId(), file.getRepresentationId(), file.getPath(),
                          file.getId());
                        List<FileLink> links = new ArrayList<>();
                        links.add(fileLink);

                        AIP aip = model.retrieveAIP(aipId);

                        DIP dip = new DIP();
                        dip.setId(IdUtils.createUUID());
                        dip.setFileIds(links);
                        dip.setPermissions(aip.getPermissions());
                        dip.setTitle(dipTitle);
                        dip.setDescription(dipDescription);
                        dip.setType(RodaConstants.DIP_TYPE_CONVERSION);
                        dip = model.createDIP(dip, true);
                        newRepresentationID = dip.getId();
                      } else {
                        // INFO will be a parameter
                        String newRepresentationType = RodaConstants.REPRESENTATION_TYPE_MIXED;

                        if (StringUtils.isNotBlank(representationType)) {
                          newRepresentationType = representationType;
                        }

                        model.createRepresentation(aipId, newRepresentationID, original, newRepresentationType, notify,
                          job.getUsername());
                        reportItem.setSourceAndOutcomeObjectId(reportItem.getSourceObjectId(),
                          IdUtils.getRepresentationId(representation.getAipId(), newRepresentationID));
                      }
                    }

                    newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                    if (createDIP) {
                      DIPFile f = model.createDIPFile(newRepresentationID, file.getPath(), newFileId,
                        directAccess.getPath().toFile().length(), payload, notify);
                      newDIPFiles.add(f);
                    } else {
                      File newFile = model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload,
                        job.getUsername(), notify);
                      newFiles.add(newFile);
                    }

                    alteredFiles.add(file);
                    IOUtils.closeQuietly(directAccess);

                    Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                      AIPState.ACTIVE);
                    fileReportItem.setPluginState(PluginState.SUCCESS).setPluginDetails(result);
                    reportItem.addReport(fileReportItem);

                  } catch (CommandException e) {
                    reportState = PluginState.FAILURE;
                    reportItem.setPluginState(reportState).addPluginDetails(getOutputMessage(e));

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
            }
          } else {
            LOGGER.error("Cannot process AIP representation file", oFile.getCause());
          }
        }

        reportItem.setPluginState(reportState);
        if (reportState.equals(PluginState.SUCCESS) && ignoreFiles && !validationReport.getIssues().isEmpty()) {
          reportItem.setHtmlPluginDetails(true)
            .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
        }

        if (hasNonPdfFiles) {
          reportItem.setPluginDetails(
            "Source files include formats that are not accepted by this plugin and the plugin was not set to ignore these files. The conversion was not run.");
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

        // add unchanged files to the new representation
        if (!alteredFiles.isEmpty()) {
          if (createDIP) {
            createNewFilesOnDIP(storage, model, unchangedFiles, newRepresentationID, notify);
          } else {
            createNewFilesOnRepresentation(storage, model, unchangedFiles, newRepresentationID, job.getUsername(),
              notify);
          }
        }

      } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException | IOException | AlreadyExistsException e) {
        LOGGER.error("Error processing Representation {}: {}", representation.getId(), e.getMessage(), e);
        reportState = PluginState.FAILURE;

        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
        report.addReport(reportItem);
      }

      jobPluginInfo.incrementObjectsProcessed(reportState);
      LOGGER.debug("Creating convert plugin event for the representation " + representation.getId());
      boolean notifyEvent = false;
      createEvent(model, index, aipId, representation.getId(), null, null, outputFormat, reportState, alteredFiles,
        newFiles, notifyEvent, job);

      if (!createDIP) {
        try {
          Representation rep = model.retrieveRepresentation(representation.getAipId(), newRepresentationID);
          createPremisSkeletonOnRepresentation(model, representation.getAipId(), rep, job.getUsername());
        } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
          | ValidationException | IOException e) {
          LOGGER.error("Error running premis skeleton on new representation: {}", e.getMessage());
        }
      }
    }

    try {
      model.notifyAipUpdated(aipId);
    } catch (RODAException e) {
      LOGGER.error("Error running creating agent for AbstractConvertPlugin", e);
    }

    return report;
  }

  @Override
  protected Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, List<File> list, Job job) throws PluginException {

    Map<String, String> changedRepresentationsOnAIPs = new HashMap<>();
    boolean notify = true;
    String newRepresentationID = null;
    String newFileId = null;
    ArrayList<File> newFiles = new ArrayList<>();
    ArrayList<DIPFile> newDIPFiles = new ArrayList<>();
    Report reportItem = null;
    PluginState reportState = PluginState.SUCCESS;
    PluginState pluginResultState = PluginState.SUCCESS;

    for (File file : list) {
      try {
        if (FSUtils.isManifestOfExternalFiles(file.getId())) {
          for (OptionalWithCause<File> fileShallows : model.listExternalFilesUnder(file)) {
            executeOnFile(index, model, storage, report, jobPluginInfo, Arrays.asList(fileShallows.get()), job);
          }
        } else {
          StorageService tmpStorageService = null;
          try {
            LOGGER.debug("Processing file {}", file.getId());
            newRepresentationID = IdUtils.createUUID();
            pluginResultState = PluginState.SUCCESS;

            reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class, AIPState.ACTIVE);
            if (createDIP) {
              reportItem.setOutcomeObjectClass(DIP.class.getName());
            }

            if (!file.isDirectory()) {
              IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file),
                RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
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
                if (file.isReference()) {
                  tmpStorageService = ModelUtils.resolveTemporaryResourceShallow(job.getId(), storage,
                    ModelUtils.getAIPStoragePath(file.getAipId()));
                  directAccess = tmpStorageService.getDirectAccess(fileStoragePath);
                }

                LOGGER.debug("Running a ConvertPlugin ({} to {}) on {}", fileFormat, outputFormat, file.getId());
                try {
                  Path pluginResult = Files.createTempFile(getWorkingDirectory(), "converted",
                    "." + FilenameUtils.normalize(getOutputFormat()));
                  String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);

                  ContentPayload payload = new FSPathContentPayload(pluginResult);
                  StoragePath storagePath = ModelUtils.getRepresentationStoragePath(file.getAipId(),
                    file.getRepresentationId());

                  // create a new representation if it does not exist
                  LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, file.getAipId());
                  boolean original = false;
                  if (createDIP) {
                    FileLink fileLink = new FileLink(file.getAipId(), file.getRepresentationId(), file.getPath(),
                      file.getId());
                    List<FileLink> links = new ArrayList<>();
                    links.add(fileLink);

                    AIP aip = model.retrieveAIP(file.getAipId());

                    DIP dip = new DIP();
                    dip.setId(IdUtils.createUUID());
                    dip.setFileIds(links);
                    dip.setPermissions(aip.getPermissions());
                    dip.setTitle(dipTitle);
                    dip.setDescription(dipDescription);
                    dip.setType(RodaConstants.DIP_TYPE_CONVERSION);
                    dip = model.createDIP(dip, true);
                    newRepresentationID = dip.getId();
                  } else {
                    // INFO will be a parameter
                    String newRepresentationType = RodaConstants.REPRESENTATION_TYPE_MIXED;

                    if (StringUtils.isNotBlank(representationType)) {
                      newRepresentationType = representationType;
                    }

                    model.createRepresentation(file.getAipId(), newRepresentationID, original, newRepresentationType,
                      model.getStorage(), storagePath, true, job.getUsername());
                  }

                  // update file on new representation
                  newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);

                  if (createDIP) {
                    DIPFile f = model.createDIPFile(newRepresentationID, file.getPath(), newFileId,
                      directAccess.getPath().toFile().length(), payload, notify);
                    newDIPFiles.add(f);
                  } else {
                    model.deleteFile(file.getAipId(), newRepresentationID, file.getPath(), file.getId(),
                      job.getUsername(), notify);
                    File f = model.createFile(file.getAipId(), newRepresentationID, file.getPath(), newFileId, payload,
                      job.getUsername(), notify);
                    newFiles.add(f);
                    reportItem.setSourceAndOutcomeObjectId(reportItem.getSourceObjectId(), IdUtils.getFileId(f));
                    changedRepresentationsOnAIPs.put(file.getRepresentationId(), file.getAipId());
                  }

                  Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                    AIPState.ACTIVE);
                  fileReportItem.setPluginState(PluginState.SUCCESS).setPluginDetails(result);
                  reportItem.addReport(fileReportItem);

                } catch (CommandException e) {
                  pluginResultState = PluginState.FAILURE;
                  Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                    AIPState.ACTIVE);
                  fileReportItem.setPluginState(PluginState.FAILURE).setPluginDetails(getOutputMessage(e));
                  reportItem.addReport(fileReportItem);

                  LOGGER.debug("Conversion ({} to {}) failed on file {} of representation {} from AIP {}", fileFormat,
                    outputFormat, file.getId(), file.getRepresentationId(), file.getAipId());
                }
              } else {
                if (ignoreFiles) {
                  reportItem.setPluginDetails("This file was ignored.");
                } else {
                  pluginResultState = PluginState.FAILURE;
                  reportItem.setPluginDetails(
                    "This file was not ignored and it is not listed on the supported input file formats.");
                }
              }
            }

            if (!pluginResultState.equals(PluginState.SUCCESS)) {
              reportState = PluginState.FAILURE;
              jobPluginInfo.incrementObjectsProcessedWithFailure();
            } else {
              jobPluginInfo.incrementObjectsProcessedWithSuccess();
            }

          } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
            | AuthorizationDeniedException | IOException | AlreadyExistsException e) {
            LOGGER.error("Error processing File {}: {}", file.getId(), e.getMessage(), e);
            reportState = PluginState.FAILURE;
            reportItem.setPluginDetails(e.getMessage());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
          } finally {
            if (file.isReference() && tmpStorageService != null) {
              try {
                if (!job.getPluginType().equals(PluginType.INGEST)) {
                  ModelUtils.removeTemporaryResourceShallow(job.getId(), ModelUtils.getAIPStoragePath(file.getAipId()));
                }
              } catch (RequestNotValidException | IOException e) {
                LOGGER.error("Error on removing temporary AIP " + file.getAipId(), e);
              }
            }
            reportItem.setPluginState(pluginResultState);
            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
          }

          boolean notifyEvent = true;
          createEvent(model, index, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
            outputFormat, reportState, Arrays.asList(file), newFiles, notifyEvent, job);

          if (!createDIP) {
            try {
              Representation rep = model.retrieveRepresentation(file.getAipId(), newRepresentationID);
              createPremisSkeletonOnRepresentation(model, file.getAipId(), rep, job.getUsername());
            } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
              | ValidationException | IOException e) {
              LOGGER.error("Error running premis skeleton on new representation: {}", e.getMessage());
            }
          }
        }
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        reportState = PluginState.FAILURE;
        reportItem.setPluginDetails(e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    }

    return report;
  }

  public abstract String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException;

  private void createPremisSkeletonOnRepresentation(ModelService model, String aipId, Representation representation,
    String username) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    ValidationException, IOException {
    List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
    PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aipId, representation.getId(), algorithms,
      username);
    model.notifyRepresentationUpdated(representation).failOnError();
  }

  private void createEvent(ModelService model, IndexService index, String aipId, String representationId,
    List<String> filePath, String fileId, String outputFormat, PluginState outcome, List<File> alteredFiles,
    List<File> newFiles, boolean notify, Job cachedJob) throws PluginException {

    List<LinkingIdentifier> premisSourceFilesIdentifiers = new ArrayList<>();
    List<LinkingIdentifier> premisTargetFilesIdentifiers = new ArrayList<>();

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
    if (PluginState.FAILURE.equals(outcome)
      || (outcome == PluginState.PARTIAL_SUCCESS && !hasPartialSuccessOnOutcome)) {
      outcome = PluginState.FAILURE;
      stringBuilder.setLength(0);
    }

    try {
      PluginHelper.createPluginEvent(this, aipId, representationId, filePath, fileId, model, index,
        premisSourceFilesIdentifiers, premisTargetFilesIdentifiers, outcome, stringBuilder.toString(), notify,
        cachedJob);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  private void processShallowFile(IndexService index, File file, Job job, String newRepresentationID,
    ModelService model, PluginState pluginResultState, Report reportItem, boolean notify, StorageService storage,
    String newFileId, List<DIPFile> newDIPFiles, List<File> newFiles, PluginState reportState, Report report)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    PluginException {

    Map<String, String> changedRepresentationsOnAIPs = new HashMap<>();

    for (OptionalWithCause<File> fileShallows : model.listExternalFilesUnder(file)) {
      for (File file1 : Arrays.asList(fileShallows.get())) {
        StorageService tmpStorageService = null;

        try {
          LOGGER.debug("Processing file {}", file1.getId());
          newRepresentationID = IdUtils.createUUID();
          pluginResultState = PluginState.SUCCESS;

          reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file1), File.class, AIPState.ACTIVE);
          if (createDIP) {
            reportItem.setOutcomeObjectClass(DIP.class.getName());
          }

          if (!file1.isDirectory()) {
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file1),
              RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
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

              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file1);
              DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
              if (file1.isReference()) {
                tmpStorageService = ModelUtils.resolveTemporaryResourceShallow(job.getId(), storage,
                  ModelUtils.getAIPStoragePath(file1.getAipId()));
                directAccess = tmpStorageService.getDirectAccess(fileStoragePath);
              }

              LOGGER.debug("Running a ConvertPlugin ({} to {}) on {}", fileFormat, outputFormat, file1.getId());
              try {
                Path pluginResult = Files.createTempFile(getWorkingDirectory(), "converted", "." + getOutputFormat());
                String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);

                ContentPayload payload = new FSPathContentPayload(pluginResult);
                StoragePath storagePath = ModelUtils.getRepresentationStoragePath(file1.getAipId(),
                  file1.getRepresentationId());

                // create a new representation if it does not exist
                LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, file1.getAipId());
                boolean original = false;
                if (createDIP) {
                  FileLink fileLink = new FileLink(file1.getAipId(), file1.getRepresentationId(), file1.getPath(),
                    file1.getId());
                  List<FileLink> links = new ArrayList<>();
                  links.add(fileLink);

                  AIP aipFile = model.retrieveAIP(file1.getAipId());

                  DIP dip = new DIP();
                  dip.setId(IdUtils.createUUID());
                  dip.setFileIds(links);
                  dip.setPermissions(aipFile.getPermissions());
                  dip.setTitle(dipTitle);
                  dip.setDescription(dipDescription);
                  dip.setType(RodaConstants.DIP_TYPE_CONVERSION);
                  dip = model.createDIP(dip, true);
                  newRepresentationID = dip.getId();
                } else {
                  // INFO will be a parameter
                  String newRepresentationType = RodaConstants.REPRESENTATION_TYPE_MIXED;

                  if (StringUtils.isNotBlank(representationType)) {
                    newRepresentationType = representationType;
                  }

                  model.createRepresentation(file1.getAipId(), newRepresentationID, original, newRepresentationType,
                    model.getStorage(), storagePath, true, job.getUsername());
                }

                // update file on new representation
                newFileId = file1.getId().replaceFirst("[.][^.]+$", "." + outputFormat);

                if (createDIP) {
                  DIPFile f = model.createDIPFile(newRepresentationID, file1.getPath(), newFileId,
                    directAccess.getPath().toFile().length(), payload, notify);
                  newDIPFiles.add(f);
                } else {
                  model.deleteFile(file1.getAipId(), newRepresentationID, file1.getPath(), file1.getId(),
                    job.getUsername(), notify);
                  File f = model.createFile(file1.getAipId(), newRepresentationID, file1.getPath(), newFileId, payload,
                    job.getUsername(), notify);
                  newFiles.add(f);
                  reportItem.setSourceAndOutcomeObjectId(reportItem.getSourceObjectId(), IdUtils.getFileId(f));
                  changedRepresentationsOnAIPs.put(file1.getRepresentationId(), file1.getAipId());
                }

                Report fileReportItem = PluginHelper.initPluginReportItem(this, file1.getId(), File.class,
                  AIPState.ACTIVE);
                fileReportItem.setPluginState(PluginState.SUCCESS).setPluginDetails(result);
                reportItem.addReport(fileReportItem);

              } catch (CommandException e) {
                pluginResultState = PluginState.PARTIAL_SUCCESS;
                Report fileReportItem = PluginHelper.initPluginReportItem(this, file1.getId(), File.class,
                  AIPState.ACTIVE);
                fileReportItem.setPluginState(PluginState.PARTIAL_SUCCESS)
                  .setPluginDetails(e.getMessage() + "\n" + e.getOutput());
                reportItem.addReport(fileReportItem);

                LOGGER.debug("Conversion ({} to {}) failed on file {} of representation {} from AIP {}", fileFormat,
                  outputFormat, file1.getId(), file1.getRepresentationId(), file1.getAipId());
              }
            } else {
              if (ignoreFiles) {
                reportItem.setPluginDetails("This file was ignored.");
              } else {
                pluginResultState = PluginState.FAILURE;
                reportItem.setPluginDetails(
                  "This file was not ignored and it is not listed on the supported input file formats.");
              }
            }
          }

          if (!pluginResultState.equals(PluginState.SUCCESS)) {
            reportState = PluginState.FAILURE;
          }

        } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
          | AuthorizationDeniedException | IOException | AlreadyExistsException e) {
          LOGGER.error("Error processing File {}: {}", file1.getId(), e.getMessage(), e);
          reportState = PluginState.FAILURE;
          reportItem.setPluginDetails(e.getMessage());
        } finally {
          if (file1.isReference() && tmpStorageService != null) {
            try {
              if (!job.getPluginType().equals(PluginType.INGEST)) {
                ModelUtils.removeTemporaryResourceShallow(job.getId(), ModelUtils.getAIPStoragePath(file1.getAipId()));
              }
            } catch (RequestNotValidException | IOException e) {
              LOGGER.error("Error on removing temporary AIP " + file1.getAipId(), e);
            }
          }
          reportItem.setPluginState(pluginResultState);
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
        }

        boolean notifyEvent = true;
        createEvent(model, index, file1.getAipId(), file1.getRepresentationId(), file1.getPath(), file1.getId(),
          outputFormat, reportState, Arrays.asList(file1), newFiles, notifyEvent, job);

        if (!createDIP) {
          try {
            Representation rep = model.retrieveRepresentation(file1.getAipId(), newRepresentationID);
            createPremisSkeletonOnRepresentation(model, file1.getAipId(), rep, job.getUsername());
          } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
            | ValidationException | IOException e) {
            LOGGER.error("Error running premis skeleton on new representation: {}", e.getMessage());
          }
        }
      }
    }

  }

  private boolean doPluginExecute(String fileFormat, String filePronom, String fileMimetype, List<String> applicableTo,
    List<String> convertableTo, Map<String, List<String>> pronomToExtension,
    Map<String, List<String>> mimetypeToExtension) {
    String lowerCaseFileFormat = fileFormat == null ? null : fileFormat.toLowerCase();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Testing if input and output formats are correct: [{}, {}, {}, {}, {}, {}, {}]", lowerCaseFileFormat,
        filePronom, fileMimetype, applicableTo, convertableTo, pronomToExtension, mimetypeToExtension);
    }

    boolean format = getInputFormat().isEmpty() || getInputFormat().equalsIgnoreCase(lowerCaseFileFormat);
    boolean applicable = applicableTo.isEmpty() || (filePronom != null && pronomToExtension.containsKey(filePronom))
      || (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype))
      || (applicableTo.contains(lowerCaseFileFormat));
    boolean convertable = convertableTo.isEmpty() || convertableTo.contains(outputFormat.toLowerCase());

    LOGGER.debug("Input and ouput test results: format={} applicable={} convertable={}", format, applicable,
      convertable);
    return format && applicable && convertable;
  }

  private String getNewFileFormat(String fileFormat, String filePronom, String fileMimetype, List<String> applicableTo,
    Map<String, List<String>> pronomToExtension, Map<String, List<String>> mimetypeToExtension) {
    String newFileFormat = fileFormat;

    if (!applicableTo.isEmpty()) {
      if (StringUtils.isNotBlank(filePronom) && pronomToExtension.get(filePronom) != null
        && !pronomToExtension.get(filePronom).contains(fileFormat)) {
        newFileFormat = pronomToExtension.get(filePronom).get(0);
      } else if (StringUtils.isNotBlank(fileMimetype) && mimetypeToExtension.get(fileMimetype) != null
        && !mimetypeToExtension.get(fileMimetype).contains(fileFormat)) {
        newFileFormat = mimetypeToExtension.get(fileMimetype).get(0);
      }
    }

    return newFileFormat;
  }

  private void createNewFilesOnRepresentation(StorageService storage, ModelService model, List<File> unchangedFiles,
    String newRepresentationID, String username, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    UnsupportedOperationException, IOException, AlreadyExistsException {
    for (File f : unchangedFiles) {
      StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
      Binary binary = storage.getBinary(fileStoragePath);
      Path uriPath = Paths.get(binary.getContent().getURI());
      ContentPayload payload = new FSPathContentPayload(uriPath);
      model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, username, notify);
    }
  }

  private void createNewFilesOnDIP(StorageService storage, ModelService model, List<File> unchangedFiles,
    String newRepresentationID, boolean notify) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, UnsupportedOperationException, IOException, AlreadyExistsException {
    for (File f : unchangedFiles) {
      StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
      Binary binary = storage.getBinary(fileStoragePath);
      Path uriPath = Paths.get(binary.getContent().getURI());
      ContentPayload payload = new FSPathContentPayload(uriPath);
      model.createDIPFile(newRepresentationID, f.getPath(), f.getId(), uriPath.toFile().length(), payload, notify);
    }
  }

  private String getOutputMessage(CommandException exception) {
    if (StringUtils.isNotEmpty(exception.getOutput())) {
      return String.format("%s%n%s", exception.getMessage(), exception.getOutput());
    }

    return exception.getMessage();
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
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_CONVERSION, RodaConstants.PLUGIN_CATEGORY_DISSEMINATION);
  }

  public Path getWorkingDirectory() {
    return PluginHelper.getJobWorkingDirectory(this);
  }
}
