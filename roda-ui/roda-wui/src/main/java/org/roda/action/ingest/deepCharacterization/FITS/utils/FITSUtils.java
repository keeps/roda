package org.roda.action.ingest.deepCharacterization.FITS.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.roda.common.RodaCoreFactory;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.storage.Binary;
import org.roda.util.CommandException;
import org.roda.util.CommandUtility;

import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.exceptions.FitsException;

public class FITSUtils {
  static final private Logger logger = Logger.getLogger(FITSUtils.class);

  public static String inspect(File f) throws FitsException {
    try {
      List<String> command = getCommand();
      command.add(f.getAbsolutePath());
      String fitsOutput = CommandUtility.execute(command);
      fitsOutput = fitsOutput.substring(fitsOutput.indexOf("<?xml"));
      FitsOutput output = new FitsOutput(fitsOutput);
      return new XMLOutputter().outputString(output.getFitsXml());
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
      command = new ArrayList<String>(Arrays.asList(FITS_DIRECTORY.getAbsolutePath() + File.separator + "fits.bat", "-i"));
    } else {
      command = new ArrayList<String>(Arrays.asList(FITS_DIRECTORY.getAbsolutePath() + File.separator + "fits.sh", "-i"));
    }
    return command;
  }

  public static RepresentationFilePreservationObject deepCharacterization(
    RepresentationFilePreservationObject premisObject, org.roda.model.File file, Binary binary,
    Map<String, String> parameterValues) throws IOException, FitsException {
    java.io.File f = File.createTempFile("temp", ".temp");
    FileOutputStream fos = new FileOutputStream(f);
    IOUtils.copy(binary.getContent().createInputStream(), fos);
    fos.close();
    String fitsOutput = inspect(f);
    premisObject.setObjectCharacteristicsExtension(fitsOutput);
    return premisObject;
  }

}
