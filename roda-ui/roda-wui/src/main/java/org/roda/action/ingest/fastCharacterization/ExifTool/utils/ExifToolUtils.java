/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.ingest.fastCharacterization.ExifTool.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.roda.common.RodaCoreFactory;
import org.roda.plugins.PluginException;
import org.roda.storage.Binary;
import org.roda.util.CommandException;
import org.roda.util.CommandUtility;

public class ExifToolUtils {
  static final private Logger logger = Logger.getLogger(ExifToolUtils.class);

  public static Path inspect(File f) throws PluginException {
    try {
      List<String> command = getCommand();
      command.add(f.getAbsolutePath());
      String exifToolOutput = CommandUtility.execute(command);
      Path p = Files.createTempFile("exiftool", ".xml");
      Files.write(p, exifToolOutput.getBytes());
      return p;
    } catch (CommandException e) {
      throw new PluginException("Error while executing ExifTool");
    } catch (IOException e) {
      throw new PluginException("Error while executing ExifTool");
    }
  }

  private static List<String> getCommand() {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path exifToolHome = rodaHome.resolve(RodaCoreFactory.getRodaConfigurationAsString("tools", "exiftool", "home"));

    File EXIFTOOL_DIRECTORY = exifToolHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(
        Arrays.asList(EXIFTOOL_DIRECTORY.getAbsolutePath() + File.separator + "exiftool.exe", "-X"));
    } else {
      command = new ArrayList<String>(
        Arrays.asList(EXIFTOOL_DIRECTORY.getAbsolutePath() + File.separator + "exiftool", "-X"));
    }
    return command;
  }
  
  private static List<String> getBatchCommand(Path sourceDirectory, Path exifToolOutputDirectory) {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path exifToolHome = rodaHome.resolve(RodaCoreFactory.getRodaConfigurationAsString("tools", "exiftool", "home"));

    File EXIFTOOL_DIRECTORY = exifToolHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(
        Arrays.asList(EXIFTOOL_DIRECTORY.getAbsolutePath() + File.separator + "exiftool.exe", "-X", "-w", exifToolOutputDirectory.toFile().getAbsolutePath()+"/%f.%e.xml",sourceDirectory.toFile().getAbsolutePath()));
    } else {
      command = new ArrayList<String>(
        Arrays.asList(EXIFTOOL_DIRECTORY.getAbsolutePath() + File.separator + "exiftool", "-X", "-w", exifToolOutputDirectory.toFile().getAbsolutePath()+"/%f.%e.xml",sourceDirectory.toFile().getAbsolutePath()));
    }
    return command;
  }

  public static Path runExifTool(org.roda.model.File file, Binary binary, Map<String, String> parameterValues)
    throws IOException, PluginException {
    java.io.File f = File.createTempFile("temp", ".temp");
    FileOutputStream fos = new FileOutputStream(f);
    IOUtils.copy(binary.getContent().createInputStream(), fos);
    fos.close();
    return inspect(f);
  }

  public static String runExifToolOnPath(Path sourceDirectory, Path exifToolOutputDirectory) throws CommandException {
    List<String> command = getBatchCommand(sourceDirectory,exifToolOutputDirectory);
    String exifToolOutput = CommandUtility.execute(command);
    return exifToolOutput;
  }

}
