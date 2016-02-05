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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ghost4j.GhostscriptException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.verapdf.core.VeraPDFException;

public class PdfToPdfaPlugin extends AbstractConvertPlugin {

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "PDF to PDFA conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a PDF/A file format from a PDF one that passes veraPDF validation.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new PdfToPdfaPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<PluginParameter>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    fillFileFormatStructures();
  }

  @Override
  public Path executePlugin(Binary binary, String fileFormat) throws UnsupportedOperationException, IOException,
    CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult = null;

    try {
      if (Files.exists(uriPath)) {
        pluginResult = PdfToPdfaPluginUtils.runPdfToPdfa(uriPath);
      } else {
        pluginResult = PdfToPdfaPluginUtils.runPdfToPdfa(binary.getContent().createInputStream());
      }
    } catch (VeraPDFException | GhostscriptException e) {
      logger.error("Error when running PDFtoPDFAPluginUtils ", e);
    }

    return pluginResult;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void fillFileFormatStructures() {
    outputFormat = "pdf";

    applicableTo.add("pdf");
    convertableTo.add("pdf");
    mimetypeToExtension.put("application/pdf", Arrays.asList("pdf"));
    pronomToExtension = PdfToPdfaPluginUtils.getPronomToExtension();
  }

}
