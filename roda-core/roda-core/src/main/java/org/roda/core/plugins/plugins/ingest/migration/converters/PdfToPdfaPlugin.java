/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration.converters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.ghost4j.GhostscriptException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.ingest.migration.GhostScriptConvertPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;
import org.verapdf.core.VeraPDFException;

public class PdfToPdfaPlugin extends GhostScriptConvertPlugin {

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    inputFormat = "pdf";
    outputFormat = "pdf";
    conversionProfile = "pdfToPdfa";
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new PdfToPdfaPlugin();
  }

  @Override
  public Path executePlugin(Binary binary) throws UnsupportedOperationException, IOException, CommandException {
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

}
