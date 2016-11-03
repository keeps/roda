package org.roda.core.plugins.plugins.base;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationPluginUtils.class);

  protected static String PROPERTIES_ERROR_MESSAGE = "Rsync properties are not well defined";

  public static String executeRsyncAIP(AIP aip, boolean hasCompression, boolean syncHistory) throws CommandException {
    String dataTarget = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target");

    if (dataTarget == null) {
      return PROPERTIES_ERROR_MESSAGE;
    }

    List<String> rsyncCommand = addInitialCommandPart(hasCompression);
    Path sourceAipPath = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(aip.getId());

    String targetAipPath = dataTarget + "/" + RodaConstants.CORE_STORAGE_FOLDER + "/"
      + RodaConstants.STORAGE_CONTAINER_AIP + "/" + aip.getId() + "/";

    String ret;
    rsyncCommand.add(sourceAipPath + "/");
    rsyncCommand.add(targetAipPath);

    LOGGER.debug("Executing AIPs rsync: {}", rsyncCommand);
    ret = CommandUtility.execute(rsyncCommand);

    if (syncHistory) {
      Path sourceAipHistoryDataPath = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_HISTORY_FOLDER)
        .resolve(RodaConstants.STORAGE_HISTORY_CONTAINER_DATA).resolve(RodaConstants.STORAGE_CONTAINER_AIP)
        .resolve(aip.getId());

      Path sourceAipHistoryMetadataPath = RodaCoreFactory.getDataPath()
        .resolve(RodaConstants.CORE_STORAGE_HISTORY_FOLDER).resolve(RodaConstants.STORAGE_HISTORY_CONTAINER_METADATA)
        .resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(aip.getId());

      String targetAipHistoryDataPath = dataTarget + "/" + RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/"
        + RodaConstants.STORAGE_HISTORY_CONTAINER_DATA + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/" + aip.getId()
        + "/";

      String targetAipHistoryMetadataPath = dataTarget + "/" + RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/"
        + RodaConstants.STORAGE_HISTORY_CONTAINER_METADATA + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/"
        + aip.getId() + "/";

      List<String> rsyncHistoryDataCommand = addInitialCommandPart(hasCompression);
      rsyncHistoryDataCommand.add(sourceAipHistoryDataPath + "/");
      rsyncHistoryDataCommand.add(targetAipHistoryDataPath);

      LOGGER.debug("Executing AIP history data rsync: {}", rsyncHistoryDataCommand);
      ret += CommandUtility.execute(rsyncHistoryDataCommand);

      List<String> rsyncHistoryMetadataCommand = addInitialCommandPart(hasCompression);
      rsyncHistoryMetadataCommand.add(sourceAipHistoryMetadataPath + "/");
      rsyncHistoryMetadataCommand.add(targetAipHistoryMetadataPath);

      LOGGER.debug("Executing AIP history metadata rsync: {}", rsyncHistoryMetadataCommand);
      ret += CommandUtility.execute(rsyncHistoryMetadataCommand);

    }

    return ret;
  }

  public static String executeRsyncEvent(PreservationMetadata pm, boolean hasCompression) throws CommandException {
    String dataTarget = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target");

    if (dataTarget == null) {
      return PROPERTIES_ERROR_MESSAGE;
    }

    List<String> rsyncCommand = addInitialCommandPart(hasCompression);
    Path sourceEventPath = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(pm.getAipId())
      .resolve(RodaConstants.STORAGE_DIRECTORY_METADATA).resolve(RodaConstants.STORAGE_DIRECTORY_PRESERVATION)
      .resolve(pm.getId() + RodaConstants.PREMIS_SUFFIX);

    String targetEventPath = dataTarget + "/" + RodaConstants.CORE_STORAGE_FOLDER + "/"
      + RodaConstants.STORAGE_CONTAINER_AIP + "/" + pm.getAipId() + "/" + RodaConstants.STORAGE_DIRECTORY_METADATA + "/"
      + RodaConstants.STORAGE_DIRECTORY_PRESERVATION + "/" + pm.getId() + RodaConstants.PREMIS_SUFFIX + "/";

    rsyncCommand.add(sourceEventPath + "/");
    rsyncCommand.add(targetEventPath);

    LOGGER.debug("Executing AIP events rsync: {}", rsyncCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  public static String executeRsyncAgents(boolean hasCompression) throws CommandException {
    String dataTarget = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target");

    if (dataTarget == null) {
      return PROPERTIES_ERROR_MESSAGE;
    }

    List<String> rsyncCommand = addInitialCommandPart(hasCompression);

    Path sourceAgentPath = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION)
      .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION_AGENTS);

    String targetAgentPath = dataTarget + "/" + RodaConstants.CORE_STORAGE_FOLDER + "/"
      + RodaConstants.STORAGE_CONTAINER_PRESERVATION + "/" + RodaConstants.STORAGE_CONTAINER_PRESERVATION_AGENTS + "/";

    rsyncCommand.add(sourceAgentPath + "/");
    rsyncCommand.add(targetAgentPath);

    LOGGER.debug("Executing agents rsync: {}", rsyncCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  private static List<String> addInitialCommandPart(boolean hasCompression) {
    List<String> command = new ArrayList<String>();
    command.add("rsync");

    if (hasCompression) {
      command.add("-vzurltDLK");
    } else {
      command.add("-vurltDLK");
    }

    command.add("--delete");
    // command.add("--dry-run");
    return command;
  }
}
