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
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class SoxConvertPluginUtils {

  private SoxConvertPluginUtils() {
    // do nothing
  }

  public static String executeSox(Path input, Path output, String commandArguments)
    throws CommandException, IOException, UnsupportedOperationException {

    String command = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "soxconvert", "commandLine");
    command = command.replace("{output_file}", output.toString());

    if (commandArguments.length() > 0) {
      command = command.replace("{arguments}", commandArguments);
    } else {
      command = command.replace("{arguments}", "-e gsm-full-rate");
    }

    List<String> commandList = Arrays.asList(command.split("\\s+"));
    for (int i = 0; i < commandList.size(); i++) {
      if ("{input_file}".equals(commandList.get(i))) {
        commandList.set(i, input.toString());
      }
    }

    // running the command
    return CommandUtility.execute(commandList);
  }

  public static String getVersion() throws CommandException, IOException, UnsupportedOperationException {
    String command = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "soxconvert", "versionCommand");
    String version = CommandUtility.execute(command.split("\\s+"));
    if (version.indexOf('\n') >= 1) {
      version = version.replace(" ", "");
      version = version.substring(0, version.indexOf('\n'));
    }
    version = version.replaceAll("/usr/bin/sox:SoXv([0-9.])", "$1");
    return version.trim();
  }

}