/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.process.CreateDefaultJob;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class JobActions extends AbstractActionable<Job> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final HistoryResolver newProcessResolver;

  private JobActions(HistoryResolver newProcessResolver) {
    this.newProcessResolver = newProcessResolver;

    // ingest/process, IngestProcess.java

    // new SimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE,
    // PluginType.INGEST.toString());

    // administration/process, ActionProcess.java

    // new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE,
    // PluginType.INTERNAL.toString());
    // new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE,
    // PluginType.INGEST.toString());

    // administration/internal, InternalProcess.java

    // new SimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE,
    // PluginType.INTERNAL.toString());
  }

  public enum JobAction implements Action<Job> {
    NEW_PROCESS(), STOP(), INGEST_APPRAISAL(), INGEST_PROCESS();

    private List<String> methods;

    JobAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  private static final Set<HistoryResolver> NEW_PROCESS_RESOLVERS = new HashSet<>(
    Arrays.asList(IngestTransfer.RESOLVER, CreateDefaultJob.RESOLVER));

  public static JobActions get(HistoryResolver newProcessResolver) {
    return new JobActions(newProcessResolver);
  }

  @Override
  public Action<Job> actionForName(String name) {
    return JobAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<Job> action) {
    return JobAction.NEW_PROCESS.equals(action) && NEW_PROCESS_RESOLVERS.contains(newProcessResolver);
  }

  @Override
  public boolean canAct(Action<Job> action, Job object) {
    if (JobAction.STOP.equals(action)) {
      return !object.isInFinalState() && !object.isStopping();
    } else if (JobAction.INGEST_APPRAISAL.equals(action)) {
      return object.getJobStats().getOutcomeObjectsWithManualIntervention() > 0;
    } else if (JobAction.INGEST_PROCESS.equals(action)) {
      return object.getPluginType().equals(PluginType.INGEST);
    } else {
      return false;
    }
  }

  @Override
  public void act(Action<Job> action, AsyncCallback<ActionImpact> callback) {
    if (JobAction.NEW_PROCESS.equals(action)) {
      newProcess(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<Job> action, Job object, AsyncCallback<ActionImpact> callback) {
    if (JobAction.STOP.equals(action)) {
      stop(object, callback);
    } else if (JobAction.INGEST_APPRAISAL.equals(action)) {
      ingestAppraisal(object, callback);
    } else if (JobAction.INGEST_PROCESS.equals(action)) {
      ingestProcess(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void ingestProcess(Job object, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(Search.RESOLVER, "items", RodaConstants.ALL_INGEST_JOB_IDS, object.getId());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void ingestAppraisal(Job object, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(IngestAppraisal.RESOLVER, RodaConstants.SEARCH_ITEMS, RodaConstants.INGEST_JOB_ID,
      object.getId());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void stop(Job object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.jobStopConfirmDialogTitle(), messages.jobStopConfirmDialogMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().stopJob(object.getId(), new ActionAsyncCallback<Void>(callback) {
              // FIXME 20160826 hsilva: do proper handling of the failure

              @Override
              public void onSuccess(Void result) {
                // FIXME 20160826 hsilva: do proper handling of the success
                doActionCallbackUpdated();
              }
            });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void newProcess(AsyncCallback<ActionImpact> callback) {
    if (newProcessResolver != null) {
      HistoryUtils.newHistory(newProcessResolver);
    }
    callback.onSuccess(ActionImpact.UPDATED);
  }

  @Override
  public ActionableBundle<Job> createActionsBundle() {
    ActionableBundle<Job> jobActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<Job> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newProcessPreservation(), JobAction.NEW_PROCESS, ActionImpact.UPDATED,
      "btn-plus");
    managementGroup.addButton(messages.stopButton(), JobAction.STOP, ActionImpact.UPDATED, "btn-danger btn-stop");

    // FIXME 20180731 bferreira: JobAction.INGEST_APPRAISAL button text should be
    // dynamic and equal to messages.appraisalTitle() + " (" +
    // job.getJobStats().getOutcomeObjectsWithManualIntervention() + ")"

    managementGroup.addButton(messages.sidebarAppraisalTitle(), JobAction.INGEST_APPRAISAL, ActionImpact.NONE,
      "btn-play");
    managementGroup.addButton(messages.listButton(), JobAction.INGEST_PROCESS, ActionImpact.NONE, "btn-play");

    jobActionableBundle.addGroup(managementGroup);

    return jobActionableBundle;
  }
}
