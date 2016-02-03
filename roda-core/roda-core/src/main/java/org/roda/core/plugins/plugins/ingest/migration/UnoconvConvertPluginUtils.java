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

public class UnoconvConvertPluginUtils {

  public static Path runUnoconvConvert(InputStream is, String inputFormat, String outputFormat, String commandArguments)
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
    return executeUnoconvConvert(input, output, inputFormat, outputFormat, commandArguments);
  }

  public static Path runUnoconvConvert(Path input, String inputFormat, String outputFormat, String commandArguments)
    throws IOException, CommandException {

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeUnoconvConvert(input, output, inputFormat, outputFormat, commandArguments);
  }

  private static Path executeUnoconvConvert(Path input, Path output, String inputFormat, String outputFormat,
    String commandArguments) throws CommandException, IOException, UnsupportedOperationException {

    String command = RodaCoreFactory.getRodaConfigurationAsString("tools", "unoconvconvert", "commandLine");
    command = command.replace("{input_file}", input.toString());
    command = command.replace("{output_file}", output.toString());
    command = command.replace("{output_format}", outputFormat);
    command = command.replace("{arguments}", commandArguments);

    // filling a list of the command line arguments
    List<String> commandList = Arrays.asList(command.split("\\s+"));

    // running the command
    CommandUtility.execute(commandList);
    return output;
  }

  public static String getVersion() throws CommandException, IOException, UnsupportedOperationException {
    String version = CommandUtility.execute("soffice", "-h");
    if (version.indexOf('\n') > 0) {
      version = version.substring(0, version.indexOf('\n'));
    }

    version = version.replaceAll("(LibreOffice\\s[0-9.]+).*", "$1");
    return version.trim();
  }

  public static Map<String, List<String>> getPronomToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    // TODO add missing pronoms
    map.put("x-fmt/111", new ArrayList<String>(Arrays.asList("txt")));
    map.put("x-fmt/281", new ArrayList<String>(Arrays.asList("xls")));
    return map;
  }

  public static Map<String, List<String>> getMimetypeToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    // TODO add missing mimetypes
    map.put("application/pdf", new ArrayList<String>(Arrays.asList("pdf")));
    map.put("text/plain", new ArrayList<String>(Arrays.asList("txt")));
    map.put("application/msword", new ArrayList<String>(Arrays.asList("doc")));
    map.put("application/vnd.ms-excel", new ArrayList<String>(Arrays.asList("xls")));
    map.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      new ArrayList<String>(Arrays.asList("xls")));
    map.put("application/vnd.ms-powerpoint", new ArrayList<String>(Arrays.asList("ppt")));
    map.put("application/vnd.openxmlformats-officedocument.presentationml.presentation",
      new ArrayList<String>(Arrays.asList("ppt")));
    map.put("text/html", new ArrayList<String>(Arrays.asList("html")));
    map.put("application/vnd.oasis.opendocument.text", new ArrayList<String>(Arrays.asList("odt")));
    map.put("application/vnd.oasis.opendocument.spreadsheet", new ArrayList<String>(Arrays.asList("ods")));
    map.put("application/vnd.oasis.opendocument.presentation", new ArrayList<String>(Arrays.asList("odp")));
    map.put("application/vnd.oasis.opendocument.graphics", new ArrayList<String>(Arrays.asList("odg")));
    map.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      new ArrayList<String>(Arrays.asList("docx")));
    map.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      new ArrayList<String>(Arrays.asList("xlsx")));
    map.put("application/vnd.openxmlformats-officedocument.presentationml.presentation",
      new ArrayList<String>(Arrays.asList("pptx")));
    map.put("image/tiff", new ArrayList<String>(Arrays.asList("tif", "tiff")));
    map.put("application/rtf", new ArrayList<String>(Arrays.asList("rtf")));
    map.put("text/rtf", new ArrayList<String>(Arrays.asList("rtf")));
    map.put("text/csv", new ArrayList<String>(Arrays.asList("csv")));
    return map;
  }

  public static List<String> getInputExtensions() {
    // TODO add missing extensions
    return Arrays.asList("pdf", "txt", "doc", "xls", "ppt", "html", "odt", "ods", "odp", "odg", "docx", "xlsx", "pptx",
      "tif", "rtf", "csv");
  }
}
