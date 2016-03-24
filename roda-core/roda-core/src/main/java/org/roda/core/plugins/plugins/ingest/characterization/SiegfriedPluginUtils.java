/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.util.Base64;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.roda.core.util.HTTPUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.premis.v3.File;

public class SiegfriedPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPluginUtils.class);

  private static List<String> getBatchCommand(Path sourceDirectory) {
    List<String> command;
    String siegfriedPath = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "siegfried", "binary");
    command = new ArrayList<String>(
      Arrays.asList(siegfriedPath.toString(), "-json=true", "-z=false", sourceDirectory.toFile().getAbsolutePath()));
    return command;
  }

  private static String getSiegfriedServerEndpoint(Path sourceDirectory) {
    String siegfriedServer = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "siegfried", "server");
    String endpoint = siegfriedServer + "/identify/" + new String(Base64.encode(sourceDirectory.toString().getBytes()));
    return endpoint;
  }

  public static String runSiegfriedOnPath(Path sourceDirectory) throws PluginException {
    try {
      String siegfriedMode = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "siegfried", "mode");
      if (siegfriedMode != null && siegfriedMode.equalsIgnoreCase("server")) {
        LOGGER.debug("Running Siegfried on server mode");
        String endpoint = getSiegfriedServerEndpoint(sourceDirectory);
        return HTTPUtility.doGet(endpoint);
      } else {
        LOGGER.debug("Running Siegfried on standalone mode");
        List<String> command = getBatchCommand(sourceDirectory);
        return CommandUtility.execute(command, false);
      }
    } catch (CommandException | IOException | GenericException e) {
      throw new PluginException("Error while executing Siegfried: " + e.getMessage());
    }
  }

  public static String getVersion() {
    String version = null;
    try {
      String siegfriedPath = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "siegfried", "binary");
      List<String> command = new ArrayList<String>(Arrays.asList(siegfriedPath.toString(), "--version"));
      String siegfriedOutput = CommandUtility.execute(command);
      if (siegfriedOutput.contains("\n")) {
        return siegfriedOutput.split("\\n")[0].split(" ")[1];
      }
    } catch (CommandException ce) {
      LOGGER.error("Error getting Siegfried version: " + ce.getMessage(), ce);
    }
    return version;
  }

  public static <T extends Serializable> void runSiegfriedOnRepresentation(Plugin<T> plugin, IndexService index,
    ModelService model, StorageService storage, AIP aip, Representation representation)
      throws GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException,
      AuthorizationDeniedException, PluginException {

    StoragePath representationDataPath = ModelUtils.getRepresentationDataStoragePath(aip.getId(),
      representation.getId());
    DirectResourceAccess directAccess = storage.getDirectAccess(representationDataPath);

    Path representationFsPath = directAccess.getPath();
    String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(representationFsPath);
    IOUtils.closeQuietly(directAccess);

    boolean notify = false;
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
        SiegfriedPlugin.FILE_SUFFIX, SiegfriedPlugin.OTHER_METADATA_TYPE, payload, notify);

      // Update PREMIS files
      JSONArray matches = (JSONArray) fileObject.get("matches");
      if (matches.length() > 0) {
        for (int j = 0; j < matches.length(); j++) {
          JSONObject match = (JSONObject) matches.get(j);
          String format = null;
          String version = null;
          String pronom = null;
          String mime = null;

          if (plugin.getVersion().startsWith("1.5")) {
            if (match.getString("ns").equalsIgnoreCase("pronom")) {
              format = match.getString("format");
              version = match.getString("version");
              pronom = match.getString("id");
              mime = match.getString("mime");
            }
          } else {
            if (match.getString("id").equalsIgnoreCase("pronom")) {
              format = match.getString("format");
              version = match.getString("version");
              pronom = match.getString("puid");
              mime = match.getString("mime");
            }
          }
          try {
            Binary premisBin = model.retrievePreservationFile(aip.getId(), representation.getId(), fileDirectoryPath,
              fileId);

            File premisFile = PremisV3Utils.binaryToFile(premisBin.getContent(), false);
            PremisV3Utils.updateFileFormat(premisFile, format, version, pronom, mime);

            PreservationMetadataType type = PreservationMetadataType.OBJECT_FILE;
            String id = IdUtils.getPreservationMetadataId(type, aip.getId(), representation.getId(), fileDirectoryPath,
              fileId);

            ContentPayload premisFilePayload = PremisV3Utils.fileToBinary(premisFile);
            model.updatePreservationMetadata(id, type, aip.getId(), representation.getId(), fileDirectoryPath, fileId,
              premisFilePayload, notify);
          } catch (NotFoundException e) {
            LOGGER.debug("Siegfried will not update PREMIS because it doesn't exist");
          } catch (RODAException e) {
            LOGGER.error("Siegfried will not update PREMIS due to an error", e);
          }
        }
      }
    }
  }

}
