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
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
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
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractAIPComponentsPlugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPluginUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.CommandException;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConvertPlugin<T extends IsRODAObject> extends AbstractAIPComponentsPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConvertPlugin.class);

  private String inputFormat;
  private String outputFormat;
  private boolean ignoreFiles = true;
  private boolean createDIP = false;
  private boolean hasPartialSuccessOnOutcome = false;
  private String dipTitle = "";
  private String dipDescription = "";

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT, "Input format", PluginParameterType.STRING, "",
        true, false,
        "Input file format to be converted (check documentation for list of supported formats). If the input file format is not specified, the task will"
          + " run on all supported formats (check roda-core-formats.properties for list of supported formats)."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, "Output format", PluginParameterType.STRING, "",
        true, false, "Output file format to be converted (check documentation for list of supported formats)."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES, "Ignore other files",
        PluginParameterType.BOOLEAN, "true", false, false,
        "Do not process files that have a different format from the indicated."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "Create dissemination", PluginParameterType.BOOLEAN, "true",
      false, false,
      "If this is selected then the plugin will convert the files and create a new dissemination. If not, a new representation will be created."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE, "Dissemination title",
        PluginParameterType.STRING, "Dissemination title", false, false,
        "If the 'create dissemination' option is checked, then this will be the respective dissemination title."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION, "Dissemination description", PluginParameterType.STRING,
      "Dissemination description", false, false,
      "If the 'create dissemination' option is checked, then this will be the respective dissemination description."));
  }

  protected AbstractConvertPlugin() {
    super();
    inputFormat = "";
    outputFormat = "";
  }

  protected Map<String, PluginParameter> getDefaultParameters() {
    return pluginParameters.entrySet().stream()
      .collect(Collectors.toMap(e -> e.getKey(), e -> new PluginParameter(e.getValue())));
  }

  protected List<PluginParameter> orderParameters(Map<String, PluginParameter> params) {
    List<PluginParameter> orderedList = new ArrayList<>();
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE));
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION));
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
      createDIP = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE)) {
      dipTitle = parameters.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION)) {
      dipDescription = parameters.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION);
    }

    hasPartialSuccessOnOutcome = Boolean.parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "tools",
      "convert", "allplugins", "hasPartialSuccessOnOutcome"));
  }

  @Override
  protected Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<AIP> list, Job job) throws PluginException {
    for (AIP aip : list) {
      LOGGER.debug("Processing AIP {}", aip.getId());
      List<String> newRepresentations = new ArrayList<>();
      String newRepresentationID = null;
      boolean notify = true;
      PluginState reportState = PluginState.SUCCESS;
      ValidationReport validationReport = new ValidationReport();
      boolean hasNonPdfFiles = false;
      List<File> alteredFiles = new ArrayList<>();
      List<File> newFiles = new ArrayList<>();
      List<DIPFile> newDIPFiles = new ArrayList<>();
      List<File> unchangedFiles = new ArrayList<>();

      for (Representation representation : aip.getRepresentations()) {
        newRepresentationID = IdUtils.createUUID();
        PluginState pluginResultState = PluginState.SUCCESS;

        Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
          IdUtils.getRepresentationId(representation), Representation.class, AIPState.ACTIVE);
        if (createDIP) {
          reportItem.setOutcomeObjectClass(DIP.class.getName());
        }

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
                IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file),
                  RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
                String fileMimetype = ifile.getFileFormat().getMimeType();
                String filePronom = ifile.getFileFormat().getPronom();
                String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());
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

                    String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
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
                        LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, aip.getId());
                        boolean original = false;
                        newRepresentations.add(newRepresentationID);
                        String newRepresentationType = representation.getType();
                        model.createRepresentation(aip.getId(), newRepresentationID, original, newRepresentationType,
                          notify);
                        reportItem.setOutcomeObjectId(
                          IdUtils.getRepresentationId(representation.getAipId(), newRepresentationID));
                      }

                      File f = model.createFile(aip.getId(), newRepresentationID, file.getPath(), newFileId, payload,
                        notify);
                      newFiles.add(f);
                    }

                    alteredFiles.add(file);
                    IOUtils.closeQuietly(directAccess);

                    Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                      AIPState.ACTIVE);
                    fileReportItem.setPluginState(pluginResultState).setPluginDetails(result);
                    reportItem.addReport(fileReportItem);

                  } catch (CommandException e) {
                    pluginResultState = PluginState.PARTIAL_SUCCESS;
                    reportState = pluginResultState;
                    reportItem.setPluginState(pluginResultState)
                      .addPluginDetails(e.getMessage() + "\n" + e.getOutput() + "\n");

                    LOGGER.debug("Conversion ({} to {}) failed on file {} of representation {} from AIP {}", fileFormat,
                      outputFormat, file.getId(), representation.getId(), aip.getId());
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
            if (ignoreFiles && !validationReport.getIssues().isEmpty()) {
              reportItem.setHtmlPluginDetails(true)
                .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
            }
          }

          if (hasNonPdfFiles) {
            reportItem.setPluginDetails("Certain files were not ignored");
          }

          // add unchanged files to the new representation if created
          if (!alteredFiles.isEmpty() && !createDIP) {
            createNewFilesOnRepresentation(storage, model, unchangedFiles, newRepresentationID, notify);
          }

        } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
          | AuthorizationDeniedException | IOException | AlreadyExistsException e) {
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
              createPremisSkeletonOnRepresentation(model, aip.getId(), rep);
            } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
              | ValidationException | IOException | XmlException e) {
              LOGGER.error("Error running premis skeleton on new representation: {}", e.getMessage());
            }
          }
        }
      }

      try {
        LOGGER.debug("Creating convert plugin event for the AIP {}", aip.getId());
        boolean notifyEvent = false;
        createEvent(model, index, aip.getId(), null, null, null, outputFormat, reportState, alteredFiles, newFiles,
          notifyEvent);
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
    Report report, SimpleJobPluginInfo jobPluginInfo, List<Representation> list, Job job) throws PluginException {
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
      boolean notify = true;

      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
        Representation.class, AIPState.ACTIVE);
      if (createDIP) {
        reportItem.setOutcomeObjectClass(DIP.class.getName());
      }

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

                LOGGER.debug("Running a ConvertPlugin ({} to {}) on {}", fileFormat, outputFormat, file.getId());
                try {
                  Path pluginResult = Files.createTempFile("converted", "." + getOutputFormat());
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
                      model.createRepresentation(aipId, newRepresentationID, original, newRepresentationType, notify);
                      reportItem.setOutcomeObjectId(
                        IdUtils.getRepresentationId(representation.getAipId(), newRepresentationID));
                    }
                  }

                  String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                  if (createDIP) {
                    DIPFile f = model.createDIPFile(newRepresentationID, file.getPath(), newFileId,
                      directAccess.getPath().toFile().length(), payload, notify);
                    newDIPFiles.add(f);
                  } else {
                    File newFile = model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload,
                      notify);
                    newFiles.add(newFile);
                  }

                  alteredFiles.add(file);
                  IOUtils.closeQuietly(directAccess);

                  Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                    AIPState.ACTIVE);
                  fileReportItem.setPluginState(PluginState.SUCCESS).setPluginDetails(result);
                  reportItem.addReport(fileReportItem);

                } catch (CommandException e) {
                  reportState = PluginState.PARTIAL_SUCCESS;
                  reportItem.setPluginState(reportState).addPluginDetails(e.getMessage() + "\n" + e.getOutput() + "\n");

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

        if (reportState.equals(PluginState.SUCCESS) && ignoreFiles && !validationReport.getIssues().isEmpty()) {
          reportItem.setHtmlPluginDetails(true)
            .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
        }

        if (hasNonPdfFiles) {
          reportItem.setPluginDetails("Certain files were not ignored");
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

        // add unchanged files to the new representation
        if (!alteredFiles.isEmpty()) {
          if (createDIP) {
            createNewFilesOnDIP(storage, model, unchangedFiles, newRepresentationID, notify);
          } else {
            createNewFilesOnRepresentation(storage, model, unchangedFiles, newRepresentationID, notify);
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
        newFiles, notifyEvent);

      if (!createDIP) {
        try {
          Representation rep = model.retrieveRepresentation(representation.getAipId(), newRepresentationID);
          createPremisSkeletonOnRepresentation(model, representation.getAipId(), rep);
        } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
          | ValidationException | IOException | XmlException e) {
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
    SimpleJobPluginInfo jobPluginInfo, List<File> list, Job job) throws PluginException {

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
                model.createRepresentation(file.getAipId(), newRepresentationID, original, newRepresentationType,
                  model.getStorage(), storagePath, true);
              }

              // update file on new representation
              newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);

              if (createDIP) {
                DIPFile f = model.createDIPFile(newRepresentationID, file.getPath(), newFileId,
                  directAccess.getPath().toFile().length(), payload, notify);
                newDIPFiles.add(f);
              } else {
                model.deleteFile(file.getAipId(), newRepresentationID, file.getPath(), file.getId(), notify);
                File f = model.createFile(file.getAipId(), newRepresentationID, file.getPath(), newFileId, payload,
                  notify);
                newFiles.add(f);
                reportItem.setOutcomeObjectId(IdUtils.getFileId(f));
                changedRepresentationsOnAIPs.put(file.getRepresentationId(), file.getAipId());
              }

              Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                AIPState.ACTIVE);
              fileReportItem.setPluginState(PluginState.SUCCESS).setPluginDetails(result);
              reportItem.addReport(fileReportItem);

            } catch (CommandException e) {
              pluginResultState = PluginState.PARTIAL_SUCCESS;
              Report fileReportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class,
                AIPState.ACTIVE);
              fileReportItem.setPluginState(PluginState.PARTIAL_SUCCESS)
                .setPluginDetails(e.getMessage() + "\n" + e.getOutput());
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
        | AuthorizationDeniedException | ValidationException | IOException | AlreadyExistsException e) {
        LOGGER.error("Error processing File {}: {}", file.getId(), e.getMessage(), e);
        reportState = PluginState.FAILURE;
        reportItem.setPluginDetails(e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } finally {
        reportItem.setPluginState(pluginResultState);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }

      boolean notifyEvent = false;
      createEvent(model, index, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), outputFormat,
        reportState, Arrays.asList(file), newFiles, notifyEvent);

      if (!createDIP) {
        try {
          Representation rep = model.retrieveRepresentation(file.getAipId(), newRepresentationID);
          createPremisSkeletonOnRepresentation(model, file.getAipId(), rep);
        } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
          | ValidationException | IOException | XmlException e) {
          LOGGER.error("Error running premis skeleton on new representation: {}", e.getMessage());
        }
      }
    }

    return report;
  }

  public abstract String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException;

  private void createPremisSkeletonOnRepresentation(ModelService model, String aipId, Representation representation)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    ValidationException, IOException, XmlException {
    List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
    PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aipId, representation.getId(), algorithms);
    model.notifyRepresentationUpdated(representation);
  }

  private void createEvent(ModelService model, IndexService index, String aipId, String representationId,
    List<String> filePath, String fileId, String outputFormat, PluginState outcome, List<File> alteredFiles,
    List<File> newFiles, boolean notify) throws PluginException {

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
        premisSourceFilesIdentifiers, premisTargetFilesIdentifiers, outcome, stringBuilder.toString(), notify);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      throw new PluginException(e.getMessage(), e);
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
      if (filePronom != null && !filePronom.isEmpty() && pronomToExtension.get(filePronom) != null
        && !pronomToExtension.get(filePronom).contains(fileFormat)) {
        newFileFormat = pronomToExtension.get(filePronom).get(0);
      } else if (fileMimetype != null && !fileMimetype.isEmpty() && mimetypeToExtension.get(fileMimetype) != null
        && !mimetypeToExtension.get(fileMimetype).contains(fileFormat)) {
        newFileFormat = mimetypeToExtension.get(fileMimetype).get(0);
      }
    }

    return newFileFormat;
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

}
