/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.dialogs.utils.DisposalScheduleDialogResult;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class DisposalAssociationActions extends AbstractActionable<IndexedAIP> {
  private static final DisposalAssociationActions INSTANCE = new DisposalAssociationActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

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
  public CanActResult userCanAct(Action<IndexedAIP> action, ActionableObject<IndexedAIP> object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action, ActionableObject<IndexedAIP> object) {
    return new CanActResult(object.getObject().getDisposalConfirmationId() == null, CanActResult.Reason.CONTEXT,
      messages.reasonNoDisposalConfirmation());
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (DisposalAssociationAction.EDIT.equals(action)) {
      edit(objectToSelectedItems(aip, IndexedAIP.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void edit(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, aips, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        Filter activeFilter = new Filter(
          new NotSimpleFilterParameter(RodaConstants.DISPOSAL_SCHEDULE_STATE,
            DisposalScheduleState.INACTIVE.name()));

        DisposalDialogs.showDisposalScheduleSelection(messages.disposalScheduleSelectionDialogTitle(),
          activeFilter, new ActionNoAsyncCallback<DisposalScheduleDialogResult>(callback) {
            @Override
            public void onFailure(Throwable caught) {
              doActionCallbackNone();
            }

            @Override
            public void onSuccess(DisposalScheduleDialogResult result) {
              associateDisposalSchedule(aips, size, result.getDisposalScheduleId(), callback);
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
    String disposalScheduleId, AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.associateDisposalScheduleDialogTitle(),
      messages.associateDisposalScheduleDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Associated disposal schedule", "job");
            services.disposalScheduleResource(s -> s
              .associatedDisposalSchedule(SelectedItemsUtils.convertToRESTRequest(aips), disposalScheduleId))
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
    ActionableBundle<IndexedAIP> disposalAssociationActionableBundle = new ActionableBundle<>();

    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.associateDisposalScheduleButton(), DisposalAssociationAction.EDIT,
      ActionImpact.UPDATED, "btn-edit");

    disposalAssociationActionableBundle.addGroup(managementGroup);
    return disposalAssociationActionableBundle;
  }

  public enum DisposalAssociationAction implements Action<IndexedAIP> {
    EDIT(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE);

    private List<String> methods;

    DisposalAssociationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
