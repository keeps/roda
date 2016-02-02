package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class ImageMagickConvertPluginUtils {

  public static Path runImageMagickConvert(InputStream is, String inputFormat, String outputFormat,
    String commandArguments) throws IOException, CommandException {

    // write the inputstream data on a new file (absolute path needed)
    Path input = Files.createTempFile("copy", "." + inputFormat);
    byte[] buffer = new byte[is.available()];
    is.read(buffer);
    OutputStream os = new FileOutputStream(input.toFile());
    os.write(buffer);
    os.close();
    is.close();

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeImageMagick(input, output, inputFormat, outputFormat, commandArguments);
  }

  public static Path runImageMagickConvert(Path input, String inputFormat, String outputFormat, String commandArguments)
    throws IOException, CommandException {
    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeImageMagick(input, output, inputFormat, outputFormat, commandArguments);
  }

  private static Path executeImageMagick(Path input, Path output, String inputFormat, String outputFormat,
    String commandArguments) throws CommandException {

    String command = RodaCoreFactory.getRodaConfigurationAsString("tools", "imagemagickconvert", "commandLine");
    command = command.replace("{input_file}", inputFormat + ":" + input.toString());
    command = command.replace("{output_file}", outputFormat + ":" + output.toString());
    command = command.replace("{arguments}", commandArguments);

    // filling a list of the command line arguments
    List<String> commandList = Arrays.asList(command.split("\\s+"));

    // running the command
    CommandUtility.execute(commandList);
    return output;
  }

  public static String getVersion() throws CommandException {
    String version = CommandUtility.execute("convert", "--version");
    if (version.indexOf('\n') > 0) {
      version = version.substring(0, version.indexOf('\n'));
    }

    version = version.replaceAll("Version:\\s+([a-zA-Z]+\\s+[a-zA-Z0-9.-]+).*", "$1");
    return version.trim();
  }
}
