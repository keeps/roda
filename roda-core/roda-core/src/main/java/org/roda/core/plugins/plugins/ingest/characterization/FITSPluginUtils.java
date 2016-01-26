/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.roda.core.RodaCoreFactory;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.exceptions.FitsException;

public class FITSPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(FITSPluginUtils.class);

  public static Path inspect(File f) throws FitsException {
    try {
      List<String> command = getCommand();
      command.add(f.getAbsolutePath());
      String fitsOutput = CommandUtility.execute(command);
      fitsOutput = fitsOutput.substring(fitsOutput.indexOf("<?xml"));
      FitsOutput output = new FitsOutput(fitsOutput);
      Path p = Files.createTempFile("fits", ".xml");

      new XMLOutputter().output(output.getFitsXml(), new FileOutputStream(p.toFile()));
      return p;
    } catch (CommandException e) {
      throw new FitsException("Error while executing FITS command");
    } catch (JDOMException e) {
      throw new FitsException("Error while parsing FITS output");
    } catch (IOException e) {
      throw new FitsException("Error while parsing FITS output");
    }
  }

  private static List<String> getCommand() {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path fitsHome = rodaHome.resolve(RodaCoreFactory.getRodaConfigurationAsString("tools", "fits", "home"));

    File FITS_DIRECTORY = fitsHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(
        Arrays.asList(FITS_DIRECTORY.getAbsolutePath() + File.separator + "fits.bat", "-i"));
    } else {
      command = new ArrayList<String>(
        Arrays.asList(FITS_DIRECTORY.getAbsolutePath() + File.separator + "fits.sh", "-i"));
    }
    return command;
  }

  public static Path runFits(org.roda.core.data.v2.ip.File file, Binary binary, Map<String, String> parameterValues)
    throws IOException, FitsException {
    java.io.File f = File.createTempFile("temp", ".temp");
    FileOutputStream fos = new FileOutputStream(f);
    IOUtils.copy(binary.getContent().createInputStream(), fos);
    fos.close();
    return inspect(f);
  }

  private static List<String> getBatchCommand(Path sourceDirectory, Path outputDirectory) {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path fitsHome = rodaHome.resolve(RodaCoreFactory.getRodaConfigurationAsString("tools", "fits", "home"));

    File FITS_DIRECTORY = fitsHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(Arrays.asList(FITS_DIRECTORY.getAbsolutePath() + File.separator + "fits.bat",
        "-o", outputDirectory.toFile().getAbsolutePath(), "-i"));
    } else {
      command = new ArrayList<String>(Arrays.asList(FITS_DIRECTORY.getAbsolutePath() + File.separator + "fits.sh", "-o",
        outputDirectory.toFile().getAbsolutePath(), "-i"));
    }
    command.add(sourceDirectory.toFile().getAbsolutePath());
    return command;
  }

  public static String runFITSOnPath(Path sourceDirectory, Path outputDirectory) throws PluginException {
    try {
      List<String> command = getBatchCommand(sourceDirectory, outputDirectory);
      String fitsOutput = CommandUtility.execute(command);
      return fitsOutput;
    } catch (CommandException e) {
      throw new PluginException("Error while executing FITS command");
    }
  }
}
