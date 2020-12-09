package org.roda.wui.client.common.actions;

import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_DELETE_DISPOSAL_CONFIRMATION;
import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_DESTROY_RECORDS_DISPOSAL_CONFIRMATION;
import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_PERMANENTLY_DELETE_RECORDS_DISPOSAL_CONFIRMATION;
import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_RETRIEVE_DISPOSAL_CONFIRMATION_REPORT;
import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_RESTORE_RECORDS_DISPOSAL_CONFIRMATION;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.confirmations.ShowDisposalConfirmation;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationReportActions extends AbstractActionable<DisposalConfirmation> {
  private static final DisposalConfirmationReportActions INSTANCE = new DisposalConfirmationReportActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_PENDING = new HashSet<>(
    Arrays.asList(DisposalConfirmationReportAction.PRINT, DisposalConfirmationReportAction.DESTROY,
      DisposalConfirmationReportAction.DELETE_REPORT));

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_APPROVED = new HashSet<>(
    Arrays.asList(DisposalConfirmationReportAction.PRINT, DisposalConfirmationReportAction.REMOVE_FROM_BIN,
      DisposalConfirmationReportAction.RESTORE_FROM_BIN));

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_RECOVERED = new HashSet<>();

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_DELETED = POSSIBLE_ACTIONS_FOR_RECOVERED;

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_EXECUTION_FAILED = new HashSet<>(
    Arrays.asList(DisposalConfirmationReportAction.RE_EXECUTE, DisposalConfirmationReportAction.RECOVER_STATE));

  public enum DisposalConfirmationReportAction implements Action<DisposalConfirmation> {
    PRINT(PERMISSION_METHOD_RETRIEVE_DISPOSAL_CONFIRMATION_REPORT),
    DESTROY(PERMISSION_METHOD_DESTROY_RECORDS_DISPOSAL_CONFIRMATION),
    DELETE_REPORT(PERMISSION_METHOD_DELETE_DISPOSAL_CONFIRMATION),
    REMOVE_FROM_BIN(PERMISSION_METHOD_PERMANENTLY_DELETE_RECORDS_DISPOSAL_CONFIRMATION),
    RESTORE_FROM_BIN(PERMISSION_METHOD_RESTORE_RECORDS_DISPOSAL_CONFIRMATION),
    RE_EXECUTE(PERMISSION_METHOD_DESTROY_RECORDS_DISPOSAL_CONFIRMATION),
    RECOVER_STATE(PERMISSION_METHOD_DESTROY_RECORDS_DISPOSAL_CONFIRMATION);

    private List<String> methods;

    DisposalConfirmationReportAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  private DisposalConfirmationReportActions() {
  }

  public static DisposalConfirmationReportActions get() {
    return INSTANCE;
  }

  @Override
  public DisposalConfirmationReportAction[] getActions() {
    return DisposalConfirmationReportAction.values();
  }

  @Override
  public Action<DisposalConfirmation> actionForName(String name) {
    return DisposalConfirmationReportAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<DisposalConfirmation> action) {
    return hasPermissions(action);
  }

  @Override
  public boolean canAct(Action<DisposalConfirmation> action, DisposalConfirmation object) {
    switch (object.getState()) {
      case PENDING:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_PENDING.contains(action);
      case APPROVED:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_APPROVED.contains(action);
      case RESTORED:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_RECOVERED.contains(action);
      case PERMANENTLY_DELETED:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_DELETED.contains(action);
      case EXECUTION_FAILED:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_EXECUTION_FAILED.contains(action);
      default:
        return false;
    }
  }

  @Override
  public void act(Action<DisposalConfirmation> action, DisposalConfirmation object,
    AsyncCallback<ActionImpact> callback) {
    if (DisposalConfirmationReportAction.DESTROY.equals(action)) {
      destroyDisposalConfirmationContent(object, callback);
    } else if (DisposalConfirmationReportAction.DELETE_REPORT.equals(action)) {
      deleteDisposalConfirmationReport(object, callback);
    } else if (DisposalConfirmationReportAction.REMOVE_FROM_BIN.equals(action)) {
      permanentlyDeleteDisposalConfirmationReport(object, callback);
    } else if (DisposalConfirmationReportAction.RESTORE_FROM_BIN.equals(action)) {
      restoreDestroyedRecord(object, callback);
    } else if (DisposalConfirmationReportAction.RE_EXECUTE.equals(action)) {
      reExecuteDisposalConfirmation(object, callback);
    } else if (DisposalConfirmationReportAction.RECOVER_STATE.equals(action)) {
      recoverDisposalConfirmationExecutionFailed(object, callback);
    } else if (DisposalConfirmationReportAction.PRINT.equals(action)) {
      retrieveDisposalConfirmationReportForPrint(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void retrieveDisposalConfirmationReportForPrint(DisposalConfirmation confirmation,
    AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().retrieveDisposalConfirmationReport(confirmation.getId(),true,
      new ActionNoAsyncCallback<String>(callback) {
        @Override
        public void onSuccess(String report) {
          JavascriptUtils.print(report);
          doActionCallbackNone();
        }
      });
  }

  private void reExecuteDisposalConfirmation(DisposalConfirmation confirmation, AsyncCallback<ActionImpact> callback) {

  }

  private void recoverDisposalConfirmationExecutionFailed(DisposalConfirmation confirmation,
    AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.recoverDestroyedRecordsConfirmDialogTitle(),
      messages.recoverDestroyedRecordsConfirmDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().recoverRecordsInDisposalConfirmationReport(
              objectToSelectedItems(confirmation, DisposalConfirmation.class), new ActionAsyncCallback<Job>(callback) {
                @Override
                public void onSuccess(Job job) {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.recoverDestroyedRecordsSuccessTitle(),
                        messages.recoverDestroyedRecordsSuccessMessage());
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackUpdated();
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

  private void restoreDestroyedRecord(DisposalConfirmation confirmation, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.restoreDestroyedRecordsConfirmDialogTitle(),
      messages.restoreDestroyedRecordsConfirmDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().restoreRecordsInDisposalConfirmationReport(
              objectToSelectedItems(confirmation, DisposalConfirmation.class), new ActionAsyncCallback<Job>(callback) {

                @Override
                public void onSuccess(Job result) {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.restoreDestroyedRecordsSuccessTitle(),
                        messages.restoreDestroyedRecordsSuccessMessage());
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
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

  private void permanentlyDeleteDisposalConfirmationReport(DisposalConfirmation confirmation,
    AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.permanentlyDeleteConfirmDialogTitle(),
      messages.permanentlyDeleteConfirmDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().permanentlyDeleteRecordsInDisposalConfirmationReport(
              objectToSelectedItems(confirmation, DisposalConfirmation.class), new ActionAsyncCallback<Job>(callback) {

                @Override
                public void onSuccess(Job result) {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.permanentlyDeleteRecordsSuccessTitle(),
                        messages.permanentlyDeleteRecordsSuccessMessage());
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
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

  private void destroyDisposalConfirmationContent(DisposalConfirmation report, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.deleteConfirmationReportDialogTitle(),
      messages.deleteConfirmationReportDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().destroyRecordsInDisposalConfirmationReport(
              objectToSelectedItems(report, DisposalConfirmation.class), new ActionAsyncCallback<Job>(callback) {

                @Override
                public void onSuccess(Job result) {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.deleteConfirmationReportSuccessTitle(),
                        messages.deleteConfirmationReportSuccessMessage());
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
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

  private void deleteDisposalConfirmationReport(DisposalConfirmation report, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.deleteConfirmationReportDialogTitle(),
      messages.deleteConfirmationReportDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                @Override
                public void onSuccess(final String details) {
                  BrowserService.Util.getInstance().deleteDisposalConfirmationReport(
                    objectToSelectedItems(report, DisposalConfirmation.class), details,
                    new ActionAsyncCallback<Job>(callback) {

                      @Override
                      public void onSuccess(Job result) {
                        Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                          @Override
                          public void onFailure(Throwable caught) {
                            Toast.showInfo(messages.deleteConfirmationReportSuccessTitle(),
                              messages.deleteConfirmationReportSuccessMessage());
                            doActionCallbackDestroyed();
                            HistoryUtils.newHistory(ShowDisposalConfirmation.RESOLVER);
                          }

                          @Override
                          public void onSuccess(final Void nothing) {
                            doActionCallbackNone();
                            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                          }
                        });
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

  @Override
  public ActionableBundle<DisposalConfirmation> createActionsBundle() {
    ActionableBundle<DisposalConfirmation> confirmationActionableBundle = new ActionableBundle<>();

    // SCHEDULE
    ActionableGroup<DisposalConfirmation> scheduleGroup = new ActionableGroup<>(
      messages.sidebarDisposalScheduleTitle());
    scheduleGroup.addButton(messages.applyDisposalScheduleButton(), DisposalConfirmationReportAction.DESTROY,
      ActionImpact.UPDATED, "btn-trash-alt");

    // REPORT
    ActionableGroup<DisposalConfirmation> reportGroup = new ActionableGroup<>(
      messages.sidebarDisposalConfirmationReportTitle());
    reportGroup.addButton(messages.printButton(), DisposalConfirmationReportAction.PRINT, ActionImpact.NONE,
      "btn-print");
    reportGroup.addButton(messages.deleteDisposalConfirmationReport(), DisposalConfirmationReportAction.DELETE_REPORT,
      ActionImpact.DESTROYED, "btn-remove");

    // DISPOSAL CONFIRMATION
    ActionableGroup<DisposalConfirmation> confirmationGroup = new ActionableGroup<>(
      messages.sidebarDisposalConfirmationTitle());
    confirmationGroup.addButton(messages.reExecuteDisposalDestroyActionButton(),
      DisposalConfirmationReportAction.RE_EXECUTE, ActionImpact.UPDATED, "btn-play-circle");
    confirmationGroup.addButton(messages.recoverDisposalConfirmationExecutionFailedButton(),
      DisposalConfirmationReportAction.RECOVER_STATE, ActionImpact.UPDATED, "btn-history");

    // DISPOSAL BIN
    ActionableGroup<DisposalConfirmation> disposalBinGroup = new ActionableGroup<>(messages.sidebarDisposalBinTitle());
    disposalBinGroup.addButton(messages.permanentlyDeleteFromBinButton(),
      DisposalConfirmationReportAction.REMOVE_FROM_BIN, ActionImpact.DESTROYED, "btn-eraser");
    disposalBinGroup.addButton(messages.restoreFromBinButton(), DisposalConfirmationReportAction.RESTORE_FROM_BIN,
      ActionImpact.UPDATED, "btn-history");

    confirmationActionableBundle.addGroup(scheduleGroup).addGroup(reportGroup).addGroup(confirmationGroup)
      .addGroup(disposalBinGroup);

    return confirmationActionableBundle;
  }
}
