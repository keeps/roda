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

  public static Path runImageMagickConvert(InputStream is, String inputFormat, String outputFormat) throws IOException,
    CommandException {

    // write the inputstream data on a new file (absolute path needed)
    Path input = Files.createTempFile("copy", "." + inputFormat);
    byte[] buffer = new byte[is.available()];
    is.read(buffer);
    OutputStream os = new FileOutputStream(input.toFile());
    os.write(buffer);
    os.close();
    is.close();

    Path output = Files.createTempFile("result", "." + outputFormat);

    return executeImageMagick(input, output, inputFormat, outputFormat);
  }

  public static Path runImageMagickConvert(Path input, String inputFormat, String outputFormat) throws IOException,
    CommandException {

    Path output = Files.createTempFile("result", "." + outputFormat);

    return executeImageMagick(input, output, inputFormat, outputFormat);
  }

  private static Path executeImageMagick(Path input, Path output, String inputFormat, String outputFormat)
    throws CommandException {

    // FIXME replace error
    String command = RodaCoreFactory.getRodaConfigurationAsString("tools", "imagemagickconvert", "commandLine");
    command.replace("{input_file}", inputFormat + ":" + input.toString());
    command.replace("{output_file}", outputFormat + ":" + output.toString());

    // filling a list of the command line arguments
    List<String> commandList = Arrays.asList(command.split(" "));

    // running the command
    CommandUtility.execute(commandList);
    return output;
  }

}
