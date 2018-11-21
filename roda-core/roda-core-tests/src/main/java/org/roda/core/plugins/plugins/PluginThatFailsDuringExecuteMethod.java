/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class PluginThatFailsDuringExecuteMethod extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginThatFailsDuringExecuteMethod.class);

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public String getDescription() {
    return getClass().getName();
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return null;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return null;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return null;
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<String> getCategories() {
    return Collections.emptyList();
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new PluginThatFailsDuringExecuteMethod();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() {
    LOGGER.info("Doing nothing during init");
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) {
    LOGGER.info("Doing nothing during beforeAllExecute");
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {

      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {

        String shouldFail = getParameterValues().get(object.getId());

        if (Boolean.parseBoolean(shouldFail)) {
          // 20190822 hsilva: no failure will be set in jobPluginInfo because
          // that's the job of the orchestrator (and because this tests
          // unexpected exception events, and because of that fact setting
          // failure because of that logic makes no sense)
          throw new IllegalAccessError(
            "tried to access method org.roda.core.data.v2.common.Pair.<init>(Ljava/lang/Object;Ljava/lang/Object;)V from class...");
        } else {
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        }
      }
    }, index, model, storage, list);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) {
    LOGGER.info("Doing nothing during afterAllExecute");
    return null;
  }

  @Override
  public void shutdown() {
    LOGGER.info("Doing nothing during shutdown");
  }

  @Override
  public String getVersionImpl() {
    return null;
  }

}
