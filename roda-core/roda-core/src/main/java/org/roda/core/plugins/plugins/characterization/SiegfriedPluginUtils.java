/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.Base64;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.roda.core.util.HTTPUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class SiegfriedPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPluginUtils.class);

  private static List<String> getBatchCommand(Path sourceDirectory) {
    List<String> command;
    String siegfriedPath = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "siegfried", "binary");
    command = new ArrayList<>(
      Arrays.asList(siegfriedPath, "-json=true", "-z=false", sourceDirectory.toFile().getAbsolutePath()));
    return command;
  }

  private static String getSiegfriedServerEndpoint(Path sourceDirectory) {
    String siegfriedServer = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "siegfried", "server");
    return String.format("%s/identify/%s?base64=true&format=json", siegfriedServer,
      new String(Base64.encode(sourceDirectory.toString().getBytes())));
  }

  public static String runSiegfriedOnPath(Path sourceDirectory) throws PluginException {
    try {
      String siegfriedMode = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "siegfried", "mode");
      if (siegfriedMode != null && "server".equalsIgnoreCase(siegfriedMode)) {
        LOGGER.debug("Running Siegfried on server mode");
        String endpoint = getSiegfriedServerEndpoint(sourceDirectory);
        return HTTPUtility.doGet(endpoint);
      } else {
        LOGGER.debug("Running Siegfried on standalone mode");
        List<String> command = getBatchCommand(sourceDirectory);
        return CommandUtility.execute(command, false);
      }
    } catch (CommandException | GenericException e) {
      throw new PluginException("Error while executing Siegfried: " + e.getMessage());
    }
  }

  public static String getVersion() {
    String version = null;
    try {
      String siegfriedPath = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "siegfried", "binary");
      List<String> command = new ArrayList<>(Arrays.asList(siegfriedPath, "--version"));
      String siegfriedOutput = CommandUtility.execute(command);
      if (siegfriedOutput.contains("\n")) {
        return siegfriedOutput.split("\\n")[0].split(" ")[1];
      }
    } catch (CommandException ce) {
      LOGGER.error("Error getting Siegfried version: " + ce.getMessage(), ce);
    }
    return version;
  }

  public static <T extends IsRODAObject> List<LinkingIdentifier> runSiegfriedOnRepresentation(Plugin<T> plugin,
    ModelService model, Representation representation) throws GenericException, RequestNotValidException,
    AlreadyExistsException, NotFoundException, AuthorizationDeniedException, PluginException {

    StoragePath representationDataPath = ModelUtils.getRepresentationDataStoragePath(representation.getAipId(),
      representation.getId());
    DirectResourceAccess directAccess = model.getStorage().getDirectAccess(representationDataPath);

    Path representationFsPath = directAccess.getPath();
    List<LinkingIdentifier> sources = runSiegfriedOnRepresentationOrFile(plugin, model, representation.getAipId(),
      representation.getId(), new ArrayList<>(), null, representationFsPath);

    IOUtils.closeQuietly(directAccess);
    return sources;
  }

  public static <T extends IsRODAObject> List<LinkingIdentifier> runSiegfriedOnFile(Plugin<T> plugin,
    ModelService model, File file) throws GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, AuthorizationDeniedException, PluginException {

    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
    DirectResourceAccess directAccess = model.getStorage().getDirectAccess(fileStoragePath);

    Path filePath = directAccess.getPath();
    List<LinkingIdentifier> sources = runSiegfriedOnRepresentationOrFile(plugin, model, file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), filePath);
    IOUtils.closeQuietly(directAccess);

    model.notifyFileUpdated(file);
    return sources;
  }

  private static <T extends IsRODAObject> List<LinkingIdentifier> runSiegfriedOnRepresentationOrFile(Plugin<T> plugin,
    ModelService model, String aipId, String representationId, List<String> fileDirectoryPath, String fileId, Path path)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    PluginException {
    List<LinkingIdentifier> sources = new ArrayList<>();

    if (FSUtils.exists(path)) {
      String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(path);

      final JsonNode jsonObject = JsonUtils.parseJson(siegfriedOutput);
      final JsonNode files = jsonObject.get("files");

      for (JsonNode file : files) {
        Path fullFsPath = Paths.get(file.get("filename").asText());
        Path relativeFsPath = path.relativize(fullFsPath);

        String jsonFileId = fullFsPath.getFileName().toString();

        List<String> jsonFilePath = new ArrayList<>(fileDirectoryPath);
        if (fileId != null) {
          jsonFilePath.add(fileId);
        }
        for (int j = 0; j < relativeFsPath.getNameCount()
          && StringUtils.isNotBlank(relativeFsPath.getName(j).toString()); j++) {
          jsonFilePath.add(relativeFsPath.getName(j).toString());
        }
        jsonFilePath.remove(jsonFilePath.size() - 1);

        ContentPayload payload = new StringContentPayload(file.toString());
        model.createOrUpdateOtherMetadata(aipId, representationId, jsonFilePath, jsonFileId,
          SiegfriedPlugin.FILE_SUFFIX, RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED, payload, false);

        sources.add(PluginHelper.getLinkingIdentifier(aipId, representationId, jsonFilePath, jsonFileId,
          RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

        // Update PREMIS files
        final JsonNode matches = file.get("matches");
        for (JsonNode match : matches) {
          String format = null;
          String version = null;
          String pronom = null;
          String mime = null;
          String[] pluginVersion = plugin.getVersion().split("\\.");

          if ("1".equals(pluginVersion[0])) {
            if (Integer.parseInt(pluginVersion[1]) > 4) {
              if ("pronom".equalsIgnoreCase(match.get("ns").textValue())) {
                format = match.get("format").textValue();
                version = match.get("version").textValue();
                pronom = match.get("id").textValue();
                mime = match.get("mime").textValue();
              }
            } else {
              if ("pronom".equalsIgnoreCase(match.get("id").textValue())) {
                format = match.get("format").textValue();
                version = match.get("version").textValue();
                pronom = match.get("puid").textValue();
                mime = match.get("mime").textValue();
              }
            }
          }

          PremisV3Utils.updateFormatPreservationMetadata(model, aipId, representationId, jsonFilePath, jsonFileId,
            format, version, pronom, mime, true);
        }
      }
    }

    return sources;
  }

}
