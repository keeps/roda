package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.ghost4j.Ghostscript;
import org.ghost4j.GhostscriptException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class GhostScriptConvertPluginUtils {

  public static Path runGhostScriptConvert(InputStream is, String inputFormat, String outputFormat, String device,
    String conversionProfile) throws IOException, GhostscriptException {

    // write the inputstream data on a new file (absolute path needed)
    Path input = Files.createTempFile("copy", "." + inputFormat);
    byte[] buffer = new byte[is.available()];
    is.read(buffer);
    OutputStream os = new FileOutputStream(input.toFile());
    os.write(buffer);
    os.close();
    is.close();

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeGS(input, output, device, conversionProfile);
  }

  public static Path runGhostScriptConvert(Path input, String inputFormat, String outputFormat, String device,
    String conversionProfile) throws IOException, CommandException, GhostscriptException {

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeGS(input, output, device, conversionProfile);
  }

  private static Path executeGS(Path input, Path output, String device, String conversionProfile)
    throws GhostscriptException {

    String command = RodaCoreFactory.getRodaConfigurationAsString("tools", "ghostscriptconvert", conversionProfile,
      "commandLine");
    command = command.replace("{input_file}", input.toString());
    command = command.replace("{output_file}", output.toString());
    command = command.replace("{device}", device);

    // GhostScript transformation command
    String[] gsArgs = command.split(" ");
    Ghostscript gs = Ghostscript.getInstance();

    try {
      gs.initialize(gsArgs);
      gs.exit();
    } catch (GhostscriptException e) {
      throw new GhostscriptException("Exception when using GhostScript: ", e);
    }

    return output;
  }

  public static String getVersion() throws CommandException {
    String version = CommandUtility.execute("gs", "--version");
    if (version.indexOf('\n') > 0) {
      version = version.substring(0, version.indexOf('\n'));
    }
    return version;
  }

}
