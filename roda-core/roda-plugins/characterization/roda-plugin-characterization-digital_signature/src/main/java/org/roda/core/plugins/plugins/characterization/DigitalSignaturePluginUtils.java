/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.File;
import org.roda.core.model.ModelService;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;

public class DigitalSignaturePluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignaturePluginUtils.class);

  public static String runDigitalSignatureVerify(Path input, String fileFormat, String mimetype) {

    String generalFileFormat = SignatureUtils.canHaveEmbeddedSignature(fileFormat, mimetype);

    try {
      if (generalFileFormat.equals("pdf")) {
        return PDFSignatureUtils.runDigitalSignatureVerify(input);
      } else if (generalFileFormat.equals("ooxml")) {
        return OOXMLSignatureUtils.runDigitalSignatureVerify(input);
      } else if (generalFileFormat.equals("odf")) {
        return ODFSignatureUtils.runDigitalSignatureVerify(input);
      }
    } catch (IOException | GeneralSecurityException e) {
      LOGGER.warn("Problems running digital signature verification, reason: {}", e.getMessage());
    }

    return "Not a supported format";
  }

  public static int runDigitalSignatureExtraction(ModelService model, File file, Path input, String fileFormat,
    String mimetype) {
    List<Path> extractResult = new ArrayList<Path>();

    try {
      String generalFileFormat = SignatureUtils.canHaveEmbeddedSignature(fileFormat, mimetype);
      if (generalFileFormat.equals("pdf")) {
        extractResult = PDFSignatureUtils.runDigitalSignatureExtract(input);

        if (!extractResult.isEmpty()) {
          ContentPayload mainPayload = new FSPathContentPayload(extractResult.get(0));
          ContentPayload contentsPayload = new FSPathContentPayload(extractResult.get(1));

          model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(),
            file.getId().substring(0, file.getId().lastIndexOf('.')), ".xml",
            RodaConstants.OTHER_METADATA_TYPE_DIGITAL_SIGNATURE, mainPayload, true);

          if (extractResult.get(1).toFile().length() > 0) {
            model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(),
              file.getId().substring(0, file.getId().lastIndexOf('.')), ".pkcs7",
              RodaConstants.OTHER_METADATA_TYPE_DIGITAL_SIGNATURE, contentsPayload, true);
          }
        }
      } else if (generalFileFormat.equals("ooxml")) {
        Map<Path, String> extractMap = OOXMLSignatureUtils.runDigitalSignatureExtract(input);
        extractResult = new ArrayList<Path>(extractMap.keySet());

        for (Path p : extractResult) {
          ContentPayload mainPayload = new FSPathContentPayload(p);
          model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(),
            file.getId().substring(0, file.getId().lastIndexOf('.')) + "_" + extractMap.get(p), ".xml",
            RodaConstants.OTHER_METADATA_TYPE_DIGITAL_SIGNATURE, mainPayload, true);
        }
      } else if (generalFileFormat.equals("odf")) {
        extractResult = ODFSignatureUtils.runDigitalSignatureExtract(input);

        if (!extractResult.isEmpty()) {
          ContentPayload mainPayload = new FSPathContentPayload(extractResult.get(0));
          model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(),
            file.getId().substring(0, file.getId().lastIndexOf('.')), ".xml",
            RodaConstants.OTHER_METADATA_TYPE_DIGITAL_SIGNATURE, mainPayload, true);
        }
      }
    } catch (IOException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException | SignatureException e) {
      LOGGER.warn("Problems running a document digital signature extraction, reason: {}", e.getMessage());
    }

    return extractResult.size();
  }

  public static Path runDigitalSignatureStrip(Path input, String fileFormat, String mimetype) {
    try {
      Path output = Files.createTempFile("stripped", "." + fileFormat);

      String generalFileFormat = SignatureUtils.canHaveEmbeddedSignature(fileFormat, mimetype);
      if (generalFileFormat.equals("pdf")) {
        PDFSignatureUtils.runDigitalSignatureStrip(input, output);
      } else if (generalFileFormat.equals("ooxml")) {
        OOXMLSignatureUtils.runDigitalSignatureStrip(input, output);
      } else if (generalFileFormat.equals("odf")) {
        ODFSignatureUtils.runDigitalSignatureStrip(input, output);
      }

      return output;
    } catch (IOException | InvalidFormatException | DocumentException e) {
      LOGGER.warn("Problems running a document signature stripping, reason: {}", e.getMessage());
      return null;
    }
  }

}
