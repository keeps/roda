/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class ReplicationPluginUtils {
  private static List<String> initialCommand = addInitialCommandPart();
  private static List<String> finalStorageCommand = addFinalCommandPart(RodaConstants.CORE_STORAGE_FOLDER);
  private static List<String> finalHistoryCommand = addFinalCommandPart(RodaConstants.CORE_STORAGE_HISTORY_FOLDER);

  public static String executeRsyncAIP(AIP aip) throws CommandException, IOException, UnsupportedOperationException {
    List<String> rsyncCommand = new ArrayList<String>(initialCommand);
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AIP + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AIP + "/" + aip.getId() + "/***");
    rsyncCommand.addAll(finalStorageCommand);
    String result = CommandUtility.execute(rsyncCommand);

    rsyncCommand = new ArrayList<String>(initialCommand);
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AIP + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AIP + "/" + aip.getId() + "/***");
    rsyncCommand.addAll(finalHistoryCommand);
    result += CommandUtility.execute(rsyncCommand);

    return result;
  }

  public static String executeRsyncEvents(PreservationMetadata pm)
    throws CommandException, IOException, UnsupportedOperationException {
    List<String> rsyncCommand = new ArrayList<String>(initialCommand);
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AIP + "/");

    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AIP + "/" + pm.getAipId() + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.STORAGE_CONTAINER_AIP + "/" + pm.getAipId() + "/" + RodaConstants.STORAGE_DIRECTORY_METADATA + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AIP + "/" + pm.getAipId() + "/"
      + RodaConstants.STORAGE_DIRECTORY_METADATA + "/" + RodaConstants.STORAGE_DIRECTORY_PRESERVATION + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AIP + "/" + pm.getAipId() + "/"
      + RodaConstants.STORAGE_DIRECTORY_METADATA + "/" + RodaConstants.STORAGE_DIRECTORY_PRESERVATION + "/***");

    rsyncCommand.addAll(finalStorageCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  public static String executeRsyncAgents() throws CommandException, IOException, UnsupportedOperationException {
    List<String> rsyncCommand = new ArrayList<String>(initialCommand);
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.STORAGE_CONTAINER_AGENT + "/***");
    rsyncCommand.addAll(finalStorageCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  private static List<String> addFinalCommandPart(String folder) {
    List<String> command = new ArrayList<String>();

    String sourcePath = RodaCoreFactory.getDataPath().toString();
    String targetUser = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_user");
    String targetHost = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_host");
    String targetPath = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", folder, "target_path");

    command.add("--exclude");
    command.add("*");
    command.add(sourcePath + "/" + folder + "/");
    command.add(targetUser + "@" + targetHost + ":" + targetPath);

    return command;
  }

  private static List<String> addInitialCommandPart() {
    List<String> command = new ArrayList<String>();
    command.add("rsync");
    command.add("-vzurltD");
    command.add("--delete");
    // command.add("--dry-run");
    return command;
  }

  private static List<String> addIncludeOnCommand(List<String> command, String toInclude) {
    command.add("--include");
    command.add(toInclude);
    return command;
  }
}
