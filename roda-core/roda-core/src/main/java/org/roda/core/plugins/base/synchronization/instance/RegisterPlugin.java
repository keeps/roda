/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.synchronization.instance;

import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.TokenManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.DistributedModeType;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.roda.core.util.RESTClientUtility;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class RegisterPlugin extends AbstractPlugin<Void> {

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Subscription of Local Instance";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Subscription of Local Instance";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
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
    return new RegisterPlugin();
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
        subscribeLocalInstance(model, cachedJob, report, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void subscribeLocalInstance(ModelService model, Job cachedJob, Report pluginReport,
    JobPluginInfo jobPluginInfo) {

    Report reportItem = PluginHelper.initPluginReportItem(this, cachedJob.getId(), Job.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

    if (DistributedModeType.LOCAL.equals(RodaCoreFactory.getDistributedModeType())) {
      try {
        LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
        AccessToken accessToken = TokenManager.getInstance().getAccessToken(localInstance);
        String resource = RodaConstants.API_SEP + RodaConstants.API_REST_V2_DISTRIBUTED_INSTANCE
          + RodaConstants.API_PATH_PARAM_DISTRIBUTED_INSTANCE_REGISTER;
        RESTClientUtility.sendPostRequest(localInstance, null, localInstance.getCentralInstanceURL(), resource,
          accessToken);
        localInstance.setIsSubscribed(true);
        localInstance.setStatus(SynchronizingStatus.ACTIVE);
        RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (GenericException | AuthenticationDeniedException e) {
        String details = e.getMessage() + "\n";
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE);
        reportItem.addPluginDetails(details);
      }

      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
      pluginReport.addReport(reportItem);
    } else {
      reportItem.setPluginState(PluginState.SKIPPED);
      reportItem.setPluginDetails("Skipped because RODA is not configured as a local instance");
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

}
