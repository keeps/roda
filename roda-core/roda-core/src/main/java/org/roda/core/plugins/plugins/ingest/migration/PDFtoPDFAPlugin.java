/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.ghost4j.GhostscriptException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.verapdf.core.VeraPDFException;

public class PDFtoPDFAPlugin extends AbstractConvertPlugin {

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
    return "Generates PDFa format files from PDF files allowing them to pass on veraPDF validation";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new PDFtoPDFAPlugin();
  }

  @Override
  public Path executePlugin(Binary binary) throws UnsupportedOperationException, IOException, CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult = null;

    try {
      if (Files.exists(uriPath)) {
        pluginResult = PDFtoPDFAPluginUtils.runPDFtoPDFA(uriPath);
      } else {
        pluginResult = PDFtoPDFAPluginUtils.runPDFtoPDFA(binary.getContent().createInputStream());
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
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

}
