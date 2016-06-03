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

public class AIPRsyncPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(AIPRsyncPluginUtils.class);

  public static String executeRsyncAIP(List<AIP> list)
    throws CommandException, IOException, UnsupportedOperationException {

    String sourcePath = RodaCoreFactory.getDataPath().toString();
    String targetUser = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_user");
    String targetHost = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_host");
    String targetPath = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_path");

    List<String> rsyncCommand = new ArrayList<String>();
    rsyncCommand.add("rsync");
    // rsyncCommand.add("--dry-run");
    rsyncCommand.add("-avzu");
    rsyncCommand.add("--delete");
    rsyncCommand.add("--include");
    rsyncCommand.add("'storage/'");
    rsyncCommand.add("--include");
    rsyncCommand.add("'storage/aip/'");
    rsyncCommand.add("--include");
    rsyncCommand.add("'storage-history/'");
    rsyncCommand.add("--include");
    rsyncCommand.add("'storage-history/aip/'");

    for (AIP aip : list) {
      rsyncCommand.add("--include");
      rsyncCommand.add("\"storage/aip/" + aip.getId() + "/***\"");
      rsyncCommand.add("--include");
      rsyncCommand.add("\"storage-history/aip/" + aip.getId() + "/***\"");
    }

    rsyncCommand.add("--exclude");
    rsyncCommand.add("'*'");
    rsyncCommand.add(sourcePath);
    rsyncCommand.add(targetUser + "@" + targetHost + ":" + targetPath);

    return CommandUtility.execute(rsyncCommand);
  }

  public static String executeRsyncEvents(List<PreservationMetadata> list)
    throws CommandException, IOException, UnsupportedOperationException {

    String sourcePath = RodaCoreFactory.getDataPath().toString();
    String targetUser = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_user");
    String targetHost = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_host");
    String targetPath = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_path");

    List<String> rsyncCommand = new ArrayList<String>();
    rsyncCommand.add("rsync");
    rsyncCommand.add("--dry-run");
    rsyncCommand.add("-avzu");
    rsyncCommand.add("--delete");
    rsyncCommand.add("--include");
    rsyncCommand.add("'storage/'");
    rsyncCommand.add("--include");
    rsyncCommand.add("'storage/aip/'");

    for (PreservationMetadata pm : list) {
      rsyncCommand.add("--include");
      rsyncCommand.add("\"storage/aip/" + pm.getAipId() + "\"");
      rsyncCommand.add("--include");
      rsyncCommand.add("\"storage/aip/" + pm.getAipId() + "/metadata\"");
      rsyncCommand.add("--include");
      rsyncCommand.add("\"storage/aip/" + pm.getAipId() + "/metadata/preservation\"");
      rsyncCommand.add("--include");
      rsyncCommand.add("\"storage/aip/" + pm.getAipId() + "/metadata/preservation/" + pm.getId() + "\"");
    }

    rsyncCommand.add("--exclude");
    rsyncCommand.add("'*'");
    rsyncCommand.add(sourcePath);
    rsyncCommand.add(targetUser + "@" + targetHost + ":" + targetPath);

    return CommandUtility.execute(rsyncCommand);
  }

}
