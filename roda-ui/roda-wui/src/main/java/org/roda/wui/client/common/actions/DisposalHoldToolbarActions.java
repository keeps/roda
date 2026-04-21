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

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.ip.disposalhold.UpdateDisposalHoldRequest;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.disposal.hold.EditDisposalHold;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldToolbarActions extends AbstractActionable<DisposalHold> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalHoldAction> POSSIBLE_ACTIONS_ON_DISPOSAL_HOLD = new HashSet<>(Arrays
    .asList(DisposalHoldAction.LIFT, DisposalHoldAction.SAVE, DisposalHoldAction.EDIT, DisposalHoldAction.UPDATE));

  private static final DisposalHoldToolbarActions GENERAL_INSTANCE = new DisposalHoldToolbarActions();

  private DisposalHoldToolbarActions() {
  }

  public static DisposalHoldToolbarActions get() {
    return GENERAL_INSTANCE;
  }

  @Override
  public Action<DisposalHold>[] getActions() {
    return DisposalHoldAction.values();
  }

  @Override
  public CanActResult userCanAct(Action<DisposalHold> action, ActionableObject<DisposalHold> object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalHold> action, DisposalHold object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalHold> action, DisposalHold object) {
    if (object.getState().equals(DisposalHoldState.LIFTED)) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonHoldAlreadyLifted());
    }

    return new CanActResult(POSSIBLE_ACTIONS_ON_DISPOSAL_HOLD.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnGroup());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalHold> action, ActionableObject<DisposalHold> object) {
    if (object.getObject().getState().equals(DisposalHoldState.LIFTED)) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonHoldAlreadyLifted());
    }
    return new CanActResult(POSSIBLE_ACTIONS_ON_DISPOSAL_HOLD.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnGroup());
  }

  @Override
  public void act(Action<DisposalHold> action, DisposalHold hold, AsyncCallback<ActionImpact> callback) {
    if (DisposalHoldAction.SAVE.equals(action)) {
      create(hold, callback);
    } else if (DisposalHoldAction.UPDATE.equals(action)) {
      update(hold, callback);
    } else if (DisposalHoldAction.EDIT.equals(action)) {
      edit(hold, callback);
    } else if (DisposalHoldAction.LIFT.equals(action)) {
      DisposalHoldActions.lift(hold, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<DisposalHold> createActionsBundle() {
    ActionableBundle<DisposalHold> disposalHoldActionableBundle = new ActionableBundle<>();

    ActionableGroup<DisposalHold> managementGroup = new ActionableGroup<>(messages.manage(), "btn-edit");
    managementGroup.addButton(messages.editButton(), DisposalHoldAction.EDIT, ActionImpact.UPDATED, "fas fa-pencil");
    managementGroup.addButton(messages.liftButton(), DisposalHoldAction.LIFT, ActionImpact.UPDATED, "btn-lift-hold");
    managementGroup.addButton(messages.saveButton(), DisposalHoldAction.SAVE, ActionImpact.UPDATED, "btn-play");
    managementGroup.addButton(messages.updateButton(), DisposalHoldAction.UPDATE, ActionImpact.UPDATED, "btn-play");

    disposalHoldActionableBundle.addGroup(managementGroup);
    return disposalHoldActionableBundle;
  }

  @Override
  public Action<DisposalHold> actionForName(String name) {
    return null;
  }

  private void edit(DisposalHold hold, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(EditDisposalHold.RESOLVER, hold.getId());
    callback.onSuccess(ActionImpact.NONE);
  }

  private void create(DisposalHold hold, AsyncCallback<ActionImpact> callback) {

    Services services = new Services("Create disposal hold", "create");
    services.disposalHoldResource(s -> s.createDisposalHold(hold)).whenComplete((created, throwable) -> {
      if (throwable != null) {
        Toast.showError(throwable);
        callback.onSuccess(ActionImpact.NONE);
      } else {
        Toast.showInfo(messages.showDisposalHoldTitle(), messages.disposalHoldSuccessfullyCreated());
        HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, created.getId());
        callback.onSuccess(ActionImpact.UPDATED);
      }
    });
  }

  private void update(DisposalHold hold, AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Update disposal hold", "update");
    UpdateDisposalHoldRequest request = new UpdateDisposalHoldRequest();
    request.setDisposalHold(hold);
    services.disposalHoldResource(s -> s.updateDisposalHold(request)).whenComplete((updated, throwable) -> {
      if (throwable != null) {
        Toast.showError(throwable);
        callback.onSuccess(ActionImpact.NONE);
      } else {
        Toast.showInfo(messages.showDisposalHoldTitle(), messages.disposalHoldSuccessfullyUpdated());
        HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, updated.getId());
        callback.onSuccess(ActionImpact.UPDATED);
      }
    });
  }
}
