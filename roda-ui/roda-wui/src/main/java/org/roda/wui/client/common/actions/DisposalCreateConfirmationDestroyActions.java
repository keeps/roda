package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalCreateConfirmationDestroyActions extends AbstractActionable<IndexedAIP> {
  private static final DisposalCreateConfirmationDestroyActions INSTANCE = new DisposalCreateConfirmationDestroyActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalCreateConfirmationDestroyAction> POSSIBLE_ACTIONS_WITH_RECORDS = new HashSet<>(Arrays
    .asList(DisposalCreateConfirmationDestroyAction.DESTROY, DisposalCreateConfirmationDestroyAction.CHANGE_SCHEDULE));

  public enum DisposalCreateConfirmationDestroyAction implements Action<IndexedAIP> {
    DESTROY(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION),
    CHANGE_SCHEDULE(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION);

    private List<String> methods;

    DisposalCreateConfirmationDestroyAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  private DisposalCreateConfirmationDestroyActions() {
  }

  public static DisposalCreateConfirmationDestroyActions get() {
    return INSTANCE;
  }

  @Override
  public DisposalCreateConfirmationDestroyAction[] getActions() {
    return DisposalCreateConfirmationDestroyAction.values();
  }

  @Override
  public Action<IndexedAIP> actionForName(String name) {
    return DisposalCreateConfirmationDestroyAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, IndexedAIP object) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_WITH_RECORDS.contains(action);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_WITH_RECORDS.contains(action);
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP object, AsyncCallback<ActionImpact> callback) {
    if (DisposalCreateConfirmationDestroyAction.DESTROY.equals(action)) {
      createDisposalConfirmationReport(objectToSelectedItems(object, IndexedAIP.class), callback);
    } else if (DisposalCreateConfirmationDestroyAction.CHANGE_SCHEDULE.equals(action)) {
      changeDisposalSchedule(objectToSelectedItems(object, IndexedAIP.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects, AsyncCallback<ActionImpact> callback) {
    if (DisposalCreateConfirmationDestroyAction.DESTROY.equals(action)) {
      createDisposalConfirmationReport(objects, callback);
    } else if (DisposalCreateConfirmationDestroyAction.CHANGE_SCHEDULE.equals(action)) {
      changeDisposalSchedule(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void createDisposalConfirmationReport(SelectedItems<IndexedAIP> selectedItemsList,
    AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, selectedItemsList, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.createDisposalConfirmationReportDialogTitle(),
          messages.createDisposalConfirmationReportDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
          new ActionNoAsyncCallback<Boolean>(callback) {
            @Override
            public void onSuccess(Boolean result) {
              if (result) {
                DisposalConfirmationMetadata confirmationMetadata = new DisposalConfirmationMetadata();
                confirmationMetadata.setTitle("Test");
                BrowserService.Util.getInstance().createDisposalConfirmationReport(selectedItemsList,
                  confirmationMetadata, new ActionAsyncCallback<Job>(callback) {
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

                          doActionCallbackUpdated();
                          HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
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
                doActionCallbackNone();
              }
            }
          });
      }
    });
  }

  private void changeDisposalSchedule(SelectedItems<IndexedAIP> items, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, items, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        BrowserService.Util.getInstance().listDisposalSchedules(new ActionNoAsyncCallback<DisposalSchedules>(callback) {
          @Override
          public void onSuccess(DisposalSchedules schedules) {
            // Show the active disposal schedules only
            schedules.getObjects().removeIf(schedule -> DisposalScheduleState.INACTIVE.equals(schedule.getState()));
            DisposalDialogs.showDisposalScheduleSelection(messages.disposalScheduleSelectionDialogTitle(), schedules,
              new ActionNoAsyncCallback<DisposalSchedule>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  doActionCallbackNone();
                }

                @Override
                public void onSuccess(DisposalSchedule disposalSchedule) {
                  // the result can be null, if null treat as removing the disposal schedule

                  Dialogs.showConfirmDialog(
                    disposalSchedule != null ? messages.associateDisposalScheduleDialogTitle()
                      : messages.dissociateDisposalScheduleDialogTitle(),
                    disposalSchedule != null ? messages.associateDisposalScheduleDialogMessage(size)
                      : messages.dissociateDisposalScheduleDialogMessage(size),
                    messages.dialogNo(), messages.dialogYes(), new ActionNoAsyncCallback<Boolean>(callback) {
                      @Override
                      public void onSuccess(Boolean result) {
                        if (result) {
                          if (disposalSchedule == null) {
                            BrowserService.Util.getInstance().disassociateDisposalSchedule(items,
                              new ActionAsyncCallback<Job>(callback) {

                                @Override
                                public void onFailure(Throwable caught) {
                                  callback.onFailure(caught);
                                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                                }

                                @Override
                                public void onSuccess(Job job) {
                                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(),
                                    new AsyncCallback<Void>() {

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
                            BrowserService.Util.getInstance().associateDisposalSchedule(items, disposalSchedule.getId(),
                              new ActionAsyncCallback<Job>(callback) {

                                @Override
                                public void onFailure(Throwable caught) {
                                  callback.onFailure(caught);
                                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                                }

                                @Override
                                public void onSuccess(Job job) {
                                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(),
                                    new AsyncCallback<Void>() {

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
                        } else {
                          doActionCallbackNone();
                        }
                      }
                    });
                }
              });
          }
        });
      }
    });
  }

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> confirmationActionableBundle = new ActionableBundle<>();

    // management
    ActionableGroup<IndexedAIP> actionsGroup = new ActionableGroup<>(messages.sidebarActionsTitle());

    actionsGroup.addButton(messages.confirmDisposalScheduleActionTitle(),
      DisposalCreateConfirmationDestroyAction.DESTROY, ActionImpact.NONE, "btn-plus-circle");

    actionsGroup.addButton(messages.changeDisposalScheduleActionTitle(),
      DisposalCreateConfirmationDestroyAction.CHANGE_SCHEDULE, ActionImpact.UPDATED, "btn-edit");

    confirmationActionableBundle.addGroup(actionsGroup);

    return confirmationActionableBundle;
  }
}
