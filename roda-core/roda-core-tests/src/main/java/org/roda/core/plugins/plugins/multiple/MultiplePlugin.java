package org.roda.core.plugins.plugins.multiple;

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
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.characterization.SiegfriedPlugin;
import org.roda.core.storage.StorageService;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MultiplePlugin extends DefaultMultipleStepPlugin<AIP> {
  private static List<Step> steps = new ArrayList<>();
  static {
    steps.add(new Step(AntivirusPlugin.class.getName(), AntivirusPlugin.class, RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, true, true));
    steps.add(new Step(PremisSkeletonPlugin.class.getName(), PremisSkeletonPlugin.class, "",
        true, true));
    steps.add(new Step(SiegfriedPlugin.class.getName(), SiegfriedPlugin.class, "",
      true, true));
  }

  private Map<String, PluginParameter> pluginParameters = new HashMap<>();

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
   * Method executed by {@link PluginOrchestrator} after all workers have finished
   * their work
   *
   * @param index
   * @param model
   * @param storage
   * @throws PluginException
   */
  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
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
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, AntivirusPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "true", true, true, AntivirusPlugin.getStaticDescription()));

    if (pluginParameters.get(pluginParameterId) != null) {
      return pluginParameters.get(pluginParameterId);
    } else {
      return new PluginParameter();
    }
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, AntivirusPlugin.getStaticName(),
            PluginParameterType.BOOLEAN, "true", true, true, AntivirusPlugin.getStaticDescription()));
    setTotalSteps();
    super.setParameterValues(parameters);
  }
}
