/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class VeraPDFPluginUtils {

  private static final LoadingCache<Pair<String, Boolean>, ItemProcessor> PROCESSOR_CACHE = CacheBuilder.newBuilder()
    .build(new CacheLoader<Pair<String, Boolean>, ItemProcessor>() {

      @Override
      public ItemProcessor load(Pair<String, Boolean> config) throws Exception {
        String profile = config.getFirst();
        Boolean hasFeatures = config.getSecond();

        PdfBoxFoundryProvider.initialise();
        PDFAFlavour flavour = PDFAFlavour.byFlavourId(profile);

        ValidatorConfig validatorConfig = ValidatorFactory.createConfig(flavour, true, 10);
        FeatureExtractorConfig featureConfig = FeatureFactory.defaultConfig();
        MetadataFixerConfig fixerConfig = FixerFactory.defaultConfig();
        EnumSet<TaskType> tasks = EnumSet.of(TaskType.VALIDATE);

        if (hasFeatures) {
          tasks.add(TaskType.EXTRACT_FEATURES);
        }

        return ProcessorFactory
          .createProcessor(ProcessorFactory.fromValues(validatorConfig, featureConfig, fixerConfig, tasks));
      }
    });

  public static Pair<StringContentPayload, Boolean> runVeraPDF(Path input, String profile, boolean hasFeatures)
    throws GenericException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      ItemProcessor processor = PROCESSOR_CACHE.get(Pair.create(profile, hasFeatures));
      ProcessorResult result = processor.process(input.toFile());

      boolean prettyPrint = true;
      ProcessorFactory.resultToXml(result, os, prettyPrint);

      StringContentPayload s = new StringContentPayload(os.toString(RodaConstants.DEFAULT_ENCODING));
      return Pair.create(s, result.getValidationResult().isCompliant());
    } catch (ExecutionException | VeraPDFException | JAXBException | UnsupportedEncodingException e) {
      throw new GenericException("Could not run VeraPDF: [" + e.getClass().getSimpleName() + "] " + e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(os);
    }
  }

  public static List<String> getProfileList() {
    List<String> ret = new ArrayList<>();
    for (PDFAFlavour pdfaFlavour : PDFAFlavour.values()) {
      ret.add(pdfaFlavour.getId());
    }
    return ret;
  }
}
