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

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
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

public final class PDFSignatureUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PDFSignatureUtils.class);

  /** Private empty constructor */
  private PDFSignatureUtils() {

  }

  public static String runDigitalSignatureVerify(Path input) throws IOException, GeneralSecurityException {
    Security.addProvider(new BouncyCastleProvider());

    PdfReader reader = new PdfReader(input.toString());
    AcroFields fields = reader.getAcroFields();
    ArrayList<String> names = fields.getSignatureNames();
    String result = "Passed";

    for (int i = 0; i < names.size(); i++) {
      String name = names.get(i);

      try {
        PdfPKCS7 pk = fields.verifySignature(name);
        X509Certificate certificate = pk.getSigningCertificate();
        certificate.checkValidity();

        if (!SignatureUtils.isCertificateSelfSigned(certificate)) {

          Set<Certificate> trustedRootCerts = new HashSet<Certificate>();
          Set<Certificate> intermediateCerts = new HashSet<Certificate>();

          for (Certificate c : pk.getSignCertificateChain()) {
            X509Certificate cert = (X509Certificate) c;
            cert.checkValidity();

            if (SignatureUtils.isCertificateSelfSigned(c))
              trustedRootCerts.add(c);
            else
              intermediateCerts.add(c);
          }

          SignatureUtils.verifyCertificateChain(trustedRootCerts, intermediateCerts, certificate);
          if (pk.getCRLs() != null) {
            for (CRL crl : pk.getCRLs()) {
              if (crl.isRevoked(certificate)) {
                result = "Signing certificate is included on a Certificate Revocation List";
              }
            }
          }
        }
      } catch (NoSuchFieldError e) {
        result = "Missing signature timestamp field";
      } catch (CertificateExpiredException e) {
        result = "Contains expired certificates";
      } catch (CertificateNotYetValidException e) {
        result = "Contains certificates not yet valid";
      }
    }

    reader.close();
    return result;
  }

  public static List<Path> runDigitalSignatureExtract(Path input) throws SignatureException, IOException {
    Security.addProvider(new BouncyCastleProvider());

    List<Path> paths = new ArrayList<Path>();
    Path output = Files.createTempFile("extraction", ".xml");
    Path outputContents = Files.createTempFile("contents", ".pkcs7");
    PdfReader reader = new PdfReader(input.toString());
    AcroFields fields = reader.getAcroFields();
    ArrayList<?> names = fields.getSignatureNames();
    String filename = input.getFileName().toString();
    filename = filename.substring(0, filename.lastIndexOf('.'));

    if (names.isEmpty())
      return paths;

    StringBuilder sb = getExtractionInformation(fields, names, outputContents, filename);

    FileOutputStream fos = new FileOutputStream(output.toString());
    OutputStreamWriter osw = new OutputStreamWriter(fos);
    PrintWriter out = new PrintWriter(osw, true);

    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.println("<signatures>");
    out.println(sb.toString());
    out.println("</signatures>");

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

  public static Path runDigitalSignatureSign(Path input, String keystore, String alias, String password, String reason,
    String location, String contact)
    throws IOException, GeneralSecurityException, DocumentException, FileNotFoundException {

    Security.addProvider(new BouncyCastleProvider());
    Path signedPDF = Files.createTempFile("signed", ".pdf");

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

    InputStream is = new FileInputStream(keystore);
    ks.load(is, password.toCharArray());
    IOUtils.closeQuietly(is);

    PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray());
    Certificate[] chain = ks.getCertificateChain(alias);

    PdfReader reader = new PdfReader(input.toString());
    FileOutputStream os = new FileOutputStream(signedPDF.toFile());
    PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
    PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
    appearance.setReason(reason);
    appearance.setLocation(location);
    appearance.setContact(contact);
    appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "RODASignature");
    ExternalDigest digest = new BouncyCastleDigest();
    ExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, "BC");
    MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, null);
    IOUtils.closeQuietly(os);
    reader.close();

    return signedPDF;
  }

  private static StringBuilder getExtractionInformation(AcroFields fields, ArrayList<?> names, Path outputContents,
    String filename) throws IOException {

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < names.size(); i++) {
      String name = (String) names.get(i);
      Item item = fields.getFieldItem(name);

      PdfDictionary widget = item.getWidget(0);
      PdfDictionary infoDictionary = widget.getAsDict(PdfName.V);
      sb.append("<signature>\n");

      try {
        PdfPKCS7 pk = fields.verifySignature(name);
        sb = addElementToExtractionResult(sb, "name", name);
        sb = addElementToExtractionResult(sb, "sign-name", pk.getSignName());
        sb = addElementToExtractionResult(sb, "version", Integer.toString(pk.getVersion()));
        sb = addElementToExtractionResult(sb, "reason", pk.getReason());
        sb = addElementToExtractionResult(sb, "location", pk.getLocation());

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        if (pk.getTimeStampDate() != null) {
          String timestamp = formatter.format(pk.getTimeStampDate().getTime());
          sb = addElementToExtractionResult(sb, "timestamp-time", timestamp);
        }

        if (pk.getSignDate() != null) {
          String sign = formatter.format(pk.getSignDate().getTime());
          sb = addElementToExtractionResult(sb, "sign-time", sign);
        }

        sb = addElementToExtractionResult(sb, "digest-algorithm", pk.getDigestAlgorithm());
        sb = addElementToExtractionResult(sb, "hash-algorithm", pk.getHashAlgorithm());
        sb = addElementToExtractionResult(sb, "covers-whole-document",
          Boolean.toString(fields.signatureCoversWholeDocument(name)));
        sb = addElementToExtractionResult(sb, "ft", widget.get(PdfName.FT).toString());

        if (infoDictionary.contains(PdfName.CONTACTINFO))
          sb = addElementToExtractionResult(sb, "contact-info",
            infoDictionary.getAsString(PdfName.CONTACTINFO).toString());

        if (infoDictionary.contains(PdfName.FILTER))
          sb = addElementToExtractionResult(sb, "filter", infoDictionary.get(PdfName.FILTER).toString());

        if (infoDictionary.contains(PdfName.SUBFILTER))
          sb = addElementToExtractionResult(sb, "subfilter", infoDictionary.get(PdfName.SUBFILTER).toString());

        if (infoDictionary.contains(PdfName.LOCK))
          sb = addElementToExtractionResult(sb, "lock", "true");

        if (infoDictionary.contains(PdfName.CONTENTS)) {
          PdfString elementName = infoDictionary.getAsString(PdfName.CONTENTS);
          Files.write(outputContents, elementName.toUnicodeString().getBytes());
          sb = addElementToExtractionResult(sb, "contents", filename + ".pkcs7");
        }

      } catch (NoSuchFieldError e) {
        LOGGER.warn("DS information extraction did not execute properly");
      }

      sb.append("</signature>");
    }

    return sb;
  }

  private static StringBuilder addElementToExtractionResult(StringBuilder sb, String tagName, String value) {
    sb.append("<").append(tagName).append(">");
    sb.append(value);
    sb.append("</").append(tagName).append(">\n");
    return sb;
  }

  public static int countSignaturesPDF(Path file) {
    int counter = -1;
    try {
      PdfReader reader = new PdfReader(file.toAbsolutePath().toString());
      AcroFields af = reader.getAcroFields();
      ArrayList<String> names = af.getSignatureNames();
      counter = names.size();
    } catch (IOException e) {
      LOGGER.error("Error getting path of file {}", e.getMessage());
    }
    return counter;
  }
}
