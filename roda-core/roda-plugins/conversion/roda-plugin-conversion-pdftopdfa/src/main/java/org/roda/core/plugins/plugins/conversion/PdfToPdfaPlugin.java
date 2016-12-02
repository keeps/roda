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

import org.ghost4j.GhostscriptException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.common.AbstractConvertPlugin;
import org.roda.core.plugins.plugins.common.FileFormatUtils;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.verapdf.core.VeraPDFException;

public class PdfToPdfaPlugin<T extends IsRODAObject> extends AbstractConvertPlugin<T> {
  private static final String TOOLNAME = "pdftopdfa";
  private static boolean validatePDF = true;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES, "Ignore non PDF files",
        PluginParameterType.BOOLEAN, "true", false, false,
        "Ignore files that are not identified as Portable Document Format (PDF)."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_IGNORE_VERAPDF_VALIDATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_IGNORE_VERAPDF_VALIDATION, "Use veraPDF validation",
        PluginParameterType.BOOLEAN, "true", false, false, "Use veraPDF validation and metadata fixing."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "Convert to a DIP", PluginParameterType.BOOLEAN, "true", false,
      false,
      "If this is selected then the plugin will convert the files to a new DIP. If not, a new representation will be created."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE, "Dissemination title",
        PluginParameterType.STRING, "PDF/A document", false, false,
        "If the 'create dissemination' option is checked, then this will be the respective dissemination title."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION, "Dissemination description", PluginParameterType.STRING,
      "PDF/A document converted from PDF", false, false,
      "If the 'create dissemination' option is checked, then this will be the respective dissemination description."));
  }

  public PdfToPdfaPlugin() {
    super();
    super.setInputFormat("pdf");
    super.setOutputFormat("pdf");
  }

  @Override
  public List<PluginParameter> getParameters() {
    List<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_VERAPDF_VALIDATION));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    setInputFormat("pdf");
    setOutputFormat("pdf");

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_IGNORE_VERAPDF_VALIDATION)) {
      validatePDF = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_VERAPDF_VALIDATION));
    }
  }

  public static String getStaticName() {
    return "PDF to PDF/A conversion (ghostscript)";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Converts standard Portable Document Format (PDF) files to PDF/A using the “ghostscript” tool. The results of conversion will "
      + "be placed on a new representation under the same Archival Information Package (AIP) where the files were originally found. A "
      + "PREMIS event is also recorded after the task is run.\nPDF/A is an ISO-standardized version of the Portable Document Format (PDF) "
      + "specialized for use in the archiving and long-term preservation of electronic documents. PDF/A differs from PDF by prohibiting "
      + "features ill-suited to long-term archiving, such as font linking (as opposed to font embedding) and encryption.\nThe ISO "
      + "requirements for PDF/A file viewers include colour management guidelines, support for embedded fonts, and a user interface "
      + "for reading embedded annotations.\nThe outcome of this action is a new representation where all the PDF files have been converted "
      + "to PDF/A. The resulting converted files will be valid, if wanted, according to VeraPDF (the Industry Supported PDF/A Validation tool).";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<T> cloneMe() {
    return new PdfToPdfaPlugin<T>();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {

    try {
      return PdfToPdfaPluginUtils.executePdfToPdfa(inputPath, outputPath, validatePDF);
    } catch (VeraPDFException | GhostscriptException e) {
      return null;
    }

  }

  @Override
  public List<String> getApplicableTo() {
    return Arrays.asList("pdf");
  }

  @Override
  public List<String> getConvertableTo() {
    return Arrays.asList("pdf");
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return FileFormatUtils.getPronomToExtension(TOOLNAME);
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    map.put("application/pdf", Arrays.asList("pdf"));
    return map;
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

}
