package org.roda.core.plugins.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class MultiplePlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MultiplePlugin.class);
  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();
  private int totalSteps;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "MultiplePlugin";
  }

  @Override
  public String getDescription() {
    return "Run Mutiple plugins";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.FORMAT_VALIDATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "run mutiple plugins";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failure";
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MISC);
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new MultiplePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS, "2");
    totalSteps = Integer
      .parseInt(PluginHelper.getStringFromParameters(this, RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS, "2"));
  }

  @Override
  public void init() throws PluginException {
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this,
      (RODAObjectsProcessingLogic<AIP>) (index1, model1, storage1, report, cachedJob, jobPluginInfo, plugin,
        objects) -> processAIP(index1, model1, storage1, report, cachedJob, jobPluginInfo, objects),
      index, model, storage, liteList);
  }

  private void processAIP(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo, List<AIP> aips) {

    try {
      for (AIP aip : aips) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
        Report fixityCheckReport = null;
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
        PluginState state = PluginState.SUCCESS;
        String outcomeDetailsText = "";
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        jobPluginInfo.incrementStepsCompletedByOne();
        report.addReport(reportItem);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.setTotalSteps(totalSteps));
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

        fixityCheckReport = executePlugin(index, model, storage, aip, cachedJob,
          "org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin");

        PluginHelper.updateJobInformationAsync(this,jobPluginInfo.incrementStepsCompletedByOne());
        if (fixityCheckReport != null) {
          report.addReport(fixityCheckReport);
          jobPluginInfo.incrementObjectsProcessed(fixityCheckReport.getPluginState());
        }

        PluginHelper.updateJobInformationAsync(this, jobPluginInfo);
        LinkingIdentifier linkingIdentifier = PluginHelper.getLinkingIdentifier(aip.getId(),
          RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME);
        model.createRepositoryEvent(getPreservationEventType(), "EVENT_DESCRIPTION",
          Collections.singletonList(linkingIdentifier), null, state, outcomeDetailsText, "", cachedJob.getUsername(),
          true);

      }
    } catch (InvalidParameterException | PluginException | JobException e) {
      e.printStackTrace();
    }

  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, AIP aip, Job job,
    final String pluginId) throws InvalidParameterException, PluginException {
    Plugin<AIP> plugin = RodaCoreFactory.getPluginManager().getPlugin(pluginId, AIP.class);
    Map<String, String> mergedParams = new HashMap<>(getParameterValues());
    mergedParams.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, job.getId());
    plugin.setParameterValues(mergedParams);
    List<LiteOptionalWithCause> lites = LiteRODAObjectFactory.transformIntoLiteWithCause(model,
      Collections.singletonList(aip));
    plugin.setMandatory(false);
    return plugin.execute(index, model, storage, lites);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
