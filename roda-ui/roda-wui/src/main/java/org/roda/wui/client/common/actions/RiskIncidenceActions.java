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
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionLoadingAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionsBundle;
import org.roda.wui.client.common.actions.model.ActionsGroup;
import org.roda.wui.client.common.dialogs.EditMultipleRiskIncidenceDialog;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.planning.EditRiskIncidence;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
    EDIT, REMOVE, START_PROCESS
  }

  public static RiskIncidenceActions get() {
    return INSTANCE;
  }

  public static RiskIncidenceActions getForMultipleEdit() {
    return INSTANCE_MULTIPLE_EDIT;
  }

  @Override
  public boolean canAct(Action<RiskIncidence> action, RiskIncidence object) {
    return POSSIBLE_ACTIONS_ON_SINGLE_RISK_INCIDENCE.contains(action);
  }

  @Override
  public boolean canAct(Action<RiskIncidence> action, SelectedItems<RiskIncidence> objects) {
    return POSSIBLE_ACTIONS_ON_MULTIPLE_RISK_INCIDENCES.contains(action);
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
    LastSelectedItemsSingleton.getInstance().setSelectedItems(objects);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void remove(SelectedItems<RiskIncidence> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(RiskIncidence.class, objects, new ActionNoAsyncCallback<Long>(callback) {

      @Override
      public void onSuccess(final Long result) {
        BrowserService.Util.getInstance().deleteRiskIncidences(objects, new ActionAsyncCallback<Void>(callback) {
          @Override
          public void onSuccess(Void nothing) {
            Toast.showInfo(messages.removeSuccessTitle(), messages.removeSuccessMessage(result));
            doActionCallbackDestroyed();
          }
        });
      }
    });
  }

  // ACTIONS
  private void edit(RiskIncidence object, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(EditRiskIncidence.RESOLVER, object.getId());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void edit(SelectedItems<RiskIncidence> objects, AsyncCallback<ActionImpact> callback) {
    EditMultipleRiskIncidenceDialog dialog = new EditMultipleRiskIncidenceDialog();
    dialog.showAndCenter();
    dialog.addValueChangeHandler((ValueChangeHandler<RiskIncidence>) event -> {
      EditMultipleRiskIncidenceDialog editDialog = (EditMultipleRiskIncidenceDialog) event.getSource();

      BrowserService.Util.getInstance().updateMultipleIncidences(objects, editDialog.getStatus(),
        editDialog.getSeverity(), editDialog.getMitigatedOn(), editDialog.getMitigatedBy(),
        editDialog.getMitigatedDescription(), new ActionLoadingAsyncCallback<Void>(callback) {

          @Override
          public void onSuccessImpl(Void result) {
            doActionCallbackUpdated();
          }
        });
    });
  }

  @Override
  public ActionsBundle<RiskIncidence> createActionsBundle() {
    ActionsBundle<RiskIncidence> actionableBundle = new ActionsBundle<>();

    // MANAGEMENT
    ActionsGroup<RiskIncidence> managementGroup = new ActionsGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.editButton(), RiskIncidenceAction.EDIT, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.removeButton(), RiskIncidenceAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionsGroup<RiskIncidence> preservationGroup = new ActionsGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.formatRegisterProcessButton(), RiskIncidenceAction.START_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    actionableBundle.addGroup(managementGroup).addGroup(preservationGroup);

    return actionableBundle;
  }
}
