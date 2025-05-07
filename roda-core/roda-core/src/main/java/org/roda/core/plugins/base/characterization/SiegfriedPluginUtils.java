/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.characterization;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
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
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
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

  private SiegfriedPluginUtils() {
    // do nothing
  }

  private static List<String> getBatchCommand(Path sourceDirectory) {
    List<String> command;
    String siegfriedPath = RodaCoreFactory.getRodaConfiguration().getString("core.tools.siegfried.binary", "sf");
    command = new ArrayList<>(
      Arrays.asList(siegfriedPath, "-json=true", "-z=false", sourceDirectory.toFile().getAbsolutePath()));
    return command;
  }

  private static String getSiegfriedServerEndpoint(Path sourceDirectory) {
    String siegfriedServer = RodaCoreFactory.getRodaConfiguration().getString("core.tools.siegfried.server",
      "http://localhost:5138");

    return String.format("%s/identify/%s?base64=true&format=json", siegfriedServer,
      new String(Base64.encode(sourceDirectory.toString().getBytes())));
  }

  public static String runSiegfriedOnPath(Path sourceDirectory) throws PluginException {
    try {
      String siegfriedMode = RodaCoreFactory.getRodaConfiguration().getString("core.tools.siegfried.mode", "server");
      if ("server".equalsIgnoreCase(siegfriedMode)) {
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

    String siegfriedMode = RodaCoreFactory.getRodaConfiguration().getString("core.tools.siegfried.mode", "server");
    if ("server".equalsIgnoreCase(siegfriedMode)) {
      LOGGER.debug("Running Siegfried on server mode");
      String endpoint = getSiegfriedServerEndpoint(Paths.get("/dev/null"));
      try {
        String json = HTTPUtility.doGet(endpoint);
        JsonNode jn = JsonUtils.parseJson(json);
        StringBuilder result = new StringBuilder();
        result.append(jn.get("siegfried").asText());

        version = result.toString();
      } catch (GenericException ce) {
        LOGGER.error("Error getting Siegfried version: " + ce.getMessage(), ce);
      }

    } else {
      LOGGER.debug("Running Siegfried on standalone mode");
      try {
        String siegfriedPath = RodaCoreFactory.getRodaConfiguration().getString("core.tools.siegfried.binary", "sf");
        List<String> command = new ArrayList<>(Arrays.asList(siegfriedPath, "--version"));

        String siegfriedOutput = CommandUtility.execute(command);
        StringBuilder result = new StringBuilder();

        if (siegfriedOutput.contains("\n")) {
          result.append(siegfriedOutput.split("\\n")[0].split(" ")[1]);
        }

        if (siegfriedOutput.contains("DROID_SignatureFile_")) {
          result.append(" w/ ");

          Pattern pattern = Pattern.compile("DROID_SignatureFile_V[0-9]+");
          Matcher matcher = pattern.matcher(siegfriedOutput);
          if (matcher.find()) {
            result.append(matcher.group(0));
          }
        }

        version = result.toString();
      } catch (CommandException ce) {
        LOGGER.error("Error getting Siegfried version: " + ce.getMessage(), ce);
      }
    }

    return version;
  }

  public static <T extends IsRODAObject> List<LinkingIdentifier> runSiegfriedOnRepresentation(ModelService model,
    IndexService index, Representation representation, String jobId, String username, boolean overwriteManual)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException, PluginException,
    AlreadyExistsException {
    if (representation.getHasShallowFiles()) {
      StorageService tmpStorageService = model.resolveTemporaryResourceShallow(jobId,
        ModelUtils.getAIPStoragePath(representation.getAipId()));
      try (DirectResourceAccess directAccess = model.getRepresentationDataDirectAccess(representation,
        tmpStorageService)) {
        Path representationFsPath = directAccess.getPath();
        return runSiegfriedOnRepresentationOrFile(model, index, representation.getAipId(), representation.getId(),
          new ArrayList<>(), null, representationFsPath, username, overwriteManual);
      } catch (IOException e) {
        throw new GenericException(e);
      } finally {
        try {
          Job job = PluginHelper.getJob(jobId, model);
          if (!job.getPluginType().equals(PluginType.INGEST)) {
            ModelUtils.removeTemporaryRepresentationDataShallow(job.getId(), representation.getAipId(),
              representation.getId());
          }
        } catch (IOException e) {
          LOGGER.error("Error on removing temporary Representation " + representation.getId(), e);
        }
      }
    } else {
      try (DirectResourceAccess directAccess = model.getRepresentationDataDirectAccess(representation)) {
        Path representationFsPath = directAccess.getPath();
        return runSiegfriedOnRepresentationOrFile(model, index, representation.getAipId(), representation.getId(),
          new ArrayList<>(), null, representationFsPath, username, overwriteManual);
      } catch (IOException e) {
        throw new GenericException(e);
      }
    }
  }

  public static <T extends IsRODAObject> List<LinkingIdentifier> runSiegfriedOnFile(ModelService model,
    IndexService index, File file, String username, boolean overwriteManual) throws GenericException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException {
    try (DirectResourceAccess directAccess = model.getDirectAccess(file)) {
      Path filePath = directAccess.getPath();
      List<LinkingIdentifier> sources = runSiegfriedOnRepresentationOrFile(model, index, file.getAipId(),
        file.getRepresentationId(), file.getPath(), file.getId(), filePath, username, overwriteManual);
      model.notifyFileUpdated(file).failOnError();
      return sources;
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  private static <T extends IsRODAObject> List<LinkingIdentifier> runSiegfriedOnRepresentationOrFile(ModelService model,
    IndexService index, String aipId, String representationId, List<String> fileDirectoryPath, String fileId, Path path,
    String username, Boolean overwriteManual) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, PluginException, AlreadyExistsException {
    List<LinkingIdentifier> sources = new ArrayList<>();

    if (FSUtils.exists(path)) {
      String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(path);
      final JsonNode jsonObject = JsonUtils.parseJson(siegfriedOutput);
      final JsonNode files = jsonObject.get("files");

      for (JsonNode file : files) {
        Path fullFsPath = Paths.get(FilenameUtils.normalize(file.get("filename").asText()));
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

        JsonNode matches = file.get(RodaConstants.SIEGFRIED_PAYLOAD_MATCHES);
        if (matches != null) {
          if (!PremisV3Utils.formatWasManuallyModified(model, aipId, representationId, jsonFilePath, jsonFileId,
            username) || overwriteManual) {
            ContentPayload payload = new StringContentPayload(file.toString());
            model.createOrUpdateOtherMetadata(aipId, representationId, jsonFilePath, jsonFileId,
              SiegfriedPlugin.FILE_SUFFIX, RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED, payload, username, false);

            sources.add(PluginHelper.getLinkingIdentifier(aipId, representationId, jsonFilePath, jsonFileId,
              RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

            // Update PREMIS files
            for (JsonNode match : matches) {
              String format = null;
              String version = null;
              String pronom = null;
              String mime = null;

              if ("pronom".equalsIgnoreCase(match.get("ns").textValue())) {
                format = match.get("format").textValue();
                version = match.get("version").textValue();
                pronom = match.get("id").textValue();
                mime = match.get("mime").textValue();
              }

              JsonNode warning = match.get(RodaConstants.SIEGFRIED_PAYLOAD_MATCH_WARNING);
              List<String> notes = new ArrayList<>();
              if (StringUtils.isNotBlank(warning.textValue())) {
                notes.add(RodaConstants.PRESERVATION_FORMAT_NOTE_SIEGFRIED_WARNING + ": " + warning.textValue());
                updateFileRiskIncidences(model, index, aipId, representationId, jsonFileId, jsonFilePath,
                  warning.textValue());
              }
              PremisV3Utils.updateFormatPreservationMetadata(model, aipId, representationId, jsonFilePath, jsonFileId,
                format, version, pronom, mime, notes, username, true);
            }
          }
        }
      }
    }
    return sources;
  }

  private static void updateFileRiskIncidences(ModelService model, IndexService index, String aipId,
    String representationId, String fileId, List<String> filePath, String warning) throws RequestNotValidException,
    GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    // Mitigate previous incidences
    for (RiskIncidence incidence : getPreviousSiegfriedIncidences(model, index, fileId)) {
      incidence.setStatus(IncidenceStatus.MITIGATED);
      model.updateRiskIncidence(incidence, true);
    }
    // Create a new incidence
    RiskIncidence riskIncidence = new RiskIncidence();
    if (fileId != null) {
      riskIncidence.setFileId(fileId);
      riskIncidence.setFilePath(filePath);
      riskIncidence.setObjectClass(File.class.getName());
    } else {
      riskIncidence.setObjectClass(Representation.class.getName());
    }
    riskIncidence.setRiskId(RodaConstants.RISK_ID_SIEGFRIED_IDENTIFICATION_WARNING);
    riskIncidence.setDetectedBy(SiegfriedPlugin.getStaticName());
    riskIncidence.setByPlugin(true);
    riskIncidence.setStatus(IncidenceStatus.UNMITIGATED);
    riskIncidence.setRepresentationId(representationId);
    riskIncidence.setAipId(aipId);
    riskIncidence.setDescription(warning);
    riskIncidence.setFileId(fileId);
    riskIncidence.setSeverity(SeverityLevel.LOW);
    model.createRiskIncidence(riskIncidence, true);
  }

  public static List<RiskIncidence> getPreviousSiegfriedIncidences(ModelService model, IndexService index,
    String fileId) throws RequestNotValidException, GenericException, AuthorizationDeniedException, NotFoundException {
    List<RiskIncidence> riskIncidences = new ArrayList<>();
    Filter filter = new Filter();
    List<FilterParameter> filterParameters = new ArrayList<>();
    filterParameters.add(new SimpleFilterParameter("riskId", RodaConstants.RISK_ID_SIEGFRIED_IDENTIFICATION_WARNING));
    filterParameters.add(new SimpleFilterParameter("fileId", fileId));
    filterParameters.add(new SimpleFilterParameter("status", IncidenceStatus.UNMITIGATED.name()));
    filter.add(filterParameters);
    try (IterableIndexResult<RiskIncidence> results = index.findAll(RiskIncidence.class, filter, true,
      Arrays.asList("id", "status"))) {
      for (RiskIncidence incidence : results) {
        RiskIncidence modelIncidence = model.retrieveRiskIncidence(incidence.getId());
        riskIncidences.add(modelIncidence);
      }
    } catch (IOException e) {
      LOGGER.error("Error finding file id {}'s associated risk incidences", fileId, e);
    }
    return riskIncidences;
  }
}
