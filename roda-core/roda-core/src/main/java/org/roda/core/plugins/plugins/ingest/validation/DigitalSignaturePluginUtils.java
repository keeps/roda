/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.PdfPKCS7;

public class DigitalSignaturePluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignaturePluginUtils.class);
  private static final String PDF_STRING = "String";
  private static final String PDF_OBJECT = "Object";
  private static final String PDF_BOOLEAN = "Boolean";

  private static final String SIGN_CONTENT_TYPE_OOXML = "application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml";
  private static final String SIGN_REL_TYPE_OOXML = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/origin";

  /*************************** VERIFY FUNCTIONS ***************************/

  public static String runDigitalSignatureVerify(Path input, String fileFormat) {

    try {
      if (fileFormat.equals("pdf")) {
        return DigitalSignaturePluginUtils.runDigitalSignatureVerifyPDF(input);
      } else if (fileFormat.equals("docx") || fileFormat.equals("xlsx") || fileFormat.equals("pptx")) {
        return DigitalSignaturePluginUtils.runDigitalSignatureVerifyOOXML(input);
      } else if (fileFormat.equals("odt") || fileFormat.equals("ods") || fileFormat.equals("odp")) {
        return DigitalSignaturePluginUtils.runDigitalSignatureVerifyODF(input);
      }
    } catch (IOException | GeneralSecurityException e) {
      LOGGER.warn("Problems running digital signature verification");
    }

    return "Not a supported format";
  }

  private static String runDigitalSignatureVerifyPDF(Path input) throws IOException, GeneralSecurityException {
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

        if (!DigitalSignaturePluginUtils.isCertificateSelfSigned(cert)) {

          Set<Certificate> trustedRootCerts = new HashSet<Certificate>();
          Set<Certificate> intermediateCerts = new HashSet<Certificate>();

          for (Certificate c : pk.getSignCertificateChain()) {
            X509Certificate x509c = (X509Certificate) c;
            x509c.checkValidity();

            if (DigitalSignaturePluginUtils.isCertificateSelfSigned(c))
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

  private static String runDigitalSignatureVerifyOOXML(Path input) throws IOException, GeneralSecurityException {
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

            if (DigitalSignaturePluginUtils.isCertificateSelfSigned(c))
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

  private static String runDigitalSignatureVerifyODF(Path input) throws IOException, GeneralSecurityException {
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

  public static Path runDigitalSignatureStrip(Path input, String fileFormat) {
    try {
      Path output = Files.createTempFile("stripped", "." + fileFormat);

      if (fileFormat.equals("pdf")) {
        DigitalSignaturePluginUtils.runDigitalSignatureStripPDF(input, output);
      } else if (fileFormat.equals("docx") || fileFormat.equals("xlsx") || fileFormat.equals("pptx")) {
        DigitalSignaturePluginUtils.runDigitalSignatureStripOOXML(input, output);
      } else if (fileFormat.equals("odt") || fileFormat.equals("ods") || fileFormat.equals("odp")) {
        DigitalSignaturePluginUtils.runDigitalSignatureStripODF(input, output);
      }

      return output;
    } catch (IOException | AlreadyExistsException | GenericException | InvalidFormatException | DocumentException e) {
      LOGGER.warn("Problems running a document stripping");
      return null;
    }
  }

  private static void runDigitalSignatureStripPDF(Path input, Path output) throws IOException, DocumentException {
    PdfReader reader = new PdfReader(input.toString());
    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(output.toString()));
    stamper.setFormFlattening(true);
    stamper.close();
    reader.close();
  }

  private static void runDigitalSignatureStripOOXML(Path input, Path output) throws IOException,
    AlreadyExistsException, GenericException, InvalidFormatException {

    FSUtils.copy(input, output, true);
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

  private static void runDigitalSignatureStripODF(Path input, Path output) throws IOException, DocumentException {
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

  /*************************** SUB FUNCTIONS ***************************/

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
