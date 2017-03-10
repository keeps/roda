/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.bouncycastle.cms.CMSException;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;

public class DigitalSignatureDIPPluginUtils {

  private DigitalSignatureDIPPluginUtils() {

  }

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignatureDIPPluginUtils.class);

  private static String KEYSTORE_PATH = RodaCoreFactory.getRodaHomePath()
    .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "signature", "keystore", "path")).toString();
  private static final String KEYSTORE_PASSWORD = RodaCoreFactory.getRodaConfigurationAsString("core", "signature",
    "keystore", "password");
  private static final String KEYSTORE_ALIAS = RodaCoreFactory.getRodaConfigurationAsString("core", "signature",
    "keystore", "alias");

  public static void setKeystorePath(String path) {
    KEYSTORE_PATH = path;
  }

  public static void addDetachedSignature(Path input) throws IOException, GeneralSecurityException, CMSException {
    SignatureUtility signatureUtility = new SignatureUtility();
    if (KEYSTORE_PATH != null) {
      try (InputStream is = new FileInputStream(KEYSTORE_PATH)) {
        signatureUtility.loadKeyStore(is, KEYSTORE_PASSWORD.toCharArray());
      }
    }
    signatureUtility.initSign(KEYSTORE_ALIAS, KEYSTORE_PASSWORD.toCharArray());

    File inputFile = input.toFile();
    signatureUtility.sign(inputFile, input.getParent().resolve(inputFile.getName() + ".p7s").toFile());
  }

  public static Path addEmbeddedSignature(Path input, String fileFormat, String mimetype) throws IOException,
    GeneralSecurityException, InvalidFormatException, XMLSignatureException, MarshalException, DocumentException {
    String generalFileFormat = SignatureUtils.canHaveEmbeddedSignature(fileFormat, mimetype);
    if (DigitalSignaturePluginUtils.PDF_FORMAT.equals(generalFileFormat)) {
      String reason = RodaCoreFactory.getRodaConfigurationAsString("core", "signature", "reason");
      String location = RodaCoreFactory.getRodaConfigurationAsString("core", "signature", "location");
      String contact = RodaCoreFactory.getRodaConfigurationAsString("core", "signature", "contact");
      return PDFSignatureUtils.runDigitalSignatureSign(input, KEYSTORE_PATH, KEYSTORE_ALIAS, KEYSTORE_PASSWORD, reason,
        location, contact);
    } else if (DigitalSignaturePluginUtils.OOXML_FORMAT.equals(generalFileFormat)) {
      return OOXMLSignatureUtils.runDigitalSignatureSign(input, KEYSTORE_PATH, KEYSTORE_ALIAS, KEYSTORE_PASSWORD,
        fileFormat);
    } else if (DigitalSignaturePluginUtils.ODF_FORMAT.equals(generalFileFormat)) {
      return ODFSignatureUtils.runDigitalSignatureSign(input, KEYSTORE_PATH, KEYSTORE_ALIAS, KEYSTORE_PASSWORD,
        fileFormat);
    }

    return null;
  }

  /**** Digital signing with zip files ****/

  public static void addElementToRepresentationZip(ZipOutputStream zout, Path file, String name) throws IOException {
    ZipEntry entry = new ZipEntry(name);
    try (InputStream in = new FileInputStream(file.toString())) {
      zout.putNextEntry(entry);
      byte[] data = IOUtils.toByteArray(in);
      zout.write(data);
      zout.closeEntry();
    }
  }

  public static Path runZipDigitalSigner(Path input) {

    try {
      SignatureUtility signatureUtility = new SignatureUtility();
      if (KEYSTORE_PATH != null) {
        try (InputStream is = new FileInputStream(KEYSTORE_PATH)) {
          signatureUtility.loadKeyStore(is, KEYSTORE_PASSWORD.toCharArray());
        }
      }
      signatureUtility.initSign(KEYSTORE_ALIAS, KEYSTORE_PASSWORD.toCharArray());

      Path signatureTempFile = Files.createTempFile("signature_", ".p7s");
      signatureUtility.sign(input.toFile(), signatureTempFile.toFile());

      Path zipResult = Files.createTempFile("signed_", ".zip");
      OutputStream os = new FileOutputStream(zipResult.toString());
      try (ZipOutputStream zout = new ZipOutputStream(os)) {

        // add representation zip
        ZipEntry zipEntry = new ZipEntry(input.toFile().getName());
        try (InputStream in = new FileInputStream(input.toString())) {
          zout.putNextEntry(zipEntry);
          byte[] data = IOUtils.toByteArray(in);
          zout.write(data);
          zout.closeEntry();
        }

        // add signature
        ZipEntry zipEntry2 = new ZipEntry(signatureTempFile.toFile().getName());
        try (InputStream in2 = new FileInputStream(signatureTempFile.toString())) {
          zout.putNextEntry(zipEntry2);
          byte[] data2 = IOUtils.toByteArray(in2);
          zout.write(data2);
          zout.closeEntry();
        }

        zout.finish();
      }

      input.toFile().delete();
      signatureTempFile.toFile().delete();
      return zipResult;
    } catch (CertificateException | IOException e) {
      LOGGER.error("Cannot load keystore " + KEYSTORE_PATH, e);
    } catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException e) {
      LOGGER.error("Error initializing SignatureUtility", e);
    } catch (UnrecoverableKeyException | CMSException | InvalidAlgorithmParameterException e) {
      LOGGER.error("Error running initSign of SignatureUtility", e);
    } catch (CertStoreException e) {
      LOGGER.error("Error retrieving certificate from store", e);
    }

    return null;
  }
}
