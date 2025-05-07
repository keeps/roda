/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.multiple;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.MultipleJobPluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class NoObjectsMultipleStepPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(NoObjectsMultipleStepPlugin.class);

  protected int totalSteps;

  protected int sourceObjectsCount;

  private boolean sourceObjectsCountSet = false;

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public PluginType getType() {
    return PluginType.MULTI;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model)
    throws PluginException {
    // do nothing
    LOGGER.debug("Doing nothing in beforeAllExecute");
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model,
                        List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
                          JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        processInternally(index, model, report, jobPluginInfo, cachedJob);
      }
    }, index, model);
  }

  protected void processInternally(IndexService index, ModelService model, Report report,
    JobPluginInfo outerJobPluginInfo, Job cachedJob) {
    try {
      boolean updateMetaPluginInformation = true;
      final MultipleJobPluginInfo jobPluginInfo = (MultipleJobPluginInfo) outerJobPluginInfo;
      jobPluginInfo.setTotalSteps(getTotalSteps());

      List<Step> steps = getPluginSteps();
      if (sourceObjectsCountSet) {
        jobPluginInfo.setSourceObjectsCount(sourceObjectsCount);
      }

      PluginHelper.updateJobInformationAsync(this, jobPluginInfo);
      for (Step step : steps) {
        MultipleStepBundle bundle = new MultipleStepBundle(this, index, model, jobPluginInfo,
          getPluginParameter(step.getParameterName()), getParameterValues(), Collections.emptyList(), cachedJob);
        step.execute(bundle);
        if (updateMetaPluginInformation) {
          updateMetaPluginInformation = false;
          jobPluginInfo.updateMetaPluginInformation(report, cachedJob);
          PluginHelper.updateJobReportMetaPluginInformation(this, model, report, cachedJob, jobPluginInfo);
        }
      }

      jobPluginInfo.updateSourceObjectsProcessed();
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo);
    } catch (JobException e) {
      // do nothing
    } finally {
      // remove locks if any
      PluginHelper.releaseObjectLock(this);
    }
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    totalSteps = calculateEffectiveTotalSteps();
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS, Integer.toString(getTotalSteps()));
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, getClass().getName());
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public abstract void setTotalSteps();

  public void setSourceObjectsCount(int value) {
    sourceObjectsCount = value;
    sourceObjectsCountSet = true;
  }

  public abstract List<Step> getPluginSteps();

  public abstract PluginParameter getPluginParameter(String pluginParameterId);

  private int calculateEffectiveTotalSteps() {
    int effectiveTotalSteps = getTotalSteps();

    for (Step step : getPluginSteps()) {
      if (!PluginHelper.verifyIfStepShouldBePerformed(this, getPluginParameter(step.getParameterName()))) {
        effectiveTotalSteps--;
      }
    }

    return effectiveTotalSteps;
  }
}
