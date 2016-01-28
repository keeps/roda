package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.ghost4j.GhostscriptException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;

public abstract class GhostScriptConvertPlugin extends AbstractConvertPlugin {

  public String device;

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
    return "GhostScript conversion";
  }

  @Override
  public String getDescription() {
    return "Converts files using GhostScript.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public abstract Plugin<AIP> cloneMe();

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // defines a ghostscript device
    if (parameters.containsKey("device")) {
      device = parameters.get("device");
    }
  }

  @Override
  public Path executePlugin(Binary binary) throws UnsupportedOperationException, IOException, CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult = null;

    try {

      if (Files.exists(uriPath)) {
        pluginResult = GhostScriptConvertPluginUtils.runGhostScriptConvert(uriPath, inputFormat, outputFormat, device,
          conversionProfile);
      } else {
        pluginResult = GhostScriptConvertPluginUtils.runGhostScriptConvert(binary.getContent().createInputStream(),
          inputFormat, outputFormat, device, conversionProfile);
      }

    } catch (GhostscriptException e) {
      throw new CommandException("Exception when using GhostScript: ", e);
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
