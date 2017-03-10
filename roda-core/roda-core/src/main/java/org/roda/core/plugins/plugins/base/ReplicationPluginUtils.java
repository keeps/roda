package org.roda.core.plugins.plugins.base;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class ReplicationPluginUtils {
  protected static String PROPERTIES_ERROR_MESSAGE = "Rsync properties are not well defined";

  public static String executeRsyncAIP(AIP aip, boolean hasCompression) throws CommandException {
    String dataTarget = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target");

    if (dataTarget == null) {
      return PROPERTIES_ERROR_MESSAGE;
    }

    List<String> rsyncCommand = addInitialCommandPart(hasCompression);
    Path sourceAipPath = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(aip.getId());

    String targetAipPath = dataTarget + RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP
      + "/" + aip.getId() + "/";

    StringBuilder ret = new StringBuilder();

    if (sourceAipPath.toFile().exists()) {
      rsyncCommand.add(sourceAipPath + "/");
      rsyncCommand.add(targetAipPath);

      String output = CommandUtility.execute(rsyncCommand);

      ret.append("Executing AIPs rsync: ").append(StringUtils.join(rsyncCommand, " ")).append("\n");
      ret.append(output);
    }

    // data
    Path sourceAipHistoryDataPath = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_HISTORY_FOLDER)
      .resolve(RodaConstants.STORAGE_HISTORY_CONTAINER_DATA).resolve(RodaConstants.STORAGE_CONTAINER_AIP)
      .resolve(aip.getId());

    String targetAipHistoryDataPath = dataTarget + RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/"
      + RodaConstants.STORAGE_HISTORY_CONTAINER_DATA + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/" + aip.getId()
      + "/";

    if (sourceAipHistoryDataPath.toFile().exists()) {
      List<String> rsyncHistoryDataCommand = addInitialCommandPart(hasCompression);
      rsyncHistoryDataCommand.add(sourceAipHistoryDataPath + "/");
      rsyncHistoryDataCommand.add(targetAipHistoryDataPath);

      String output = CommandUtility.execute(rsyncHistoryDataCommand);

      ret.append("\nExecuting AIP history data rsync: ").append(StringUtils.join(rsyncHistoryDataCommand, " "))
        .append("\n");
      ret.append(output);
    } else {
      // TODO lfaria 20161104: if source doesn't exist it should be deleted on
      // target
    }

    // metadata
    Path sourceAipHistoryMetadataPath = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_HISTORY_FOLDER)
      .resolve(RodaConstants.STORAGE_HISTORY_CONTAINER_METADATA).resolve(RodaConstants.STORAGE_CONTAINER_AIP)
      .resolve(aip.getId());

    String targetAipHistoryMetadataPath = dataTarget + RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/"
      + RodaConstants.STORAGE_HISTORY_CONTAINER_METADATA + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/" + aip.getId()
      + "/";

    if (sourceAipHistoryMetadataPath.toFile().exists()) {
      List<String> rsyncHistoryMetadataCommand = addInitialCommandPart(hasCompression);
      rsyncHistoryMetadataCommand.add(sourceAipHistoryMetadataPath + "/");
      rsyncHistoryMetadataCommand.add(targetAipHistoryMetadataPath);

      String output = CommandUtility.execute(rsyncHistoryMetadataCommand);

      ret.append("\nExecuting AIP history metadata rsync: ").append(StringUtils.join(rsyncHistoryMetadataCommand, " "))
        .append("\n");
      ret.append(output);
    } else {
      // TODO lfaria 20161104: if source doesn't exist it should be deleted on
      // target
    }

    return ret.toString();
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

    String targetEventPath = dataTarget + RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP
      + "/" + pm.getAipId() + "/" + RodaConstants.STORAGE_DIRECTORY_METADATA + "/"
      + RodaConstants.STORAGE_DIRECTORY_PRESERVATION + "/" + pm.getId() + RodaConstants.PREMIS_SUFFIX;

    StringBuilder ret = new StringBuilder();
    if (sourceEventPath.toFile().exists()) {

      rsyncCommand.add(sourceEventPath.toString());
      rsyncCommand.add(targetEventPath);

      String output = CommandUtility.execute(rsyncCommand);

      ret.append("Executing AIP preservation events rsync: ").append(StringUtils.join(rsyncCommand, " ")).append("\n");
      ret.append(output);
    } else {
      // TODO lfaria 20161104: if source doesn't exist it should be deleted on
      // target
    }
    return ret.toString();
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

    String targetAgentPath = dataTarget + RodaConstants.CORE_STORAGE_FOLDER + "/"
      + RodaConstants.STORAGE_CONTAINER_PRESERVATION + "/" + RodaConstants.STORAGE_CONTAINER_PRESERVATION_AGENTS + "/";

    StringBuilder ret = new StringBuilder();
    if (sourceAgentPath.toFile().exists()) {
      rsyncCommand.add(sourceAgentPath + "/");
      rsyncCommand.add(targetAgentPath);

      String output = CommandUtility.execute(rsyncCommand);

      ret.append("Executing AIP preservation agent rsync: ").append(StringUtils.join(rsyncCommand, " ")).append("\n");
      ret.append(output);
    } else {
      // TODO lfaria 20161104: if source doesn't exist it should be deleted on
      // target
    }
    return ret.toString();
  }

  private static List<String> addInitialCommandPart(boolean hasCompression) {
    List<String> command = new ArrayList<>();
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
