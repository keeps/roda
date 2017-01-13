/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

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
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.validators.ValidatorConfig;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.ItemProcessor;
import org.verapdf.processor.ProcessorFactory;
import org.verapdf.processor.ProcessorResult;
import org.verapdf.processor.TaskType;
import org.verapdf.report.HTMLReport;
import org.verapdf.report.XsltTransformer;

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

        VeraGreenfieldFoundryProvider.initialise();
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

  private static final String resourceRoot = "org/verapdf/report/";
  private static final String detailedReport = resourceRoot + "DetailedHtmlReport.xsl";

  public static Pair<StringContentPayload, Boolean> runVeraPDF(Path input, String profile, boolean hasFeatures)
    throws GenericException {

    ByteArrayOutputStream xmlOutput = new ByteArrayOutputStream();
    ByteArrayOutputStream htmlOutput = new ByteArrayOutputStream();
    ByteArrayInputStream xmlInput = null;
    try {
      ItemProcessor processor = PROCESSOR_CACHE.get(Pair.create(profile, hasFeatures));
      ProcessorResult result = processor.process(input.toFile());

      boolean prettyPrint = true;
      ProcessorFactory.resultToXml(result, xmlOutput, prettyPrint);

      xmlInput = new ByteArrayInputStream(xmlOutput.toByteArray());
      Map<String, String> arguments = new HashMap<>();
      arguments.put("wikiPath", "");
      arguments.put("isFullHTML", Boolean.toString(true));
      XsltTransformer.transform(xmlInput, HTMLReport.class.getClassLoader().getResourceAsStream(detailedReport),
        htmlOutput, arguments);

      StringContentPayload s = new StringContentPayload(htmlOutput.toString(RodaConstants.DEFAULT_ENCODING));
      return Pair.create(s, result.getValidationResult().isCompliant());
    } catch (ExecutionException | VeraPDFException | JAXBException | UnsupportedEncodingException
      | TransformerException e) {
      throw new GenericException("Could not run VeraPDF: [" + e.getClass().getSimpleName() + "] " + e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(xmlOutput);
      IOUtils.closeQuietly(htmlOutput);
      IOUtils.closeQuietly(xmlInput);
    }
  }

  public static List<String> getProfileList() {
    List<String> ret = new ArrayList<>();
    for (PDFAFlavour pdfaFlavour : PDFAFlavour.values()) {
      ret.add(pdfaFlavour.getId());
    }
    return ret;
  }

  public static String createOtherMetadataDownloadUri(String fileUUID, String type, String suffix) {
    // api/v1/files/{fileUUID}/other_metadata/{type}/{suffix}?acceptFormat=bin
    StringBuilder b = new StringBuilder();
    b.append(RodaConstants.API_REST_V1_FILES).append(fileUUID).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_OTHER_METADATA).append(RodaConstants.API_SEP).append(type).append(RodaConstants.API_SEP)
      .append(suffix).append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);
    return b.toString();
  }
}
