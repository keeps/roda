/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ghost4j.GhostscriptException;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;
import org.verapdf.core.VeraPDFException;

public class PdfToPdfaPlugin<T extends Serializable> extends AbstractConvertPlugin<T> {

  private static final String TOOLNAME = "pdftopdfa";

  public PdfToPdfaPlugin() {
    super.setInputFormat("pdf");
    super.setOutputFormat("pdf");
  }

  @Override
  public String getName() {
    return "PDF to PDFA conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a PDF/A format file, from a PDF one, that passes veraPDF validation.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<T> cloneMe() {
    return new PdfToPdfaPlugin<T>();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat) throws UnsupportedOperationException,
    IOException, CommandException {

    try {
      return PdfToPdfaPluginUtils.executePdfToPdfa(inputPath, outputPath);
    } catch (VeraPDFException | GhostscriptException e) {
      return null;
    }

  }

  @Override
  public List<String> getApplicableTo() {
    return Arrays.asList("pdf");
  }

  @Override
  public List<String> getConvertableTo() {
    return Arrays.asList("pdf");
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return FileFormatUtils.getPronomToExtension(TOOLNAME);
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    map.put("application/pdf", Arrays.asList("pdf"));
    return map;
  }

}
