package org.roda.core.plugins.plugins.internal.synchronization.instanceIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierJobPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIdentifierJobPlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, "Instance Identifier",
        PluginParameter.PluginParameterType.STRING, RODAInstanceUtils.retrieveLocalInstanceIdentifierToPlugin(), true,
        true, "Identifier from the RODA local instance"));
  }

  private String instanceId;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Job instance identifier";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Add the instance identifier on the data that exists on the storage as also on the index. "
      + "If an object already has an instance identifier it will be updated by the new one. "
      + "This task aims to help the synchronization between a RODA central instance and the RODA local instance, "
      + "since when an local object is accessed in RODA Central it should have the instance identifier in order to "
      + "inform from which source is it from.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER)) {
      instanceId = parameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER);
    }
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Updated the instance identifier";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The instance identifier was updated successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Could not update the instance identifier";
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public boolean areParameterValuesValid() {
    return false;
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new InstanceIdentifierJobPlugin();
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        try {
          modifyInstanceId(model, index, cachedJob, report, jobPluginInfo);
        } catch (RequestNotValidException | GenericException | NotFoundException e) {
          LOGGER.error("Could not modify Instance ID's in objects");
        }
      }
    }, index, model, storage);
  }

  private void modifyInstanceId(ModelService model, IndexService index, Job cachedJob, Report pluginReport,
    JobPluginInfo jobPluginInfo) throws RequestNotValidException, GenericException, NotFoundException {
    String details = "";
    PluginState pluginState = PluginState.SKIPPED;
    int countFail = 0;
    int countSuccess = 0;
    // Get Jobs from index
    IterableIndexResult<Job> indexedJobs = retrieveList(index);
    Report reportItem = PluginHelper.initPluginReportItem(this, cachedJob.getId(), Job.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    for (Job indexedJob : indexedJobs) {
      try {
        model.updateJobInstanceId(model.retrieveJob(indexedJob.getId()));
        pluginState = PluginState.SUCCESS;
        countSuccess++;
      } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
        details = e.getMessage() + "\n";
        pluginState = PluginState.FAILURE;
        countFail++;
      }
    }

    if (countFail > 0) {
      details = "Updated the instance identifier on " + countSuccess + " Job's and failed to update " + countFail;
    } else if (countSuccess > 0) {
      details = "Updated the instance identifier on " + countSuccess + " Job's";
    }

    reportItem.setPluginDetails(details);

    jobPluginInfo.incrementObjectsProcessed(pluginState);
    reportItem.setPluginState(pluginState);
    pluginReport.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  private IterableIndexResult<Job> retrieveList(IndexService index) throws RequestNotValidException, GenericException {
    Filter filter = new Filter();

    return index.findAll(Job.class, filter, Collections.singletonList(RodaConstants.INDEX_UUID));
  }
}
