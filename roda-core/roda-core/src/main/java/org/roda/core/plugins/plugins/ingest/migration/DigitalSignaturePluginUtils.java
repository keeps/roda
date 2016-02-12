package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Calendar;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;

public class DigitalSignaturePluginUtils {

  public static Path runDigitalSignatureVerify(Path input, String fileFormat) throws SignatureException, IOException {
    KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
    System.out.println("TESTE");

    PdfReader reader = new PdfReader(input.toString());
    AcroFields af = reader.getAcroFields();

    // Search of the whole signature
    ArrayList names = af.getSignatureNames();

    // For every signature :
    for (int k = 0; k < names.size(); k++) {
      String name = (String) names.get(k);
      System.out.println("Signature name: " + name);
      System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
      System.out.println("Document revision: " + af.getRevision(name) + " of " + af.getTotalRevisions());

      Path outputFile = Files.createTempFile("sign", ".pdf");
      FileOutputStream out = new FileOutputStream(outputFile.toString());
      byte bb[] = new byte[8192];
      InputStream ip = af.extractRevision(name);
      int n = 0;
      while ((n = ip.read(bb)) > 0)
        out.write(bb, 0, n);
      out.close();
      ip.close();

      PdfPKCS7 pk = af.verifySignature(name);
      Calendar cal = pk.getSignDate();
      java.security.cert.Certificate[] pkc = pk.getCertificates();

      System.out.println("Subject: " + PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
      System.out.println("Document modified: " + !pk.verify());

      Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
      if (fails == null)
        System.out.println("Certificates verified against the KeyStore");
      else
        System.out.println("Certificate failed: " + fails[1]);
    }
    return null;
  }

  public static Path runDigitalSignatureExtract(Path input, String fileFormat) {
    return null;
  }

  public static Path runDigitalSignatureStrip(Path input, String fileFormat) {
    return null;
  }

}
