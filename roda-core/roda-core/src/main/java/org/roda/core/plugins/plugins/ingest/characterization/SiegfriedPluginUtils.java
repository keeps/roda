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

import org.apache.commons.io.IOUtils;
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
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StringContentPayload;
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

  public static <T extends IsRODAObject> List<LinkingIdentifier> runSiegfriedOnRepresentation(Plugin<T> plugin,
    IndexService index, ModelService model, Representation representation) throws GenericException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException, PluginException {

    StoragePath representationDataPath = ModelUtils.getRepresentationDataStoragePath(representation.getAipId(),
      representation.getId());
    DirectResourceAccess directAccess = model.getStorage().getDirectAccess(representationDataPath);
    List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

    Path representationFsPath = directAccess.getPath();
    if (Files.exists(representationFsPath)) {

      String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(representationFsPath);
      IOUtils.closeQuietly(directAccess);

      boolean notify = false;
      final JsonNode jsonObject = JsonUtils.parseJson(siegfriedOutput);
      final JsonNode files = jsonObject.get("files");

      for (JsonNode file : files) {
        Path fullFsPath = Paths.get(file.get("filename").asText());
        Path relativeFsPath = representationFsPath.relativize(fullFsPath);

        String fileId = relativeFsPath.getFileName().toString();
        List<String> fileDirectoryPath = new ArrayList<>();
        for (int j = 0; j < relativeFsPath.getNameCount() - 1; j++) {
          fileDirectoryPath.add(relativeFsPath.getName(j).toString());
        }

        ContentPayload payload = new StringContentPayload(file.toString());

        model.createOtherMetadata(representation.getAipId(), representation.getId(), fileDirectoryPath, fileId,
          SiegfriedPlugin.FILE_SUFFIX, RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED, payload, notify);

        sources.add(PluginHelper.getLinkingIdentifier(representation.getAipId(), representation.getId(),
          fileDirectoryPath, fileId, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

        // Update PREMIS files
        final JsonNode matches = file.get("matches");
        for (JsonNode match : matches) {
          String format = null;
          String version = null;
          String pronom = null;
          String mime = null;

          if (plugin.getVersion().startsWith("1.5") || plugin.getVersion().startsWith("1.6")) {
            if (match.get("ns").textValue().equalsIgnoreCase("pronom")) {
              format = match.get("format").textValue();
              version = match.get("version").textValue();
              pronom = match.get("id").textValue();
              mime = match.get("mime").textValue();
            }
          } else {
            if (match.get("id").textValue().equalsIgnoreCase("pronom")) {
              format = match.get("format").textValue();
              version = match.get("version").textValue();
              pronom = match.get("puid").textValue();
              mime = match.get("mime").textValue();
            }
          }

          PremisV3Utils.updateFormatPreservationMetadata(model, representation.getAipId(), representation.getId(),
            fileDirectoryPath, fileId, format, version, pronom, mime, notify);
        }
      }
    }

    return sources;
  }

}
