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

import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class RODAMemberSearchWrapperActions extends AbstractActionable<RODAMember> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final RODAMemberSearchWrapperActions GENERAL_INSTANCE = new RODAMemberSearchWrapperActions();

  private static final Set<RODAMemberAction> POSSIBLE_ACTIONS_WITHOUT_MEMBER = new HashSet<>(
    Arrays.asList(RODAMemberAction.NEW_USER, RODAMemberAction.NEW_GROUP));

  private static final Set<RODAMemberAction> POSSIBLE_ACTIONS_ON_GROUP = new HashSet<>(
    Arrays.asList(RODAMemberAction.REMOVE));

  private static final Set<RODAMemberAction> POSSIBLE_ACTIONS_ON_MEMBERS = new HashSet<>(
    Arrays.asList(RODAMemberAction.ACTIVATE, RODAMemberAction.DEACTIVATE, RODAMemberAction.REMOVE));

  private RODAMemberSearchWrapperActions() {
    // do nothing
  }

  public static RODAMemberSearchWrapperActions get() {
    return GENERAL_INSTANCE;
  }

  @Override
  public RODAMemberAction[] getActions() {
    return RODAMemberAction.values();
  }

  @Override
  public RODAMemberAction actionForName(String name) {
    return RODAMemberAction.valueOf(name);
  }

  @Override
  public CanActResult userCanAct(Action<RODAMember> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<RODAMember> action) {
    return new CanActResult(POSSIBLE_ACTIONS_WITHOUT_MEMBER.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<RODAMember> action, RODAMember object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<RODAMember> action, RODAMember object) {
    if (object.isUser()) {
      return new CanActResult((action.equals(RODAMemberAction.DEACTIVATE) && object.isActive())
        || (action.equals(RODAMemberAction.ACTIVATE) && !object.isActive()) || (action.equals(RODAMemberAction.REMOVE)),
        CanActResult.Reason.CONTEXT, messages.reasonCantActOnUser());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_GROUP.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonCantActOnGroup());
    }
  }

  @Override
  public CanActResult userCanAct(Action<RODAMember> action, SelectedItems<RODAMember> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.CONTEXT, messages.reasonCantActOnGroup());
  }

  @Override
  public CanActResult contextCanAct(Action<RODAMember> action, SelectedItems<RODAMember> objects) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_MEMBERS.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<RODAMember> action, AsyncCallback<ActionImpact> callback) {
    if (action.equals(RODAMemberAction.NEW_USER)) {
      RODAMemberActions.createUser(callback);
    } else if (action.equals(RODAMemberAction.NEW_GROUP)) {
      RODAMemberActions.createGroup(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<RODAMember> action, RODAMember object, AsyncCallback<ActionImpact> callback) {
    if (action.equals(RODAMemberAction.ACTIVATE)) {
      RODAMemberActions.activate(objectToSelectedItems(object, RODAMember.class), callback);
    } else if (action.equals(RODAMemberAction.DEACTIVATE)) {
      RODAMemberActions.deactivate(objectToSelectedItems(object, RODAMember.class), callback);
    } else if (action.equals(RODAMemberAction.REMOVE)) {
      RODAMemberActions.remove(objectToSelectedItems(object, RODAMember.class), callback);
    } else if (action.equals(RODAMemberAction.EDIT)) {
      RODAMemberActions.edit(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<RODAMember> action, SelectedItems<RODAMember> objects, AsyncCallback<ActionImpact> callback) {
    if (action.equals(RODAMemberAction.ACTIVATE)) {
      RODAMemberActions.activate(objects, callback);
    } else if (action.equals(RODAMemberAction.DEACTIVATE)) {
      RODAMemberActions.deactivate(objects, callback);
    } else if (action.equals(RODAMemberAction.REMOVE)) {
      RODAMemberActions.remove(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<RODAMember> createActionsBundle() {
    ActionableBundle<RODAMember> transferredResourcesActionableBundle = new ActionableBundle<>();

    // ACTIONS
    ActionableGroup<RODAMember> actionableGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    actionableGroup.addButton(messages.addUserButton(), RODAMemberAction.NEW_USER, ActionImpact.UPDATED,
      "btn-plus-circle");
    actionableGroup.addButton(messages.addGroupButton(), RODAMemberAction.NEW_GROUP, ActionImpact.UPDATED,
      "btn-plus-circle");
    actionableGroup.addButton(messages.editUserActivate(), RODAMemberAction.ACTIVATE, ActionImpact.UPDATED,
      "btn-enable-user");
    actionableGroup.addButton(messages.editUserDeactivate(), RODAMemberAction.DEACTIVATE, ActionImpact.UPDATED,
      "btn-disable-user");
    actionableGroup.addButton(messages.editUserRemove(), RODAMemberAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    transferredResourcesActionableBundle.addGroup(actionableGroup);
    return transferredResourcesActionableBundle;
  }
}
