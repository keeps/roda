package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.metadata.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaEmbeddedPluginOrchestrator;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConvertPlugin implements Plugin<Serializable> {

  public Logger logger;
  public String inputFormat;
  public String outputFormat;
  public long maxKbytes;
  public boolean hasPartialSuccessOnOutcome;
  public List<String> applicableTo;
  public List<String> convertableTo;
  public Map<String, List<String>> pronomToExtension;
  public Map<String, List<String>> mimetypeToExtension;

  protected AbstractConvertPlugin() {
    logger = LoggerFactory.getLogger(getClass());
    inputFormat = "";
    outputFormat = "";
    maxKbytes = 20000; // default value: 20000 kb

    applicableTo = new ArrayList<>();
    convertableTo = new ArrayList<>();
    pronomToExtension = new HashMap<>();
    mimetypeToExtension = new HashMap<>();

    hasPartialSuccessOnOutcome = Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("tools", "allplugins", "hasPartialSuccessOnOutcome"));
  }

  public void init() throws PluginException {
    // do nothing
  }

  public void shutdown() {
    // do nothing
  }

  public abstract String getName();

  public abstract String getDescription();

  public abstract String getVersion();

  public abstract Plugin<Serializable> cloneMe();

  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  public boolean areParameterValuesValid() {
    return true;
  }

  public List<PluginParameter> getParameters() {
    List<PluginParameter> params = new ArrayList<PluginParameter>();

    PluginParameter outputParam = new PluginParameter("outputParams", "Output parameters", PluginParameterType.STRING,
      "", convertableTo, true, true, "Lists the possible output formats");

    params.add(outputParam);
    return params;
  }

  public Map<String, String> getParameterValues() {
    Map<String, String> parametersMap = new HashMap<String, String>();
    parametersMap.put("inputFormat", inputFormat);
    parametersMap.put("outputFormat", outputFormat);
    parametersMap.put("maxKbytes", Long.toString(maxKbytes));
    parametersMap.put("hasPartialSuccessOnOutcome", Boolean.toString(hasPartialSuccessOnOutcome));
    return parametersMap;
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // indicates the maximum kbytes the files that will be processed must have
    if (parameters.containsKey("maxKbytes")) {
      maxKbytes = Long.parseLong(parameters.get("maxKbytes"));
    }

    // input image format
    if (parameters.containsKey("inputFormat")) {
      inputFormat = parameters.get("inputFormat");
    }

    // output image format
    if (parameters.containsKey("outputFormat")) {
      outputFormat = parameters.get("outputFormat");
    }
  }

  public Report execute(IndexService index, ModelService model, StorageService storage, List<Serializable> list)
    throws PluginException {

    IndexedPreservationAgent agent = null;
    try {
      agent = PremisUtils.createPremisAgentBinary(this, RodaConstants.PRESERVATION_AGENT_TYPE_CONVERSION_PLUGIN, model);
    } catch (RODAException e) {
      logger.error("Error running adding Conversion plugin: " + e.getMessage(), e);
    }

    if (list.size() > 0) {
      if (list.get(0) instanceof AIP)
        return executeOnAIP(index, model, storage, (List<AIP>) (List<?>) list, agent);
      if (list.get(0) instanceof Representation)
        return executeOnRepresentation(index, model, storage, (List<Representation>) (List<?>) list, agent);
      if (list.get(0) instanceof File)
        return executeOnFile(index, model, storage, (List<File>) (List<?>) list, agent);
    }

    return null;
  }

  private Report executeOnAIP(IndexService index, ModelService model, StorageService storage, List<AIP> list,
    IndexedPreservationAgent agent) throws PluginException {

    for (AIP aip : list) {
      logger.debug("Processing AIP " + aip.getId());
      List<String> newRepresentations = new ArrayList<String>();
      String newRepresentationID = null;
      boolean notify = true;

      for (Representation representation : aip.getRepresentations()) {
        List<String> alteredFiles = new ArrayList<String>();
        List<File> unchangedFiles = new ArrayList<File>();
        newRepresentationID = UUID.randomUUID().toString();

        int state = 1;

        try {
          logger.debug("Processing representation: " + representation);

          ClosableIterable<File> allFiles = model.listAllFiles(aip.getId(), representation.getId());

          for (File file : allFiles) {
            logger.debug("Processing file: " + file);

            if (!file.isDirectory()) {

              IndexedFile ifile = index.retrieve(IndexedFile.class,
                SolrUtils.getId(file.getAipId(), file.getRepresentationId(), file.getId()));
              String fileMimetype = ifile.getFileFormat().getMimeType();
              String filePronom = ifile.getFileFormat().getPronom();
              String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());

              if (((!inputFormat.isEmpty() && fileFormat.equalsIgnoreCase(inputFormat)) || inputFormat.isEmpty())
                && ((filePronom != null && pronomToExtension.containsKey(filePronom))
                  || (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype))
                  || applicableTo.contains(fileFormat) || applicableTo.size() == 0)
                && ifile.getSize() < (maxKbytes * 1024)) {

                if (filePronom != null && pronomToExtension.containsKey(filePronom)) {
                  fileFormat = pronomToExtension.get(filePronom).get(0);
                }

                if (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype)
                  && !pronomToExtension.containsKey(filePronom)) {
                  fileFormat = mimetypeToExtension.get(fileMimetype).get(0);
                }

                StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                Binary binary = storage.getBinary(fileStoragePath);

                // FIXME file that doesn't get deleted afterwards
                logger.debug("Running a ConvertPlugin (" + fileFormat + " to " + outputFormat + ") on " + file.getId());
                Path pluginResult = executePlugin(binary, fileFormat);

                if (pluginResult != null) {
                  ContentPayload payload = new FSPathContentPayload(pluginResult);
                  StoragePath storagePath = ModelUtils.getRepresentationPath(aip.getId(), representation.getId());

                  // create a new representation if it does not exist
                  if (!newRepresentations.contains(newRepresentationID)) {
                    logger.debug("Creating a new representation " + newRepresentationID + " on AIP " + aip.getId());
                    boolean original = false;
                    newRepresentations.add(newRepresentationID);
                    model.createRepresentation(aip.getId(), newRepresentationID, original, false);

                    StoragePath storagePreservationPath = ModelUtils.getPreservationPath(aip.getId(),
                      newRepresentationID);
                    model.getStorage().createDirectory(storagePreservationPath);
                  }

                  // update file on new representation
                  String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                  model.createFile(aip.getId(), newRepresentationID, file.getPath(), newFileId, payload, true);
                  alteredFiles.add(file.getId());

                } else {
                  logger.debug("Conversion (" + fileFormat + " to " + outputFormat + ") failed on file " + file.getId()
                    + " of representation " + representation.getId() + " from AIP " + aip.getId());
                  state = 2;
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
              model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, true);
            }

            if (!representation.isOriginal()) {
              model.deleteRepresentation(aip.getId(), representation.getId());
            }
          }

        } catch (Throwable e) {
          logger.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          state = 0;
        }

        logger.debug("Creating convert plugin event for the representation " + representation.getId());
        createEvent(alteredFiles, aip, newRepresentationID, model, state, agent);

      }

      try {

        for (String repId : newRepresentations) {
          AbstractConvertPluginUtils.reIndexingRepresentation(index, model, storage, aip.getId(), repId);
        }

        model.notifyAIPUpdated(aip.getId());
      } catch (Exception e) {
        logger.debug("Error re-indexing new representation " + newRepresentationID);
      }

    }

    return null;
  }

  private Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage,
    List<Representation> list, IndexedPreservationAgent agent) throws PluginException {

    List<String> newRepresentations = new ArrayList<String>();
    boolean notify = true;
    String aipId = null;

    for (Representation representation : list) {
      List<File> unchangedFiles = new ArrayList<File>();
      String newRepresentationID = UUID.randomUUID().toString();
      List<String> alteredFiles = new ArrayList<String>();
      aipId = representation.getAipId();
      int state = 1;

      try {
        logger.debug("Processing representation: " + representation);

        ClosableIterable<File> allFiles = model.listAllFiles(aipId, representation.getId());

        for (File file : allFiles) {
          logger.debug("Processing file: " + file);

          if (!file.isDirectory()) {

            IndexedFile ifile = index.retrieve(IndexedFile.class,
              SolrUtils.getId(file.getAipId(), file.getRepresentationId(), file.getId()));
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String filePronom = ifile.getFileFormat().getPronom();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

            if (((!inputFormat.isEmpty() && fileFormat.equalsIgnoreCase(inputFormat)) || (inputFormat.isEmpty()))
              && ((filePronom != null && pronomToExtension.containsKey(filePronom))
                || (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype))
                || (applicableTo.contains(fileFormat)))
              && ifile.getSize() < (maxKbytes * 1024)) {

              if (filePronom != null && pronomToExtension.containsKey(filePronom)) {
                fileFormat = pronomToExtension.get(filePronom).get(0);
              }

              if (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype)
                && !pronomToExtension.containsKey(filePronom)) {
                fileFormat = mimetypeToExtension.get(fileMimetype).get(0);
              }

              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
              Binary binary = storage.getBinary(fileStoragePath);

              // FIXME file that doesn't get deleted afterwards
              logger.debug("Running a ConvertPlugin (" + fileFormat + " to " + outputFormat + ") on " + file.getId());
              Path pluginResult = executePlugin(binary, fileFormat);

              if (pluginResult != null) {
                ContentPayload payload = new FSPathContentPayload(pluginResult);
                StoragePath storagePath = ModelUtils.getRepresentationPath(aipId, representation.getId());

                if (!newRepresentations.contains(newRepresentationID)) {
                  logger.debug("Creating a new representation " + newRepresentationID + " on AIP " + aipId);
                  boolean original = false;
                  newRepresentations.add(newRepresentationID);
                  model.createRepresentation(aipId, newRepresentationID, original, notify);

                  StoragePath storagePreservationPath = ModelUtils.getPreservationPath(aipId, newRepresentationID);
                  model.getStorage().createDirectory(storagePreservationPath);
                }

                // update file on new representation
                String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload, notify);
                alteredFiles.add(file.getId());

              } else {
                logger.debug("Conversion (" + fileFormat + " to " + outputFormat + ") failed on file " + file.getId()
                  + " of representation " + representation.getId() + " from AIP " + aipId);
                state = 2;
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
        }

        AbstractConvertPluginUtils.reIndexingRepresentation(index, model, storage, aipId, newRepresentationID);

        logger.debug("Creating convert plugin event for the representation " + representation.getId());
        createEvent(alteredFiles, model.retrieveAIP(aipId), newRepresentationID, model, state, agent);

      } catch (Throwable e) {
        logger.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        state = 0;
      }
    }

    try {
      agent = PremisUtils.createPremisAgentBinary(this, RodaConstants.PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN,
        model);
      model.notifyAIPUpdated(aipId);
    } catch (RODAException e) {
      logger.error("Error running adding Siegfried plugin: " + e.getMessage(), e);

    }

    return null;
  }

  private Report executeOnFile(IndexService index, ModelService model, StorageService storage, List<File> list,
    IndexedPreservationAgent agent) throws PluginException {

    int state = 1;
    Set<String> aipSet = new HashSet<String>();

    for (File file : list) {
      try {
        logger.debug("Processing file " + file.getId());

        String newRepresentationID = UUID.randomUUID().toString();

        if (!file.isDirectory()) {

          IndexedFile ifile = index.retrieve(IndexedFile.class,
            SolrUtils.getId(file.getAipId(), file.getRepresentationId(), file.getId()));
          String fileMimetype = ifile.getFileFormat().getMimeType();
          String filePronom = ifile.getFileFormat().getPronom();
          String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

          if (((!inputFormat.isEmpty() && fileFormat.equalsIgnoreCase(inputFormat)) || (inputFormat.isEmpty()))
            && ((filePronom != null && pronomToExtension.containsKey(filePronom))
              || (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype))
              || (applicableTo.contains(fileFormat)))
            && ifile.getSize() < (maxKbytes * 1024)) {

            if (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype)
              && !applicableTo.contains(fileFormat)) {
              fileFormat = mimetypeToExtension.get(fileMimetype).get(0);
            }

            if (filePronom != null && pronomToExtension.containsKey(filePronom)
              && !mimetypeToExtension.containsKey(fileMimetype) && !applicableTo.contains(fileFormat)) {
              fileFormat = pronomToExtension.get(filePronom).get(0);
            }

            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
            Binary binary = storage.getBinary(fileStoragePath);

            // FIXME file that doesn't get deleted afterwards
            logger.debug("Running a ConvertPlugin (" + fileFormat + " to " + outputFormat + ") on " + file.getId());
            Path pluginResult = executePlugin(binary, fileFormat);

            if (pluginResult != null) {
              ContentPayload payload = new FSPathContentPayload(pluginResult);
              StoragePath storagePath = ModelUtils.getRepresentationPath(file.getAipId(), file.getRepresentationId());

              // create a new representation if it does not exist
              logger.debug("Creating a new representation " + newRepresentationID + " on AIP " + file.getAipId());
              boolean original = false;
              model.createRepresentation(file.getAipId(), newRepresentationID, original, model.getStorage(),
                storagePath);

              StoragePath storagePreservationPath = ModelUtils.getPreservationPath(file.getAipId(),
                newRepresentationID);
              model.getStorage().createDirectory(storagePreservationPath);

              // update file on new representation
              String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
              model.deleteFile(file.getAipId(), newRepresentationID, file.getPath(), file.getId(), false);
              model.createFile(file.getAipId(), newRepresentationID, file.getPath(), newFileId, payload, false);

              aipSet.add(file.getAipId());

            } else {
              logger.debug("Conversion (" + fileFormat + " to " + outputFormat + ") failed on file " + file.getId()
                + " of representation " + file.getRepresentationId() + " from AIP " + file.getAipId());
              state = 2;
            }
          }
        }

      } catch (Throwable e) {
        logger.error("Error processing file " + file.getId() + ": " + e.getMessage(), e);
        state = 0;
      }
    }

    try {
      Plugin<AIP> psp = new PremisSkeletonPlugin();
      Plugin<AIP> sfp = new SiegfriedPlugin();
      Plugin<AIP> ttp = new TikaFullTextPlugin();

      Map<String, String> params = new HashMap<String, String>();
      params.put("createsPluginEvent", "false");
      psp.setParameterValues(params);
      sfp.setParameterValues(params);
      ttp.setParameterValues(params);

      PluginOrchestrator pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
      pluginOrchestrator.runPluginOnAIPs(psp, new ArrayList<String>(aipSet));
      pluginOrchestrator.runPluginOnAIPs(sfp, new ArrayList<String>(aipSet));
      pluginOrchestrator.runPluginOnAIPs(ttp, new ArrayList<String>(aipSet));

      for (String aipId : aipSet) {
        model.notifyAIPUpdated(aipId);
      }
    } catch (Throwable e) {
      logger.debug("Error re-indexing AIPs after conversion.");
    }

    return null;
  }

  public abstract Path executePlugin(Binary binary, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException;

  public void createEvent(List<String> alteredFiles, AIP aip, String newRepresentationID, ModelService model, int state,
    IndexedPreservationAgent agent) throws PluginException {

    // building the detail extension for the plugin event
    String outcome = "success";
    StringBuilder stringBuilder = new StringBuilder();
    if (alteredFiles.size() == 0) {
      stringBuilder.append("No file was converted on this representation.");
    } else {
      stringBuilder.append("The following files were converted to a new format: ");
      for (String fileID : alteredFiles) {
        stringBuilder.append(fileID + ", ");
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
        Arrays.asList(newRepresentationID), null, outcome, stringBuilder.toString(), null, agent);
    } catch (PremisMetadataException | IOException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException | ValidationException | AlreadyExistsException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  public abstract void fillFileFormatStructures();

}
