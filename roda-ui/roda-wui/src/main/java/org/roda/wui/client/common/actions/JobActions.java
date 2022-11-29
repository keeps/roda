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
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
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
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class JobActions extends AbstractActionable<Job> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final HistoryResolver newProcessResolver;

  private JobActions(HistoryResolver newProcessResolver) {
    this.newProcessResolver = newProcessResolver;
  }

  public enum JobAction implements Action<Job> {
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

  @Override
  public JobAction[] getActions() {
    return JobAction.values();
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
    return hasPermissions(action) && JobAction.NEW_PROCESS.equals(action)
      && NEW_PROCESS_RESOLVERS.contains(newProcessResolver);
  }

  @Override
  public boolean canAct(Action<Job> action, Job object) {
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
  public boolean canAct(Action<Job> action, SelectedItems<Job> objects) {
    if (hasPermissions(action) && objects != null) {
      if (JobAction.APPROVE.equals(action) || JobAction.REJECT.equals(action)) {
        return true;
      }
    }
    return false;
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
    } else if (JobAction.APPROVE.equals(action)) {
      approve(object, callback);
    } else if (JobAction.REJECT.equals(action)) {
      reject(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  public void act(Action<Job> action, SelectedItems<Job> jobs, AsyncCallback<ActionImpact> callback) {

    if (JobAction.APPROVE.equals(action)) {
      approve(jobs, callback);
    } else if (JobAction.REJECT.equals(action)) {
      reject(jobs, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void ingestProcess(Job object, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(Search.RESOLVER, SearchFilters.classesToHistoryTokens(IndexedAIP.class),
      RodaConstants.ALL_INGEST_JOB_IDS, object.getId());
  }

  private void ingestAppraisal(Job object, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(IngestAppraisal.RESOLVER, RodaConstants.INGEST_JOB_ID,
      object.getId());
  }

  private void stop(Job object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.jobStopConfirmDialogTitle(), messages.jobStopConfirmDialogMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().stopJob(object.getId(), new ActionAsyncCallback<Void>(callback) {
              @Override
              public void onFailure(Throwable caught) {
                // FIXME 20160826 hsilva: do proper handling of the failure
                super.onFailure(caught);
                doActionCallbackDestroyed();
              }

              @Override
              public void onSuccess(Void result) {
                // FIXME 20160826 hsilva: do proper handling of the success
                doActionCallbackDestroyed();
              }
            });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void approve(Job object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.jobApproveConfirmDialogTitle(), messages.jobApproveConfirmDialogMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().approveJob(objectToSelectedItems(object, Job.class),
              new ActionAsyncCallback<Void>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  // FIXME 20160826 hsilva: do proper handling of the failure
                  super.onFailure(caught);
                  doActionCallbackDestroyed();
                }

                @Override
                public void onSuccess(Void result) {
                  // FIXME 20160826 hsilva: do proper handling of the success
                  doActionCallbackDestroyed();
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void approve(SelectedItems<Job> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(Job.class, objects, new ActionNoAsyncCallback<Long>(callback) {
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
                      BrowserService.Util.getInstance().approveJob(objects, new ActionAsyncCallback<Void>(callback) {

                        @Override
                        public void onSuccess(Void unused) {
                          doActionCallbackDestroyed();
                        }

                        public void onFailure(Throwable caught) {
                          super.onFailure(caught);
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

  private void reject(Job object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.jobRejectConfirmDialogTitle(), messages.jobRejectConfirmDialogMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                public void onSuccess(final String details) {
                  BrowserService.Util.getInstance().rejectJob(objectToSelectedItems(object, Job.class), details,
                    new ActionAsyncCallback<Void>(callback) {
                      @Override
                      public void onFailure(Throwable caught) {
                        // FIXME 20160826 hsilva: do proper handling of the failure
                        super.onFailure(caught);
                        doActionCallbackNone();
                      }

                      @Override
                      public void onSuccess(Void result) {
                        // FIXME 20160826 hsilva: do proper handling of the success
                        doActionCallbackUpdated();
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

  private void reject(SelectedItems<Job> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(Job.class, objects, new ActionNoAsyncCallback<Long>(callback) {
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
                      BrowserService.Util.getInstance().rejectJob(objects, details,
                        new ActionAsyncCallback<Void>(callback) {

                          @Override
                          public void onSuccess(Void unused) {
                            doActionCallbackDestroyed();
                          }

                          public void onFailure(Throwable caught) {
                            super.onFailure(caught);
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
  public ActionableBundle<Job> createActionsBundle() {
    ActionableBundle<Job> jobActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<Job> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newProcessPreservation(), JobAction.NEW_PROCESS, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.stopButton(), JobAction.STOP, ActionImpact.DESTROYED, "btn-stop");
    managementGroup.addButton(messages.approveButton(), JobAction.APPROVE, ActionImpact.UPDATED, "btn-check");
    managementGroup.addButton(messages.rejectButton(), JobAction.REJECT, ActionImpact.DESTROYED, "btn-times");

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
