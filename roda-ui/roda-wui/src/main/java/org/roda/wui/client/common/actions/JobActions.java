/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.process.CreateDefaultJob;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class JobActions extends AbstractActionable<IndexedJob> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Set<HistoryResolver> NEW_PROCESS_RESOLVERS = new HashSet<>(
    Arrays.asList(IngestTransfer.RESOLVER, CreateDefaultJob.RESOLVER));
  private final HistoryResolver newProcessResolver;

  private JobActions(HistoryResolver newProcessResolver) {
    this.newProcessResolver = newProcessResolver;
  }

  public static JobActions get(HistoryResolver newProcessResolver) {
    return new JobActions(newProcessResolver);
  }

  @Override
  public JobAction[] getActions() {
    return JobAction.values();
  }

  @Override
  public Action<IndexedJob> actionForName(String name) {
    return JobAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<IndexedJob> action) {
    return hasPermissions(action) && JobAction.NEW_PROCESS.equals(action)
      && NEW_PROCESS_RESOLVERS.contains(newProcessResolver);
  }

  @Override
  public boolean canAct(Action<IndexedJob> action, IndexedJob object) {
    if (hasPermissions(action) && object != null) {
      if (JobAction.STOP.equals(action)) {
        return !object.isInFinalState() && !object.isStopping();
      } else if (JobAction.APPROVE.equals(action)) {
        return Job.JOB_STATE.PENDING_APPROVAL.equals(object.getState());
      } else if (JobAction.REJECT.equals(action)) {
        return Job.JOB_STATE.PENDING_APPROVAL.equals(object.getState());
      } else if (JobAction.INGEST_APPRAISAL.equals(action)) {
        return object.getJobStats() != null && object.getJobStats().getOutcomeObjectsWithManualIntervention() > 0;
      } else if (JobAction.INGEST_PROCESS.equals(action)) {
        return PluginType.INGEST.equals(object.getPluginType());
      }
    }
    return false;
  }

  @Override
  public boolean canAct(Action<IndexedJob> action, SelectedItems<IndexedJob> objects) {
    if (hasPermissions(action) && objects != null) {
      if (JobAction.APPROVE.equals(action) || JobAction.REJECT.equals(action)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void act(Action<IndexedJob> action, AsyncCallback<ActionImpact> callback) {
    if (JobAction.NEW_PROCESS.equals(action)) {
      newProcess(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedJob> action, IndexedJob object, AsyncCallback<ActionImpact> callback) {
    if (JobAction.STOP.equals(action)) {
      stop(object, callback);
    } else if (JobAction.INGEST_APPRAISAL.equals(action)) {
      ingestAppraisal(object, callback);
    } else if (JobAction.INGEST_PROCESS.equals(action)) {
      ingestProcess(object, callback);
    } else if (JobAction.APPROVE.equals(action)) {
      approve(object, callback);
    } else if (JobAction.REJECT.equals(action)) {
      reject(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  public void act(Action<IndexedJob> action, SelectedItems<IndexedJob> jobs, AsyncCallback<ActionImpact> callback) {

    if (JobAction.APPROVE.equals(action)) {
      approve(jobs, callback);
    } else if (JobAction.REJECT.equals(action)) {
      reject(jobs, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void ingestProcess(IndexedJob object, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(Search.RESOLVER, RodaConstants.SEARCH_WITH_PREFILTER_HANDLER, "title",
      messages.searchPrefilterCreatedByJob(object.getName()), SearchFilters.classesToHistoryTokens(IndexedAIP.class),
      RodaConstants.ALL_INGEST_JOB_IDS, object.getId());
  }

  private void ingestAppraisal(IndexedJob object, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(IngestAppraisal.RESOLVER, RodaConstants.INGEST_JOB_ID, object.getId());
  }

  private void stop(IndexedJob object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.jobStopConfirmDialogTitle(), messages.jobStopConfirmDialogMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Stop job", "Stop");
            services.jobsResource(s -> s.stopJob(object.getId())).whenComplete((value, error) -> {
              if (error == null) {
                doActionCallbackDestroyed();
              } else {
                callback.onFailure(error);
                doActionCallbackDestroyed();
              }
            });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void approve(IndexedJob object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.jobApproveConfirmDialogTitle(), messages.jobApproveConfirmDialogMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Approve job", "Approve");
            services
              .jobsResource(
                s -> s.approveJob(new SelectedItemsListRequest(Collections.singletonList(object.getUUID()))))
              .whenComplete((value, error) -> {
                if (error == null) {
                  // FIXME 20160826 hsilva: do proper handling of the success
                  doActionCallbackDestroyed();
                } else {
                  // FIXME 20160826 hsilva: do proper handling of the failure
                  callback.onFailure(error);
                  doActionCallbackDestroyed();
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void approve(SelectedItems<IndexedJob> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedJob.class, objects, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.jobApproveConfirmDialogTitle(),
          messages.jobSelectedApproveConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
          new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(final String details) {
                      Services services = new Services("Approve selected jobs", "Approve");
                      services.jobsResource(s -> s.approveJob(SelectedItemsUtils.convertToRESTRequest(objects)))
                        .whenComplete((value, error) -> {
                          if (error == null) {
                            doActionCallbackDestroyed();
                          } else {
                            callback.onFailure(error);
                            doActionCallbackDestroyed();
                          }
                        });
                    }
                  });
              } else {
                doActionCallbackNone();
              }
            }
          });
      }
    });
  }

  private void reject(IndexedJob object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.jobRejectConfirmDialogTitle(), messages.jobRejectConfirmDialogMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                public void onSuccess(final String details) {
                  Services services = new Services("Reject job", "Reject");
                  services.jobsResource(
                    s -> s.rejectJob(new SelectedItemsListRequest(Collections.singletonList(object.getUUID())), details))
                    .whenComplete((value, error) -> {
                      if (error == null) {
                        // FIXME 20160826 hsilva: do proper handling of the success
                        doActionCallbackUpdated();
                      } else {
                        // FIXME 20160826 hsilva: do proper handling of the failure
                        super.onFailure(error);
                        doActionCallbackNone();
                      }
                    });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void reject(SelectedItems<IndexedJob> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedJob.class, objects, new ActionNoAsyncCallback<Long>(callback) {
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.jobRejectConfirmDialogTitle(),
          messages.jobSelectedRejectConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
          new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(final String details) {
                      Services services = new Services("Reject selected jobs", "Reject");
                      services.jobsResource(s -> s.rejectJob(SelectedItemsUtils.convertToRESTRequest(objects), details)).whenComplete((value, error) -> {
                        if (error == null) {
                          doActionCallbackDestroyed();
                        } else {
                          callback.onFailure(error);
                          doActionCallbackDestroyed();
                        }
                      });
                    }
                  });
              } else {
                doActionCallbackNone();
              }
            }
          });
      }
    });
  }

  private void newProcess(AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    if (newProcessResolver != null) {
      HistoryUtils.newHistory(newProcessResolver);
    }
  }

  @Override
  public ActionableBundle<IndexedJob> createActionsBundle() {
    ActionableBundle<IndexedJob> jobActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedJob> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newProcessPreservation(), JobAction.NEW_PROCESS, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.stopButton(), JobAction.STOP, ActionImpact.DESTROYED, "btn-stop");
    managementGroup.addButton(messages.approveButton(), JobAction.APPROVE, ActionImpact.UPDATED, "btn-check");
    managementGroup.addButton(messages.rejectButton(), JobAction.REJECT, ActionImpact.DESTROYED, "btn-times");

    // FIXME 20180731 bferreira: JobAction.INGEST_APPRAISAL button text should
    // be
    // dynamic and equal to messages.appraisalTitle() + " (" +
    // job.getJobStats().getOutcomeObjectsWithManualIntervention() + ")"

    managementGroup.addButton(messages.sidebarAppraisalTitle(), JobAction.INGEST_APPRAISAL, ActionImpact.NONE,
      "btn-play");
    managementGroup.addButton(messages.listButton(), JobAction.INGEST_PROCESS, ActionImpact.NONE, "btn-play");

    jobActionableBundle.addGroup(managementGroup);

    return jobActionableBundle;
  }

  public enum JobAction implements Action<IndexedJob> {
    NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB), STOP(RodaConstants.PERMISSION_METHOD_STOP_JOB),
    INGEST_APPRAISAL(RodaConstants.PERMISSION_METHOD_APPRAISAL),
    INGEST_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB), APPROVE(RodaConstants.PERMISSION_METHOD_APPROVE_JOB),
    REJECT(RodaConstants.PERMISSION_METHOD_REJECT_JOB);

    private List<String> methods;

    JobAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
