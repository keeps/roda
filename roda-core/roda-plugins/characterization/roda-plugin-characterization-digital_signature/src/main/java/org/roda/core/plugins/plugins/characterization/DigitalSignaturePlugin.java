/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.common.FileFormatUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalSignaturePlugin<T extends IsRODAObject> extends AbstractAIPComponentsPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignaturePlugin.class);

  private boolean doVerify;
  private boolean doExtract;
  private boolean doStrip;
  private boolean verificationAffectsOnOutcome;
  private List<String> applicableTo;
  private Map<String, List<String>> pronomToExtension;
  private Map<String, List<String>> mimetypeToExtension;
  private boolean ignoreFiles = true;
  private boolean createDIP = false;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIGNATURE_VERIFY,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_SIGNATURE_VERIFY, "Verify digital signature",
        PluginParameterType.BOOLEAN, "true", true, false, "Verifies the digital signature of files."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIGNATURE_EXTRACT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_SIGNATURE_EXTRACT, "Extract digital signature",
        PluginParameterType.BOOLEAN, "false", true, false,
        "Extracts the digital signature and stores it in the AIP under metadata/other folder."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIGNATURE_STRIP,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_SIGNATURE_STRIP, "Strip digital signature",
        PluginParameterType.BOOLEAN, "true", true, false, "Removes the digital signature from the original file."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES, "Ignore non PDF files",
        PluginParameterType.BOOLEAN, "true", false, false, "Ignore files that are not recognised as PDF."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "Create dissemination", PluginParameterType.BOOLEAN, "true",
      false, false,
      "If this is selected then the plugin will strip the file to a new dissemination. If not, a new representation will be created."));
  }

  public DigitalSignaturePlugin() {
    super();
    doVerify = true;
    doExtract = false;
    doStrip = false;
    verificationAffectsOnOutcome = Boolean.parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "tools",
      "digitalsignature", "verificationAffectsOnOutcome"));

    applicableTo = FileFormatUtils.getInputExtensions("digitalsignature");
    pronomToExtension = FileFormatUtils.getPronomToExtension("digitalsignature");
    mimetypeToExtension = FileFormatUtils.getMimetypeToExtension("digitalsignature");
  }

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
    return "Digital signature validation";
  }

  @Override
  public String getDescription() {
    return "Checks if digital signatures embedded in files are valid. \nThe task supports the following formats: PDF, "
      + "Microsoft Office Formats (.docx, .xslx, .pptx) and OpenDocument formats (.odt, .ods, .odp).\nThe outcome of this action is "
      + "three-fold: \n1) the outcome of verification is stored in a PREMIS event; \n2) the extracted digital signatures are stored within "
      + "the AIP under the “metadata/other” folder; and \n3) the files with the digital signature removed are stored under a new representation "
      + "in the Archival Information Package (AIP).";
  }

  private String getDIPTitle() {
    return "Signature-stripped file";
  }

  private String getDIPDescription() {
    return "Result file after stripping its digital signature";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_SIGNATURE_VERIFY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_SIGNATURE_EXTRACT));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_SIGNATURE_STRIP));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // do the digital signature verification
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_SIGNATURE_VERIFY)) {
      doVerify = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_SIGNATURE_VERIFY));
    }

    // do the digital signature information extraction
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_SIGNATURE_EXTRACT)) {
      doExtract = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_SIGNATURE_EXTRACT));
    }

    // do the digital signature strip
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_SIGNATURE_STRIP)) {
      doStrip = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_SIGNATURE_STRIP));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES)) {
      ignoreFiles = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP)) {
      createDIP = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP));
    }

  }

  @Override
  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<AIP> list, Job job) throws PluginException {
    List<String> newRepresentations = new ArrayList<>();

    for (AIP aip : list) {
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState reportState = PluginState.SUCCESS;
      ValidationReport validationReport = new ValidationReport();
      boolean hasNonPdfFiles = false;
      Map<String, String> verifiedFiles = new HashMap<>();
      List<File> alteredFiles = new ArrayList<>();
      List<File> extractedFiles = new ArrayList<>();
      List<File> newFiles = new ArrayList<>();

      try {
        for (Representation representation : aip.getRepresentations()) {
          List<File> unchangedFiles = new ArrayList<>();
          String newRepresentationID = IdUtils.createUUID();
          String verification = null;
          boolean notify = true;

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
                String fileInfoPath = ModelUtils
                  .getFileStoragePath(aip.getId(), representation.getId(), file.getPath(), file.getId()).toString();

                if (((filePronom != null && pronomToExtension.containsKey(filePronom))
                  || (fileMimetype != null && getMimetypeToExtension().containsKey(fileMimetype))
                  || (applicableTo.contains(fileFormat)))) {

                  fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype);
                  StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                  DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
                  LOGGER.debug("Running DigitalSignaturePlugin on {}", file.getId());

                  if (doVerify) {
                    LOGGER.debug("Verifying digital signatures on {}", file.getId());

                    verification = DigitalSignaturePluginUtils.runDigitalSignatureVerify(directAccess.getPath(),
                      fileFormat, fileMimetype);
                    verifiedFiles.put(file.getId(), verification);

                    if (!"Passed".equals(verification) && verificationAffectsOnOutcome) {
                      reportState = PluginState.FAILURE;
                      reportItem.addPluginDetails(" Signature validation failed on " + fileInfoPath + ".\n");
                    }
                  }

                  if (doExtract) {
                    LOGGER.debug("Extracting digital signatures information of {}", file.getId());
                    int extractResultSize = DigitalSignaturePluginUtils.runDigitalSignatureExtraction(model, file,
                      directAccess.getPath(), fileFormat, fileMimetype);

                    if (extractResultSize > 0) {
                      extractedFiles.add(file);
                    }
                  }

                  if (doStrip) {
                    LOGGER.debug("Stripping digital signatures from {}", file.getId());
                    Path pluginResult = DigitalSignaturePluginUtils.runDigitalSignatureStrip(directAccess.getPath(),
                      fileFormat, fileMimetype);

                    if (pluginResult != null) {
                      ContentPayload payload = new FSPathContentPayload(pluginResult);
                      String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + fileFormat);

                      if (createDIP) {
                        FileLink fileLink = new FileLink(representation.getAipId(), representation.getId(),
                          file.getPath(), file.getId());
                        List<FileLink> links = new ArrayList<>();
                        links.add(fileLink);

                        DIP dip = new DIP();
                        dip.setId(IdUtils.createUUID());
                        dip.setFileIds(links);
                        dip.setPermissions(aip.getPermissions());
                        dip.setTitle(getDIPTitle());
                        dip.setDescription(getDIPDescription());
                        dip.setType(RodaConstants.DIP_TYPE_DIGITAL_SIGNATURE);
                        dip = model.createDIP(dip, true);

                        model.createDIPFile(dip.getId(), file.getPath(), newFileId, 0L, payload, notify);
                      } else {
                        if (!newRepresentations.contains(newRepresentationID)) {
                          LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, aip.getId());
                          boolean original = false;
                          newRepresentations.add(newRepresentationID);
                          model.createRepresentation(aip.getId(), newRepresentationID, original,
                            representation.getType(), notify);
                        }

                        // update file on new representation
                        File f = model.createFile(aip.getId(), newRepresentationID, file.getPath(), newFileId, payload,
                          notify);
                        newFiles.add(f);
                      }

                      alteredFiles.add(file);
                    } else {
                      LOGGER.debug("Process failed on file {} of representation {} from AIP {}", file.getId(),
                        representation.getId(), aip.getId());
                      reportState = PluginState.FAILURE;
                      reportItem.addPluginDetails(" Signature validation stripping on " + fileInfoPath + ".");
                    }
                  }
                  IOUtils.closeQuietly(directAccess);

                } else {
                  unchangedFiles.add(file);

                  if (ignoreFiles) {
                    validationReport.addIssue(new ValidationIssue(fileInfoPath));
                  } else {
                    reportState = PluginState.FAILURE;
                    hasNonPdfFiles = true;
                  }
                }
              }
            } else {
              LOGGER.error("Cannot process representation file", oFile.getCause());
            }
          }

          IOUtils.closeQuietly(allFiles);

          // add unchanged files to the new representation
          if (!alteredFiles.isEmpty() && !createDIP) {
            for (File f : unchangedFiles) {
              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
              Binary binary = storage.getBinary(fileStoragePath);
              Path uriPath = Paths.get(binary.getContent().getURI());
              ContentPayload payload = new FSPathContentPayload(uriPath);
              model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, notify);
            }
          }
        }

        jobPluginInfo.incrementObjectsProcessed(reportState);
        reportItem.setPluginState(reportState);

        if (!reportState.equals(PluginState.FAILURE)) {
          if (ignoreFiles && !validationReport.getIssues().isEmpty()) {
            reportItem.setHtmlPluginDetails(true)
              .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
          }
        }

        if (hasNonPdfFiles) {
          reportItem.setPluginDetails("Non PDF files were not ignored");
        }

      } catch (RODAException | IOException | RuntimeException e) {
        LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } finally {
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

        LOGGER.debug("Creating digital signature plugin event on AIP {}", aip.getId());
        boolean notifyEvent = true;
        createEvent(model, index, aip.getId(), null, null, null, reportState, alteredFiles, extractedFiles, newFiles,
          verifiedFiles, notifyEvent);
      }
    }

    return report;
  }

  @Override
  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<Representation> list, Job job) throws PluginException {
    List<String> newRepresentations = new ArrayList<>();

    for (Representation representation : list) {
      String newRepresentationID = IdUtils.createUUID();
      List<File> unchangedFiles = new ArrayList<>();
      List<File> alteredFiles = new ArrayList<>();
      List<File> extractedFiles = new ArrayList<>();
      List<File> newFiles = new ArrayList<>();
      Map<String, String> verifiedFiles = new HashMap<>();
      String aipId = representation.getAipId();
      String verification = null;
      boolean notify = true;

      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
        Representation.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState reportState = PluginState.SUCCESS;
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
              String fileInfoPath = ModelUtils
                .getFileStoragePath(representation.getAipId(), representation.getId(), file.getPath(), file.getId())
                .toString();

              if (((filePronom != null && pronomToExtension.containsKey(filePronom))
                || (fileMimetype != null && getMimetypeToExtension().containsKey(fileMimetype))
                || (applicableTo.contains(fileFormat)))) {

                fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype);
                StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
                LOGGER.debug("Running DigitalSignaturePlugin on {}", file.getId());

                if (doVerify) {
                  LOGGER.debug("Verifying digital signatures on {}", file.getId());

                  verification = DigitalSignaturePluginUtils.runDigitalSignatureVerify(directAccess.getPath(),
                    fileFormat, fileMimetype);
                  verifiedFiles.put(file.getId(), verification);

                  if (!"Passed".equals(verification) && verificationAffectsOnOutcome) {
                    reportState = PluginState.FAILURE;
                    reportItem.addPluginDetails(" Signature validation failed on " + fileInfoPath + ".");
                  }
                }

                if (doExtract) {
                  LOGGER.debug("Extracting digital signatures information of {}", file.getId());
                  int extractResultSize = DigitalSignaturePluginUtils.runDigitalSignatureExtraction(model, file,
                    directAccess.getPath(), fileFormat, fileMimetype);

                  if (extractResultSize > 0) {
                    extractedFiles.add(file);
                  }
                }

                if (doStrip) {
                  LOGGER.debug("Stripping digital signatures from {}", file.getId());
                  Path pluginResult = DigitalSignaturePluginUtils.runDigitalSignatureStrip(directAccess.getPath(),
                    fileFormat, fileMimetype);

                  if (pluginResult != null) {
                    ContentPayload payload = new FSPathContentPayload(pluginResult);

                    if (!newRepresentations.contains(newRepresentationID)) {
                      LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, aipId);
                      boolean original = false;
                      newRepresentations.add(newRepresentationID);
                      model.createRepresentation(aipId, newRepresentationID, original, representation.getType(),
                        notify);
                      reportItem.setOutcomeObjectId(
                        IdUtils.getRepresentationId(representation.getAipId(), newRepresentationID));
                    }

                    // update file on new representation
                    String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + fileFormat);
                    File f = model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload, notify);
                    alteredFiles.add(file);
                    newFiles.add(f);
                    reportItem.setPluginState(reportState);

                  } else {
                    LOGGER.debug("Process failed on file {} of representation {} from AIP {}", file.getId(),
                      representation.getId(), aipId);
                    reportState = PluginState.FAILURE;
                    reportItem.addPluginDetails(" Signature validation stripping on " + fileInfoPath + ".");
                  }
                }
                IOUtils.closeQuietly(directAccess);
              } else {
                unchangedFiles.add(file);

                if (ignoreFiles) {
                  validationReport.addIssue(new ValidationIssue(fileInfoPath));
                } else {
                  reportState = PluginState.FAILURE;
                  hasNonPdfFiles = true;
                }
              }
            }
          } else {
            LOGGER.error("Cannot process representation file", oFile.getCause());
          }
        }

        IOUtils.closeQuietly(allFiles);

        // add unchanged files to the new representation
        if (!alteredFiles.isEmpty()) {
          for (File f : unchangedFiles) {
            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
            Binary binary = storage.getBinary(fileStoragePath);
            Path uriPath = Paths.get(binary.getContent().getURI());
            ContentPayload payload = new FSPathContentPayload(uriPath);
            model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, notify);
          }
        }

        LOGGER.debug("Creating digital signature plugin event for the representation {}", representation.getId());
        boolean notifyEvent = true;
        createEvent(model, index, aipId, representation.getId(), null, null, reportState, alteredFiles, extractedFiles,
          newFiles, verifiedFiles, notifyEvent);
        reportItem.setPluginState(reportState);
        jobPluginInfo.incrementObjectsProcessed(reportState);

        if (!reportState.equals(PluginState.FAILURE)) {
          if (ignoreFiles) {
            reportItem.setHtmlPluginDetails(true)
              .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
          }
        }

        if (hasNonPdfFiles) {
          reportItem.setPluginDetails("Non PDF files were not ignored");
        }

      } catch (RODAException | IOException | RuntimeException e) {
        LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        reportState = PluginState.FAILURE;
        reportItem.setPluginState(reportState).setPluginDetails(e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } finally {
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }

    return report;
  }

  @Override
  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<File> list, Job job) throws PluginException {
    List<String> newRepresentations = new ArrayList<>();

    String newRepresentationID = IdUtils.createUUID();
    List<File> unchangedFiles = new ArrayList<>();
    List<File> alteredFiles = new ArrayList<>();
    List<File> extractedFiles = new ArrayList<>();
    List<File> newFiles = new ArrayList<>();
    Map<String, String> verifiedFiles = new HashMap<>();

    String verification = null;
    boolean notify = true;

    for (File file : list) {
      LOGGER.debug("Processing file {}", file);
      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState reportState = PluginState.SUCCESS;

      try {
        if (!file.isDirectory()) {
          IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file),
            RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
          String fileMimetype = ifile.getFileFormat().getMimeType();
          String filePronom = ifile.getFileFormat().getPronom();
          String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

          if (((filePronom != null && pronomToExtension.containsKey(filePronom))
            || (fileMimetype != null && getMimetypeToExtension().containsKey(fileMimetype))
            || (applicableTo.contains(fileFormat)))) {

            fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype);
            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
            DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
            LOGGER.debug("Running DigitalSignaturePlugin on {}", file.getId());

            if (doVerify) {
              LOGGER.debug("Verifying digital signatures on {}", file.getId());

              verification = DigitalSignaturePluginUtils.runDigitalSignatureVerify(directAccess.getPath(), fileFormat,
                fileMimetype);
              verifiedFiles.put(file.getId(), verification);

              if (!"Passed".equals(verification) && verificationAffectsOnOutcome) {
                reportState = PluginState.FAILURE;
                reportItem.addPluginDetails("Signature validation failed on " + file.getId() + ".");
              }
            }

            if (doExtract) {
              LOGGER.debug("Extracting digital signatures information of {}", file.getId());
              int extractResultSize = DigitalSignaturePluginUtils.runDigitalSignatureExtraction(model, file,
                directAccess.getPath(), fileFormat, fileMimetype);

              if (extractResultSize > 0) {
                extractedFiles.add(file);
              }
            }

            if (doStrip) {
              LOGGER.debug("Stripping digital signatures from {}", file.getId());
              Path pluginResult = DigitalSignaturePluginUtils.runDigitalSignatureStrip(directAccess.getPath(),
                fileFormat, fileMimetype);

              if (pluginResult != null) {
                ContentPayload payload = new FSPathContentPayload(pluginResult);

                if (!newRepresentations.contains(newRepresentationID)) {
                  LOGGER.debug("Creating a new representation {} on AIP {}", newRepresentationID, file.getAipId());
                  boolean original = false;
                  newRepresentations.add(newRepresentationID);
                  Representation representation = model.retrieveRepresentation(file.getAipId(),
                    file.getRepresentationId());
                  model.createRepresentation(file.getAipId(), newRepresentationID, original, representation.getType(),
                    notify);
                }

                // update file on new representation
                String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + fileFormat);
                File f = model.createFile(file.getAipId(), newRepresentationID, file.getPath(), newFileId, payload,
                  notify);
                alteredFiles.add(file);
                newFiles.add(f);

                reportItem.setOutcomeObjectId(IdUtils.getFileId(f));
                reportItem.setPluginState(reportState);

              } else {
                LOGGER.debug("Process failed on file {} of representation {} from AIP {}", file.getId(),
                  file.getRepresentationId(), file.getAipId());

                reportState = PluginState.FAILURE;
                reportItem.setPluginState(reportState)
                  .addPluginDetails(" Signature validation stripping on " + file.getId() + ".");
              }
            }
            IOUtils.closeQuietly(directAccess);
          } else {
            unchangedFiles.add(file);

            if (!reportState.equals(PluginState.FAILURE)) {
              if (ignoreFiles) {
                reportItem.setPluginDetails("This file was ignored.");
              } else {
                reportState = PluginState.FAILURE;
                reportItem.setPluginDetails("This file was not ignored.");
              }
            }
          }
        }

        // add unchanged files to the new representation
        if (!alteredFiles.isEmpty()) {
          for (File f : unchangedFiles) {
            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
            Binary binary = storage.getBinary(fileStoragePath);
            Path uriPath = Paths.get(binary.getContent().getURI());
            ContentPayload payload = new FSPathContentPayload(uriPath);
            model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, notify);
          }
        }

        reportItem.setPluginState(reportState);

      } catch (RODAException | IOException | RuntimeException e) {
        LOGGER.error("Error processing Representation " + file.getRepresentationId() + ": " + e.getMessage(), e);
        reportState = PluginState.FAILURE;
        reportItem.setPluginState(reportState).setPluginDetails(e.getMessage());
      } finally {
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }

      LOGGER.debug("Creating digital signature plugin event for the representation {}", file.getRepresentationId());
      boolean notifyEvent = true;
      createEvent(model, index, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), reportState,
        alteredFiles, extractedFiles, newFiles, verifiedFiles, notifyEvent);
      jobPluginInfo.incrementObjectsProcessed(reportState);
    }

    return report;
  }

  private void createEvent(ModelService model, IndexService index, String aipId, String representationId,
    List<String> filePath, String fileId, PluginState pluginResultState, List<File> alteredFiles,
    List<File> extractedFiles, List<File> newFiles, Map<String, String> verifiedFiles, boolean notify)
    throws PluginException {

    List<LinkingIdentifier> premisSourceFilesIdentifiers = new ArrayList<>();
    List<LinkingIdentifier> premisTargetFilesIdentifiers = new ArrayList<>();

    // building the detail for the plugin event
    StringBuilder stringBuilder = new StringBuilder();

    if (doVerify) {
      if (!verifiedFiles.isEmpty()) {
        stringBuilder.append("The DS verification ran on: ");
        StringBuilder verifies = new StringBuilder();
        for (Entry<String, String> fileEntry : verifiedFiles.entrySet()) {
          verifies.append(fileEntry.getKey()).append(" (").append(fileEntry.getValue()).append("), ");
        }

        String verifiesString = verifies.toString();
        stringBuilder.append(verifiesString.substring(0, verifiesString.lastIndexOf(',')) + ". ");
      }
    }

    if (doExtract) {
      if (!extractedFiles.isEmpty()) {
        stringBuilder.append("The following files DS information were extracted: ");
        StringBuilder extracts = new StringBuilder();

        for (File file : extractedFiles) {
          extracts.append(file.getId()).append(", ");
        }

        if (extracts.length() > 0) {
          String extractString = extracts.toString();
          stringBuilder.append(extractString.substring(0, extractString.lastIndexOf(',')) + ". ");
        }
      }
    }

    if (alteredFiles.isEmpty()) {
      stringBuilder.append("No file was stripped on this representation.");
    } else {
      stringBuilder.append("The digital signature (DS) operation stripped some files. ");
      for (File file : alteredFiles) {
        premisSourceFilesIdentifiers.add(PluginHelper.getLinkingIdentifier(aipId, file.getRepresentationId(),
          file.getPath(), file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
      }
      for (File file : newFiles) {
        premisTargetFilesIdentifiers.add(PluginHelper.getLinkingIdentifier(aipId, file.getRepresentationId(),
          file.getPath(), file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
      }
    }

    try {
      PluginHelper.createPluginEvent(this, aipId, representationId, filePath, fileId, model, index,
        premisSourceFilesIdentifiers, premisTargetFilesIdentifiers, pluginResultState, stringBuilder.toString(),
        notify);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  private String getNewFileFormat(String fileFormat, String filePronom, String fileMimetype) {
    String changedFileFormat = fileFormat;
    if (!applicableTo.isEmpty()) {
      if (StringUtils.isNotBlank(filePronom) && pronomToExtension.get(filePronom) != null
        && !pronomToExtension.get(filePronom).contains(fileFormat)) {
        changedFileFormat = pronomToExtension.get(filePronom).get(0);
      } else if (StringUtils.isNotBlank(fileMimetype) && mimetypeToExtension.get(fileMimetype) != null
        && !mimetypeToExtension.get(fileMimetype).contains(fileFormat)) {
        changedFileFormat = mimetypeToExtension.get(fileMimetype).get(0);
      }
    }
    return changedFileFormat;
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public Plugin<T> cloneMe() {
    return new DigitalSignaturePlugin<>();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.DIGITAL_SIGNATURE_VALIDATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Checked if digital signatures were valid and/or stripped them.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Digital signatures were valid and/or they were stripped with success.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to validate and/or strip digital signatures.";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_VALIDATION, RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION);
  }

  public List<String> getApplicableTo() {
    return applicableTo;
  }

  public Map<String, List<String>> getPronomToExtension() {
    return pronomToExtension;
  }

  public Map<String, List<String>> getMimetypeToExtension() {
    return mimetypeToExtension;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    list.add(File.class);
    return (List) list;
  }

}
