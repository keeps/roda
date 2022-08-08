package org.roda.core.plugins.base;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.FileFormatUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.base.conversion.CommandConvertPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class AbstractConvertPluginDummy<T extends IsRODAObject> extends CommandConvertPlugin<T> {

  private static final String TOOLNAME = "dummyconvert";

  public AbstractConvertPluginDummy() {
    super();
  }

  @Override
  public String getName() {
    return "Abstract Convert Plugin Dummy";
  }

  @Override
  public String getDescription() {
    return "Dummy command";
  }

  @Override
  public List<PluginParameter> getParameters() {
    Map<String, PluginParameter> parameters = super.getDefaultParameters();
    return super.orderParameters(parameters);
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<T> cloneMe() {
    return new AbstractConvertPluginDummy<>();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws IOException, CommandException, UnsupportedOperationException {

    return executeCommand(inputPath);
  }

  @Override
  public List<String> getApplicableTo() {
    return FileFormatUtils.getInputExtensions(TOOLNAME);
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", TOOLNAME, "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return FileFormatUtils.getPronomToExtension(TOOLNAME);
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return FileFormatUtils.getMimetypeToExtension(TOOLNAME);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return new Report();
  }

  public static String executeCommand(Path input)
          throws CommandException {

    String command = "stat " + input;

    List<String> commandList = Arrays.asList(command.split("\\s+"));

    // running the command
    return CommandUtility.execute(commandList, true);
  }
}
