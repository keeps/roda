/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.AgentPreservationObject;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.FileFormat;
import org.roda.core.data.v2.JobReport.PluginState;
import org.roda.core.data.v2.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisAgentHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiegfriedPlugin implements Plugin<AIP> {
  private AgentPreservationObject agent;
  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPlugin.class);

  @Override
  public void init() throws PluginException {
    agent = new AgentPreservationObject();
    agent.setAgentName(getName() + "/" + getVersion()); //$NON-NLS-1$
    agent.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN);
    agent.setId("characterization-siegfried");
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Siegfried characterization action";
  }

  @Override
  public String getDescription() {
    return "Update the premis files with the object characterization";
  }

  @Override
  public String getVersion() {
    return SiegfriedPluginUtils.getVersion();
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // no params
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    boolean created = false;
    try {
      AgentPreservationObject apo = model.getAgentPreservationObject(agent.getId());
      created = true;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {

    }
    if (!created) {
      try {
        byte[] serializedPremisAgent = new PremisAgentHelper(agent).saveToByteArray();
        Path agentFile = Files.createTempFile("agent_preservation", ".xml");
        Files.copy(new ByteArrayInputStream(serializedPremisAgent), agentFile, StandardCopyOption.REPLACE_EXISTING);
        Binary agentResource = (Binary) FSUtils.convertPathToResource(agentFile.getParent(), agentFile);
        model.createAgentMetadata(agent.getId(), agentResource);
      } catch (RequestNotValidException | PremisMetadataException | IOException | NotFoundException | GenericException
        | AlreadyExistsException | AuthorizationDeniedException e) {

      }
    }

    for (AIP aip : list) {
      LOGGER.debug("Processing AIP " + aip.getId());
      for (String representationID : aip.getRepresentationIds()) {
        LOGGER.debug("Processing representation " + representationID + " of AIP " + aip.getId());
        try {
          Path data = Files.createTempDirectory("data");
          StorageService tempStorage = new FileStorageService(data);
          StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representationID);
          tempStorage.copy(storage, representationPath, representationPath);
          String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(data.resolve(representationPath.asString()));

          final JSONObject obj = new JSONObject(siegfriedOutput);
          JSONArray files = (JSONArray) obj.get("files");
          List<org.roda.core.model.File> updatedFiles = new ArrayList<org.roda.core.model.File>();
          for (int i = 0; i < files.length(); i++) {
            JSONObject fileObject = files.getJSONObject(i);

            String fileName = fileObject.getString("filename");
            fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1);
            long fileSize = fileObject.getLong("filesize");

            Path p = Files.createTempFile("temp", ".temp");
            Files.write(p, fileObject.toString().getBytes());
            Binary resource = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
            LOGGER.debug("Creating other metadata (AIP: " + aip.getId() + ", REPRESENTATION: " + representationID
              + ", FILE: " + fileName + ")");

            model.createOtherMetadata(aip.getId(), representationID, fileName + ".xml", "Siegfried", resource);

            p.toFile().delete();

            JSONArray matches = (JSONArray) fileObject.get("matches");
            if (matches.length() > 0) {
              for (int j = 0; j < matches.length(); j++) {
                JSONObject match = (JSONObject) matches.get(j);
                if (match.getString("id").equalsIgnoreCase("pronom")) {
                  String pronom = match.getString("puid");
                  String mime = match.getString("mime");
                  String version = match.getString("version");
                  String extension = "";
                  if (fileName.contains(".")) {
                    extension = fileName.substring(fileName.lastIndexOf('.'));
                  }
                  try {
                    org.roda.core.model.File f = model.retrieveFile(aip.getId(), representationID, fileName);
                    FileFormat ff = new org.roda.core.data.v2.FileFormat();
                    ff.setPronom(pronom);
                    ff.setMimeType(mime);
                    ff.setVersion(version);
                    ff.setExtension(extension);
                    f.setFileFormat(ff);
                    f.setSize(fileSize);
                    f.setOriginalName(fileName);
                    updatedFiles.add(f);
                  } catch (RequestNotValidException | AuthorizationDeniedException e) {
                    LOGGER.error("Error updating file: " + e.getMessage(), e);
                  }
                }
              }
            }
          }
          model.updateFileFormats(updatedFiles);
          createEvent(siegfriedOutput, PluginState.OK, aip, model);
          FSUtils.deletePath(data);
        } catch (PluginException | IOException | ModelServiceException | NotFoundException | GenericException
          | RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException e) {
          e.printStackTrace();
          LOGGER.error("Error running SIEGFRIED " + aip.getId() + ": " + e.getMessage());
        }
      }

    }
    return null;
  }

  private void createEvent(String outcomeDetail, PluginState state, AIP aip, ModelService model)
    throws PluginException {

    try {
      boolean success = (state == PluginState.OK);

      for (String representationID : aip.getRepresentationIds()) {
        PluginUtils.createPluginEvent(aip.getId(), representationID, model,
          EventPreservationObject.PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION,
          "The files of the representation were successfully identified.",
          EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK, agent.getId(),
          Arrays.asList(representationID), success ? "success" : "error", success ? "" : "Error", outcomeDetail);
      }
    } catch (PremisMetadataException | IOException | RODAException e) {
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
  public Plugin<AIP> cloneMe() {
    return new DroidPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
