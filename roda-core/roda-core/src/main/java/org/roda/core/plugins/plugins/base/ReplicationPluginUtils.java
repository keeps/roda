package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationPluginUtils.class);

  private static List<String> finalCommand = addFinalCommandPart();
  private static String PROPERTIES_ERROR_MESSAGE = "Rsync properties are not well defined";

  public static String executeRsyncAIPList(List<AIP> aips, boolean hasCompression)
    throws CommandException, IOException, UnsupportedOperationException {
    if (!finalCommand.isEmpty()) {
      Path path = creatingIncludeFileForAIPs(aips);

      List<String> rsyncCommand = addInitialCommandPart(hasCompression);
      rsyncCommand.add("--include-from=" + path.toString());
      rsyncCommand.addAll(finalCommand);
      try {
        LOGGER.debug("Executing AIPs rsync: {}", rsyncCommand);
        return CommandUtility.execute(rsyncCommand);
      } catch (CommandException e) {
        throw e;
      } finally {
        FSUtils.deletePathQuietly(path);
      }
    }

    return PROPERTIES_ERROR_MESSAGE;
  }

  public static String executeRsyncEvents(List<PreservationMetadata> pms, boolean hasCompression)
    throws CommandException, IOException, UnsupportedOperationException {
    if (!finalCommand.isEmpty()) {
      Path path = creatingIncludeFileForEvents(pms);

      List<String> rsyncCommand = addInitialCommandPart(hasCompression);
      rsyncCommand.add("--include-from=" + path.toString());
      rsyncCommand.addAll(finalCommand);

      try {
        LOGGER.debug("Executing events rsync: {}", rsyncCommand);
        return CommandUtility.execute(rsyncCommand);
      } catch (CommandException e) {
        throw e;
      } finally {
        FSUtils.deletePathQuietly(path);
      }
    }

    return PROPERTIES_ERROR_MESSAGE;
  }

  public static String executeRsyncAgents(boolean hasCompression)
    throws CommandException, IOException, UnsupportedOperationException {
    if (!finalCommand.isEmpty()) {
      List<String> rsyncCommand = addInitialCommandPart(hasCompression);
      rsyncCommand.add("--include");
      rsyncCommand.add(RodaConstants.CORE_STORAGE_FOLDER + "/");
      rsyncCommand.add("--include");
      rsyncCommand.add(RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_PRESERVATION + "/");
      rsyncCommand.add("--include");
      rsyncCommand.add(RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_PRESERVATION + "/"
        + RodaConstants.STORAGE_CONTAINER_PRESERVATION_AGENTS + "/***");
      rsyncCommand.addAll(finalCommand);

      LOGGER.debug("Executing agents rsync: {}", rsyncCommand);
      return CommandUtility.execute(rsyncCommand);
    }

    return PROPERTIES_ERROR_MESSAGE;
  }

  private static Path creatingIncludeFileForAIPs(List<AIP> aips) throws IOException {
    Path path = Files.createTempFile("replication", ".txt");
    PrintWriter writer = new PrintWriter(path.toFile());

    String storageAIP = RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/";

    String historyDataAIP = RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/"
      + RodaConstants.STORAGE_HISTORY_CONTAINER_DATA + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/";

    String historyMetadataAIP = RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/"
      + RodaConstants.STORAGE_HISTORY_CONTAINER_METADATA + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/";

    writer.println(RodaConstants.CORE_STORAGE_FOLDER + "/");
    writer.println(storageAIP);
    writer.println(RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/");
    writer
      .println(RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/" + RodaConstants.STORAGE_HISTORY_CONTAINER_DATA + "/");
    writer.println(historyDataAIP);
    writer.println(
      RodaConstants.CORE_STORAGE_HISTORY_FOLDER + "/" + RodaConstants.STORAGE_HISTORY_CONTAINER_METADATA + "/");
    writer.println(historyMetadataAIP);

    for (AIP aip : aips) {
      writer.println(storageAIP + aip.getId() + "/***");
      writer.println(historyDataAIP + aip.getId() + "/***");
      writer.println(historyMetadataAIP + aip.getId() + "/***");
    }

    IOUtils.closeQuietly(writer);
    return path;
  }

  private static Path creatingIncludeFileForEvents(List<PreservationMetadata> pms) throws IOException {
    Path path = Files.createTempFile("replication_event", ".txt");
    PrintWriter writer = new PrintWriter(path.toFile());

    String storageAIP = RodaConstants.CORE_STORAGE_FOLDER + "/" + RodaConstants.STORAGE_CONTAINER_AIP + "/";

    writer.println(RodaConstants.CORE_STORAGE_FOLDER + "/");
    writer.println(storageAIP);

    for (PreservationMetadata pm : pms) {
      writer.println(storageAIP + pm.getAipId() + "/");
      writer.println(storageAIP + pm.getAipId() + "/" + RodaConstants.STORAGE_DIRECTORY_METADATA + "/");
      writer.println(storageAIP + pm.getAipId() + "/" + RodaConstants.STORAGE_DIRECTORY_METADATA + "/"
        + RodaConstants.STORAGE_DIRECTORY_PRESERVATION + "/");
      writer.println(storageAIP + pm.getAipId() + "/" + RodaConstants.STORAGE_DIRECTORY_METADATA + "/"
        + RodaConstants.STORAGE_DIRECTORY_PRESERVATION + "/***");
    }

    IOUtils.closeQuietly(writer);
    return path;
  }

  private static List<String> addFinalCommandPart() {
    List<String> command = new ArrayList<String>();

    String sourcePath = RodaCoreFactory.getDataPath().toString();
    String target = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target");

    if (target != null) {
      command.add("--exclude");
      command.add("*");
      command.add(sourcePath + "/");
      command.add(target);
    }

    return command;
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

  private static List<String> addIncludeOnCommand(List<String> command, String toInclude) {
    command.add("--include");
    command.add(RodaConstants.CORE_STORAGE_FOLDER + "/" + toInclude);
    return command;
  }
}
