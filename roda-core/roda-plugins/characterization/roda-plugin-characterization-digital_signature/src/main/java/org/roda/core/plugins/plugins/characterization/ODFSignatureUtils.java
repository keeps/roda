/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
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
import org.apache.commons.io.IOUtils;
import org.apache.poi.poifs.crypt.dsig.KeyInfoKeySelector;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.IdUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

public final class ODFSignatureUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ODFSignatureUtils.class);
  private static final String META_INF_DOCUMENTSIGNATURES_XML = "META-INF/documentsignatures.xml";
  private static final String OPENOFFICE = "urn:oasis:names:tc:opendocument:xmlns:digitalsignature:1.0";

  /** Private empty constructor */
  private ODFSignatureUtils() {
    // do nothing
  }

  public static String runDigitalSignatureVerify(Path input) throws IOException {
    String result = "Passed";
    try (ZipFile zipFile = new ZipFile(input.toString())) {
      ZipEntry documentSignatureEntry = null;
      Enumeration<?> enumeration;

      for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
        ZipEntry entry = (ZipEntry) enumeration.nextElement();
        if (META_INF_DOCUMENTSIGNATURES_XML.equalsIgnoreCase(entry.getName())) {
          documentSignatureEntry = entry;
          break;
        }
      }

      if (documentSignatureEntry != null) {
        try (InputStream zipStream = zipFile.getInputStream(documentSignatureEntry)) {
          InputSource inputSource = new InputSource(zipStream);
          DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
          documentBuilderFactory.setNamespaceAware(true);
          DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
          Document document = documentBuilder.parse(inputSource);
          NodeList signatureNodeList = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

          for (int i = 0; i < signatureNodeList.getLength(); i++) {
            Node signatureNode = signatureNodeList.item(i);
            verifyCertificates(signatureNode);
          }
        } catch (ParserConfigurationException | SAXException e) {
          result = "Signatures document can not be parsed";
        } catch (CertificateExpiredException e) {
          result = "Contains expired certificates";
        } catch (CertificateRevokedException e) {
          result = "Contains revoked certificates";
        } catch (CertificateNotYetValidException e) {
          result = "Contains certificates not yet valid";
        } catch (MarshalException | XMLSignatureException e) {
          result = "Digital signatures are not valid";
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
          result = "Could not verify certificates";
        }
      } else {
        result = "Could not find " + META_INF_DOCUMENTSIGNATURES_XML;
      }

    }

    return result;

  }

  public static List<Path> runDigitalSignatureExtract(Path input) throws SignatureException, IOException {
    List<Path> paths = new ArrayList<>();

    try (ZipFile zipFile = new ZipFile(input.toString())) {
      Enumeration<?> enumeration;
      ZipEntry documentSignatureEntry = null;

      for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
        ZipEntry entry = (ZipEntry) enumeration.nextElement();
        if (META_INF_DOCUMENTSIGNATURES_XML.equalsIgnoreCase(entry.getName())) {
          documentSignatureEntry = entry;
          break;
        }
      }

      if (documentSignatureEntry != null) {
        Path extractedSignature = Files.createTempFile("extraction", ".xml");
        try (InputStream zipStream = zipFile.getInputStream(documentSignatureEntry)) {
          FileUtils.copyInputStreamToFile(zipStream, extractedSignature.toFile());
          paths.add(extractedSignature);
        }
      }
    }
    return paths;
  }

  public static void runDigitalSignatureStrip(Path input, Path output) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output.toFile()));
    try (ZipFile zipFile = new ZipFile(input.toString()); ZipOutputStream zout = new ZipOutputStream(bos)) {
      Enumeration<?> enumeration;

      for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
        ZipEntry entry = (ZipEntry) enumeration.nextElement();
        String entryName = entry.getName();
        if (!META_INF_DOCUMENTSIGNATURES_XML.equalsIgnoreCase(entryName) && entry.getSize() > 0) {

          try (InputStream zipStream = zipFile.getInputStream(entry)) {
            ZipEntry destEntry = new ZipEntry(entryName);
            zout.putNextEntry(destEntry);

            byte[] data = new byte[(int) entry.getSize()];
            while ((zipStream.read(data, 0, (int) entry.getSize())) != -1) {
            }

            zout.write(data);
            zout.closeEntry();
          }
        }
      }
    }
  }

  public static Path runDigitalSignatureSign(Path input, String ks, String alias, String password, String fileFormat)
    throws IOException, GeneralSecurityException, DocumentException {

    Security.addProvider(new BouncyCastleProvider());
    Path output = Files.createTempFile("odfsigned", "." + fileFormat);

    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

    try (InputStream storeStream = new FileInputStream(ks)) {
      keystore.load(storeStream, password.toCharArray());

      X509Certificate certificate = (X509Certificate) keystore.getCertificate(keystore.aliases().nextElement());
      Key key = keystore.getKey(alias, password.toCharArray());

      try (ByteArrayInputStream bais = createSignature(input.toString(), certificate, key)) {
        File file = output.toFile();
        if (file != null && bais != null) {
          byte[] buffer = new byte[2048];
          int length = 0;
          try (FileOutputStream fos = new FileOutputStream(file)) {
            while ((length = bais.read(buffer)) >= 0) {
              fos.write(buffer, 0, length);
            }
          }
        }
      }
    }

    return output;
  }

  public static ByteArrayInputStream createSignature(String inputPath, X509Certificate certificate, Key key) {
    try (ZipFile zipFile = new ZipFile(new File(inputPath))) {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Init.init();

      XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
      DigestMethod digestMethod = factory.newDigestMethod(DigestMethod.SHA1, null);

      try (InputStream manifest = zipFile.getInputStream(zipFile.getEntry("META-INF/manifest.xml"))) {
        Document docManifest = documentBuilder.parse(manifest);
        Element rootManifest = docManifest.getDocumentElement();
        NodeList listFileEntry = rootManifest.getElementsByTagName("manifest:file-entry");
        Document docSignatures;
        Element rootSignatures;

        if (zipFile.getEntry(META_INF_DOCUMENTSIGNATURES_XML) != null) {
          InputStream is = zipFile.getInputStream(zipFile.getEntry(META_INF_DOCUMENTSIGNATURES_XML));
          docSignatures = documentBuilder.parse(is);
          rootSignatures = docSignatures.getDocumentElement();
          IOUtils.closeQuietly(is);
        } else {
          docSignatures = documentBuilder.newDocument();
          rootSignatures = docSignatures.createElement("document-signatures");
          rootSignatures.setAttribute("xmlns", OPENOFFICE);
          docSignatures.appendChild(rootSignatures);

          Element nodeDocumentSignatures = docManifest.createElement("manifest:file-entry");
          nodeDocumentSignatures.setAttribute("manifest:media-type", "");
          nodeDocumentSignatures.setAttribute("manifest:full-path", META_INF_DOCUMENTSIGNATURES_XML);
          rootManifest.appendChild(nodeDocumentSignatures);

          Element nodeMetaInf = docManifest.createElement("manifest:file-entry");
          nodeMetaInf.setAttribute("manifest:media-type", "");
          nodeMetaInf.setAttribute("manifest:full-path", "META-INF/");
          rootManifest.appendChild(nodeMetaInf);
        }

        List<Reference> referenceList = getReferenceList(zipFile, documentBuilder, factory, listFileEntry,
          digestMethod);
        digitalSign(factory, referenceList, digestMethod, certificate, docSignatures, rootSignatures, key);

        ByteArrayOutputStream baos = addSignatureToStream(zipFile, rootManifest, rootSignatures);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        baos.close();
        return bais;
      }
    } catch (Exception e) {
      LOGGER.debug("ODF signature creation went wrong.");
    }

    return null;
  }

  private static void writeXML(OutputStream outStream, Node node, boolean indent)
    throws TransformerFactoryConfigurationError, TransformerException, IOException {

    OutputStreamWriter osw = new OutputStreamWriter(outStream, Charset.forName(RodaConstants.DEFAULT_ENCODING));
    try (BufferedWriter bw = new BufferedWriter(osw)) {
      TransformerFactory transformerFactory = new org.apache.xalan.processor.TransformerFactoryImpl();
      Transformer serializer = transformerFactory.newTransformer();
      serializer.setOutputProperty(OutputKeys.ENCODING, RodaConstants.DEFAULT_ENCODING);

      if (indent) {
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      }

      DOMSource domSource = new DOMSource(node);
      StreamResult streamResult = new StreamResult(bw);
      serializer.transform(domSource, streamResult);
    }
  }

  private static void verifyCertificates(Node signatureNode) throws MarshalException, XMLSignatureException,
    NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {

    XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");
    DOMValidateContext domValidateContext = new DOMValidateContext(new KeyInfoKeySelector(), signatureNode);
    XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
    xmlSignature.getSignatureValue().validate(domValidateContext);
    // xmlSignature.validate(domValidateContext);

    KeyInfo keyInfo = xmlSignature.getKeyInfo();
    Iterator<?> it = keyInfo.getContent().iterator();
    List<X509Certificate> certs = new ArrayList<>();
    List<CRL> crls = new ArrayList<>();

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

  private static List<XMLObject> getXMLObjectList(XMLSignatureFactory factory, Document docSignatures,
    String signatureId, String signaturePropertyId) {

    Element content = docSignatures.createElement("dc:date");
    content.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SS");
    content.setTextContent(sdf.format(new Date()));
    XMLStructure str = new DOMStructure(content);
    List<XMLStructure> contentList = new ArrayList<>();
    contentList.add(str);

    SignatureProperty sp = factory.newSignatureProperty(contentList, "#" + signatureId, signaturePropertyId);
    List<SignatureProperty> spList = new ArrayList<>();
    spList.add(sp);

    SignatureProperties sps = factory.newSignatureProperties(spList, null);
    List<SignatureProperties> spsList = new ArrayList<>();
    spsList.add(sps);

    XMLObject object = factory.newXMLObject(spsList, null, null, null);
    List<XMLObject> objectList = new ArrayList<>();
    objectList.add(object);

    return objectList;
  }

  private static KeyInfo getKeyInfo(XMLSignatureFactory factory, X509Certificate certificate) {
    KeyInfoFactory kif = factory.getKeyInfoFactory();
    List<Object> x509Content = new ArrayList<>();
    x509Content.add(certificate.getSubjectX500Principal().getName());
    x509Content.add(certificate);
    X509Data cerData = kif.newX509Data(x509Content);
    return kif.newKeyInfo(Collections.singletonList(cerData), null);
  }

  private static ByteArrayOutputStream addSignatureToStream(ZipFile zipFile, Element rootManifest,
    Element rootSignatures) throws IOException, TransformerFactoryConfigurationError, TransformerException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(new ByteArrayOutputStream())) {

      Enumeration<?> enumeration;
      for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
        ZipEntry zeOut = (ZipEntry) enumeration.nextElement();
        String fileName = zeOut.getName();

        if (!fileName.equals(META_INF_DOCUMENTSIGNATURES_XML) && !"META-INF/manifest.xml".equals(fileName)) {
          zos.putNextEntry(zeOut);
          try (InputStream is = zipFile.getInputStream(zipFile.getEntry(fileName))) {
            zos.write(IOUtils.toByteArray(is));
            zos.closeEntry();
          }
        }
      }

      ZipEntry zeDocumentSignatures = new ZipEntry(META_INF_DOCUMENTSIGNATURES_XML);
      zos.putNextEntry(zeDocumentSignatures);
      try (ByteArrayOutputStream baosXML = new ByteArrayOutputStream()) {
        writeXML(baosXML, rootSignatures, false);
        zos.write(baosXML.toByteArray());
        zos.closeEntry();
      }

      ZipEntry zeManifest = new ZipEntry("META-INF/manifest.xml");
      zos.putNextEntry(zeManifest);
      try (ByteArrayOutputStream baosManifest = new ByteArrayOutputStream()) {
        writeXML(baosManifest, rootManifest, false);
        zos.write(baosManifest.toByteArray());
        zos.closeEntry();
      }

      zipFile.close();
    }

    return baos;
  }

  private static List<Reference> getReferenceList(ZipFile zipFile, DocumentBuilder documentBuilder,
    XMLSignatureFactory factory, NodeList listFileEntry, DigestMethod digestMethod)
    throws IOException, SAXException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
    CanonicalizationException, InvalidCanonicalizerException {

    Transform transform = factory.newTransform(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS, (TransformParameterSpec) null);
    List<Transform> transformList = new ArrayList<>();
    transformList.add(transform);

    MessageDigest messageDigest = MessageDigest.getInstance(RodaConstants.SHA1);
    List<Reference> referenceList = new ArrayList<>();

    for (int i = 0; i < listFileEntry.getLength(); i++) {
      String fullPath = ((Element) listFileEntry.item(i)).getAttribute("manifest:full-path");
      Reference reference;

      if (!fullPath.endsWith("/") && !fullPath.equals(META_INF_DOCUMENTSIGNATURES_XML)) {
        if ("content.xml".equals(fullPath) || "meta.xml".equals(fullPath) || "styles.xml".equals(fullPath)
          || "settings.xml".equals(fullPath)) {
          try (InputStream xmlFile = zipFile.getInputStream(zipFile.getEntry(fullPath))) {
            Element root = documentBuilder.parse(xmlFile).getDocumentElement();
            Canonicalizer canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
            byte[] docCanonicalize = canonicalizer.canonicalizeSubtree(root);
            byte[] digestValue = messageDigest.digest(docCanonicalize);
            reference = factory.newReference(fullPath.replaceAll(" ", "%20"), digestMethod, transformList, null, null,
              digestValue);
          }
        } else {
          try (InputStream is = zipFile.getInputStream(zipFile.getEntry(fullPath))) {
            byte[] digestValue = messageDigest.digest(IOUtils.toByteArray(is));
            reference = factory.newReference(fullPath.replaceAll(" ", "%20"), digestMethod, null, null, null,
              digestValue);
          }
        }

        referenceList.add(reference);
      }
    }

    return referenceList;
  }

  private static void digitalSign(XMLSignatureFactory factory, List<Reference> referenceList, DigestMethod digestMethod,
    X509Certificate certificate, Document docSignatures, Element rootSignatures, Key key)
    throws MarshalException, XMLSignatureException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

    String signatureId = IdUtils.createUUID();
    String signaturePropertyId = IdUtils.createUUID();
    CanonicalizationMethod canMethod = factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
      (C14NMethodParameterSpec) null);
    SignatureMethod signMethod = factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);

    Reference signaturePropertyReference = factory.newReference("#" + signaturePropertyId, digestMethod);
    referenceList.add(signaturePropertyReference);
    SignedInfo si = factory.newSignedInfo(canMethod, signMethod, referenceList);

    KeyInfo ki = getKeyInfo(factory, certificate);
    List<XMLObject> objectList = getXMLObjectList(factory, docSignatures, signatureId, signaturePropertyId);
    XMLSignature signature = factory.newXMLSignature(si, ki, objectList, signatureId, null);
    DOMSignContext signContext = new DOMSignContext(key, rootSignatures);
    signature.sign(signContext);
  }
}
