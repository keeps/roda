/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.common.certification;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

public class PDFSignatureUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PDFSignatureUtils.class);
  private static final String PDF_STRING = "String";
  private static final String PDF_OBJECT = "Object";
  private static final String PDF_BOOLEAN = "Boolean";
  private static final String SIGNATURE_REASON = "test reason";
  private static final String SIGNATURE_LOCATION = "test location";

  public static String runDigitalSignatureVerify(Path input) throws IOException, GeneralSecurityException {
    Security.addProvider(new BouncyCastleProvider());

    PdfReader reader = new PdfReader(input.toString());
    AcroFields fields = reader.getAcroFields();
    ArrayList<String> names = fields.getSignatureNames();
    String result = "Passed";

    for (int i = 0; i < names.size(); i++) {
      String name = (String) names.get(i);

      try {
        PdfPKCS7 pk = fields.verifySignature(name);
        X509Certificate cert = pk.getSigningCertificate();
        cert.checkValidity();

        if (!SignatureUtils.isCertificateSelfSigned(cert)) {

          Set<Certificate> trustedRootCerts = new HashSet<Certificate>();
          Set<Certificate> intermediateCerts = new HashSet<Certificate>();

          for (Certificate c : pk.getSignCertificateChain()) {
            X509Certificate x509c = (X509Certificate) c;
            x509c.checkValidity();

            if (SignatureUtils.isCertificateSelfSigned(c))
              trustedRootCerts.add(c);
            else
              intermediateCerts.add(c);
          }

          SignatureUtils.verifyCertificateChain(trustedRootCerts, intermediateCerts, cert);
          if (pk.getCRLs() != null) {
            for (CRL crl : pk.getCRLs()) {
              if (crl.isRevoked(cert)) {
                result = "Signing certificate is included on a Certificate Revocation List (CRL)";
              }
            }
          }
        }
      } catch (NoSuchFieldError e) {
        result = "Missing signature timestamp field";
      } catch (CertificateExpiredException | CertificateNotYetValidException e) {
        result = "Signing certificate does not pass the validity check";
      }
    }

    reader.close();
    return result;
  }

  public static List<Path> runDigitalSignatureExtract(Path input) throws SignatureException, IOException {
    Security.addProvider(new BouncyCastleProvider());

    List<Path> paths = new ArrayList<Path>();
    Path output = Files.createTempFile("extraction", ".txt");
    Path outputContents = null;
    PdfReader reader = new PdfReader(input.toString());
    AcroFields fields = reader.getAcroFields();
    ArrayList<?> names = fields.getSignatureNames();
    String filename = input.getFileName().toString();
    filename = filename.substring(0, filename.lastIndexOf('.'));

    if (names.size() == 0)
      return paths;

    FileOutputStream fos = new FileOutputStream(output.toString());
    OutputStreamWriter osw = new OutputStreamWriter(fos);
    PrintWriter out = new PrintWriter(osw, true);

    for (int i = 0; i < names.size(); i++) {
      String name = (String) names.get(i);
      Item item = fields.getFieldItem(name);
      String extractionResult = "";

      PdfDictionary widget = item.getWidget(0);
      PdfDictionary infoDictionary = widget.getAsDict(PdfName.V);

      try {
        PdfPKCS7 pk = fields.verifySignature(name);
        extractionResult += "Name: " + name + "\n";
        extractionResult += "Sign Name: " + pk.getSignName() + "\n";
        extractionResult += "Version: " + pk.getVersion() + "\n";
        extractionResult += "Reason: " + pk.getReason() + "\n";
        extractionResult += "Location: " + pk.getLocation() + "\n";

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        if (pk.getTimeStampDate() != null)
          extractionResult += "Timestamp Time: " + formatter.format(pk.getTimeStampDate().getTime()) + "\n";

        if (pk.getSignDate() != null)
          extractionResult += "Sign Time: " + formatter.format(pk.getSignDate().getTime()) + "\n";

        extractionResult += "Digest Algorithm: " + pk.getDigestAlgorithm() + "\n";
        extractionResult += "Hash Algorithm: " + pk.getHashAlgorithm() + "\n";
        extractionResult += "CoversWholeDocument: " + fields.signatureCoversWholeDocument(name) + "\n";
        extractionResult += addElementToExtractionResult(infoDictionary, PdfName.CONTACTINFO, PDF_STRING);
        extractionResult += addElementToExtractionResult(infoDictionary, PdfName.FILTER, PDF_OBJECT);
        extractionResult += addElementToExtractionResult(infoDictionary, PdfName.SUBFILTER, PDF_OBJECT);
        extractionResult += addElementToExtractionResult(widget, PdfName.FT, PDF_OBJECT);
        extractionResult += addElementToExtractionResult(widget, PdfName.LOCK, PDF_BOOLEAN);

        if (infoDictionary.contains(PdfName.CONTENTS)) {
          PdfString elementName = infoDictionary.getAsString(PdfName.CONTENTS);
          outputContents = Files.createTempFile("contents", ".pkcs7");
          Files.write(outputContents, elementName.toUnicodeString().getBytes());
          extractionResult += PdfName.CONTENTS.toString() + ": " + filename + ".pkcs7 \n";
        }

      } catch (NoSuchFieldError e) {
        LOGGER.warn("DS information extraction did not execute properly");
      }

      out.println("- Signature " + name + ":\n");
      out.println(extractionResult);
    }

    IOUtils.closeQuietly(out);
    IOUtils.closeQuietly(osw);
    IOUtils.closeQuietly(fos);
    reader.close();

    paths.add(output);
    paths.add(outputContents);
    return paths;
  }

  public static void runDigitalSignatureStrip(Path input, Path output) throws IOException, DocumentException {
    PdfReader reader = new PdfReader(input.toString());
    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(output.toString()));
    stamper.setFormFlattening(true);
    stamper.close();
    reader.close();
  }

  public static Path runDigitalSignatureSign(Path input, String keystore, String alias, String password)
    throws IOException, GeneralSecurityException, DocumentException {

    Security.addProvider(new BouncyCastleProvider());
    Path signedPDF = Files.createTempFile("signed", ".pdf");

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    InputStream is = new FileInputStream(keystore);
    ks.load(is, password.toCharArray());
    PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray());
    Certificate[] chain = ks.getCertificateChain(alias);
    IOUtils.closeQuietly(is);

    PdfReader reader = new PdfReader(input.toString());
    FileOutputStream os = new FileOutputStream(signedPDF.toFile());
    PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
    PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
    appearance.setReason(SIGNATURE_REASON);
    appearance.setLocation(SIGNATURE_LOCATION);
    appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "RODASignature");
    ExternalDigest digest = new BouncyCastleDigest();
    ExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, "BC");
    MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, null);
    IOUtils.closeQuietly(os);
    reader.close();

    return signedPDF;
  }

  private static String addElementToExtractionResult(PdfDictionary parent, PdfName element, String type) {
    String result = "";
    if (parent.contains(element)) {
      if (type.equals(PDF_STRING)) {
        PdfString elementName = parent.getAsString(element);
        if (elementName.toString().matches("[0-9a-zA-Z'\\.\\/\\-\\_\\: ]+"))
          result += element.toString() + ": " + elementName.toString() + "\n";
      } else if (type.equals(PDF_OBJECT)) {
        PdfObject elementObject = parent.get(element);
        result += element.toString() + ": " + elementObject.toString() + "\n";
      } else if (type.equals(PDF_BOOLEAN)) {
        result += element.toString() + ": true\n";
      }
    }
    return result;
  }

}
