package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;

public class ImageMagickConvertPlugin extends AbstractConvertPlugin {

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
    return "Image conversion";
  }

  @Override
  public String getDescription() {
    return "Generates an image format file from other image format one using Imagemagick.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ImageMagickConvertPlugin();
  }

  @Override
  public PluginType getType() {
    return super.getType();
  }

  @Override
  public boolean areParameterValuesValid() {
    return super.areParameterValuesValid();
  }

  @Override
  public List<PluginParameter> getParameters() {
    return super.getParameters();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return super.getParameterValues();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    return super.execute(index, model, storage, list);
  }

  public Path executePlugin(Binary binary) throws UnsupportedOperationException, IOException, CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult;

    if (Files.exists(uriPath)) {
      pluginResult = ImageMagickConvertPluginUtils.runImageMagickConvert(uriPath, inputFormat, outputFormat);
    } else {
      pluginResult = ImageMagickConvertPluginUtils.runImageMagickConvert(binary.getContent().createInputStream(),
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