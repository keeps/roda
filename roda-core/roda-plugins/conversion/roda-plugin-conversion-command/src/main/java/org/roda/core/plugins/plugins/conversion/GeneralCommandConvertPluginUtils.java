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

import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class GeneralCommandConvertPluginUtils {

  private GeneralCommandConvertPluginUtils() {
    // do nothing
  }

  public static String executeGeneralCommand(Path input, Path output, String command)
    throws CommandException, IOException, UnsupportedOperationException {
    String updatedCommand = command;
    updatedCommand = updatedCommand.replace("{input_file}", input.toString());
    updatedCommand = updatedCommand.replace("{output_file}", output.toString());

    // filling a list of the command line arguments
    List<String> commandList = Arrays.asList(updatedCommand.split("\\s+"));

    // running the command
    return CommandUtility.execute(commandList);
  }

}
