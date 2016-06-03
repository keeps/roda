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
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationPluginUtils.class);

  public static String executeRsyncAIP(List<AIP> list)
    throws CommandException, IOException, UnsupportedOperationException {
    List<String> rsyncCommand = new ArrayList<String>();
    addInitialCommandPart(rsyncCommand);

    rsyncCommand = addIncludeOnCommand(rsyncCommand, "storage-history/");
    rsyncCommand = addIncludeOnCommand(rsyncCommand, "storage-history/aip/");

    for (AIP aip : list) {
      rsyncCommand = addIncludeOnCommand(rsyncCommand, "storage/aip/" + aip.getId() + "/***");
      rsyncCommand = addIncludeOnCommand(rsyncCommand, "storage-history/aip/" + aip.getId() + "/***");
    }

    rsyncCommand = addFinalCommandPart(rsyncCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  public static String executeRsyncEvents(List<PreservationMetadata> list)
    throws CommandException, IOException, UnsupportedOperationException {

    List<String> rsyncCommand = new ArrayList<String>();
    addInitialCommandPart(rsyncCommand);

    for (PreservationMetadata pm : list) {
      rsyncCommand = addIncludeOnCommand(rsyncCommand, "storage/aip/" + pm.getAipId() + "/");
      rsyncCommand = addIncludeOnCommand(rsyncCommand, "storage/aip/" + pm.getAipId() + "/metadata/");
      rsyncCommand = addIncludeOnCommand(rsyncCommand, "storage/aip/" + pm.getAipId() + "/metadata/preservation/");
      rsyncCommand = addIncludeOnCommand(rsyncCommand,
        "storage/aip/" + pm.getAipId() + "/metadata/preservation/" + pm.getId());
    }

    rsyncCommand = addFinalCommandPart(rsyncCommand);
    return CommandUtility.execute(rsyncCommand);
  }

  private static List<String> addFinalCommandPart(List<String> command) {
    String sourcePath = RodaCoreFactory.getDataPath().toString();
    String targetUser = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_user");
    String targetHost = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_host");
    String targetPath = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_path");

    command.add("--exclude");
    command.add("*");
    command.add(sourcePath);
    command.add(targetUser + "@" + targetHost + ":" + targetPath);

    return command;
  }

  private static List<String> addInitialCommandPart(List<String> command) {
    command.add("rsync");
    command.add("--dry-run");
    command.add("-vzurltD");
    command.add("--delete");

    command = addIncludeOnCommand(command, "storage/");
    command = addIncludeOnCommand(command, "storage/aip/");

    return command;
  }

  private static List<String> addIncludeOnCommand(List<String> command, String toInclude) {
    command.add("--include");
    command.add(toInclude);
    return command;
  }
}
