/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldToolbarActions extends AbstractActionable<DisposalHold> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalHoldAction> POSSIBLE_ACTIONS_ON_DISPOSAL_HOLD = new HashSet<>(
    Arrays.asList(DisposalHoldAction.LIFT, DisposalHoldAction.EDIT));

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
    if (DisposalHoldAction.EDIT.equals(action)) {
      DisposalHoldActions.edit(hold, callback);
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
