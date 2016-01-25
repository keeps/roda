package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;

public class SoxSoundConvertPlugin extends AbstractConvertPlugin {

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
    return "Sound conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a sound format file from other sound format one using SOX.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new SoxSoundConvertPlugin();
  }

  @Override
  public Path executePlugin(Binary binary) throws UnsupportedOperationException, IOException, CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult;

    if (Files.exists(uriPath)) {
      pluginResult = SoxSoundConvertPluginUtils.runSoxSoundConvert(uriPath, inputFormat, outputFormat);
    } else {
      pluginResult = SoxSoundConvertPluginUtils.runSoxSoundConvert(binary.getContent().createInputStream(),
        inputFormat, outputFormat);
    }

    return pluginResult;
  }

  @Override
  public void createEvent(List<String> alteredFiles, AIP aip, String representationID, String newRepresentionID,
    ModelService model, int state) throws PluginException {
    // TODO Auto-generated method stub
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
