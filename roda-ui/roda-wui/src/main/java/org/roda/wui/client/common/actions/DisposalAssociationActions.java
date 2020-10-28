package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class DisposalAssociationActions extends AbstractActionable<IndexedAIP> {
  private static final DisposalAssociationActions INSTANCE = new DisposalAssociationActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public enum DisposalAssociationAction implements Action<IndexedAIP> {
    EDIT();

    private List<String> methods;

    DisposalAssociationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  public DisposalAssociationActions() {
  }

  public static DisposalAssociationActions get() {
    return INSTANCE;
  }

  @Override
  public Action<IndexedAIP>[] getActions() {
    return DisposalAssociationAction.values();
  }

  @Override
  public Action<IndexedAIP> actionForName(String name) {
    return DisposalAssociationAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, ActionableObject<IndexedAIP> object) {
    return hasPermissions(action);
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (DisposalAssociationAction.EDIT.equals(action)) {
      edit(aip, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void edit(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().listDisposalSchedules(new ActionNoAsyncCallback<DisposalSchedules>(callback) {
      @Override
      public void onSuccess(DisposalSchedules schedules) {
        schedules.getObjects().removeIf(schedule -> DisposalScheduleState.INACTIVE.equals(schedule.getState()));
        DisposalDialogs.showDisposalScheduleSelection(messages.disposalScheduleSelectionDialogTitle(), schedules,
          new ActionNoAsyncCallback<DisposalSchedule>(callback) {
            @Override
            public void onFailure(Throwable caught) {
              doActionCallbackNone();
            }

            @Override
            public void onSuccess(DisposalSchedule disposalSchedule) {
              if (disposalSchedule == null) {
                BrowserService.Util.getInstance()
                  .disassociateDisposalSchedule(objectToSelectedItems(aip, IndexedAIP.class), new AsyncCallback<Job>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      callback.onFailure(caught);
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    }

                    @Override
                    public void onSuccess(Job job) {
                      Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                          Toast.showInfo(messages.runningInBackgroundTitle(),
                            messages.runningInBackgroundDescription());

                          Timer timer = new Timer() {
                            @Override
                            public void run() {
                              doActionCallbackUpdated();
                            }
                          };

                          timer.schedule(RodaConstants.ACTION_TIMEOUT);
                        }

                        @Override
                        public void onSuccess(final Void nothing) {
                          doActionCallbackNone();
                          HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                        }
                      });
                    }
                  });
              } else {
                BrowserService.Util.getInstance().associateDisposalSchedule(
                  objectToSelectedItems(aip, IndexedAIP.class), disposalSchedule.getId(), new AsyncCallback<Job>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      callback.onFailure(caught);
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    }

                    @Override
                    public void onSuccess(Job job) {
                      Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                          Toast.showInfo(messages.runningInBackgroundTitle(),
                            messages.runningInBackgroundDescription());
                          Timer timer = new Timer() {
                            @Override
                            public void run() {
                              doActionCallbackUpdated();
                            }
                          };

                          timer.schedule(RodaConstants.ACTION_TIMEOUT);
                        }

                        @Override
                        public void onSuccess(final Void nothing) {
                          doActionCallbackNone();
                          HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                        }
                      });
                    }
                  });
              }
            }
          });
      }
    });
  }

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> disposalAssociationActionableBundle = new ActionableBundle<>();
    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.editButton(), DisposalAssociationAction.EDIT, ActionImpact.UPDATED, "btn-edit");

    disposalAssociationActionableBundle.addGroup(managementGroup);
    return disposalAssociationActionableBundle;
  }
}
