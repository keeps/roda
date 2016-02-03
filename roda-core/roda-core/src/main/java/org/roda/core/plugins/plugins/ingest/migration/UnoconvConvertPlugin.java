package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
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

public class UnoconvConvertPlugin extends CommandConvertPlugin {

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
    return "Document conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a document format file from other document format one using Unoconv.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new UnoconvConvertPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "unoconvconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));

    return super.getParameters();
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    fillFileFormatStructures();
  }

  @Override
  public Path executePlugin(Binary binary, String fileFormat) throws UnsupportedOperationException, IOException,
    CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult;

    if (Files.exists(uriPath)) {
      String mime = Files.probeContentType(uriPath);
      pluginResult = UnoconvConvertPluginUtils.runUnoconvConvert(uriPath, fileFormat, outputFormat, commandArguments);
    } else {
      pluginResult = UnoconvConvertPluginUtils.runUnoconvConvert(binary.getContent().createInputStream(), fileFormat,
        outputFormat, commandArguments);
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

  @Override
  public void fillFileFormatStructures() {
    pronomToExtension = UnoconvConvertPluginUtils.getPronomToExtension();
    mimetypeToExtension = UnoconvConvertPluginUtils.getMimetypeToExtension();
    applicableTo = UnoconvConvertPluginUtils.getInputExtensions();

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "imagemagickconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
  }

}
