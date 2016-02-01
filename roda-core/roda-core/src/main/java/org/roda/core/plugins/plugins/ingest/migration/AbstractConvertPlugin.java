package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConvertPlugin implements Plugin<Serializable> {

  public final Logger logger = LoggerFactory.getLogger(getClass());
  public String inputFormat;
  public String outputFormat;
  public long maxKbytes = 20000; // default value: 20000 kb

  public List<String> applicableTo = new ArrayList<>();
  public List<String> convertableTo = new ArrayList<>();

  public boolean hasPartialSuccessOnOutcome = Boolean
    .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("tools", "allplugins", "hasPartialSuccessOnOutcome"));

  public abstract void init() throws PluginException;

  public abstract void shutdown();

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

    if (list.size() > 0) {
      if (list.get(0) instanceof AIP)
        return executeOnAIP(index, model, storage, (List<AIP>) (List<?>) list);
      else
        return executeOnFile(index, model, storage, (List<File>) (List<?>) list);
    }

    return null;
  }

  private Report executeOnAIP(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    for (AIP aip : list) {
      logger.debug("Processing AIP " + aip.getId());
      List<String> newRepresentations = new ArrayList<String>();
      String newRepresentationID = UUID.randomUUID().toString();

      for (Representation representation : aip.getRepresentations()) {
        List<String> alteredFiles = new ArrayList<String>();
        int state = 1;

        try {
          logger.debug("Processing representation: " + representation);

          Iterable<File> allFiles = model.listAllFiles(aip.getId(), representation.getId());

          for (File file : allFiles) {
            logger.debug("Processing file: " + file);

            if (!file.isDirectory()) {
              // TODO filter by file type and size
              // && file.getId().endsWith("." + inputFormat)
              // && (file.getSize() <= maxKbytes * 1024)
              StoragePath fileStoragePath = ModelUtils.getRepresentationFileStoragePath(file);
              Binary binary = storage.getBinary(fileStoragePath);

              // FIXME file that doesn't get deleted afterwards
              logger.debug("Running a ConvertPlugin (" + inputFormat + " to " + outputFormat + ") on " + file.getId());
              Path pluginResult = executePlugin(binary);

              if (pluginResult != null) {
                ContentPayload payload = new FSPathContentPayload(pluginResult);
                StoragePath storagePath = ModelUtils.getRepresentationPath(aip.getId(), representation.getId());

                // create a new representation if it does not exist
                if (!newRepresentations.contains(newRepresentationID)) {
                  logger.debug("Creating a new representation " + newRepresentationID + " on AIP " + aip.getId());
                  boolean original = false;
                  model.createRepresentation(aip.getId(), newRepresentationID, original, model.getStorage(),
                    storagePath);

                  StoragePath storagePreservationPath = ModelUtils.getPreservationPath(aip.getId(),
                    newRepresentationID);
                  model.getStorage().createDirectory(storagePreservationPath);
                }

                // update file on new representation
                String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                model.deleteFile(aip.getId(), newRepresentationID, file.getPath(), file.getId());
                model.createFile(aip.getId(), newRepresentationID, file.getPath(), newFileId, payload);
                newRepresentations.add(newRepresentationID);
                alteredFiles.add(file.getId());

              } else {
                logger.debug("Conversion (" + inputFormat + " to " + outputFormat + ") failed on file " + file.getId()
                  + " of representation " + representation.getId() + " from AIP " + aip.getId());
                state = 2;
              }
            }
          }

        } catch (Throwable e) {
          logger.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          state = 0;
        }

        logger.debug("Creating convert plugin event for the representation " + representation.getId());
        createEvent(alteredFiles, aip, representation.getId(), newRepresentationID, model, state);
      }
    }

    return null;
  }

  private Report executeOnFile(IndexService index, ModelService model, StorageService storage, List<File> list)
    throws PluginException {

    int state = 1;

    for (File file : list) {
      try {
        logger.debug("Processing file " + file.getId());

        String newRepresentationID = UUID.randomUUID().toString();

        if (!file.isDirectory()) {
          // TODO filter by file type and size
          // && file.getId().endsWith("." + inputFormat)
          // && (file.getSize() <= maxKbytes * 1024)
          StoragePath fileStoragePath = ModelUtils.getRepresentationFileStoragePath(file);
          Binary binary = storage.getBinary(fileStoragePath);

          // FIXME file that doesn't get deleted afterwards
          logger.debug("Running a ConvertPlugin (" + inputFormat + " to " + outputFormat + ") on " + file.getId());
          Path pluginResult = executePlugin(binary);

          if (pluginResult != null) {
            ContentPayload payload = new FSPathContentPayload(pluginResult);
            StoragePath storagePath = ModelUtils.getRepresentationPath(file.getAipId(), file.getRepresentationId());

            // create a new representation if it does not exist
            logger.debug("Creating a new representation " + newRepresentationID + " on AIP " + file.getAipId());
            boolean original = false;
            model.createRepresentation(file.getAipId(), newRepresentationID, original, model.getStorage(), storagePath);

            StoragePath storagePreservationPath = ModelUtils.getPreservationPath(file.getAipId(), newRepresentationID);
            model.getStorage().createDirectory(storagePreservationPath);

            // update file on new representation
            String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
            model.deleteFile(file.getAipId(), newRepresentationID, file.getPath(), file.getId());
            model.createFile(file.getAipId(), newRepresentationID, file.getPath(), newFileId, payload);

          } else {
            logger.debug("Conversion (" + inputFormat + " to " + outputFormat + ") failed on file " + file.getId()
              + " of representation " + file.getRepresentationId() + " from AIP " + file.getAipId());
            state = 2;
          }
        }
      } catch (Throwable e) {
        logger.error("Error processing file " + file.getId() + ": " + e.getMessage(), e);
        state = 0;
      }
    }

    return null;
  }

  public abstract Path executePlugin(Binary binary) throws UnsupportedOperationException, IOException, CommandException;

  public void createEvent(List<String> alteredFiles, AIP aip, String representationID, String newRepresentionID,
    ModelService model, int state) throws PluginException {

    // building the detail extension for the plugin event
    String outcome = "success";
    StringBuilder stringBuilder = new StringBuilder();
    if (alteredFiles.size() == 0) {
      stringBuilder.append("No file was converted on this representation.");
    } else {
      stringBuilder
        .append("The following files were converted on a new representation (ID: " + newRepresentionID + "): ");
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
      PluginHelper.createPluginEventAndAgent(aip.getId(), representationID, model,
        EventPreservationObject.PRESERVATION_EVENT_TYPE_MIGRATION, "Some files were converted on a new representation",
        EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_EXECUTING_PROGRAM_TASK, getClass().getName(),
        Arrays.asList(representationID), outcome, stringBuilder.toString(), null,
        getClass().getName() + "@" + getVersion(), AgentPreservationObject.PRESERVATION_AGENT_TYPE_CONVERSION_PLUGIN);

    } catch (PremisMetadataException | IOException | RequestNotValidException | NotFoundException | GenericException
      | AlreadyExistsException | AuthorizationDeniedException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  public abstract Report beforeExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException;

  public abstract Report afterExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException;

}
