/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.conversion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;

import org.ghost4j.Ghostscript;
import org.ghost4j.GhostscriptException;
import org.roda.core.util.CommandException;
import org.verapdf.core.VeraPDFException;
import org.verapdf.features.FeatureExtractorConfig;
import org.verapdf.features.FeatureFactory;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.metadata.fixer.MetadataFixerConfig;
import org.verapdf.pdfa.PdfBoxFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.MetadataFixerResult.RepairStatus;
import org.verapdf.pdfa.validation.validators.ValidatorConfig;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.ItemProcessor;
import org.verapdf.processor.ProcessorFactory;
import org.verapdf.processor.ProcessorResult;
import org.verapdf.processor.TaskType;

public class PdfToPdfaPluginUtils {

  public static String executePdfToPdfa(Path input, Path fixed, boolean validatePDF)
    throws IOException, CommandException {
    try {
      runGS(input, fixed);

      if (!validatePDF) {
        return "";
      }

      // metadata fixer transformation
      PdfBoxFoundryProvider.initialise();
      ValidatorConfig validatorConfig = ValidatorFactory.createConfig(PDFAFlavour.PDFA_1_B, true, 10);
      FeatureExtractorConfig featureConfig = FeatureFactory.defaultConfig();
      MetadataFixerConfig fixerConfig = FixerFactory.defaultConfig();
      EnumSet<TaskType> tasks = EnumSet.of(TaskType.VALIDATE, TaskType.FIX_METADATA);

      ItemProcessor processor = ProcessorFactory
        .createProcessor(ProcessorFactory.fromValues(validatorConfig, featureConfig, fixerConfig, tasks));

      ProcessorResult result = processor.process(fixed.toFile());

      RepairStatus fixStatus = result.getFixerResult().getRepairStatus();
      if (fixStatus.equals(RepairStatus.WONT_FIX) || fixStatus.equals(RepairStatus.FIX_ERROR)) {
        throw new CommandException("There were some metadata fixing errors on: " + input.toString());
      }

    } catch (GhostscriptException | VeraPDFException e) {
      return e.getMessage();
    }

    return "";
  }

  private static void runGS(Path input, Path output) throws GhostscriptException {
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
    gsArgs[8] = "-sOutputFile=" + output.toString();
    gsArgs[9] = input.toString();

    Ghostscript gs = Ghostscript.getInstance();

    try {
      gs.initialize(gsArgs);
      gs.exit();
    } catch (GhostscriptException e) {
      throw new GhostscriptException("Exception when using GhostScript: ", e);
    }
  }

}
