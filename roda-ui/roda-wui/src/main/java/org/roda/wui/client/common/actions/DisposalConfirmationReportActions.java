package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationReportActions extends AbstractActionable<DisposalConfirmationMetadata> {
  private static final DisposalConfirmationReportActions INSTANCE = new DisposalConfirmationReportActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_PENDING = new HashSet<>(
    Arrays.asList(DisposalConfirmationReportAction.DESTROY, DisposalConfirmationReportAction.DELETE_REPORT));

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_APPROVED = new HashSet<>(
    Arrays.asList(DisposalConfirmationReportAction.REMOVE_FROM_BIN, DisposalConfirmationReportAction.RECOVER_FROM_BIN));

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_RECOVERED = new HashSet<>();

  private static final Set<DisposalConfirmationReportAction> POSSIBLE_ACTIONS_FOR_DELETED = POSSIBLE_ACTIONS_FOR_RECOVERED;

  public enum DisposalConfirmationReportAction implements Action<DisposalConfirmationMetadata> {
    DESTROY(), DELETE_REPORT(), REMOVE_FROM_BIN(), RECOVER_FROM_BIN();

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
  public Action<DisposalConfirmationMetadata> actionForName(String name) {
    return DisposalConfirmationReportAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<DisposalConfirmationMetadata> action) {
    return hasPermissions(action);
  }

  @Override
  public boolean canAct(Action<DisposalConfirmationMetadata> action, DisposalConfirmationMetadata object) {
    switch (object.getState()) {
      case PENDING:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_PENDING.contains(action);
      case APPROVED:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_APPROVED.contains(action);
      case RESTORED:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_RECOVERED.contains(action);
      case PERMANENTLY_DELETED:
        return hasPermissions(action) && POSSIBLE_ACTIONS_FOR_DELETED.contains(action);
      default:
        return false;
    }
  }

  @Override
  public void act(Action<DisposalConfirmationMetadata> action, DisposalConfirmationMetadata object,
    AsyncCallback<ActionImpact> callback) {
    if (DisposalConfirmationReportAction.DESTROY.equals(action)) {
    } else if (DisposalConfirmationReportAction.DELETE_REPORT.equals(action)) {
      deleteDisposalConfirmationReport(object, callback);
    } else if (DisposalConfirmationReportAction.REMOVE_FROM_BIN.equals(action)) {
    } else if (DisposalConfirmationReportAction.RECOVER_FROM_BIN.equals(action)) {
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void deleteDisposalConfirmationReport(DisposalConfirmationMetadata report,
    AsyncCallback<ActionImpact> callback) {
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
                    objectToSelectedItems(report, DisposalConfirmationMetadata.class), details,
                    new ActionAsyncCallback<Job>(callback) {

                      @Override
                      public void onSuccess(Job result) {
                        Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                          @Override
                          public void onFailure(Throwable caught) {
                            Toast.showInfo(messages.removingSuccessTitle(), messages.removingSuccessMessage(1L));
                            doActionCallbackDestroyed();
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
  public ActionableBundle<DisposalConfirmationMetadata> createActionsBundle() {
    ActionableBundle<DisposalConfirmationMetadata> confirmationActionableBundle = new ActionableBundle<>();

    // SCHEDULE
    ActionableGroup<DisposalConfirmationMetadata> scheduleGroup = new ActionableGroup<>("Schedule");

    // REPORT
    ActionableGroup<DisposalConfirmationMetadata> reportGroup = new ActionableGroup<>("Report");

    // DISPOSAL BIN
    ActionableGroup<DisposalConfirmationMetadata> disposalBinGroup = new ActionableGroup<>(
      messages.sidebarDisposalBinTitle());

    scheduleGroup.addButton(messages.applyDisposalScheduleButton(), DisposalConfirmationReportAction.DESTROY,
      ActionImpact.UPDATED, "btn-times-circle");
    reportGroup.addButton(messages.deleteDisposalConfirmationReport(), DisposalConfirmationReportAction.DELETE_REPORT,
      ActionImpact.DESTROYED, "btn-cancel");
    disposalBinGroup.addButton(messages.permanentlyDeleteFromBinButton(),
      DisposalConfirmationReportAction.REMOVE_FROM_BIN, ActionImpact.DESTROYED, "btn-times-circle");
    disposalBinGroup.addButton(messages.recoverFromBinButton(), DisposalConfirmationReportAction.RECOVER_FROM_BIN,
      ActionImpact.UPDATED, "btn-history");

    confirmationActionableBundle.addGroup(scheduleGroup).addGroup(reportGroup).addGroup(disposalBinGroup);

    return confirmationActionableBundle;
  }
}
