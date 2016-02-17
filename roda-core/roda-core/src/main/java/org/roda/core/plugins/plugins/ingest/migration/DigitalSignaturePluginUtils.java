package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.AcroFields.Item;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfStream;

public class DigitalSignaturePluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PremisSkeletonPlugin.class);

  public static String runDigitalSignatureVerify(Path input, String fileFormat) throws IOException {

    KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
    PdfReader reader = new PdfReader(input.toString());
    AcroFields af = reader.getAcroFields();
    ArrayList names = af.getSignatureNames();
    int result = 1;

    for (int k = 0; k < names.size(); k++) {
      String name = (String) names.get(k);
      System.out.println("Signature name: " + name);
      System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
      System.out.println("Document revision: " + af.getRevision(name) + " of " + af.getTotalRevisions());

      try {
        PdfPKCS7 pk = af.verifySignature(name);
        Calendar cal = pk.getSignDate();
        Certificate pkc[] = pk.getCertificates();
        System.out.println("Subject: " + PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
        System.out.println("Document modified: " + !pk.verify());

        Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
        if (fails == null) {
          System.out.println("Certificates verified against the KeyStore");
        } else {
          System.out.println("Certificate failed: " + fails[1]);
          result = 0;
        }
      } catch (SignatureException | NoSuchFieldError e) {
        LOGGER.warn("Problem verifying signature '" + name + "' of " + input.getFileName());
        result = -1;
      }

    }

    return "RESULT: " + result;
  }

  public static Path runDigitalSignatureExtract(Path input, String fileFormat) throws SignatureException, IOException {
    Path output = Files.createTempFile("extraction", ".txt");
    PdfReader reader = new PdfReader(input.toString());
    AcroFields fields = reader.getAcroFields();
    ArrayList<?> names = fields.getSignatureNames();
    PrintWriter out = new PrintWriter(output.toString());

    for (int i = 0; i < names.size(); i++) {
      String name = (String) names.get(i);
      Item item = fields.getFieldItem(name);

      // TODO Filter the result information using RUPS
      PdfDictionary widget = item.getWidget(0);
      PdfDictionary ap = widget.getAsDict(PdfName.AP);
      PdfStream normal = ap.getAsStream(PdfName.N);
      PdfDictionary resources = normal.getAsDict(PdfName.RESOURCES);
      PdfDictionary xobject = resources.getAsDict(PdfName.XOBJECT);
      PdfStream frm = xobject.getAsStream(PdfName.FRM);
      PdfDictionary res = frm.getAsDict(PdfName.RESOURCES);
      PdfDictionary xobj = res.getAsDict(PdfName.XOBJECT);
      PdfStream n2 = xobj.getAsStream(PdfName.N2);
      PRStream n2stream = (PRStream) n2;
      byte[] stream = PdfReader.getStreamBytes(n2stream);

      String streamResult = new String(stream);
      out.println("Signature " + i + ":");
      out.println(streamResult);
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
