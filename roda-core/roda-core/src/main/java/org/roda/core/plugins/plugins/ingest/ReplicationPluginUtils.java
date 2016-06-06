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
  private static List<String> finalCommand = addFinalCommandPart();

  public static String executeRsyncAIP(AIP aip) throws CommandException, IOException, UnsupportedOperationException {
    List<String> rsyncCommand = new ArrayList<String>(initialCommand);

    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/" + aip.getId() + "/***");
    rsyncCommand = addIncludeOnCommand(rsyncCommand, RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/"
      + RodaConstants.STORAGE_CONTAINER_AIP + "/" + aip.getId() + "/***");

    rsyncCommand.addAll(finalCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  public static String executeRsyncEvents(PreservationMetadata pm)
    throws CommandException, IOException, UnsupportedOperationException {
    List<String> rsyncCommand = new ArrayList<String>(initialCommand);
    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/");

    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/" + pm.getAipId() + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/" + pm.getAipId() + "/"
        + RodaConstants.STORAGE_DIRECTORY_METADATA + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/" + pm.getAipId() + "/"
        + RodaConstants.STORAGE_DIRECTORY_METADATA + "/" + RodaConstants.STORAGE_DIRECTORY_PRESERVATION + "/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand, "storage/aip/" + pm.getAipId() + "/"
      + RodaConstants.STORAGE_DIRECTORY_METADATA + "/" + RodaConstants.STORAGE_DIRECTORY_PRESERVATION + "/***");

    rsyncCommand.addAll(finalCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  public static String executeRsyncAgents() throws CommandException, IOException, UnsupportedOperationException {
    List<String> rsyncCommand = new ArrayList<String>(initialCommand);
    rsyncCommand = addIncludeOnCommand(rsyncCommand,
      RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AGENT + "/***");
    rsyncCommand.addAll(finalCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  private static List<String> addFinalCommandPart() {
    List<String> command = new ArrayList<String>();

    String sourcePath = RodaCoreFactory.getDataPath().toString();
    String targetUser = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_user");
    String targetHost = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_host");
    String targetPath = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_path");

    command.add("--exclude");
    command.add("*");
    command.add(sourcePath + "/");
    command.add(targetUser + "@" + targetHost + ":" + targetPath);

    return command;
  }

  private static List<String> addInitialCommandPart() {
    List<String> command = new ArrayList<String>();

    command.add("rsync");
    // command.add("--dry-run");
    command.add("-vzurltD");
    command.add("--delete");
    command = addIncludeOnCommand(command, RodaConstants.CORE_STORAGE_FOLDER + "/");

    return command;
  }

  private static List<String> addIncludeOnCommand(List<String> command, String toInclude) {
    command.add("--include");
    command.add(toInclude);
    return command;
  }
}
