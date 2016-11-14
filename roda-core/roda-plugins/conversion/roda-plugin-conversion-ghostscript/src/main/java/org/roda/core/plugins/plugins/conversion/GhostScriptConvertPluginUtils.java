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

import org.ghost4j.Ghostscript;
import org.ghost4j.GhostscriptException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class GhostScriptConvertPluginUtils {

  public static String executeGS(Path input, Path output, String commandArguments)
    throws GhostscriptException, IOException, UnsupportedOperationException {

    String command = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "ghostscriptconvert", "commandLine");
    // command = command.replace("{input_file}", input.toString());
    command = command.replace("{output_file}", output.toString());

    if (commandArguments.length() > 0) {
      command = command.replace("{arguments}", commandArguments);
    } else {
      command = command.replace("{arguments}", "-sDEVICE=pdfwrite");
    }

    // GhostScript transformation command
    String[] gsArgs = command.split("\\s+");

    for (int i = 0; i < gsArgs.length; i++) {
      if (gsArgs[i].equals("{input_file}")) {
        gsArgs[i] = input.toString();
      }
    }

    Ghostscript gs = Ghostscript.getInstance();

    try {
      gs.initialize(gsArgs);
      gs.exit();
    } catch (GhostscriptException e) {
      throw new GhostscriptException("Exception when using GhostScript: ", e);
    }

    return "";
  }

  public static String getVersion() throws CommandException, IOException, UnsupportedOperationException {
    String command = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "ghostscriptconvert",
      "versionCommand");
    String version = CommandUtility.execute(command.split("\\s+"));
    if (version.indexOf('\n') > 0) {
      version = version.substring(0, version.indexOf('\n'));
    }
    return version.trim();
  }

}
