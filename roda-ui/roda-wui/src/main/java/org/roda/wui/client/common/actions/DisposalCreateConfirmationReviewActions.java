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
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.dialogs.utils.DisposalScheduleDialogResult;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.DisposalScheduleRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalCreateConfirmationReviewActions extends AbstractActionable<IndexedAIP> {
  private static final DisposalCreateConfirmationReviewActions INSTANCE = new DisposalCreateConfirmationReviewActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalCreateConfirmationReviewAction> POSSIBLE_ACTIONS_WITH_RECORDS = new HashSet<>(
    Collections.singletonList(DisposalCreateConfirmationReviewAction.CHANGE_SCHEDULE));

  private DisposalCreateConfirmationReviewActions() {
  }

  public static DisposalCreateConfirmationReviewActions get() {
    return INSTANCE;
  }

  @Override
  public DisposalCreateConfirmationReviewAction[] getActions() {
    return DisposalCreateConfirmationReviewAction.values();
  }

  @Override
  public Action<IndexedAIP> actionForName(String name) {
    return DisposalCreateConfirmationReviewAction.valueOf(name);
  }

  @Override
  public CanActResult userCanAct(Action<IndexedAIP> action, IndexedAIP object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action, IndexedAIP object) {
    return new CanActResult(POSSIBLE_ACTIONS_WITH_RECORDS.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonDisposalConfirmationHasRecords());
  }

  @Override
  public CanActResult userCanAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    return new CanActResult(POSSIBLE_ACTIONS_WITH_RECORDS.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonDisposalConfirmationHasRecords());
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP object, AsyncCallback<ActionImpact> callback) {
    if (DisposalCreateConfirmationReviewAction.CHANGE_SCHEDULE.equals(action)) {
      changeDisposalSchedule(objectToSelectedItems(object, IndexedAIP.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects, AsyncCallback<ActionImpact> callback) {
    if (DisposalCreateConfirmationReviewAction.CHANGE_SCHEDULE.equals(action)) {
      changeDisposalSchedule(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void changeDisposalSchedule(final SelectedItems<IndexedAIP> aips,
    final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, aips, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        Services services = new Services("List disposal schedules", "get");
        services.disposalScheduleResource(DisposalScheduleRestService::listDisposalSchedules)
          .whenComplete((disposalSchedules, caught) -> {
            if (caught != null) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
              callback.onFailure(caught);
            } else {
              // Show the active disposal schedules only
              disposalSchedules.getObjects()
                .removeIf(schedule -> DisposalScheduleState.INACTIVE.equals(schedule.getState()));
              DisposalDialogs.showDisposalScheduleSelection(messages.disposalScheduleSelectionDialogTitle(),
                disposalSchedules, new ActionNoAsyncCallback<DisposalScheduleDialogResult>(callback) {
                  @Override
                  public void onFailure(Throwable caught) {
                    doActionCallbackNone();
                  }

                  @Override
                  public void onSuccess(DisposalScheduleDialogResult result) {
                    if (DisposalScheduleDialogResult.ActionType.ASSOCIATE.equals(result.getActionType())) {
                      associateDisposalSchedule(aips, size, result, callback);
                    } else if (DisposalScheduleDialogResult.ActionType.CLEAR.equals(result.getActionType())) {
                      disassociateDisposalSchedule(aips, size, result, callback);
                    }
                  }
                });
            }
          });
      }
    });
  }

  private void disassociateDisposalSchedule(SelectedItems<IndexedAIP> aips, Long size,
    DisposalScheduleDialogResult dialogResult, AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.dissociateDisposalScheduleDialogTitle(),
      messages.dissociateDisposalScheduleDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Disassociate disposal schedule from AIP", "job");
            services
              .disposalScheduleResource(
                s -> s.disassociatedDisposalSchedule(SelectedItemsUtils.convertToRESTRequest(aips)))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

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
            doActionCallbackNone();
          }
        }
      });
  }

  private void associateDisposalSchedule(SelectedItems<IndexedAIP> aips, Long size,
    DisposalScheduleDialogResult dialogResult, AsyncCallback<ActionImpact> callback) {
    DisposalSchedule disposalSchedule = dialogResult.getDisposalSchedule();

    Dialogs.showConfirmDialog(messages.associateDisposalScheduleDialogTitle(),
      messages.associateDisposalScheduleDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Associated disposal schedule", "job");
            services.disposalScheduleResource(s -> s
              .associatedDisposalSchedule(SelectedItemsUtils.convertToRESTRequest(aips), disposalSchedule.getId()))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

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
            doActionCallbackNone();
          }
        }
      });
  }

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> confirmationActionableBundle = new ActionableBundle<>();

    // management
    ActionableGroup<IndexedAIP> actionsGroup = new ActionableGroup<>(messages.sidebarActionsTitle());

    actionsGroup.addButton(messages.changeDisposalScheduleActionTitle(),
      DisposalCreateConfirmationReviewAction.CHANGE_SCHEDULE, ActionImpact.NONE, "btn-edit");

    confirmationActionableBundle.addGroup(actionsGroup);

    return confirmationActionableBundle;
  }

  public enum DisposalCreateConfirmationReviewAction implements Action<IndexedAIP> {
    CHANGE_SCHEDULE(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE);

    private List<String> methods;

    DisposalCreateConfirmationReviewAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
