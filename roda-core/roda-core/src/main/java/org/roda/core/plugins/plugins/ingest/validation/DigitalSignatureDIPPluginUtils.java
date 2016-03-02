/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.cms.CMSException;
import org.roda.common.certification.SignatureUtility;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalSignatureDIPPluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignatureDIPPluginUtils.class);

  private static final String KEYSTORE_PATH = RodaCoreFactory.getRodaHomePath()
    .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "signature", "keystore", "path")).toString();
  private static final String KEYSTORE_PASSWORD = RodaCoreFactory.getRodaHomePath()
    .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "signature", "keystore", "password")).toString();
  private static final String KEYSTORE_ALIAS = RodaCoreFactory.getRodaHomePath()
    .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "signature", "keystore", "alias")).toString();

  public static void addElementToRepresentationZip(Path input, Path file, int fileSize) {
    try {
      OutputStream os = new FileOutputStream(input.toString());
      ZipOutputStream zout = new ZipOutputStream(os);
      String result = String.join("", file.toString());
      ZipEntry entry = new ZipEntry(result);

      System.out.println("append: " + entry.getName());
      zout.putNextEntry(entry);
      zout.write(fileSize);
      zout.closeEntry();
      zout.close();
      os.close();
    } catch (IOException e) {
      LOGGER.debug("Problems create the representation zip");
    }
  }

  public static Path runDigitalSigner(Path input) {

    try {
      SignatureUtility signatureUtility = new SignatureUtility();
      if (KEYSTORE_PATH != null) {
        InputStream is = new FileInputStream(KEYSTORE_PATH);
        signatureUtility.loadKeyStore(is, KEYSTORE_PASSWORD.toCharArray());
      }
      signatureUtility.initSign(KEYSTORE_ALIAS, KEYSTORE_PASSWORD.toCharArray());

      // Create needed temporary files
      Path zipResult = Files.createTempFile("sign", ".zip");
      Path signatureTempFile = Files.createTempFile("sign", ".p7s");

      signatureUtility.sign(input.toFile(), signatureTempFile.toFile());

      OutputStream os = new FileOutputStream(zipResult.toString());
      ZipOutputStream zout = new ZipOutputStream(os);

      ZipEntry zipEntry = new ZipEntry(input.toString());
      zout.putNextEntry(zipEntry);
      zout.write((int) input.toFile().length());
      zout.closeEntry();

      ZipEntry signEntry = new ZipEntry(signatureTempFile.toString());
      zout.putNextEntry(signEntry);
      zout.write((int) signatureTempFile.toFile().length());
      zout.closeEntry();

      zout.close();
      os.close();
      input.toFile().delete();
      signatureTempFile.toFile().delete();
      IOUtils.closeQuietly(zout);
      IOUtils.closeQuietly(os);

    } catch (CertificateException | IOException e) {
      LOGGER.error("Cannot load keystore " + KEYSTORE_PATH, e);
    } catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException e) {
      LOGGER.error("Error initializing SignatureUtility", e);
    } catch (UnrecoverableKeyException | InvalidAlgorithmParameterException | CertStoreException | CMSException e) {
      LOGGER.error("Error running initSign of SignatureUtility", e);
    }

    return null;
  }

}
