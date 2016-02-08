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

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;

public class JodConverterPlugin extends AbstractConvertPlugin {

  @Override
  public String getName() {
    return "Document conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a document format file from other document format one using JODConverter.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new JodConverterPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "jodconverter", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));

    List<PluginParameter> params = new ArrayList<PluginParameter>();

    PluginParameter outputParam = new PluginParameter("outputParams", "Output parameters", PluginParameterType.STRING,
      "", convertableTo, true, true, "Lists the possible output formats");

    PluginParameter commandArgs = new PluginParameter("commandArgs", "Command arguments", PluginParameterType.STRING,
      "", true, true, "Command arguments to modify the command to execute");

    params.add(outputParam);
    params.add(commandArgs);
    return params;
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
    Path pluginResult;

    if (Files.exists(uriPath)) {
      pluginResult = JodConverterPluginUtils.runJodConverter(uriPath, fileFormat, outputFormat);
    } else {
      pluginResult = JodConverterPluginUtils.runJodConverter(binary.getContent().createInputStream(), fileFormat,
        outputFormat);
    }

    return pluginResult;
  }

  @Override
  public void fillFileFormatStructures() {
    pronomToExtension = JodConverterPluginUtils.getPronomToExtension();
    mimetypeToExtension = JodConverterPluginUtils.getMimetypeToExtension();
    applicableTo = JodConverterPluginUtils.getInputExtensions();

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "jodconverter", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
  }

}
