/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.roda.common.certification.SignatureUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.ip.StoragePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;

public class DigitalSignaturePluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignaturePluginUtils.class);

  public static String runDigitalSignatureVerify(Path input, String fileFormat) {

    try {
      if (fileFormat.equals("pdf")) {
        return SignatureUtils.runDigitalSignatureVerifyPDF(input);
      } else if (fileFormat.equals("docx") || fileFormat.equals("xlsx") || fileFormat.equals("pptx")) {
        return SignatureUtils.runDigitalSignatureVerifyOOXML(input);
      } else if (fileFormat.equals("odt") || fileFormat.equals("ods") || fileFormat.equals("odp")) {
        return SignatureUtils.runDigitalSignatureVerifyODF(input);
      }
    } catch (IOException | GeneralSecurityException e) {
      LOGGER.warn("Problems running digital signature verification");
    }

    return "Not a supported format";
  }

  public static Path runDigitalSignatureStrip(Path input, String fileFormat) {
    try {
      Path output = Files.createTempFile("stripped", "." + fileFormat);

      if (fileFormat.equals("pdf")) {
        SignatureUtils.runDigitalSignatureStripPDF(input, output);
      } else if (fileFormat.equals("docx") || fileFormat.equals("xlsx") || fileFormat.equals("pptx")) {
        SignatureUtils.runDigitalSignatureStripOOXML(input, output);
      } else if (fileFormat.equals("odt") || fileFormat.equals("ods") || fileFormat.equals("odp")) {
        SignatureUtils.runDigitalSignatureStripODF(input, output);
      }

      return output;
    } catch (IOException | InvalidFormatException | DocumentException e) {
      LOGGER.warn("Problems running a document stripping");
      return null;
    }
  }

  public static int countSignaturesPDF(Path base, StoragePath input, String intermediatePath) {
    int counter = -1;
    try {
      PdfReader reader = new PdfReader(base.toString() + intermediatePath + input.toString());
      AcroFields af = reader.getAcroFields();
      ArrayList<String> names = af.getSignatureNames();
      counter = names.size();
    } catch (IOException e) {
      LOGGER.error("Error getting path of file " + e.getMessage());
    }
    return counter;
  }

  /*********************** FILLING FILE FORMAT STRUCTURES FUNCTIONS ***********************/

  public static Map<String, List<String>> getPronomToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    String inputFormatPronoms = RodaCoreFactory.getRodaConfigurationAsString("tools", "digitalsignature",
      "inputFormatPronoms");

    for (String pronom : Arrays.asList(inputFormatPronoms.split(" "))) {
      // TODO add missing pronoms
      String pronomExtensions = RodaCoreFactory.getRodaConfigurationAsString("tools", "pronom", pronom);
      map.put(pronom, Arrays.asList(pronomExtensions.split(" ")));
    }

    return map;
  }

  public static Map<String, List<String>> getMimetypeToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    String inputFormatMimetypes = RodaCoreFactory.getRodaConfigurationAsString("tools", "digitalsignature",
      "inputFormatMimetypes");

    for (String mimetype : Arrays.asList(inputFormatMimetypes.split(" "))) {
      // TODO add missing mimetypes
      String mimeExtensions = RodaCoreFactory.getRodaConfigurationAsString("tools", "mimetype", mimetype);
      map.put(mimetype, Arrays.asList(mimeExtensions.split(" ")));
    }

    return map;
  }

  public static List<String> getInputExtensions() {
    // TODO add missing extensions
    String inputFormatExtensions = RodaCoreFactory.getRodaConfigurationAsString("tools", "digitalsignature",
      "inputFormatExtensions");
    return Arrays.asList(inputFormatExtensions.split(" "));
  }

}
