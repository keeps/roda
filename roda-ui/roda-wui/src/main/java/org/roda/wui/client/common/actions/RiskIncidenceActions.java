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
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionLoadingAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.EditMultipleRiskIncidenceDialog;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.planning.EditRiskIncidence;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class RiskIncidenceActions extends AbstractActionable<RiskIncidence> {
  private static final RiskIncidenceActions INSTANCE = new RiskIncidenceActions(false);
  private static final RiskIncidenceActions INSTANCE_MULTIPLE_EDIT = new RiskIncidenceActions(true);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RiskIncidenceAction> POSSIBLE_ACTIONS_ON_SINGLE_RISK_INCIDENCE = new HashSet<>(
    Arrays.asList(RiskIncidenceAction.values()));

  private static final Set<RiskIncidenceAction> POSSIBLE_ACTIONS_ON_MULTIPLE_RISK_INCIDENCES = new HashSet<>(
    Arrays.asList(RiskIncidenceAction.values()));

  private final boolean forceMultipleEdit;

  private RiskIncidenceActions(boolean forceMultipleEdit) {
    this.forceMultipleEdit = forceMultipleEdit;
  }

  public enum RiskIncidenceAction implements Action<RiskIncidence> {
    EDIT(RodaConstants.PERMISSION_METHOD_UPDATE_RISK_INCIDENCE),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_RISK_INCIDENCE),
    START_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB);

    private List<String> methods;

    RiskIncidenceAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public RiskIncidenceAction[] getActions() {
    return RiskIncidenceAction.values();
  }

  @Override
  public RiskIncidenceAction actionForName(String name) {
    return RiskIncidenceAction.valueOf(name);
  }

  public static RiskIncidenceActions get() {
    return INSTANCE;
  }

  public static RiskIncidenceActions getForMultipleEdit() {
    return INSTANCE_MULTIPLE_EDIT;
  }

  @Override
  public boolean canAct(Action<RiskIncidence> action, RiskIncidence object) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_ON_SINGLE_RISK_INCIDENCE.contains(action);
  }

  @Override
  public boolean canAct(Action<RiskIncidence> action, SelectedItems<RiskIncidence> objects) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_ON_MULTIPLE_RISK_INCIDENCES.contains(action);
  }

  @Override
  public void act(Action<RiskIncidence> action, RiskIncidence object, AsyncCallback<ActionImpact> callback) {
    if (action.equals(RiskIncidenceAction.REMOVE)) {
      remove(objectToSelectedItems(object, RiskIncidence.class), callback);
    } else if (action.equals(RiskIncidenceAction.EDIT)) {
      if (forceMultipleEdit) {
        edit(objectToSelectedItems(object, RiskIncidence.class), callback);
      } else {
        edit(object, callback);
      }
    } else if (action.equals(RiskIncidenceAction.START_PROCESS)) {
      startProcess(objectToSelectedItems(object, RiskIncidence.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<RiskIncidence> action, SelectedItems<RiskIncidence> objects,
    AsyncCallback<ActionImpact> callback) {
    if (action.equals(RiskIncidenceAction.REMOVE)) {
      remove(objects, callback);
    } else if (action.equals(RiskIncidenceAction.EDIT)) {
      edit(objects, callback);
    } else if (action.equals(RiskIncidenceAction.START_PROCESS)) {
      startProcess(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void startProcess(SelectedItems<RiskIncidence> objects, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setSelectedItems(objects);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void remove(SelectedItems<RiskIncidence> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(RiskIncidence.class, objects, new ActionNoAsyncCallback<Long>(callback) {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.riskIncidenceRemoveConfirmDialogTitle(),
          messages.riskIncidenceRemoveSelectedConfirmDialogMessage(size),
          messages.riskIncidenceRemoveConfirmDialogCancel(), messages.riskIncidenceRemoveConfirmDialogOk(),
          new ActionAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(final String details) {
                      BrowserService.Util.getInstance().deleteRiskIncidences(objects, details,
                        new ActionAsyncCallback<Job>(callback) {

                          @Override
                          public void onSuccess(Job result) {
                            Toast.showInfo(messages.runningInBackgroundTitle(),
                              messages.runningInBackgroundDescription());

                            Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                              new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
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
    });
  }

  // ACTIONS
  private void edit(RiskIncidence object, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(EditRiskIncidence.RESOLVER, object.getId());
  }

  private void edit(SelectedItems<RiskIncidence> objects, AsyncCallback<ActionImpact> callback) {
    EditMultipleRiskIncidenceDialog dialog = new EditMultipleRiskIncidenceDialog();
    dialog.showAndCenter();
    dialog.addValueChangeHandler((ValueChangeHandler<RiskIncidence>) event -> {
      EditMultipleRiskIncidenceDialog editDialog = (EditMultipleRiskIncidenceDialog) event.getSource();

      BrowserService.Util.getInstance().updateMultipleIncidences(objects, editDialog.getStatus(),
        editDialog.getSeverity(), editDialog.getMitigatedOn(), editDialog.getMitigatedBy(),
        editDialog.getMitigatedDescription(), new ActionLoadingAsyncCallback<Job>(callback) {

          @Override
          public void onSuccessImpl(Job result) {
            Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

            Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

              @Override
              public void onFailure(Throwable caught) {
                doActionCallbackUpdated();
              }

              @Override
              public void onSuccess(final Void nothing) {
                doActionCallbackNone();
                HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
              }
            });
          }
        });
    });
  }

  @Override
  public ActionableBundle<RiskIncidence> createActionsBundle() {
    ActionableBundle<RiskIncidence> actionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<RiskIncidence> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.editButton(), RiskIncidenceAction.EDIT, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.removeButton(), RiskIncidenceAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionableGroup<RiskIncidence> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), RiskIncidenceAction.START_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    actionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return actionableBundle;
  }
}
