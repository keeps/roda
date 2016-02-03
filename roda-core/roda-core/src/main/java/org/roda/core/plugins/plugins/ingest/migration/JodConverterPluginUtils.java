package org.roda.core.plugins.plugins.ingest.migration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

public class JodConverterPluginUtils {

  public static Path runJodConverter(InputStream is, String inputFormat, String outputFormat) throws IOException,
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
    return executeJodConverter(input, output, inputFormat, outputFormat);
  }

  public static Path runJodConverter(Path input, String inputFormat, String outputFormat) throws IOException,
    CommandException {

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeJodConverter(input, output, inputFormat, outputFormat);
  }

  private static Path executeJodConverter(Path input, Path output, String inputFormat, String outputFormat)
    throws ConnectException {

    File inputFile = new File(input.toString());
    File outputFile = new File(output.toString());

    String port = RodaCoreFactory.getRodaConfigurationAsString("tools", "jodconverter", "openOfficePort");
    int openOfficePort = Integer.parseInt(port);

    OpenOfficeConnection connection = new SocketOpenOfficeConnection(openOfficePort);
    connection.connect();

    // convert
    DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
    converter.convert(inputFile, outputFile);

    // close the connection
    connection.disconnect();

    return output;
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
    return map;
  }

  public static List<String> getInputExtensions() {
    // TODO add missing extensions
    return Arrays.asList("pdf", "txt", "doc", "xls", "ppt", "html", "odt", "ods", "odp");
  }
}
