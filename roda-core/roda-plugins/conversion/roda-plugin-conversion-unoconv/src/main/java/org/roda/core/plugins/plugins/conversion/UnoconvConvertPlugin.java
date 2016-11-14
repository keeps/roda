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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.common.CommandConvertPlugin;
import org.roda.core.plugins.plugins.common.FileFormatUtils;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.slf4j.LoggerFactory;

public class UnoconvConvertPlugin<T extends IsRODAObject> extends CommandConvertPlugin<T> {
  private static final String TOOLNAME = "unoconvconvert";

  public UnoconvConvertPlugin() {
    super();
  }

  @Override
  public String getName() {
    return "Office documents conversion (unoconv)";
  }

  @Override
  public String getDescription() {
    return "Converts office files using the “unoconv” (Universal Office Converter). The results of conversion will be placed on a new representation under the same Archival Information Package (AIP) where the files were originally found. A PREMIS event is also recorded after the task is run.\n“unoconv” is a tool that converts between any document format that OpenOffice understands. It uses OpenOffice's UNO bindings for non-interactive conversion of documents. \nSupported document formats include Open Document Format (odt), MS Word (doc), MS Office Open/MS OOXML (ooxml), Portable Document Format (pdf), HTML (html), XHTML (xhtml), RTF (rtf), Docbook (docbook), and more.\nThe outcome of this task is the creation of a new OpenOffice (and thus unoconv) support various import and export formats. Not all formats that can be imported can be exported and vice versa. For a full list of supported formats, please visit - http://dag.wiee.rs/home-made/unoconv/";
  }

  @Override
  public String getVersionImpl() {
    try {
      return UnoconvConvertPluginUtils.getVersion();
    } catch (UnsupportedOperationException | CommandException | IOException e) {
      LoggerFactory.getLogger(UnoconvConvertPlugin.class).debug("Error getting unoconv version");
      return "1.0";
    }
  }

  @Override
  public Plugin<T> cloneMe() {
    return new UnoconvConvertPlugin<T>();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {

    return UnoconvConvertPluginUtils.executeUnoconvConvert(inputPath, outputPath, super.getOutputFormat(),
      super.getCommandArguments());
  }

  @Override
  public List<String> getApplicableTo() {
    // TODO add missing extensions
    return FileFormatUtils.getInputExtensions(TOOLNAME);
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", TOOLNAME, "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    // TODO add missing pronoms
    return FileFormatUtils.getPronomToExtension(TOOLNAME);
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    // TODO add missing mimetypes
    return FileFormatUtils.getMimetypeToExtension(TOOLNAME);
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_CONVERSION);
  }

  @Override
  public String getDIPTitle() {
    return "Unoconv DIP title";
  }

  @Override
  public String getDIPDescription() {
    return "Unoconv DIP description";
  }

}
