package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class FfmpegConvertPluginUtils {

  public static Path runFfmpegVideoConvert(InputStream is, String inputFormat, String outputFormat,
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
    return executeFfmpeg(input, output, commandArguments);
  }

  public static Path runFfmpegVideoConvert(Path input, String inputFormat, String outputFormat, String commandArguments)
    throws IOException, CommandException {

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeFfmpeg(input, output, commandArguments);
  }

  private static Path executeFfmpeg(Path input, Path output, String commandArguments) throws CommandException,
    IOException, UnsupportedOperationException {

    String command = RodaCoreFactory.getRodaConfigurationAsString("tools", "ffmpegconvert", "commandLine");
    command = command.replace("{input_file}", input.toString());
    command = command.replace("{output_file}", output.toString());
    command = command.replace("{arguments}", commandArguments);

    // filling a list of the command line arguments
    List<String> commandList = Arrays.asList(command.split("\\s+"));

    // running the command
    CommandUtility.execute(commandList);
    return output;
  }

  public static String getVersion() throws CommandException, IOException, UnsupportedOperationException {
    String version = CommandUtility.execute("ffmpeg", "-version");
    version = version.replace("Copyright", "?");
    if (version.indexOf('\n') > 0) {
      version = version.substring(0, version.indexOf('?'));
    }
    return version.trim();
  }

  public static Map<String, List<String>> getPronomToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    // TODO add missing pronoms
    map.put("fmt/573", new ArrayList<String>(Arrays.asList("webm")));
    map.put("fmt/203", new ArrayList<String>(Arrays.asList("ogg")));
    map.put("fmt/596", new ArrayList<String>(Arrays.asList("mp4", "m4a")));
    map.put("fmt/199", new ArrayList<String>(Arrays.asList("mp4", "m4a")));
    map.put("fmt/3", new ArrayList<String>(Arrays.asList("gif")));
    map.put("fmt/4", new ArrayList<String>(Arrays.asList("gif")));
    map.put("fmt/5", new ArrayList<String>(Arrays.asList("avi")));
    return map;
  }

  public static Map<String, List<String>> getMimetypeToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    // TODO add missing mimetypes
    map.put("video/webm", new ArrayList<String>(Arrays.asList("webm")));
    map.put("audio/ogg", new ArrayList<String>(Arrays.asList("ogg")));
    map.put("video/mp4", new ArrayList<String>(Arrays.asList("mp4", "m4v", "m4a", "f4v", "f4a")));
    map.put("application/mp4", new ArrayList<String>(Arrays.asList("mp4", "m4v", "m4a", "f4v", "f4a")));
    map.put("audio/x-wav", new ArrayList<String>(Arrays.asList("wav")));
    map.put("image/gif", new ArrayList<String>(Arrays.asList("gif")));
    map.put("video/x-msvideo", new ArrayList<String>(Arrays.asList("avi")));
    map.put("video/x-msvideo", new ArrayList<String>(Arrays.asList("avi")));
    return map;
  }

  public static List<String> getInputExtensions() {
    // TODO add missing extensions
    return Arrays.asList("webm", "ogg", "opus", "mp4", "wav", "gif", "avi", "3g2");
  }
}
