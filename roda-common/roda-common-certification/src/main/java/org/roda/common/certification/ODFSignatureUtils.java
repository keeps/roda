package org.roda.common.certification;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
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
import org.apache.poi.poifs.crypt.dsig.KeyInfoKeySelector;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

public class ODFSignatureUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ODFSignatureUtils.class);
  private static final String OPENOFFICE = "urn:oasis:names:tc:opendocument:xmlns:digitalsignature:1.0";

  public static String runDigitalSignatureVerify(Path input) throws IOException, GeneralSecurityException {
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
        IOUtils.closeQuietly(zipStream);
        try {
          DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
          Document document = documentBuilder.parse(inputSource);
          NodeList signatureNodeList = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
          for (int i = 0; i < signatureNodeList.getLength(); i++) {
            Node signatureNode = signatureNodeList.item(i);
            verifyCertificates(input, signatureNode);
          }
        } catch (ParserConfigurationException | SAXException e) {
          result = "Signatures document can not be parsed";
        } catch (CertificateExpiredException | CertificateRevokedException e) {
          result = "There are expired or revoked certificates";
        } catch (CertificateNotYetValidException e) {
          result = "There are certificates not yet valid";
        } catch (MarshalException | XMLSignatureException e) {
          result = "Signatures are not valid";
        }
      }
    }

    zipFile.close();
    return result;
  }

  public static List<Path> runDigitalSignatureExtract(Path input) throws SignatureException, IOException {
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

  public static void runDigitalSignatureStrip(Path input, Path output) throws IOException, DocumentException {
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

  public static Path runDigitalSignatureSign(Path input, String ks, String alias, String password, String fileFormat)
    throws Exception {

    Path output = Files.createTempFile("odfsigned", "." + fileFormat);
    Security.addProvider(new BouncyCastleProvider());

    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    keystore.load(new FileInputStream(ks), password.toCharArray());
    X509Certificate certificate = (X509Certificate) keystore.getCertificate(keystore.aliases().nextElement());
    Key key = keystore.getKey(alias, password.toCharArray());
    ByteArrayInputStream bais = ODFSignatureUtils.createSignature(input.toString(), certificate, key);

    File file = output.toFile();
    if (file != null) {
      byte[] buffer = new byte[2048];
      int length = 0;
      FileOutputStream fos = new FileOutputStream(file);

      while ((length = bais.read(buffer)) >= 0) {
        fos.write(buffer, 0, length);
      }

      fos.close();
    }

    return output;
  }

  public static ByteArrayInputStream createSignature(String inputPath, X509Certificate certificate, Key key) {

    try {
      ZipFile zipFile = new ZipFile(new File(inputPath));

      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Init.init();

      XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
      DigestMethod digestMethod = fac.newDigestMethod(DigestMethod.SHA1, null);

      Transform transform = fac.newTransform(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS, (TransformParameterSpec) null);
      List<Transform> transformList = new ArrayList<Transform>();
      transformList.add(transform);

      CanonicalizationMethod cm = fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
        (C14NMethodParameterSpec) null);

      SignatureMethod sm = fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null);

      InputStream manifest = zipFile.getInputStream(zipFile.getEntry("META-INF/manifest.xml"));
      Document docManifest = documentBuilder.parse(manifest);
      Element rootManifest = docManifest.getDocumentElement();
      NodeList listFileEntry = rootManifest.getElementsByTagName("manifest:file-entry");
      List<Reference> referenceList = getReferences(zipFile, documentBuilder, fac, listFileEntry, digestMethod,
        transformList);

      Document docSignatures;
      Element rootSignatures;

      if (zipFile.getEntry("META-INF/documentsignatures.xml") != null) {
        InputStream is = zipFile.getInputStream(zipFile.getEntry("META-INF/documentsignatures.xml"));
        docSignatures = documentBuilder.parse(is);
        rootSignatures = docSignatures.getDocumentElement();
        IOUtils.closeQuietly(is);
      } else {
        docSignatures = documentBuilder.newDocument();
        rootSignatures = docSignatures.createElement("document-signatures");
        rootSignatures.setAttribute("xmlns", OPENOFFICE);
        docSignatures.appendChild(rootSignatures);

        Element nodeMetaInf = docManifest.createElement("manifest:file-entry");
        nodeMetaInf.setAttribute("manifest:media-type", "");
        nodeMetaInf.setAttribute("manifest:full-path", "META-INF/");
        rootManifest.appendChild(nodeMetaInf);

        Element nodeDocumentSignatures = docManifest.createElement("manifest:file-entry");
        nodeDocumentSignatures.setAttribute("manifest:media-type", "");
        nodeDocumentSignatures.setAttribute("manifest:full-path", "META-INF/documentsignatures.xml");
        rootManifest.appendChild(nodeDocumentSignatures);
      }

      String signatureId = UUID.randomUUID().toString();
      String signaturePropertyId = UUID.randomUUID().toString();

      Reference signaturePropertyReference = fac.newReference("#" + signaturePropertyId, digestMethod);
      referenceList.add(signaturePropertyReference);
      SignedInfo si = fac.newSignedInfo(cm, sm, referenceList);

      KeyInfo ki = getKeyInfo(fac, certificate);
      List<XMLObject> objectList = getXMLObjectList(fac, docSignatures, signatureId, signaturePropertyId);
      XMLSignature signature = fac.newXMLSignature(si, ki, objectList, signatureId, null);
      DOMSignContext signContext = new DOMSignContext(key, rootSignatures);
      signature.sign(signContext);

      ByteArrayOutputStream baos = addSignatureToStream(zipFile, rootManifest, rootSignatures);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      baos.close();
      return bais;

    } catch (Exception e) {
      LOGGER.debug("ODF signature creation went wrong.");
      return null;
    }

  }

  private static void writeXML(OutputStream outStream, Node node, boolean indent)
    throws TransformerFactoryConfigurationError, TransformerException {

    OutputStreamWriter osw = new OutputStreamWriter(outStream, Charset.forName("UTF-8"));
    BufferedWriter bw = new BufferedWriter(osw);
    Transformer serializer = TransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    if (indent) {
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    }

    DOMSource domSource = new DOMSource(node);
    StreamResult streamResult = new StreamResult(bw);
    serializer.transform(domSource, streamResult);

    IOUtils.closeQuietly(bw);
    IOUtils.closeQuietly(osw);
  }

  private static void verifyCertificates(Path input, Node signatureNode) throws MarshalException,
    XMLSignatureException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException,
    KeyStoreException {

    XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");
    DOMValidateContext domValidateContext = new DOMValidateContext(new KeyInfoKeySelector(), signatureNode);
    XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
    xmlSignature.getSignatureValue().validate(domValidateContext);
    // xmlSignature.validate(domValidateContext);

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

  private static List<XMLObject> getXMLObjectList(XMLSignatureFactory fac, Document docSignatures, String signatureId,
    String signaturePropertyId) {

    Element content = docSignatures.createElement("dc:date");
    content.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:dc", "http://purl.org/dc/elements/1.1/");
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

    return objectList;
  }

  private static KeyInfo getKeyInfo(XMLSignatureFactory fac, X509Certificate certificate) {
    KeyInfoFactory kif = fac.getKeyInfoFactory();
    List<Object> x509Content = new ArrayList<Object>();
    x509Content.add(certificate.getSubjectX500Principal().getName());
    x509Content.add(certificate);
    X509Data cerData = kif.newX509Data(x509Content);
    KeyInfo ki = kif.newKeyInfo(Collections.singletonList(cerData), null);
    return ki;
  }

  private static ByteArrayOutputStream addSignatureToStream(ZipFile zipFile, Element rootManifest,
    Element rootSignatures) throws Exception {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    Enumeration<?> enumeration;
    for (enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
      ZipEntry zeOut = (ZipEntry) enumeration.nextElement();
      String fileName = zeOut.getName();

      if (!fileName.equals("META-INF/documentsignatures.xml") && !fileName.equals("META-INF/manifest.xml")) {
        zos.putNextEntry(zeOut);
        InputStream is = zipFile.getInputStream(zipFile.getEntry(fileName));
        zos.write(IOUtils.toByteArray(is));
        zos.closeEntry();
        IOUtils.closeQuietly(is);
      }
    }

    ZipEntry zeManifest = new ZipEntry("META-INF/manifest.xml");
    zos.putNextEntry(zeManifest);
    ByteArrayOutputStream baosManifest = new ByteArrayOutputStream();
    writeXML(baosManifest, rootManifest, false);
    zos.write(baosManifest.toByteArray());
    zos.closeEntry();
    baosManifest.close();

    ZipEntry zeDocumentSignatures = new ZipEntry("META-INF/documentsignatures.xml");
    zos.putNextEntry(zeDocumentSignatures);
    ByteArrayOutputStream baosXML = new ByteArrayOutputStream();
    writeXML(baosXML, rootSignatures, false);
    zos.write(baosXML.toByteArray());
    zos.closeEntry();
    baosXML.close();

    zos.close();
    zipFile.close();

    return baos;
  }

  private static List<Reference> getReferences(ZipFile zipFile, DocumentBuilder documentBuilder,
    XMLSignatureFactory fac, NodeList listFileEntry, DigestMethod digestMethod, List<Transform> transformList)
    throws Exception {

    MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
    List<Reference> referenceList = new ArrayList<Reference>();

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
          byte[] digestValue = messageDigest.digest(docCanonicalize);
          reference = fac.newReference(fullPath.replaceAll(" ", "%20"), digestMethod, transformList, null, null,
            digestValue);

        } else {
          InputStream is = zipFile.getInputStream(zipFile.getEntry(fullPath));
          byte[] digestValue = messageDigest.digest(IOUtils.toByteArray(is));
          IOUtils.closeQuietly(is);
          reference = fac.newReference(fullPath.replaceAll(" ", "%20"), digestMethod, null, null, null, digestValue);
        }

        referenceList.add(reference);
      }
    }

    return referenceList;
  }
}
