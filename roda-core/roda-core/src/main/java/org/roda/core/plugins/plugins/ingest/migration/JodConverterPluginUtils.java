package org.roda.core.plugins.plugins.ingest.migration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

public class JodConverterPluginUtils {

  public static Path runJodConverter(InputStream is, String inputFormat, String outputFormat, String conversionProfile)
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
    return executeJodConverter(input, output, inputFormat, outputFormat, conversionProfile);
  }

  public static Path runJodConverter(Path input, String inputFormat, String outputFormat, String conversionProfile)
    throws IOException, CommandException {

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeJodConverter(input, output, inputFormat, outputFormat, conversionProfile);
  }

  private static Path executeJodConverter(Path input, Path output, String inputFormat, String outputFormat,
    String conversionProfile) throws ConnectException {

    File inputFile = new File(input.toString());
    File outputFile = new File(output.toString());

    String port = RodaCoreFactory.getRodaConfigurationAsString("tools", "jodconverter", conversionProfile,
      "openOfficePort");
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
}
