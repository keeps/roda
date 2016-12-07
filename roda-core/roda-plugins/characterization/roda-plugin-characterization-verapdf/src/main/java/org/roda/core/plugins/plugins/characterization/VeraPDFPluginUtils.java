/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.storage.StringContentPayload;
import org.verapdf.core.VeraPDFException;
import org.verapdf.features.FeatureExtractorConfig;
import org.verapdf.features.FeatureFactory;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.metadata.fixer.MetadataFixerConfig;
import org.verapdf.pdfa.PdfBoxFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.validators.ValidatorConfig;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.ItemProcessor;
import org.verapdf.processor.ProcessorFactory;
import org.verapdf.processor.ProcessorResult;
import org.verapdf.processor.TaskType;

public class VeraPDFPluginUtils {

  public static Pair<StringContentPayload, Boolean> runVeraPDF(Path input, String profile, boolean hasFeatures)
    throws VeraPDFException, IOException, JAXBException {

    PdfBoxFoundryProvider.initialise();
    PDFAFlavour flavour = PDFAFlavour.byFlavourId(profile);

    ValidatorConfig validatorConfig = ValidatorFactory.createConfig(flavour, true, 10);
    FeatureExtractorConfig featureConfig = FeatureFactory.defaultConfig();
    MetadataFixerConfig fixerConfig = FixerFactory.defaultConfig();
    EnumSet<TaskType> tasks = EnumSet.of(TaskType.VALIDATE);

    if (hasFeatures) {
      tasks.add(TaskType.EXTRACT_FEATURES);
    }

    ItemProcessor processor = ProcessorFactory
      .createProcessor(ProcessorFactory.fromValues(validatorConfig, featureConfig, fixerConfig, tasks));

    ProcessorResult result = processor.process(input.toFile());
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    boolean prettyPrint = true;
    ProcessorFactory.resultToXml(result, os, prettyPrint);

    IOUtils.closeQuietly(os);
    StringContentPayload s = new StringContentPayload(os.toString(RodaConstants.DEFAULT_ENCODING));
    return Pair.create(s, result.getValidationResult().isCompliant());
  }

  public static List<String> getProfileList() {
    List<String> ret = new ArrayList<>();
    for (PDFAFlavour pdfaFlavour : PDFAFlavour.values()) {
      ret.add(pdfaFlavour.getId());
    }
    return ret;
  }
}
