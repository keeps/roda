/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.multiple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.base.antivirus.AntivirusPlugin;
import org.roda.core.plugins.base.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MultiplePlugin extends DefaultMultipleStepPlugin<AIP> {
  private static List<Step> steps = new ArrayList<>();
  private static HashMap<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, AntivirusPlugin.getStaticName(),
          PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(AntivirusPlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON, PremisSkeletonPlugin.getStaticName(),
          PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(PremisSkeletonPlugin.getStaticDescription())
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION, SiegfriedPlugin.getStaticName(),
          PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(SiegfriedPlugin.getStaticDescription()).build());

    steps.add(new Step(AntivirusPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, true, true));
    steps.add(
      new Step(PremisSkeletonPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON, true, true));
    steps.add(
      new Step(SiegfriedPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION, true, true));
  }

  @Override
  public List<PluginParameter> getParameters() {
    final ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION));
    return parameters;
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  /**
   * Returns the name of this {@link Plugin}.
   *
   * @return a {@link String} with the name of this {@link Plugin}.
   */
  @Override
  public String getName() {
    return "Multiple step";
  }

  /**
   * Returns description of this {@link Plugin}.
   *
   * @return a {@link String} with the description of this {@link Plugin}.
   */
  @Override
  public String getDescription() {
    return "test";
  }

  /**
   * Returns the type of the execution preservation event linked to this
   * {@link Plugin}.
   *
   * @return a {@link PreservationEventType} with the type of the execution event
   *         of this {@link Plugin}.
   */
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  /**
   * Returns the description of the execution preservation event linked to this
   * {@link Plugin}.
   *
   * @return a {@link String} with the description of the execution event of this
   *         {@link Plugin}.
   */
  @Override
  public String getPreservationEventDescription() {
    return null;
  }

  /**
   * Returns the success message of the execution preservation event linked to
   * this {@link Plugin}.
   *
   * @return a {@link String} with the success message of the execution event of
   *         this {@link Plugin}.
   */
  @Override
  public String getPreservationEventSuccessMessage() {
    return null;
  }

  /**
   * Returns the failure message of the execution preservation event linked to
   * this {@link Plugin}.
   *
   * @return a {@link String} with the failure message of the execution event of
   *         this {@link Plugin}.
   */
  @Override
  public String getPreservationEventFailureMessage() {
    return null;
  }

  /**
   * Method to return Plugin type (so it can be grouped for different purposes)
   */
  @Override
  public PluginType getType() {
    return PluginType.MULTI;
  }

  /**
   * Method to return Plugin categories
   */
  @Override
  public List<String> getCategories() {
    return Collections.emptyList();
  }

  /**
   * Method used by PluginManager to obtain a new instance of a plugin, from the
   * current loaded Plugin, to provide to PluginOrchestrator
   */
  @Override
  public Plugin<AIP> cloneMe() {
    return new MultiplePlugin();
  }

  /**
   * Method that validates the parameters provided to the Plugin
   * <p>
   * FIXME this should be changed to return a report
   */
  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Collections.singletonList(AIP.class);
  }

  /**
   * Method executed by {@link org.roda.core.plugins.PluginOrchestrator} after all
   * workers have finished their work
   *
   * @param index
   * @param model
   * @throws PluginException
   */
  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    return null;
  }

  @Override
  public void setTotalSteps() {
    this.totalSteps = steps.size();
  }

  @Override
  public List<Step> getPluginSteps() {
    return steps;
  }

  @Override
  public PluginParameter getPluginParameter(String pluginParameterId) {
    if (pluginParameters.get(pluginParameterId) != null) {
      return pluginParameters.get(pluginParameterId);
    } else {
      return new PluginParameter();
    }
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    setTotalSteps();
    super.setParameterValues(parameters);
  }
}
