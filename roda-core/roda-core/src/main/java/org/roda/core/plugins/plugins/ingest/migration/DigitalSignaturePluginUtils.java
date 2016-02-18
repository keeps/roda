package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.File;
import org.roda.core.model.utils.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.AcroFields.Item;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.text.pdf.PdfString;

public class DigitalSignaturePluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignaturePluginUtils.class);

  public static int runDigitalSignatureVerify(Path input, String fileFormat) throws IOException {

    KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
    PdfReader reader = new PdfReader(input.toString());
    AcroFields af = reader.getAcroFields();
    ArrayList names = af.getSignatureNames();
    int result = 1;

    for (int k = 0; k < names.size(); k++) {
      String name = (String) names.get(k);
      // System.out.println("Signature name: " + name);
      // System.out.println("Signature covers whole document: " +
      // af.signatureCoversWholeDocument(name));
      // System.out.println("Document revision: " + af.getRevision(name) +
      // " of " + af.getTotalRevisions());

      try {
        PdfPKCS7 pk = af.verifySignature(name);
        Calendar cal = pk.getSignDate();
        Certificate pkc[] = pk.getCertificates();
        // System.out.println("Subject: " +
        // PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
        // System.out.println("Document modified: " + !pk.verify());

        Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
        if (fails == null) {
          // System.out.println("Certificates verified against the KeyStore");
        } else {
          // System.out.println("Certificate failed: " + fails[1]);
          result = 0;
        }
      } catch (NoSuchFieldError e) {
        LOGGER.warn("Problem verifying signature '" + name + "' of " + input.getFileName());
        result = -1;
      }

    }

    return result;
  }

  public static Path runDigitalSignatureExtract(Path input, String fileFormat) throws SignatureException, IOException {
    Path output = Files.createTempFile("extraction", ".txt");
    PdfReader reader = new PdfReader(input.toString());
    AcroFields fields = reader.getAcroFields();
    ArrayList<?> names = fields.getSignatureNames();
    PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output.toString()),
      StandardCharsets.UTF_8), true);

    for (int i = 0; i < names.size(); i++) {
      String name = (String) names.get(i);
      Item item = fields.getFieldItem(name);
      String infoResult = "";

      // TODO Filter the result information using RUPS
      PdfDictionary widget = item.getWidget(0);
      PdfDictionary info = widget.getAsDict(PdfName.V);

      infoResult = addElementToExtractionResult(infoResult, widget, PdfName.T, "String");
      infoResult = addElementToExtractionResult(infoResult, info, PdfName.NAME, "String");
      infoResult = addElementToExtractionResult(infoResult, info, PdfName.REASON, "String");
      infoResult = addElementToExtractionResult(infoResult, info, PdfName.LOCATION, "String");
      infoResult = addElementToExtractionResult(infoResult, info, PdfName.CONTACTINFO, "String");
      infoResult = addElementToExtractionResult(infoResult, info, PdfName.M, "String");
      infoResult = addElementToExtractionResult(infoResult, info, PdfName.FILTER, "Object");
      infoResult = addElementToExtractionResult(infoResult, info, PdfName.SUBFILTER, "Object");
      infoResult = addElementToExtractionResult(infoResult, widget, PdfName.FT, "Object");
      infoResult = addElementToExtractionResult(infoResult, widget, PdfName.LOCK, "Boolean");

      PdfDictionary ap = widget.getAsDict(PdfName.AP);
      PdfStream normal = ap.getAsStream(PdfName.N);
      PdfDictionary resources = normal.getAsDict(PdfName.RESOURCES);
      PdfDictionary xobject = resources.getAsDict(PdfName.XOBJECT);
      PdfStream frm = xobject.getAsStream(PdfName.FRM);
      PdfDictionary res = frm.getAsDict(PdfName.RESOURCES);
      PdfDictionary xobj = res.getAsDict(PdfName.XOBJECT);
      PdfStream n0 = xobj.getAsStream(PdfName.N0);
      PRStream n0stream = (PRStream) n0;
      PdfStream n2 = xobj.getAsStream(PdfName.N2);
      PRStream n2stream = (PRStream) n2;

      byte[] n2byteStream = PdfReader.getStreamBytes(n2stream);
      byte[] n0byteStream = PdfReader.getStreamBytes(n0stream);
      String streamResult = new String(n2byteStream);
      streamResult += new String(n0byteStream);

      streamResult = streamResult.replaceAll("\\)Tj[\n]*T\\*[\n]*\\(", "");
      streamResult = streamResult.replaceAll("\n[^\\(].*", "");
      streamResult = streamResult.replaceFirst(".*\n", "");
      streamResult = streamResult.replaceAll("\\((.*)\\)Tj", "$1");

      out.println("- Signature " + name + ":\n");
      out.println(infoResult);
    }

    out.close();
    return output;
  }

  public static Path runDigitalSignatureStrip(Path input, String fileFormat) throws IOException, DocumentException {
    Path output = Files.createTempFile("stripped", "." + fileFormat);
    PdfReader reader = new PdfReader(input.toString());
    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(output.toString()));
    stamper.setFormFlattening(true);
    stamper.close();
    return output;
  }

  private static String addElementToExtractionResult(String result, PdfDictionary parent, PdfName element, String type) {
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

  public static int countSignatures(File f) {
    int counter = 0;
    try {
      PdfReader reader = new PdfReader(ModelUtils.getFileStoragePath(f).asString());
      AcroFields af = reader.getAcroFields();
      ArrayList names = af.getSignatureNames();
      counter = names.size();
    } catch (IOException | RequestNotValidException e) {
      LOGGER.error("Error getting path of file " + f.getId());
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
