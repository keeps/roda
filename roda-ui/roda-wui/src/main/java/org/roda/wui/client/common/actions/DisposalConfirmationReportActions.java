/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_DELETE_DISPOSAL_CONFIRMATION;
import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_DESTROY_RECORDS_DISPOSAL_CONFIRMATION;
import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_PERMANENTLY_DELETE_RECORDS_DISPOSAL_CONFIRMATION;
import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_RESTORE_RECORDS_DISPOSAL_CONFIRMATION;
import static org.roda.core.data.common.RodaConstants.PERMISSION_METHOD_RETRIEVE_DISPOSAL_CONFIRMATION_REPORT;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.confirmations.ShowDisposalConfirmation;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
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
  public CanActResult userCanAct(Action<DisposalConfirmation> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalConfirmation> action) {
    return new CanActResult(true, CanActResult.Reason.CONTEXT, null);
  }

  @Override
  public CanActResult userCanAct(Action<DisposalConfirmation> action, DisposalConfirmation object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalConfirmation> action, DisposalConfirmation object) {
    switch (object.getState()) {
      case PENDING:
        return new CanActResult(POSSIBLE_ACTIONS_FOR_PENDING.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonDisposalConfirmationIsPending());
      case APPROVED:
        return new CanActResult(POSSIBLE_ACTIONS_FOR_APPROVED.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonDisposalConfirmationIsApproved());
      case RESTORED:
        return new CanActResult(POSSIBLE_ACTIONS_FOR_RECOVERED.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonDisposalConfirmationIsRecovered());
      case PERMANENTLY_DELETED:
        return new CanActResult(POSSIBLE_ACTIONS_FOR_DELETED.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonDisposalConfirmationIsDeleted());
      case EXECUTION_FAILED:
        return new CanActResult(POSSIBLE_ACTIONS_FOR_EXECUTION_FAILED.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonDisposalConfirmationExecutionFailed());
      default:
        return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonInvalidContext());
    }
  }

  @Override
  public void act(Action<DisposalConfirmation> action, DisposalConfirmation object,
    AsyncCallback<ActionImpact> callback) {
    if (DisposalConfirmationReportAction.DESTROY.equals(action)) {
      DisposalConfirmationActionsUtils.destroyDisposalConfirmationContent(object, callback);
    } else if (DisposalConfirmationReportAction.DELETE_REPORT.equals(action)) {
      deleteDisposalConfirmationReport(object, callback);
    } else if (DisposalConfirmationReportAction.REMOVE_FROM_BIN.equals(action)) {
      DisposalConfirmationActionsUtils.permanentlyDeleteDisposalConfirmationReport(object, callback);
    } else if (DisposalConfirmationReportAction.RESTORE_FROM_BIN.equals(action)) {
      DisposalConfirmationActionsUtils.restoreDestroyedRecord(object, callback);
    } else if (DisposalConfirmationReportAction.PRINT.equals(action)) {
      retrieveDisposalConfirmationReportForPrint(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void retrieveDisposalConfirmationReportForPrint(DisposalConfirmation confirmation,
    AsyncCallback<ActionImpact> callback) {

    SafeUri uri = RestUtils.createDisposalConfirmationHTMLUri(confirmation.getId(), true);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            JavascriptUtils.print(response.getText());
            callback.onSuccess(Actionable.ActionImpact.NONE);
          } else {
            callback.onFailure(null);
          }
        }

        @Override
        public void onError(Request request, Throwable throwable) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
          callback.onFailure(throwable);
        }
      });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
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
                  Services services = new Services("Delete disposal confirmation", "delete");
                  services
                    .disposalConfirmationResource(s -> s.deleteDisposalConfirmation(
                      new SelectedItemsListRequest(Collections.singletonList(report.getUUID())), details))
                    .whenComplete((job, throwable) -> {
                      if (throwable != null) {
                        callback.onFailure(throwable);
                      } else {
                        Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

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
                            HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
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

    // DISPOSAL CONFIRMATION
    ActionableGroup<DisposalConfirmation> confirmationGroup = new ActionableGroup<>(
      messages.sidebarDisposalConfirmationTitle());
    confirmationGroup.addButton(messages.printButton(), DisposalConfirmationReportAction.PRINT, ActionImpact.NONE,
      "btn-print");
    confirmationGroup.addButton(messages.deleteDisposalConfirmationReport(),
      DisposalConfirmationReportAction.DELETE_REPORT, ActionImpact.DESTROYED, "btn-remove");
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

    confirmationActionableBundle.addGroup(scheduleGroup).addGroup(confirmationGroup).addGroup(disposalBinGroup);

    return confirmationActionableBundle;
  }

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
}
