package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;

public class UnoconvConvertPlugin extends CommandConvertPlugin {

  @Override
  public String getName() {
    return "Document conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a document format file from other document format one using Unoconv.";
  }

  @Override
  public String getAgentType() {
    return RodaConstants.PRESERVATION_AGENT_TYPE_SOFTWARE;
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
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {

    return UnoconvConvertPluginUtils.executeUnoconvConvert(inputPath, outputPath, super.getOutputFormat(),
      super.getCommandArguments());
  }

  @Override
  public List<String> getApplicableTo() {
    return UnoconvConvertPluginUtils.getInputExtensions();
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "unoconvconvert", "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return UnoconvConvertPluginUtils.getPronomToExtension();
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return UnoconvConvertPluginUtils.getMimetypeToExtension();
  }

}
