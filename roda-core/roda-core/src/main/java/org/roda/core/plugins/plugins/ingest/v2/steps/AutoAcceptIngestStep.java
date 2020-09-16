/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.v2.steps;

import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoAcceptIngestStep extends IngestStep {
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoAcceptIngestStep.class);

  public AutoAcceptIngestStep(String pluginName, String parameterName, boolean usesCorePlugin, boolean mandatory,
    boolean needsAips, boolean removesAIPs) {
    super(pluginName, parameterName, usesCorePlugin, mandatory, needsAips, removesAIPs);
  }

  public AutoAcceptIngestStep(String pluginName, String parameterName, boolean usesCorePlugin, boolean mandatory,
    boolean needsAips, boolean removesAIPs, Map<String, String> parameters) {
    super(pluginName, parameterName, usesCorePlugin, mandatory, needsAips, removesAIPs, parameters);
  }

  @Override
  public void execute(IngestStepBundle bundle) throws JobException {
    if (PluginHelper.verifyIfStepShouldBePerformed(bundle.getIngestPlugin(), bundle.getPluginParameter(),
      !this.usesCorePlugin() ? this.getPluginName() : null)) {
      IngestStepsUtils.executePlugin(bundle, this);
      PluginHelper.updateJobInformationAsync(bundle.getIngestPlugin(),
        bundle.getJobPluginInfo().incrementStepsCompletedByOne());

      if (RodaCoreFactory.getRodaConfiguration()
        .getBoolean(RodaConstants.CORE_TRANSFERRED_RESOURCES_INGEST_MOVE_WHEN_AUTOACCEPT, false)) {
        PluginHelper.moveSIPs(bundle.getIngestPlugin(), bundle.getModel(), bundle.getIndex(), bundle.getResources(),
          bundle.getJobPluginInfo());
      }
    } else {
      IngestStepsUtils.updateAIPsToBeAppraised(bundle, bundle.getCachedJob());
    }
  }
}
