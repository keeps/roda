package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public static int runDigitalSignatureVerify(Path input, String fileFormat) throws IOException,
    GeneralSecurityException {
    Security.addProvider(new BouncyCastleProvider());
    // KeyStore keyStore = PdfPKCS7.loadCacertsKeyStore();
    PdfReader reader = new PdfReader(input.toString());
    AcroFields fields = reader.getAcroFields();
    ArrayList names = fields.getSignatureNames();
    int result = 1;

    for (int i = 0; i < names.size(); i++) {
      String name = (String) names.get(i);

      try {
        PdfPKCS7 pk = fields.verifySignature(name);

        // TODO change verification to certificate
        boolean verify = pk.verify();

        if (!verify) {
          result = 0;
          LOGGER.warn("Certificate verification failed: " + verify + " on " + input.getFileName());
        }
      } catch (NoSuchFieldError e) {
        LOGGER.warn("Problem verifying signature '" + name + "' of " + input.getFileName());
        result = -1;
      }
    }
    reader.close();
    return result;
  }

  public static List<Path> runDigitalSignatureExtract(Path input, String fileFormat) throws SignatureException,
    IOException {

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
      return null;

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

        String certChain = "";
        for (Certificate c : pk.getSignCertificateChain()) {
          certChain += c.toString() + "/";
        }

        infoResult += "Certificate Chain: " + certChain + "\n";
        infoResult += "Digest Algorithm: " + pk.getDigestAlgorithm() + "\n";
        infoResult += "Hash Algorithm: " + pk.getHashAlgorithm() + "\n";
        infoResult += "CoversWholeDocument: " + fields.signatureCoversWholeDocument(name) + "\n";
        infoResult += addElementToExtractionResult(info, PdfName.CONTACTINFO, "String");
        infoResult += addElementToExtractionResult(info, PdfName.FILTER, "Object");
        infoResult += addElementToExtractionResult(info, PdfName.SUBFILTER, "Object");
        infoResult += addElementToExtractionResult(widget, PdfName.FT, "Object");
        infoResult += addElementToExtractionResult(widget, PdfName.LOCK, "Boolean");

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

  public static Path runDigitalSignatureStrip(Path input, String fileFormat) throws IOException, DocumentException {
    Path output = Files.createTempFile("stripped", "." + fileFormat);
    PdfReader reader = new PdfReader(input.toString());
    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(output.toString()));
    stamper.setFormFlattening(true);
    stamper.close();
    reader.close();
    return output;
  }

  private static String addElementToExtractionResult(PdfDictionary parent, PdfName element, String type) {
    String result = "";
    if (parent.contains(element)) {
      if (type.equals("String")) {
        PdfString elementName = parent.getAsString(element);
        if (elementName.toString().matches("[0-9a-zA-Z'\\.\\/\\-\\_\\: ]+"))
          result += element.toString() + ": " + elementName.toString() + "\n";
      } else if (type.equals("Object")) {
        PdfObject elementObject = parent.get(element);
        result += element.toString() + ": " + elementObject.toString() + "\n";
      } else if (type.equals("Boolean")) {
        result += element.toString() + ": true\n";
      }
    }
    return result;
  }

  public static int countSignatures(Path input) {
    int counter = -1;
    try {
      PdfReader reader = new PdfReader(input.toString());
      AcroFields af = reader.getAcroFields();
      ArrayList names = af.getSignatureNames();
      counter = names.size();
    } catch (IOException e) {
      LOGGER.error("Error getting path of file " + e.getMessage());
    }
    return counter;
  }

  /*************************** FILLING FILE FORMAT STRUCTURES ***************************/

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

}
