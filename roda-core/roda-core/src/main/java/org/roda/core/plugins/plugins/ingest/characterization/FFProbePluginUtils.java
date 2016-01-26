/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

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
import org.roda.core.RodaCoreFactory;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FFProbePluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(FFProbePluginUtils.class);

  public static Path inspect(File f) throws PluginException {
    try {
      List<String> command = getCommand();
      command.add(f.getAbsolutePath());
      String ffProbeOutput = CommandUtility.execute(command);
      Path p = Files.createTempFile("ffprobe", ".xml");
      Files.write(p, ffProbeOutput.getBytes());
      return p;
    } catch (CommandException e) {
      throw new PluginException("Error while executing FFProbe command");
    } catch (IOException e) {
      throw new PluginException("Error while parsing FFProbe output");
    }
  }

  private static List<String> getCommand() {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path ffProbeHome = rodaHome.resolve(RodaCoreFactory.getRodaConfigurationAsString("tools", "ffprobe", "path"));

    File FFPROBE_DIRECTORY = ffProbeHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(
        Arrays.asList(FFPROBE_DIRECTORY.getAbsolutePath() + File.separator + "ffprobe.exe", "-show_data",
          "-show_format", "-show_error", "-show_streams", "-show_chapters", "-show_private_data", "-show_versions",
          "-print_format", "xml", "-v", "quiet", "-i"));
    } else {
      command = new ArrayList<String>(Arrays.asList(FFPROBE_DIRECTORY.getAbsolutePath() + File.separator + "ffprobe",
        "-show_data", "-show_format", "-show_error", "-show_streams", "-show_chapters", "-show_private_data",
        "-show_versions", "-print_format", "xml", "-v", "quiet", "-i"));
    }
    return command;
  }

  public static Path runFFProbe(org.roda.core.data.v2.ip.File file, Binary binary, Map<String, String> parameterValues)
    throws IOException, PluginException {
    java.io.File f = File.createTempFile("temp", ".temp");
    FileOutputStream fos = new FileOutputStream(f);
    IOUtils.copy(binary.getContent().createInputStream(), fos);
    fos.close();
    return inspect(f);
  }
}
