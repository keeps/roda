/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.verapdf.core.VeraPDFException;
import org.verapdf.features.pb.PBFeatureParser;
import org.verapdf.features.tools.FeaturesCollection;
import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.Profiles;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;
import org.verapdf.pdfa.validators.Validators;
import org.verapdf.report.MachineReadableReport;

public class VeraPDFPluginUtils {

  public static Path runVeraPDF(Path input, String profile, boolean hasFeatures)
    throws IOException, JAXBException, IllegalArgumentException, VeraPDFException {
    Path p = null;

    PDFAFlavour flavour = getFlavourFromProfileString(profile);
    boolean reportPassedChecks = false;
    long startTime = System.currentTimeMillis();

    InputStream streamPDF = new FileInputStream(input.toString());
    ModelParser loader = new ModelParser(streamPDF);

    // validation code
    ValidationProfile validationProfile = Profiles.getVeraProfileDirectory().getValidationProfileByFlavour(flavour);
    PDFAValidator validator = Validators.createValidator(validationProfile, true);
    ValidationResult result = validator.validate(loader);

    // features code
    FeaturesCollection featuresCollection = null;
    if (hasFeatures == true)
      featuresCollection = PBFeatureParser.getFeaturesCollection(loader.getPDDocument());

    // create XML report file
    p = Files.createTempFile("verapdf", ".xml");
    OutputStream os = new FileOutputStream(p.toFile());

    // create XML report
    MachineReadableReport mrr = MachineReadableReport.fromValues(input.toFile().getName(), validationProfile, result,
      reportPassedChecks, null, featuresCollection, System.currentTimeMillis() - startTime);
    MachineReadableReport.toXml(mrr, os, Boolean.TRUE);

    IOUtils.closeQuietly(os);
    IOUtils.closeQuietly(loader);
    IOUtils.closeQuietly(streamPDF);

    return p;
  }

  // function to transform profile arg string in a PDFA profile flavour
  private static PDFAFlavour getFlavourFromProfileString(String profile) {
    switch (profile) {
      case "1a":
        return PDFAFlavour.PDFA_1_A;
      case "1b":
        return PDFAFlavour.PDFA_1_B;
      case "2a":
        return PDFAFlavour.PDFA_2_A;
      case "2b":
        return PDFAFlavour.PDFA_2_B;
      case "2u":
        return PDFAFlavour.PDFA_2_U;
      case "3a":
        return PDFAFlavour.PDFA_3_A;
      case "3b":
        return PDFAFlavour.PDFA_3_B;
      case "3u":
        return PDFAFlavour.PDFA_3_U;
      default:
        return PDFAFlavour.PDFA_1_B;
    }
  }

  public static List<String> getProfileList() {
    return Arrays.asList("1a", "1b", "2a", "2b", "2u", "3a", "3b", "3u");
  }

}
