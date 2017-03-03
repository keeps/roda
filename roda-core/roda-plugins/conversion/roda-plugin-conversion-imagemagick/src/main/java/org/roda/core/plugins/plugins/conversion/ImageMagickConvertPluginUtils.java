/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.conversion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class ImageMagickConvertPluginUtils {

  private ImageMagickConvertPluginUtils() {
  }

  public static String executeImageMagick(Path input, Path output, String outputFormat, String commandArguments)
    throws CommandException {

    String command = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "imagemagickconvert", "commandLine");
    command = command.replace("{input_file}", input.toString());
    command = command.replace("{output_file}", outputFormat + ":" + output.toString());
    command = command.replace("{arguments}", commandArguments);

    // running the command
    return CommandUtility.execute(Arrays.asList(command.split("\\s+")));
  }

  public static String getVersion() throws CommandException, IOException, UnsupportedOperationException {
    String command = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "imagemagickconvert",
      "versionCommand");
    String version = CommandUtility.execute(command.split("\\s+"));
    if (version.indexOf('\n') >= 1) {
      version = version.substring(0, version.indexOf('\n'));
    }

    version = version.replaceAll("Version:\\s+[a-zA-Z]+\\s+([a-zA-Z0-9.-]+).*", "$1");
    return version.trim();
  }

}
