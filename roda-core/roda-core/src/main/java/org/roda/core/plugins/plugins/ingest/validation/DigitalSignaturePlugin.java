/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
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
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
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
import org.roda.core.plugins.plugins.ingest.migration.AbstractConvertPluginUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalSignaturePlugin extends AbstractPlugin<Representation> {

  private static Logger LOGGER = LoggerFactory.getLogger(DigitalSignaturePlugin.class);
  private static final String OTHER_METADATA_TYPE = "DigitalSignature";
  private boolean doVerify;
  private boolean doExtract;
  private boolean doStrip;
  private boolean verificationAffectsOnOutcome;
  private List<String> applicableTo;
  private Map<String, List<String>> pronomToExtension;
  private Map<String, List<String>> mimetypeToExtension;

  public DigitalSignaturePlugin() {
    doVerify = true;
    doExtract = true;
    doStrip = true;
    verificationAffectsOnOutcome = true;

    applicableTo = DigitalSignaturePluginUtils.getInputExtensions();
    pronomToExtension = DigitalSignaturePluginUtils.getPronomToExtension();
    mimetypeToExtension = DigitalSignaturePluginUtils.getMimetypeToExtension();
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
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

  public boolean getDoVerify() {
    return doVerify;
  }

  public void setDoVerify(boolean verify) {
    doVerify = verify;
  }

  public boolean getDoExtract() {
    return doExtract;
  }

  public void setDoExtract(boolean extract) {
    doExtract = extract;
  }

  public boolean getDoStrip() {
    return doStrip;
  }

  public void getDoStrip(boolean strip) {
    doStrip = strip;
  }

  public boolean getVerificationAffectsOnOutcome() {
    return verificationAffectsOnOutcome;
  }

  public void setVerificationAffectsOnOutcome(boolean affects) {
    verificationAffectsOnOutcome = affects;
  }

  @Override
  public String getName() {
    return "Validation of digital signature";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Check if a digital signatures are valid.";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    // do the digital signature verification
    if (parameters.containsKey("doVerify")) {
      doVerify = Boolean.parseBoolean(parameters.get("doVerify"));
    }

    // do the digital signature information extraction
    if (parameters.containsKey("doExtract")) {
      doExtract = Boolean.parseBoolean(parameters.get("doExtract"));
    }

    // do the digital signature strip
    if (parameters.containsKey("doStrip")) {
      doStrip = Boolean.parseBoolean(parameters.get("doStrip"));
    }

    // determines if the verification affects the outcome value
    if (parameters.containsKey("verificationAffectsOnOutcome")) {
      verificationAffectsOnOutcome = Boolean.parseBoolean(parameters.get("verificationAffectsOnOutcome"));
    }

  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Representation> list)
    throws PluginException {
    List<String> newRepresentations = new ArrayList<String>();
    String aipId = null;
    Report report = PluginHelper.createPluginReport(this);

    for (Representation representation : list) {
      List<File> unchangedFiles = new ArrayList<File>();
      String newRepresentationID = UUID.randomUUID().toString();
      List<File> alteredFiles = new ArrayList<File>();
      List<File> extractedFiles = new ArrayList<File>();
      List<File> newFiles = new ArrayList<File>();
      Map<String, String> verifiedFiles = new HashMap<String, String>();
      aipId = representation.getAipId();
      int pluginResultState = 1;
      String verification = null;
      boolean notify = true;
      Report reportItem = PluginHelper.createPluginReportItem(this, representation.getId(), null);

      try {
        LOGGER.debug("Processing representation: " + representation);
        boolean recursive = true;
        CloseableIterable<File> allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(),
          recursive);

        for (File file : allFiles) {
          LOGGER.debug("Processing file: " + file);

          if (!file.isDirectory()) {
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String filePronom = ifile.getFileFormat().getPronom();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

            if (((filePronom != null && pronomToExtension.containsKey(filePronom))
              || (fileMimetype != null && getMimetypeToExtension().containsKey(fileMimetype)) || (applicableTo
                .contains(fileFormat)))) {

              fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype);
              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
              DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
              LOGGER.debug("Running DigitalSignaturePlugin on " + file.getId());

              if (doVerify) {
                LOGGER.debug("Verying digital signatures on " + file.getId());

                verification = DigitalSignaturePluginUtils
                  .runDigitalSignatureVerify(directAccess.getPath(), fileFormat);
                verifiedFiles.put(file.getId(), verification);

                if (!verification.equals("Passed") && verificationAffectsOnOutcome)
                  pluginResultState = 0;
              }

              if (doExtract) {
                LOGGER.debug("Extracting digital signatures information of " + file.getId());
                int extractResultSize = runExtraction(model, file, directAccess.getPath(), fileFormat);

                if (extractResultSize > 0)
                  extractedFiles.add(file);
              }

              if (doStrip) {
                LOGGER.debug("Stripping digital signatures from " + file.getId());
                Path pluginResult = DigitalSignaturePluginUtils.runDigitalSignatureStrip(directAccess.getPath(),
                  fileFormat);

                if (pluginResult != null) {
                  ContentPayload payload = new FSPathContentPayload(pluginResult);

                  if (!newRepresentations.contains(newRepresentationID)) {
                    LOGGER.debug("Creating a new representation " + newRepresentationID + " on AIP " + aipId);
                    boolean original = false;
                    newRepresentations.add(newRepresentationID);
                    model.createRepresentation(aipId, newRepresentationID, original, notify);
                  }

                  // update file on new representation
                  String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + fileFormat);
                  File f = model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload, notify);
                  alteredFiles.add(file);
                  newFiles.add(f);
                  IOUtils.closeQuietly(directAccess);

                  if (pluginResultState == 1)
                    reportItem.setPluginState(PluginState.SUCCESS);

                } else {
                  LOGGER.debug("Process failed on file " + file.getId() + " of representation "
                    + representation.getId() + " from AIP " + aipId);
                  pluginResultState = 0;

                  reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(
                    "Convert process failed on file " + file.getId() + " of representation " + representation.getId()
                      + " from AIP " + aipId);
                }
              }
            } else {
              unchangedFiles.add(file);
            }
          }
        }
        IOUtils.closeQuietly(allFiles);

        // add unchanged files to the new representation
        if (alteredFiles.size() > 0) {
          for (File f : unchangedFiles) {
            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
            Binary binary = storage.getBinary(fileStoragePath);
            Path uriPath = Paths.get(binary.getContent().getURI());
            ContentPayload payload = new FSPathContentPayload(uriPath);
            model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, notify);
          }

          boolean notifyReindex = false;
          AbstractConvertPluginUtils.reIndexingRepresentationAfterConversion(index, model, storage, aipId,
            newRepresentationID, notifyReindex);
        }

        LOGGER.debug("Creating digital signature plugin event for the representation " + representation.getId());
        boolean notifyEvent = false;
        createEvent(alteredFiles, extractedFiles, newFiles, verifiedFiles, model.retrieveAIP(aipId),
          newRepresentationID, model, pluginResultState, notifyEvent);

      } catch (Throwable e) {
        LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        pluginResultState = 0;
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      }

      report.addReport(reportItem);
    }

    try {
      model.notifyAIPUpdated(aipId);
    } catch (RODAException e) {
      LOGGER.error("Error notifying update of AIP on DigitalSignaturePlugin ", e);
    }

    return report;
  }

  private void createEvent(List<File> alteredFiles, List<File> extractedFiles, List<File> newFiles,
    Map<String, String> verifiedFiles, AIP aip, String newRepresentationID, ModelService model, int pluginResultState,
    boolean notify) throws PluginException {

    List<LinkingIdentifier> premisSourceFilesIdentifiers = new ArrayList<LinkingIdentifier>();
    List<LinkingIdentifier> premisTargetFilesIdentifiers = new ArrayList<LinkingIdentifier>();

    // building the detail for the plugin event
    PluginState state = PluginState.SUCCESS;
    StringBuilder stringBuilder = new StringBuilder();

    if (doVerify) {
      stringBuilder.append("The DS verification ran on: ");
      String verifies = "";
      for (String fileId : verifiedFiles.keySet()) {
        verifies += fileId + " (" + verifiedFiles.get(fileId) + "), ";
      }
      stringBuilder.append(verifies.substring(0, verifies.lastIndexOf(',')) + ". ");
    }

    if (doExtract) {
      stringBuilder.append("The following files DS information were extracted: ");
      String extracts = "";

      for (File file : extractedFiles) {
        extracts += file.getId() + ", ";
      }

      if (extracts.length() > 0)
        stringBuilder.append(extracts.substring(0, extracts.lastIndexOf(',')) + ". ");
    }

    if (alteredFiles.isEmpty()) {
      stringBuilder.append("No file was stripped on this representation.");
    } else {
      stringBuilder.append("The digital signature (DS) operation stripped some files. ");
      for (File file : alteredFiles) {
        premisSourceFilesIdentifiers.add(PluginHelper.getLinkingIdentifier(aip.getId(), file.getRepresentationId(),
          file.getPath(), file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
      }
      for (File file : newFiles) {
        premisTargetFilesIdentifiers.add(PluginHelper.getLinkingIdentifier(aip.getId(), file.getRepresentationId(),
          file.getPath(), file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
      }
    }

    // Digital Signature plugin did not run correctly
    if (pluginResultState == 0)
      state = PluginState.FAILURE;

    // FIXME revise PREMIS generation
    try {
      PluginHelper.createPluginEvent(this, aip.getId(), model, premisSourceFilesIdentifiers,
        premisTargetFilesIdentifiers, state, stringBuilder.toString(), notify);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  private String getNewFileFormat(String fileFormat, String filePronom, String fileMimetype) {
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

  private int runExtraction(ModelService model, File file, Path input, String fileFormat) {
    List<Path> extractResult = new ArrayList<Path>();

    try {
      if (fileFormat.equals("pdf")) {
        extractResult = DigitalSignaturePluginUtils.runDigitalSignatureExtractPDF(input);

        if (extractResult.size() > 0) {
          ContentPayload mainPayload = new FSPathContentPayload(extractResult.get(0));
          ContentPayload contentsPayload = new FSPathContentPayload(extractResult.get(1));

          model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId()
            .substring(0, file.getId().lastIndexOf('.')), ".txt", DigitalSignaturePlugin.OTHER_METADATA_TYPE,
            mainPayload, true);

          if (extractResult.get(1) != null) {
            model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId()
              .substring(0, file.getId().lastIndexOf('.')), ".pkcs7", DigitalSignaturePlugin.OTHER_METADATA_TYPE,
              contentsPayload, true);
          }
        }
      } else if (fileFormat.equals("docx") || fileFormat.equals("xlsx") || fileFormat.equals("pptx")) {
        Map<Path, String> extractMap = DigitalSignaturePluginUtils.runDigitalSignatureExtractOOXML(input);
        extractResult = new ArrayList<Path>(extractMap.keySet());

        for (Path p : extractResult) {
          ContentPayload mainPayload = new FSPathContentPayload(p);
          model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId()
            .substring(0, file.getId().lastIndexOf('.')) + "_" + extractMap.get(p), ".xml",
            DigitalSignaturePlugin.OTHER_METADATA_TYPE, mainPayload, true);
        }
      } else if (fileFormat.equals("odt") || fileFormat.equals("ods") || fileFormat.equals("odp")) {
        extractResult = DigitalSignaturePluginUtils.runDigitalSignatureExtractODF(input);

        if (extractResult.size() > 0) {
          ContentPayload mainPayload = new FSPathContentPayload(extractResult.get(0));
          model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId()
            .substring(0, file.getId().lastIndexOf('.')), ".xml", DigitalSignaturePlugin.OTHER_METADATA_TYPE,
            mainPayload, true);
        }
      }
    } catch (IOException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException | SignatureException e) {
      LOGGER.warn("Problems running a document digital signature extraction");
    }

    return extractResult.size();
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public Plugin<Representation> cloneMe() {
    return new DigitalSignaturePlugin();
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

}
