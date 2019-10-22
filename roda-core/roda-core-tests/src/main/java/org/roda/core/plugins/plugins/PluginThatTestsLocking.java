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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.akka.AkkaJobsManager;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;

/**
 * Plugin that does nothing (including not throwing any type of exception) and
 * therefore it must be possible to execute a Job with this plugin and ending up
 * with a Job in the state completed
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class PluginThatTestsLocking extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginThatTestsLocking.class);

  public static final String PLUGIN_DETAILS_AT_LEAST_ONE_LOCK_REQUEST_WAITING = "AT LEAST ONE WAITING";
  public static final String PLUGIN_PARAM_AUTO_LOCKING = "param.auto_locking";

  private boolean autoLoking = true;

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
    return new PluginThatTestsLocking();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {
    LOGGER.info("Doing nothing during init");
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    String autoLokingString = PluginHelper.getStringFromParameters(this, PLUGIN_PARAM_AUTO_LOCKING, "true");
    autoLoking = new Boolean(autoLokingString);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    LOGGER.info("Doing nothing during beforeAllExecute");
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {

    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {

      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // do nothing
        }
        for (AIP aip : objects) {
          Report reportItem = PluginHelper.initPluginReportItem(plugin, aip.getId(), AIP.class);
          addDetails(reportItem, new Date().toString());
          for (Entry<String, Counter> entry : RodaCoreFactory.getMetrics().getCounters().entrySet()) {
            if (entry.getKey().endsWith(AkkaJobsManager.LOCK_REQUESTS_WAITING_TO_ACQUIRE_LOCK)
              && entry.getValue().getCount() > 0) {
              addDetails(reportItem, PLUGIN_DETAILS_AT_LEAST_ONE_LOCK_REQUEST_WAITING);
            }
          }
          addDetails(reportItem, new Date().toString());
          reportItem.setPluginState(PluginState.SUCCESS);
          PluginHelper.updatePartialJobReport(plugin, model, reportItem, false, cachedJob);
          report.addReport(reportItem);
        }
      }
    }, index, model, storage, list, autoLoking);
  }

  private static void addDetails(Report report, String details) {
    if ("".equals(report.getPluginDetails())) {
      report.setPluginDetails(details);
    } else {
      report.addPluginDetails(report.getLineSeparator() + details);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
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
