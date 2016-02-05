/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ghost4j.Ghostscript;
import org.ghost4j.GhostscriptException;
import org.roda.core.RodaCoreFactory;
import org.verapdf.core.ValidationException;
import org.verapdf.core.VeraPDFException;
import org.verapdf.metadata.fixer.impl.MetadataFixerImpl;
import org.verapdf.metadata.fixer.impl.pb.FixerConfigImpl;
import org.verapdf.metadata.fixer.utils.FixerConfig;
import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.Profiles;
import org.verapdf.pdfa.validation.ValidationProfile;
import org.verapdf.pdfa.validators.Validators;

public class PdfToPdfaPluginUtils {

  public static Path runPdfToPdfa(InputStream pdfInputStream) throws IOException, VeraPDFException,
    GhostscriptException {

    // write pdf inputstream data on a new file (absolute path needed when
    // running GS)
    Path p = Files.createTempFile("pdf_copy", ".pdf");
    byte[] buffer = new byte[pdfInputStream.available()];
    pdfInputStream.read(buffer);
    OutputStream os = new FileOutputStream(p.toFile());
    os.write(buffer);
    os.close();
    pdfInputStream.close();

    return executePdfToPdfa(p);
  }

  public static Path runPdfToPdfa(Path p) throws IOException, VeraPDFException, GhostscriptException {
    return executePdfToPdfa(p);
  }

  private static Path executePdfToPdfa(Path p) throws IOException, VeraPDFException, GhostscriptException {

    // pdfa - file to save the GS output
    // fixed - file to save the fixed representation
    Path pdfa = Files.createTempFile("pdfa", ".pdf");
    Path fixed = Files.createTempFile("pdfa_fixed", ".pdf");

    // GhostScript transformation command
    String[] gsArgs = new String[10];
    gsArgs[0] = "gs";
    gsArgs[1] = "-dPDFA";
    gsArgs[2] = "-dBATCH";
    gsArgs[3] = "-dNOPAUSE";
    gsArgs[4] = "-dUseCIEColor";
    gsArgs[5] = "-sProcessColorModel=DeviceCMYK";
    gsArgs[6] = "-sDEVICE=pdfwrite";
    gsArgs[7] = "-sPDFACompatibilityPolicy=1";
    gsArgs[8] = "-sOutputFile=" + pdfa.toString();
    gsArgs[9] = p.toString();

    Ghostscript gs = Ghostscript.getInstance();

    try {
      gs.initialize(gsArgs);
      gs.exit();
    } catch (GhostscriptException e) {
      throw new GhostscriptException("Exception when using GhostScript: ", e);
    }

    // metadata fixer transformation
    InputStream is = new FileInputStream(pdfa.toString());

    try (ModelParser loader = new ModelParser(is)) {

      // validation code
      ValidationProfile profile = Profiles.getVeraProfileDirectory()
        .getValidationProfileByFlavour(PDFAFlavour.PDFA_1_B);
      PDFAValidator validator = Validators.createValidator(profile, true);
      ValidationResult result = validator.validate(loader);
      is.close();

      // fixing metadata
      OutputStream fixedOutputStream = new FileOutputStream(fixed.toString());
      FixerConfig fconf = FixerConfigImpl.getFixerConfig(loader.getPDDocument(), result);
      MetadataFixerImpl.fixMetadata(fixedOutputStream, fconf);
      fixedOutputStream.close();
      loader.close();

    } catch (ValidationException | FileNotFoundException e) {
      throw new VeraPDFException("Exception when fixing metadata: ", e);
    }

    return fixed;
  }

  /*************************** FILLING FILE FORMAT STRUCTURES ***************************/

  public static Map<String, List<String>> getPronomToExtension() {
    Map<String, List<String>> map = new HashMap<>();
    String inputFormatPronoms = RodaCoreFactory
      .getRodaConfigurationAsString("tools", "pdftopdfa", "inputFormatPronoms");

    for (String pronom : Arrays.asList(inputFormatPronoms.split(" "))) {
      // TODO add missing pronoms
      String mimeExtensions = RodaCoreFactory.getRodaConfigurationAsString("tools", "pronom", pronom);

      if (mimeExtensions == null)
        System.out.println("MIME: " + pronom);
      map.put(pronom, Arrays.asList(mimeExtensions.split(" ")));
    }

    return map;
  }

}
