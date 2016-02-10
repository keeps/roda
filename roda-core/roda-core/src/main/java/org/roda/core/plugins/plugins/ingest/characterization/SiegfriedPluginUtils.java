/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiegfriedPluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPluginUtils.class);

  private static List<String> getBatchCommand(Path sourceDirectory) {
    List<String> command;
    String siegfriedPath = RodaCoreFactory.getRodaConfigurationAsString("tools", "siegfried", "binary");
    command = new ArrayList<String>(
      Arrays.asList(siegfriedPath.toString(), "-json=true", "-z=false", sourceDirectory.toFile().getAbsolutePath()));
    return command;
  }

  public static String runSiegfriedOnPath(Path sourceDirectory) throws PluginException {
    try {
      List<String> command = getBatchCommand(sourceDirectory);
      String siegfriedOutput = CommandUtility.execute(command, false);
      return siegfriedOutput;
    } catch (CommandException e) {
      throw new PluginException("Error while executing Siegfried command");
    }
  }

  public static String getVersion() {
    String version = null;
    try {
      List<String> command;
      String siegfriedPath = RodaCoreFactory.getRodaConfigurationAsString("tools", "siegfried", "binary");
      command = new ArrayList<String>(Arrays.asList(siegfriedPath.toString(), "--version"));
      String siegfriedOutput = CommandUtility.execute(command);
      if (siegfriedOutput.contains("\n")) {
        return siegfriedOutput.split("\\n")[0].split(" ")[1];
      }
    } catch (CommandException ce) {
      LOGGER.error("Error getting siegfried version: " + ce.getMessage(), ce);
    }
    return version;
  }

  public static void runSiegfriedOnRepresentation(IndexService index, ModelService model, StorageService storage,
    AIP aip, Representation representation, IndexedPreservationAgent agent, boolean createsPluginEvent)
      throws IOException, GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException,
      AuthorizationDeniedException, PluginException {

    // TODO run directly in storage
    Path data = Files.createTempDirectory("data");
    StorageService tempStorage = new FileStorageService(data);
    StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representation.getId());
    tempStorage.copy(storage, representationPath, representationPath);
    Path representationFsPath = data.resolve(representationPath.asString());
    String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(representationFsPath);

    final JSONObject obj = new JSONObject(siegfriedOutput);
    JSONArray files = (JSONArray) obj.get("files");

    for (int i = 0; i < files.length(); i++) {
      JSONObject fileObject = files.getJSONObject(i);

      Path fullFsPath = Paths.get(fileObject.getString("filename"));
      Path relativeFsPath = representationFsPath.relativize(fullFsPath);

      String fileId = relativeFsPath.getFileName().toString();
      List<String> fileDirectoryPath = new ArrayList<>();
      for (int j = 0; j < relativeFsPath.getNameCount() - 1; j++) {
        fileDirectoryPath.add(relativeFsPath.getName(j).toString());
      }

      ContentPayload payload = new StringContentPayload(fileObject.toString());

      model.createOtherMetadata(aip.getId(), representation.getId(), fileDirectoryPath, fileId,
        SiegfriedPlugin.FILE_SUFFIX, SiegfriedPlugin.OTHER_METADATA_TYPE, payload);

      // Update PREMIS files
      JSONArray matches = (JSONArray) fileObject.get("matches");
      if (matches.length() > 0) {
        for (int j = 0; j < matches.length(); j++) {
          JSONObject match = (JSONObject) matches.get(j);
          if (match.getString("id").equalsIgnoreCase("pronom")) {
            String format = match.getString("format");
            String version = match.getString("version");
            String pronom = match.getString("puid");
            String mime = match.getString("mime");

            try {
              Binary premis_bin = model.retrievePreservationFile(aip.getId(), representation.getId(), fileDirectoryPath,
                fileId);

              lc.xmlns.premisV2.File premis_file = PremisUtils.binaryToFile(premis_bin.getContent(), false);
              PremisUtils.updateFileFormat(premis_file, format, version, pronom, mime);

              PreservationMetadataType type = PreservationMetadataType.OBJECT_FILE;
              String id = ModelUtils.generatePreservationMetadataId(type, aip.getId(), representation.getId(),
                fileDirectoryPath, fileId);

              ContentPayload premis_file_payload = PremisUtils.fileToBinary(premis_file);
              model.updatePreservationMetadata(id, type, aip.getId(), representation.getId(), fileDirectoryPath, fileId,
                premis_file_payload);

            } catch (NotFoundException e) {
              LOGGER.debug("Siegfried will not update PREMIS because it doesn't exist");
            } catch (RODAException e) {
              LOGGER.error("Siegfried will not update PREMIS due to an error", e);
            }
          }
        }
      }
    }

    if (createsPluginEvent) {
      PluginHelper.createPremisEventPerRepresentation(model, aip, PluginState.SUCCESS,
        RodaConstants.PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION,
        "The files of the representation were successfully identified.", siegfriedOutput, agent);
    }

    FSUtils.deletePath(data);
  }

}
