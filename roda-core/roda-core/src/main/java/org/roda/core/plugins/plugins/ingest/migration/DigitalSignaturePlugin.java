package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
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
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalSignaturePlugin implements Plugin<Representation> {

  private Logger logger;
  private boolean doVerify;
  private boolean doExtract;
  private boolean doStrip;
  private long maxKbytes;
  private boolean hasPartialSuccessOnOutcome;
  private List<String> applicableTo;
  private List<String> convertableTo;
  private Map<String, List<String>> pronomToExtension;
  private Map<String, List<String>> mimetypeToExtension;

  public DigitalSignaturePlugin() {
    logger = LoggerFactory.getLogger(getClass());
    doVerify = true;
    doExtract = true;
    doStrip = true;
    maxKbytes = 20000; // default value: 20000 kb

    applicableTo = new ArrayList<>();
    convertableTo = new ArrayList<>();
    pronomToExtension = new HashMap<>();
    mimetypeToExtension = new HashMap<>();

    hasPartialSuccessOnOutcome = Boolean.parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("tools",
      "allplugins", "hasPartialSuccessOnOutcome"));
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
    return "Digital signature handler";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Plugin that can verify, extract and strip documents digital signature";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return null;
  }

  @Override
  public Map<String, String> getParameterValues() {
    return null;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // indicates the maximum kbytes the files that will be processed must have
    if (parameters.containsKey("maxKbytes")) {
      maxKbytes = Long.parseLong(parameters.get("maxKbytes"));
    }

    // ver
    if (parameters.containsKey("doVerify")) {
      doVerify = Boolean.parseBoolean(parameters.get("doVerify"));
    }

    // indicates the maximum kbytes the files that will be processed must have
    if (parameters.containsKey("doExtract")) {
      doExtract = Boolean.parseBoolean(parameters.get("doExtract"));
    }

    // indicates the maximum kbytes the files that will be processed must have
    if (parameters.containsKey("doStrip")) {
      doStrip = Boolean.parseBoolean(parameters.get("doStrip"));
    }

    fillFileFormatStructures();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Representation> list)
    throws PluginException {

    IndexedPreservationAgent agent = null;
    try {
      // Agent is detached from AIP
      boolean notify = true;
      agent = PremisUtils.createPremisAgentBinary(this, RodaConstants.PRESERVATION_AGENT_TYPE_CONVERSION_PLUGIN, model,
        notify);
    } catch (AlreadyExistsException e) {
      agent = PremisUtils.getPreservationAgent(this, RodaConstants.PRESERVATION_AGENT_TYPE_CONVERSION_PLUGIN, model);
    } catch (RODAException e) {
      logger.error("Error running adding DigitalSignature plugin: " + e.getMessage(), e);
    }

    List<String> newRepresentations = new ArrayList<String>();
    String aipId = null;

    for (Representation representation : list) {
      List<File> unchangedFiles = new ArrayList<File>();
      String newRepresentationID = UUID.randomUUID().toString();
      List<File> alteredFiles = new ArrayList<File>();
      List<File> newFiles = new ArrayList<File>();
      aipId = representation.getAipId();
      int state = 1;
      boolean notify = true;

      try {
        logger.debug("Processing representation: " + representation);

        ClosableIterable<File> allFiles = model.listAllFiles(aipId, representation.getId());

        for (File file : allFiles) {
          logger.debug("Processing file: " + file);

          if (!file.isDirectory()) {

            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String filePronom = ifile.getFileFormat().getPronom();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

            if (((filePronom != null && pronomToExtension.containsKey(filePronom))
              || (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype)) || (applicableTo
                .contains(fileFormat))) && ifile.getSize() < (maxKbytes * 1024)) {

              if (applicableTo.size() > 0) {
                if (filePronom != null && !filePronom.isEmpty()
                  && !pronomToExtension.get(filePronom).contains(fileFormat)) {
                  fileFormat = pronomToExtension.get(filePronom).get(0);
                } else if (fileMimetype != null && !fileMimetype.isEmpty()
                  && !mimetypeToExtension.get(fileMimetype).contains(fileFormat)) {
                  fileFormat = mimetypeToExtension.get(fileMimetype).get(0);
                }
              }

              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
              DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

              // FIXME file that doesn't get deleted afterwards
              logger.debug("Running DigitalSignaturePlugin on " + file.getId());

              if (doVerify) {
                DigitalSignaturePluginUtils.runDigitalSignatureVerify(directAccess.getPath(), fileFormat);
              }

              if (doExtract) {

              }

              if (doStrip) {
                Path pluginResult = DigitalSignaturePluginUtils.runDigitalSignatureStrip(directAccess.getPath(),
                  fileFormat);

                if (pluginResult != null) {
                  ContentPayload payload = new FSPathContentPayload(pluginResult);
                  StoragePath storagePath = ModelUtils.getRepresentationPath(aipId, representation.getId());

                  if (!newRepresentations.contains(newRepresentationID)) {
                    logger.debug("Creating a new representation " + newRepresentationID + " on AIP " + aipId);
                    boolean original = false;
                    newRepresentations.add(newRepresentationID);
                    model.createRepresentation(aipId, newRepresentationID, original, notify);

                    StoragePath storagePreservationPath = ModelUtils.getAIPRepresentationPreservationPath(aipId,
                      newRepresentationID);
                    model.getStorage().createDirectory(storagePreservationPath);
                  }

                  // update file on new representation
                  String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + fileFormat);
                  File f = model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload, notify);
                  alteredFiles.add(file);
                  newFiles.add(f);
                  IOUtils.closeQuietly(directAccess);

                } else {
                  logger.debug("Process failed on file " + file.getId() + " of representation "
                    + representation.getId() + " from AIP " + aipId);
                  state = 2;
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

          if (!representation.isOriginal()) {
            model.deleteRepresentation(aipId, representation.getId());
          }

          boolean notifyReindex = false;
          AbstractConvertPluginUtils.reIndexingRepresentation(index, model, storage, aipId, newRepresentationID,
            notifyReindex);

          logger.debug("Creating digital signature plugin event for the representation " + representation.getId());
          boolean notifyEvent = false;
          // createEvent(alteredFiles, newFiles, model.retrieveAIP(aipId),
          // newRepresentationID, model, state, agent, notifyEvent);
        }
      } catch (Throwable e) {
        logger.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        state = 0;
      }
    }

    try {
      model.notifyAIPUpdated(aipId);
    } catch (RODAException e) {
      logger.error("Error running creating agent for DigitalSignaturePlugin", e);
    }

    return new Report();
  }

  private void createEventStrip(List<File> alteredFiles, List<File> newFiles, AIP aip, String newRepresentationID,
    ModelService model, int state, IndexedPreservationAgent agent, boolean notify) throws PluginException {

    List<String> premisSourceFilesIdentifiers = new ArrayList<String>();
    List<String> premisTargetFilesIdentifiers = new ArrayList<String>();

    // building the detail extension for the plugin event
    String outcome = "success";
    StringBuilder stringBuilder = new StringBuilder();
    if (alteredFiles.size() == 0) {
      stringBuilder.append("No file was converted on this representation.");
    } else {
      stringBuilder.append("The following files were converted to a new format: ");

      for (File file : alteredFiles) {
        stringBuilder.append(file.getId() + ", ");
        premisSourceFilesIdentifiers.add(PremisUtils.createPremisFileIdentifier(file));
      }

      for (File file : newFiles) {
        premisTargetFilesIdentifiers.add(PremisUtils.createPremisFileIdentifier(file));
      }

      stringBuilder.setLength(stringBuilder.length() - 2);
    }

    // Conversion plugin did not run correctly
    if (state == 0 || (state == 2 && hasPartialSuccessOnOutcome == false)) {
      outcome = "failure";
      stringBuilder.setLength(0);
    }
    // some files were not converted
    if (state == 2 && hasPartialSuccessOnOutcome == true) {
      outcome = "partial success";
    }

    // FIXME revise PREMIS generation
    try {
      PluginHelper.createPluginEvent(aip.getId(), newRepresentationID, null, model,
        RodaConstants.PRESERVATION_EVENT_TYPE_MIGRATION, "Some files were converted on a new representation",
        premisSourceFilesIdentifiers, premisTargetFilesIdentifiers, outcome, stringBuilder.toString(), null, agent,
        notify);
    } catch (IOException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException | ValidationException | AlreadyExistsException e) {
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

  public void fillFileFormatStructures() {
    applicableTo.add("pdf");
    convertableTo.add("pdf");
    mimetypeToExtension.put("application/pdf", Arrays.asList("pdf"));
    pronomToExtension = PdfToPdfaPluginUtils.getPronomToExtension();
  }

}
