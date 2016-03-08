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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.roda.common.certification.ODFSignatureUtils;
import org.roda.common.certification.OOXMLSignatureUtils;
import org.roda.common.certification.PDFSignatureUtils;
import org.roda.core.data.v2.ip.StoragePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;

public class DigitalSignaturePluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignaturePluginUtils.class);

  public static String runDigitalSignatureVerify(Path input, String fileFormat, String mimetype) {

    try {
      String generalFileFormat = canHaveEmbeddedSignature(fileFormat, mimetype);
      if (generalFileFormat.equals("pdf")) {
        return PDFSignatureUtils.runDigitalSignatureVerify(input);
      } else if (generalFileFormat.equals("ooxml")) {
        return OOXMLSignatureUtils.runDigitalSignatureVerify(input);
      } else if (generalFileFormat.equals("odf")) {
        return ODFSignatureUtils.runDigitalSignatureVerify(input);
      }
    } catch (IOException | GeneralSecurityException e) {
      LOGGER.warn("Problems running digital signature verification");
    }

    return "Not a supported format";
  }

  public static Path runDigitalSignatureStrip(Path input, String fileFormat, String mimetype) {
    try {
      Path output = Files.createTempFile("stripped", "." + fileFormat);

      String generalFileFormat = canHaveEmbeddedSignature(fileFormat, mimetype);
      if (generalFileFormat.equals("pdf")) {
        PDFSignatureUtils.runDigitalSignatureStrip(input, output);
      } else if (generalFileFormat.equals("ooxml")) {
        OOXMLSignatureUtils.runDigitalSignatureStrip(input, output);
      } else if (generalFileFormat.equals("odf")) {
        ODFSignatureUtils.runDigitalSignatureStrip(input, output);
      }

      return output;
    } catch (IOException | InvalidFormatException | DocumentException e) {
      LOGGER.warn("Problems running a document stripping");
      return null;
    }
  }

  public static String canHaveEmbeddedSignature(String fileFormat, String mimetype) {
    if (fileFormat.equals("pdf") || mimetype.equals("application/pdf")) {
      return "pdf";
    } else if (fileFormat.equals("docx") || fileFormat.equals("xlsx") || fileFormat.equals("pptx")
      || mimetype.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
      || mimetype.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      || mimetype.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
      return "ooxml";
    } else if (fileFormat.equals("odt") || fileFormat.equals("ods") || fileFormat.equals("odp")
      || mimetype.equals("application/vnd.oasis.opendocument.text")
      || mimetype.equals("application/vnd.oasis.opendocument.spreadsheet")
      || mimetype.equals("application/vnd.oasis.opendocument.presentation")) {
      return "odf";
    }

    return "";
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

}
