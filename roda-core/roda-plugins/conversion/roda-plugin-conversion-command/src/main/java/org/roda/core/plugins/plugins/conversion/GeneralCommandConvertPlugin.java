/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.conversion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.common.CommandConvertPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;

public class GeneralCommandConvertPlugin<T extends IsRODAObject> extends CommandConvertPlugin<T> {

  public GeneralCommandConvertPlugin() {
    super();
  }

  @Override
  public String getName() {
    return "General command based conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a new format file using a generic command line.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<T> cloneMe() {
    return new GeneralCommandConvertPlugin<T>();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws CommandException, UnsupportedOperationException, IOException {
    return GeneralCommandConvertPluginUtils.executeGeneralCommand(inputPath, outputPath, super.getCommandArguments());
  }

  @Override
  public List<String> getApplicableTo() {
    return new ArrayList<String>();
  }

  @Override
  public List<String> getConvertableTo() {
    return new ArrayList<String>();
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return new HashMap<String, List<String>>();
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return new HashMap<String, List<String>>();
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE, RodaConstants.PLUGIN_CATEGORY_CONVERSION,
      RodaConstants.PLUGIN_CATEGORY_DISSEMINATION);
  }

}
