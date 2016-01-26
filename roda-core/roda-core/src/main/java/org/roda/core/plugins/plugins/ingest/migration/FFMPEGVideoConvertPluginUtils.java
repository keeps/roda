package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class FFMPEGVideoConvertPluginUtils {

  public static Path runFFMPEGVideoConvert(InputStream is, String inputFormat, String outputFormat) throws IOException,
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

    // filling a list of the command line arguments
    List<String> command = new ArrayList<String>();
    command.add("/usr/bin/ffmpeg");
    command.add("-y");
    command.add("-i");
    command.add(input.toString());
    command.add(output.toString());

    // running the command
    CommandUtility.execute(command);
    return output;
  }

  public static Path runFFMPEGVideoConvert(Path input, String inputFormat, String outputFormat) throws IOException,
    CommandException {

    Path output = Files.createTempFile("result", "." + outputFormat);

    // filling a list of the command line arguments
    List<String> command = new ArrayList<String>();
    command.add("/usr/bin/ffmpeg");
    command.add("-y");
    command.add("-i");
    command.add(input.toString());
    command.add(output.toString());

    // running the command
    CommandUtility.execute(command);
    return output;
  }

}
