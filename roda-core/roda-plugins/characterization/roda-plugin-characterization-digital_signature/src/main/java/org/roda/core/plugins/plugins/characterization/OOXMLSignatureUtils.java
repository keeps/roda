/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo.SignaturePart;

import com.itextpdf.text.DocumentException;

public final class OOXMLSignatureUtils {

  private static final String SIGN_CONTENT_TYPE_OOXML = "application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml";
  private static final String SIGN_REL_TYPE_OOXML = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/origin";

  /** Private empty constructor */
  private OOXMLSignatureUtils() {

  }

  public static String runDigitalSignatureVerify(Path input) throws IOException, GeneralSecurityException {
    boolean isValid = true;
    try {
      OPCPackage pkg = OPCPackage.open(input.toString(), PackageAccess.READ);
      SignatureConfig sic = new SignatureConfig();
      sic.setOpcPackage(pkg);

      SignatureInfo si = new SignatureInfo();
      si.setSignatureConfig(sic);
      Iterable<SignaturePart> it = si.getSignatureParts();
      if (it != null) {
        for (SignaturePart sp : it) {
          isValid = isValid && sp.validate();

          Set<Certificate> trustedRootCerts = new HashSet<Certificate>();
          Set<Certificate> intermediateCerts = new HashSet<Certificate>();
          List<X509Certificate> certChain = sp.getCertChain();

          for (X509Certificate c : certChain) {
            c.checkValidity();

            if (SignatureUtils.isCertificateSelfSigned(c))
              trustedRootCerts.add(c);
            else
              intermediateCerts.add(c);
          }

          SignatureUtils.verifyCertificateChain(trustedRootCerts, intermediateCerts, certChain.get(0));
        }
      }

      pkg.close();
    } catch (InvalidFormatException e) {
      return "Error opening a document file";
    } catch (CertificateExpiredException e) {
      return "Contains expired certificates";
    } catch (CertificateNotYetValidException e) {
      return "Contains certificates not yet valid";
    }

    return isValid ? "Passed" : "Not passed";
  }

  public static Map<Path, String> runDigitalSignatureExtract(Path input) throws SignatureException, IOException {
    Map<Path, String> paths = new HashMap<Path, String>();

    ZipFile zipFile = new ZipFile(input.toString());
    Enumeration<?> enumeration;
    for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) enumeration.nextElement();
      String entryName = entry.getName();
      if (entryName.startsWith("_xmlsignatures") && entryName.endsWith(".xml")) {
        Path extractedSignature = Files.createTempFile("extraction", ".xml");
        InputStream zipStream = zipFile.getInputStream(entry);
        FileUtils.copyInputStreamToFile(zipStream, extractedSignature.toFile());
        paths.put(extractedSignature, entryName.substring(entryName.lastIndexOf('/') + 1, entryName.lastIndexOf('.')));
        IOUtils.closeQuietly(zipStream);
      }
    }

    zipFile.close();
    return paths;
  }

  public static void runDigitalSignatureStrip(Path input, Path output) throws IOException, InvalidFormatException {

    CopyOption[] copyOptions = new CopyOption[] {StandardCopyOption.REPLACE_EXISTING};
    Files.copy(input, output, copyOptions);
    OPCPackage pkg = OPCPackage.open(output.toString(), PackageAccess.READ_WRITE);

    ArrayList<PackagePart> pps = pkg.getPartsByContentType(SIGN_CONTENT_TYPE_OOXML);
    for (PackagePart pp : pps) {
      pkg.removePart(pp);
    }

    ArrayList<PackagePart> ppct = pkg.getPartsByRelationshipType(SIGN_REL_TYPE_OOXML);
    for (PackagePart pp : ppct) {
      pkg.removePart(pp);
    }

    for (PackageRelationship r : pkg.getRelationships()) {
      if (r.getRelationshipType().equals(SIGN_REL_TYPE_OOXML)) {
        pkg.removeRelationship(r.getId());
      }
    }

    pkg.close();
  }

  public static Path runDigitalSignatureSign(Path input, String keystore, String alias, String password,
    String fileFormat) throws IOException, GeneralSecurityException, DocumentException, InvalidFormatException,
    XMLSignatureException, MarshalException, FileNotFoundException {

    Path output = Files.createTempFile("signed", "." + fileFormat);
    CopyOption[] copyOptions = new CopyOption[] {StandardCopyOption.REPLACE_EXISTING};
    Files.copy(input, output, copyOptions);

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

    InputStream is = new FileInputStream(keystore);
    ks.load(is, password.toCharArray());
    IOUtils.closeQuietly(is);

    PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray());
    X509Certificate x509 = (X509Certificate) ks.getCertificate(alias);

    SignatureConfig signatureConfig = new SignatureConfig();
    signatureConfig.setKey(pk);
    signatureConfig.setSigningCertificateChain(Collections.singletonList(x509));
    OPCPackage pkg = OPCPackage.open(output.toString(), PackageAccess.READ_WRITE);
    signatureConfig.setOpcPackage(pkg);

    SignatureInfo si = new SignatureInfo();
    si.setSignatureConfig(signatureConfig);
    si.confirmSignature();

    // boolean b = si.verifySignature();
    pkg.close();
    return output;
  }
}
