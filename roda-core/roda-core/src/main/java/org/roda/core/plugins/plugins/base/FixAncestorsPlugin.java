/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME 20161202 hsilva: expose params PLUGIN_PARAMS_PARENT_ID
public class FixAncestorsPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FixAncestorsPlugin.class);

  private String originalJobId;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OTHER_JOB_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OTHER_JOB_ID, "Ingest job identifier", PluginParameterType.STRING,
        "", true, false, "The identifier of the job responsible to ingest the information package to fix."));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "AIP ancestor hierarchy fix";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Attempts to fix the ancestor hierarchy of the AIPs in the catalogue by removing ghosts (i.e. AIPs with nonexistent ancestors in the catalogue) "
      + "and merging AIPs with the same Ingest SIP identifier.\nThis task aims to fix problems that may occur when SIPs are ingested but not all the "
      + "necessary items to construct the catalogue hierarchy have been received or properly ingested.";
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
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OTHER_JOB_ID));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (getParameterValues().containsKey(RodaConstants.PLUGIN_PARAMS_OTHER_JOB_ID)) {
      originalJobId = getParameterValues().get(RodaConstants.PLUGIN_PARAMS_OTHER_JOB_ID);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {

    final int counter = calculateSourceObjectsCount(index);
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        fixAncestors(index, model, report, jobPluginInfo, jobPluginInfo.getSourceObjectsCount());
      }
    }, index, model, storage, counter);
  }

  private int calculateSourceObjectsCount(IndexService index) {
    int count;
    try {
      count = index.count(IndexedAIP.class,
        new Filter(new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.TRUE.toString()))).intValue();

      // XXX 20160929 Is it really needed? (it is there to be possible to get
      // 100% done reports)
      if (count == 0) {
        count = index.count(IndexedAIP.class, Filter.ALL).intValue();
      }
    } catch (RequestNotValidException | GenericException e) {
      count = 0;
    }

    return count;
  }

  private void fixAncestors(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo,
    int counter) {
    try {
      Optional<String> computedSearchScope = PluginHelper.getSearchScopeFromParameters(this, model);
      Job originalJob = PluginHelper.getJob(originalJobId, model);
      PluginHelper.fixParents(index, model, Optional.ofNullable(originalJob.getId()), computedSearchScope,
        originalJob.getUsername());
      jobPluginInfo.incrementObjectsProcessedWithSuccess(counter);
      report.setPluginState(PluginState.SUCCESS);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error when fixing ancestors", e);
      jobPluginInfo.incrementObjectsProcessedWithFailure(counter);
      report.setPluginState(PluginState.FAILURE);

      Report reportItem = PluginHelper.initPluginReportItem(this, "", AIP.class);
      reportItem.setPluginDetails("Ancestors fix failed: " + e.getMessage());
      reportItem.setPluginState(PluginState.FAILURE);
      report.addReport(reportItem);
      try {
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, PluginHelper.getJob(this, index));
      } catch (NotFoundException | GenericException | RequestNotValidException e1) {
        LOGGER.error("Error when updating job when ancestors fix failed", e1);
      }
    }
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new FixAncestorsPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Fixed the ancestor hierarchy";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Fixed the ancestor hierarchy successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Fix of the ancestor hierarchy failed";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }
}
