package org.roda.common.certification;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRL;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.poifs.crypt.dsig.KeyInfoKeySelector;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo.SignaturePart;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
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

public class SignatureUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SignatureUtils.class);
  private static final String PDF_STRING = "String";
  private static final String PDF_OBJECT = "Object";
  private static final String PDF_BOOLEAN = "Boolean";

  private static final String SIGN_CONTENT_TYPE_OOXML = "application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml";
  private static final String SIGN_REL_TYPE_OOXML = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/origin";

  private static final String SIGNATURE_REASON = "test reason";
  private static final String SIGNATURE_LOCATION = "test location";
  private static final String OPENOFFICE = "urn:oasis:names:tc:opendocument:xmlns:digitalsignature:1.0";

  /*************************** VERIFY FUNCTIONS ***************************/

  public static String runDigitalSignatureVerifyPDF(Path input) throws IOException, GeneralSecurityException {
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

          verifyCertificateChain(trustedRootCerts, intermediateCerts, cert);
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
      } catch (CertPathBuilderException e) {
        result = "Signing certificate chain does not pass the verification";
      }
    }

    reader.close();
    return result;
  }

  public static String runDigitalSignatureVerifyOOXML(Path input) throws IOException, GeneralSecurityException {
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

          verifyCertificateChain(trustedRootCerts, intermediateCerts, certChain.get(0));
        }
      }

      pkg.close();
    } catch (InvalidFormatException e) {
      return "Error opening a document file";
    } catch (CertificateExpiredException | CertificateNotYetValidException e) {
      return "Signing certificate does not pass the validity check";
    } catch (CertPathBuilderException e) {
      return "Signing certificate chain does not pass the verification";
    }

    return isValid ? "Passed" : "Not passed";
  }

  public static String runDigitalSignatureVerifyODF(Path input) throws IOException, GeneralSecurityException {
    String result = "Passed";
    ZipFile zipFile = new ZipFile(input.toString());
    Enumeration<?> enumeration;
    for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) enumeration.nextElement();
      String entryName = entry.getName();
      if (entryName.equalsIgnoreCase("META-INF/documentsignatures.xml")) {
        InputStream zipStream = zipFile.getInputStream(entry);
        InputSource inputSource = new InputSource(zipStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        try {
          documentBuilder = documentBuilderFactory.newDocumentBuilder();
          Document document = documentBuilder.parse(inputSource);
          NodeList signatureNodeList = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
          for (int i = 0; i < signatureNodeList.getLength(); i++) {
            Node signatureNode = signatureNodeList.item(i);
            verifyCertificatesODF(input, signatureNode);
          }
        } catch (ParserConfigurationException | SAXException e) {
          result = "Signatures document can not be parsed";
        } catch (CertificateExpiredException | CertificateRevokedException e) {
          result = "There are expired or revoked certificates";
        } catch (CertificateNotYetValidException notYetValidEx) {
          result = "There are certificates not yet valid";
        } catch (MarshalException | XMLSignatureException e) {
          result = "Signatures are not valid.";
        }
      }
    }

    zipFile.close();
    return result;
  }

  /*************************** EXTRACT FUNCTIONS ***************************/

  public static List<Path> runDigitalSignatureExtractPDF(Path input) throws SignatureException, IOException {
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
      String infoResult = "";

      PdfDictionary widget = item.getWidget(0);
      PdfDictionary info = widget.getAsDict(PdfName.V);

      try {
        PdfPKCS7 pk = fields.verifySignature(name);
        infoResult += "Name: " + name + "\n";
        infoResult += "Sign Name: " + pk.getSignName() + "\n";
        infoResult += "Version: " + pk.getVersion() + "\n";
        infoResult += "Reason: " + pk.getReason() + "\n";
        infoResult += "Location: " + pk.getLocation() + "\n";

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        if (pk.getTimeStampDate() != null)
          infoResult += "Timestamp Date: " + formatter.format(pk.getTimeStampDate().getTime()) + "\n";

        if (pk.getSignDate() != null)
          infoResult += "Sign Date: " + formatter.format(pk.getSignDate().getTime()) + "\n";

        infoResult += "Digest Algorithm: " + pk.getDigestAlgorithm() + "\n";
        infoResult += "Hash Algorithm: " + pk.getHashAlgorithm() + "\n";
        infoResult += "CoversWholeDocument: " + fields.signatureCoversWholeDocument(name) + "\n";
        infoResult += addElementToExtractionResult(info, PdfName.CONTACTINFO, PDF_STRING);
        infoResult += addElementToExtractionResult(info, PdfName.FILTER, PDF_OBJECT);
        infoResult += addElementToExtractionResult(info, PdfName.SUBFILTER, PDF_OBJECT);
        infoResult += addElementToExtractionResult(widget, PdfName.FT, PDF_OBJECT);
        infoResult += addElementToExtractionResult(widget, PdfName.LOCK, PDF_BOOLEAN);

        if (info.contains(PdfName.CONTENTS)) {
          PdfString elementName = info.getAsString(PdfName.CONTENTS);
          outputContents = Files.createTempFile("contents", ".pkcs7");
          Files.write(outputContents, elementName.toUnicodeString().getBytes());
          infoResult += PdfName.CONTENTS.toString() + ": " + filename + ".pkcs7 \n";
        }
      } catch (NoSuchFieldError e) {
        LOGGER.warn("DS information extraction did not execute properly");
      }

      out.println("- Signature " + name + ":\n");
      out.println(infoResult);
    }

    IOUtils.closeQuietly(out);
    IOUtils.closeQuietly(osw);
    IOUtils.closeQuietly(fos);
    reader.close();

    paths.add(output);
    paths.add(outputContents);
    return paths;
  }

  public static Map<Path, String> runDigitalSignatureExtractOOXML(Path input) throws SignatureException, IOException {
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

  public static List<Path> runDigitalSignatureExtractODF(Path input) throws SignatureException, IOException {
    List<Path> paths = new ArrayList<Path>();

    ZipFile zipFile = new ZipFile(input.toString());
    Enumeration<?> enumeration;
    for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) enumeration.nextElement();
      String entryName = entry.getName();
      if (entryName.equalsIgnoreCase("META-INF/documentsignatures.xml")) {
        Path extractedSignature = Files.createTempFile("extraction", ".xml");
        InputStream zipStream = zipFile.getInputStream(entry);
        FileUtils.copyInputStreamToFile(zipStream, extractedSignature.toFile());
        paths.add(extractedSignature);
        IOUtils.closeQuietly(zipStream);
      }
    }

    zipFile.close();
    return paths;
  }

  /*************************** STRIP FUNCTIONS ***************************/

  public static void runDigitalSignatureStripPDF(Path input, Path output) throws IOException, DocumentException {
    PdfReader reader = new PdfReader(input.toString());
    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(output.toString()));
    stamper.setFormFlattening(true);
    stamper.close();
    reader.close();
  }

  public static void runDigitalSignatureStripOOXML(Path input, Path output) throws IOException, InvalidFormatException {

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

  public static void runDigitalSignatureStripODF(Path input, Path output) throws IOException, DocumentException {
    OutputStream os = new FileOutputStream(output.toFile());
    BufferedOutputStream bos = new BufferedOutputStream(os);
    ZipOutputStream zout = new ZipOutputStream(bos);
    ZipFile zipFile = new ZipFile(input.toString());
    Enumeration<?> enumeration;

    for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) enumeration.nextElement();
      String entryName = entry.getName();
      if (!entryName.equalsIgnoreCase("META-INF/documentsignatures.xml") && entry.getSize() > 0) {

        InputStream zipStream = zipFile.getInputStream(entry);
        ZipEntry destEntry = new ZipEntry(entryName);
        zout.putNextEntry(destEntry);

        byte[] data = new byte[(int) entry.getSize()];
        while ((zipStream.read(data, 0, (int) entry.getSize())) != -1) {
        }

        zout.write(data);
        zout.closeEntry();
        IOUtils.closeQuietly(zipStream);
      }
    }

    IOUtils.closeQuietly(zout);
    IOUtils.closeQuietly(bos);
    IOUtils.closeQuietly(os);
    zipFile.close();
  }

  /*************************** SIGN FUNCTIONS ***************************/

  public static Path runDigitalSignatureSignPDF(Path input, String keystore, String alias, String password)
    throws IOException, GeneralSecurityException, DocumentException {

    Security.addProvider(new BouncyCastleProvider());
    Path signedPDF = Files.createTempFile("signed", ".pdf");

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    InputStream is = new FileInputStream(keystore);
    ks.load(is, password.toCharArray());
    String alias1 = (String) ks.aliases().nextElement();
    PrivateKey pk = (PrivateKey) ks.getKey(alias1, password.toCharArray());
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

  public static Path runDigitalSignatureSignOOXML(Path input, String keystore, String alias, String password,
    String fileFormat) throws InvalidFormatException, KeyStoreException, NoSuchAlgorithmException,
    CertificateException, IOException, UnrecoverableKeyException, XMLSignatureException, MarshalException {

    Path output = Files.createTempFile("signed", "." + fileFormat);
    CopyOption[] copyOptions = new CopyOption[] {StandardCopyOption.REPLACE_EXISTING};
    Files.copy(input, output, copyOptions);

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    InputStream is = new FileInputStream(keystore);
    ks.load(is, password.toCharArray());

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
    IOUtils.closeQuietly(is);

    return output;
  }

  public static Path runDigitalSignatureSignODF(Path input, String keystore, String alias, String password,
    String fileFormat) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
    UnrecoverableKeyException, ParserConfigurationException, InvalidAlgorithmParameterException, SAXException,
    InvalidCanonicalizerException, CanonicalizationException, MarshalException, XMLSignatureException,
    TransformerFactoryConfigurationError, TransformerException {

    Security.addProvider(new BouncyCastleProvider());
    Path output = Files.createTempFile("signed", "." + fileFormat);

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    InputStream is = new FileInputStream(keystore);
    ks.load(is, password.toCharArray());
    IOUtils.closeQuietly(is);

    X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
    PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray());

    ZipFile zipFile = new ZipFile(input.toString());
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Init.init();

    MessageDigest md = MessageDigest.getInstance("SHA1");
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
    DigestMethod dm = fac.newDigestMethod(DigestMethod.SHA1, null);
    List<Transform> transformList = new ArrayList<Transform>();
    transformList.add(fac.newTransform(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS, (TransformParameterSpec) null));

    CanonicalizationMethod cm = fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
      (C14NMethodParameterSpec) null);
    SignatureMethod sm = fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null);

    List<Reference> referenceList = new ArrayList<Reference>();
    InputStream manifest = zipFile.getInputStream(zipFile.getEntry("META-INF/manifest.xml"));
    Document docManifest = documentBuilder.parse(manifest);
    Element rootManifest = docManifest.getDocumentElement();
    NodeList listFileEntry = rootManifest.getElementsByTagName("manifest:file-entry");
    IOUtils.closeQuietly(manifest);

    for (int i = 0; i < listFileEntry.getLength(); i++) {
      String fullPath = ((Element) listFileEntry.item(i)).getAttribute("manifest:full-path");
      Reference reference;

      if (!fullPath.endsWith("/") && !fullPath.equals("META-INF/documentsignatures.xml")) {
        if (fullPath.equals("content.xml") || fullPath.equals("meta.xml") || fullPath.equals("styles.xml")
          || fullPath.equals("settings.xml")) {

          InputStream xmlFile = zipFile.getInputStream(zipFile.getEntry(fullPath));
          Element root = documentBuilder.parse(xmlFile).getDocumentElement();
          IOUtils.closeQuietly(xmlFile);

          Canonicalizer canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
          byte[] docCanonicalize = canonicalizer.canonicalizeSubtree(root);
          byte[] digestValue = md.digest(docCanonicalize);

          reference = fac.newReference(fullPath.replaceAll(" ", "%20"), dm, transformList, null, null, digestValue);
        } else {
          is = zipFile.getInputStream(zipFile.getEntry(fullPath));
          byte[] data = IOUtils.toByteArray(is);
          byte[] digestValue = md.digest(data);
          reference = fac.newReference(fullPath.replaceAll(" ", "%20"), dm, null, null, null, digestValue);
          IOUtils.closeQuietly(is);
        }

        referenceList.add(reference);
      }
    }

    Document docSignatures;
    Element rootSignatures;

    if (zipFile.getEntry("META-INF/documentsignatures.xml") != null) {
      InputStream xmlFile = zipFile.getInputStream(zipFile.getEntry("META-INF/documentsignatures.xml"));
      docSignatures = documentBuilder.parse(xmlFile);
      rootSignatures = docSignatures.getDocumentElement();
      IOUtils.closeQuietly(xmlFile);
    } else {
      docSignatures = documentBuilder.newDocument();
      rootSignatures = docSignatures.createElement("document-signatures");
      rootSignatures.setAttribute("xmlns", OPENOFFICE);
      docSignatures.appendChild(rootSignatures);

      Element nodeDocumentSignatures = docManifest.createElement("manifest:file-entry");
      nodeDocumentSignatures.setAttribute("manifest:media-type", "");
      nodeDocumentSignatures.setAttribute("manifest:full-path", "META-INF/documentsignatures.xml");
      rootManifest.appendChild(nodeDocumentSignatures);

      Element nodeMetaInf = docManifest.createElement("manifest:file-entry");
      nodeMetaInf.setAttribute("manifest:media-type", "");
      nodeMetaInf.setAttribute("manifest:full-path", "META-INF/");
      rootManifest.appendChild(nodeMetaInf);
    }

    String signatureId = UUID.randomUUID().toString();
    String signaturePropertyId = UUID.randomUUID().toString();

    Reference signaturePropertyReference = fac.newReference("#" + signaturePropertyId, dm);
    referenceList.add(signaturePropertyReference);
    SignedInfo si = fac.newSignedInfo(cm, sm, referenceList);

    KeyInfoFactory kif = fac.getKeyInfoFactory();
    List<Object> x509Content = new ArrayList<Object>();
    x509Content.add(certificate.getSubjectX500Principal().getName());
    x509Content.add(certificate);
    X509Data cerData = kif.newX509Data(x509Content);
    KeyInfo ki = kif.newKeyInfo(Collections.singletonList(cerData), null);

    Element content = docSignatures.createElement("dc:date");
    content.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SS");
    content.setTextContent(sdf.format(new Date()));
    XMLStructure str = new DOMStructure(content);
    List<XMLStructure> contentList = new ArrayList<XMLStructure>();
    contentList.add(str);

    SignatureProperty sp = fac.newSignatureProperty(contentList, "#" + signatureId, signaturePropertyId);
    List<SignatureProperty> spList = new ArrayList<SignatureProperty>();
    spList.add(sp);

    SignatureProperties sps = fac.newSignatureProperties(spList, null);
    List<SignatureProperties> spsList = new ArrayList<SignatureProperties>();
    spsList.add(sps);

    XMLObject object = fac.newXMLObject(spsList, null, null, null);
    List<XMLObject> objectList = new ArrayList<XMLObject>();
    objectList.add(object);

    XMLSignature signature = fac.newXMLSignature(si, ki, objectList, signatureId, null);
    DOMSignContext signContext = new DOMSignContext(pk, rootSignatures);
    signature.sign(signContext);

    OutputStream os = new FileOutputStream(output.toString());
    ZipOutputStream zos = new ZipOutputStream(os);

    Enumeration<?> enumeration;
    for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) enumeration.nextElement();
      if (!entry.getName().equals("META-INF/documentsignatures.xml")
        && !entry.getName().equals("META-INF/manifest.xml")) {

        zos.putNextEntry(entry);
        is = zipFile.getInputStream(entry);
        byte[] data = IOUtils.toByteArray(is);
        zos.write(data);
        IOUtils.closeQuietly(is);
      }
    }

    ZipEntry zeDocumentSignatures = new ZipEntry("META-INF/documentsignatures.xml");
    zos.putNextEntry(zeDocumentSignatures);
    ByteArrayOutputStream baosXML = new ByteArrayOutputStream();
    writeXML(baosXML, rootSignatures, false);
    zos.write(baosXML.toByteArray());
    zos.closeEntry();
    IOUtils.closeQuietly(baosXML);

    ZipEntry zeManifest = new ZipEntry("META-INF/manifest.xml");
    zos.putNextEntry(zeManifest);
    ByteArrayOutputStream baosManifest = new ByteArrayOutputStream();
    writeXML(baosManifest, rootManifest, false);
    zos.write(baosManifest.toByteArray());
    zos.closeEntry();
    IOUtils.closeQuietly(baosManifest);

    IOUtils.closeQuietly(zos);
    IOUtils.closeQuietly(os);
    zipFile.close();
    return output;
  }

  /*************************** SUB FUNCTIONS ***************************/

  private static void writeXML(OutputStream outStream, Node node, boolean indent)
    throws TransformerFactoryConfigurationError, TransformerException {

    OutputStreamWriter osw = new OutputStreamWriter(outStream, Charset.forName("UTF-8"));
    BufferedWriter bw = new BufferedWriter(osw);
    Transformer serializer = TransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    if (indent) {
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    }

    serializer.transform(new DOMSource(node), new StreamResult(bw));
    IOUtils.closeQuietly(bw);
    IOUtils.closeQuietly(osw);
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

  private static boolean isCertificateSelfSigned(Certificate cert) throws CertificateException,
    NoSuchAlgorithmException, NoSuchProviderException {

    try {
      cert.verify(cert.getPublicKey());
      return true;
    } catch (SignatureException | InvalidKeyException e) {
      return false;
    }
  }

  private static void verifyCertificateChain(Set<Certificate> trustedRootCerts, Set<Certificate> intermediateCerts,
    X509Certificate cert) throws CertPathBuilderException, CertificateException, NoSuchAlgorithmException,
    NoSuchProviderException, InvalidAlgorithmParameterException {

    if (trustedRootCerts.size() > 0) {
      X509CertSelector selector = new X509CertSelector();
      selector.setCertificate(cert);
      Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
      for (Certificate trustedRootCert : trustedRootCerts) {
        trustAnchors.add(new TrustAnchor((X509Certificate) trustedRootCert, null));
      }

      PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);
      pkixParams.setRevocationEnabled(false);
      CertStore intermediateCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(
        intermediateCerts), "BC");
      pkixParams.addCertStore(intermediateCertStore);
      CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
      builder.build(pkixParams);
    }
  }

  private static void verifyCertificatesODF(Path input, Node signatureNode) throws CertificateExpiredException,
    CertificateNotYetValidException, MarshalException, XMLSignatureException, CertificateRevokedException {

    XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");
    DOMValidateContext domValidateContext = new DOMValidateContext(new KeyInfoKeySelector(), signatureNode);
    XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);

    KeyInfo keyInfo = xmlSignature.getKeyInfo();
    Iterator<?> it = keyInfo.getContent().iterator();
    List<X509Certificate> certs = new ArrayList<X509Certificate>();
    List<CRL> crls = new ArrayList<CRL>();

    while (it.hasNext()) {
      XMLStructure content = (XMLStructure) it.next();
      if (content instanceof X509Data) {
        X509Data certdata = (X509Data) content;
        Object[] entries = certdata.getContent().toArray();
        for (int i = 0; i < entries.length; i++) {
          if (entries[i] instanceof X509CRL) {
            X509CRL crl = (X509CRL) entries[i];
            crls.add(crl);
          }
          if (entries[i] instanceof X509Certificate) {
            X509Certificate cert = (X509Certificate) entries[i];
            cert.checkValidity();
            certs.add(cert);
          }
        }
      }
    }

    for (CRL c : crls) {
      for (X509Certificate cert : certs) {
        if (c.isRevoked(cert))
          throw new CertificateRevokedException(null, null, null, null);
      }
    }
  }

}
