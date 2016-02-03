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

public class SoxConvertPluginUtils {

  public static Path runSoxSoundConvert(InputStream is, String inputFormat, String outputFormat, String commandArguments)
    throws IOException, CommandException {

    // write the inputstream data on a new file (absolute path needed)
    Path input = Files.createTempFile("copy", "." + inputFormat);
    byte[] buffer = new byte[is.available()];
    is.read(buffer);
    OutputStream os = new FileOutputStream(input.toFile());
    os.write(buffer);
    os.close();
    is.close();

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeSox(input, output, commandArguments);
  }

  public static Path runSoxSoundConvert(Path input, String inputFormat, String outputFormat, String commandArguments)
    throws IOException, CommandException {

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeSox(input, output, commandArguments);
  }

  private static Path executeSox(Path input, Path output, String commandArguments) throws CommandException,
    IOException, UnsupportedOperationException {

    String command = RodaCoreFactory.getRodaConfigurationAsString("tools", "soxconvert", "commandLine");
    command = command.replace("{input_file}", input.toString());
    command = command.replace("{output_file}", output.toString());

    if (commandArguments.length() > 0) {
      command = command.replace("{arguments}", commandArguments);
    } else {
      command = command.replace("{arguments}", "-e gsm-full-rate");
    }

    // filling a list of the command line arguments
    List<String> commandList = Arrays.asList(command.split("\\s+"));

    // running the command
    CommandUtility.execute(commandList);
    return output;
  }

  public static String getVersion() throws CommandException, IOException, UnsupportedOperationException {
    String version = CommandUtility.execute("sox", "--version");
    if (version.indexOf('\n') > 0) {
      version = version.replace(" ", "");
      version = version.substring(0, version.indexOf('\n'));
    }
    version = version.replaceAll("sox:SoXv([0-9.])", "SoX v$1");
    return version.trim();
  }

  public static Map<String, List<String>> getPronomToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    map.put("fmt/134", new ArrayList<String>(Arrays.asList("mp3")));
    map.put("fmt/199", new ArrayList<String>(Arrays.asList("mp4")));
    map.put("fmt/203", new ArrayList<String>(Arrays.asList("ogg")));
    map.put("fmt/414", new ArrayList<String>(Arrays.asList("aif", "aiff")));
    map.put("x-fmt/139", new ArrayList<String>(Arrays.asList("au")));
    return map;
  }

  public static Map<String, List<String>> getMimetypeToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    map.put("audio/mpeg", new ArrayList<String>(Arrays.asList("mp3")));
    map.put("application/mp4", new ArrayList<String>(Arrays.asList("mp4")));
    map.put("audio/ogg", new ArrayList<String>(Arrays.asList("ogg")));
    map.put("audio/x-wav", new ArrayList<String>(Arrays.asList("wav")));
    map.put("audio/basic", new ArrayList<String>(Arrays.asList("au")));
    return map;
  }

  public static List<String> getInputExtensions() {
    return Arrays.asList("mp3", "aac", "ogg", "opus", "mp4", "wav", "aif", "aiff", "au");
  }
}