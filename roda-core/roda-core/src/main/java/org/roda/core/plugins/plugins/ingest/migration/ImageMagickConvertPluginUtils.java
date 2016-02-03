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
    throws IOException, CommandException, UnsupportedOperationException {
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

  public static String getVersion() throws CommandException, IOException, UnsupportedOperationException {
    String version = CommandUtility.execute("convert", "--version");
    if (version.indexOf('\n') > 0) {
      version = version.substring(0, version.indexOf('\n'));
    }

    version = version.replaceAll("Version:\\s+([a-zA-Z]+\\s+[a-zA-Z0-9.-]+).*", "$1");
    return version.trim();
  }

  /*************************** FILLING FILE FORMAT STRUCTURES ***************************/

  public static Map<String, List<String>> getPronomToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    // TODO add missing pronoms
    map.put("fmt/11", new ArrayList<String>(Arrays.asList("png")));
    map.put("fmt/12", new ArrayList<String>(Arrays.asList("png")));
    map.put("fmt/13", new ArrayList<String>(Arrays.asList("png")));
    map.put("fmt/3", new ArrayList<String>(Arrays.asList("gif")));
    map.put("fmt/4", new ArrayList<String>(Arrays.asList("gif")));
    map.put("fmt/152", new ArrayList<String>(Arrays.asList("tiff", "tif", "dng")));
    map.put("fmt/155", new ArrayList<String>(Arrays.asList("tiff", "tif")));
    map.put("fmt/353", new ArrayList<String>(Arrays.asList("tiff", "tif")));
    map.put("fmt/154", new ArrayList<String>(Arrays.asList("tiff", "tif")));
    map.put("fmt/153", new ArrayList<String>(Arrays.asList("tiff", "tif")));
    map.put("fmt/156", new ArrayList<String>(Arrays.asList("tiff", "tif", "tfx")));
    map.put("x-fmt/399", new ArrayList<String>(Arrays.asList("tif")));
    map.put("x-fmt/388", new ArrayList<String>(Arrays.asList("tif")));
    map.put("x-fmt/387", new ArrayList<String>(Arrays.asList("tif")));
    map.put("fmt/41", new ArrayList<String>(Arrays.asList("jpeg", "jpg", "jpe")));
    map.put("fmt/42", new ArrayList<String>(Arrays.asList("jpeg", "jpg", "jpe")));
    map.put("x-fmt/398", new ArrayList<String>(Arrays.asList("jpg")));
    map.put("x-fmt/390", new ArrayList<String>(Arrays.asList("jpg")));
    map.put("x-fmt/391", new ArrayList<String>(Arrays.asList("jpg")));
    map.put("fmt/645", new ArrayList<String>(Arrays.asList("jpg")));
    map.put("fmt/43", new ArrayList<String>(Arrays.asList("jpeg", "jpg", "jpe")));
    map.put("fmt/44", new ArrayList<String>(Arrays.asList("jpeg", "jpg", "jpe")));
    map.put("fmt/112", new ArrayList<String>(Arrays.asList("spf", "jpg")));
    map.put("fmt/91", new ArrayList<String>(Arrays.asList("svg")));
    map.put("fmt/92", new ArrayList<String>(Arrays.asList("svg")));
    map.put("fmt/413", new ArrayList<String>(Arrays.asList("svg")));
    map.put("x-fmt/109", new ArrayList<String>(Arrays.asList("svgz")));
    return map;
  }

  public static Map<String, List<String>> getMimetypeToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    // TODO add missing mimetypes
    map.put("image/png", new ArrayList<String>(Arrays.asList("png")));
    map.put("image/gif", new ArrayList<String>(Arrays.asList("gif")));
    map.put("image/tiff", new ArrayList<String>(Arrays.asList("tiff", "tif", "dng")));
    map.put("image/jpeg", new ArrayList<String>(Arrays.asList("jpg", "jpeg", "jpe", "spf")));
    map.put("image/svg+xml", new ArrayList<String>(Arrays.asList("svg", "svgz")));
    return map;
  }

  public static List<String> getInputExtensions() {
    // TODO add missing extensions
    return Arrays.asList("png", "gif", "tiff", "tif", "jpg", "jpeg", "svg");
  }
}
