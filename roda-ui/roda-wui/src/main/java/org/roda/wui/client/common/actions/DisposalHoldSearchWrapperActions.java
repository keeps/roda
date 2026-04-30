package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalHoldSearchWrapperActions extends AbstractActionable<DisposalHold> {
  private static final DisposalHoldSearchWrapperActions INSTANCE = new DisposalHoldSearchWrapperActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Set<DisposalHoldAction> POSSIBLE_ACTIONS_WITHOUT_HOLD = new HashSet<>(
    Arrays.asList(DisposalHoldAction.NEW));

  private static final Set<DisposalHoldAction> POSSIBLE_ACTIONS_ON_SINGLE_DISPOSAL_HOLD = new HashSet<>(
    Arrays.asList(DisposalHoldAction.EDIT, DisposalHoldAction.LIFT));

  private DisposalHoldSearchWrapperActions() {
  }

  public static DisposalHoldSearchWrapperActions get() {
    return INSTANCE;
  }

  @Override
  public Action<DisposalHold>[] getActions() {
    return DisposalHoldAction.values();
  }

  @Override
  public CanActResult userCanAct(Action<DisposalHold> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalHold> action) {
    return new CanActResult(POSSIBLE_ACTIONS_WITHOUT_HOLD.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalHold> action, DisposalHold object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalHold> action, DisposalHold object) {
    if (DisposalHoldState.LIFTED.equals(object.getState())) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonHoldAlreadyLifted());
    }

    return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_DISPOSAL_HOLD.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnSingleObject());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalHold> action, SelectedItems<DisposalHold> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalHold> action, SelectedItems<DisposalHold> objects) {
    return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<DisposalHold> action, DisposalHold hold, AsyncCallback<ActionImpact> callback) {
    if (DisposalHoldAction.EDIT.equals(action)) {
      DisposalHoldActions.edit(hold, callback);
    } else if (DisposalHoldAction.LIFT.equals(action)) {
      DisposalHoldActions.lift(hold, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<DisposalHold> action, AsyncCallback<ActionImpact> callback) {
    if (DisposalHoldAction.NEW.equals(action)) {
      DisposalHoldActions.newDisposalHold(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<DisposalHold> createActionsBundle() {
    ActionableBundle<DisposalHold> disposalHoldActionableBundle = new ActionableBundle<>();

    ActionableGroup<DisposalHold> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newDisposalHoldTitle(), DisposalHoldAction.NEW, ActionImpact.UPDATED,
      "btn-plus-circle");

    managementGroup.addButton(messages.editButton(), DisposalHoldAction.EDIT, ActionImpact.UPDATED, "btn-edit");

    managementGroup.addButton(messages.liftButton(), DisposalHoldAction.LIFT, ActionImpact.UPDATED, "btn-lift-hold");

    disposalHoldActionableBundle.addGroup(managementGroup);
    return disposalHoldActionableBundle;
  }

  @Override
  public Action<DisposalHold> actionForName(String name) {
    return null;
  }
}
