package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;

import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RODAMemberToolbarActions extends AbstractActionable<RODAMember> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RODAMemberAction> POSSIBLE_ACTIONS_ON_GROUP = new HashSet<>(
    Arrays.asList(RODAMemberAction.EDIT, RODAMemberAction.EDIT_PERMISSIONS, RODAMemberAction.REMOVE,
      RODAMemberAction.ADD_NEW_MEMBER));

  private static final RODAMemberToolbarActions GENERAL_INSTANCE = new RODAMemberToolbarActions();

  private RODAMemberToolbarActions() {
  }

  public static RODAMemberToolbarActions get() {
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
  public CanActResult userCanAct(Action<RODAMember> action, RODAMember object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<RODAMember> action, RODAMember object) {
    if (object.isUser()) {
      return new CanActResult(
        (action.equals(RODAMemberAction.DEACTIVATE) && object.isActive())
          || (action.equals(RODAMemberAction.ACTIVATE) && !object.isActive())
          || (action.equals(RODAMemberAction.REMOVE) || (action.equals(RODAMemberAction.CHANGE_PASSWORD))
            || (action.equals(RODAMemberAction.EDIT) || action.equals(RODAMemberAction.EDIT_PERMISSIONS)
              || (action.equals(RODAMemberAction.ADD_NEW_GROUP) && object.isUser())
              || (action.equals(RODAMemberAction.NEW_ACCESS_KEY) && object.isUser()))),
        CanActResult.Reason.CONTEXT, messages.reasonCantActOnUser());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_GROUP.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonCantActOnGroup());
    }
  }

  @Override
  public void act(Action<RODAMember> action, RODAMember object, AsyncCallback<ActionImpact> callback) {
    if (action.equals(RODAMemberAction.ACTIVATE)) {
      RODAMemberActions.activate(objectToSelectedItems(object, RODAMember.class), callback);
    } else if (action.equals(RODAMemberAction.DEACTIVATE)) {
      RODAMemberActions.deactivate(objectToSelectedItems(object, RODAMember.class), callback);
    } else if (action.equals(RODAMemberAction.EDIT)) {
      RODAMemberActions.edit(object, callback);
    } else if (action.equals(RODAMemberAction.REMOVE)) {
      RODAMemberActions.remove(objectToSelectedItems(object, RODAMember.class), object, callback);
    } else if (action.equals(RODAMemberAction.NEW_ACCESS_KEY)) {
      RODAMemberActions.createNewAccessKey(callback, object);
    } else if (action.equals(RODAMemberAction.ADD_NEW_GROUP)) {
      RODAMemberActions.addNewGroup(object, callback);
    } else if (action.equals(RODAMemberAction.ADD_NEW_MEMBER)) {
      RODAMemberActions.addNewMember(object, callback);
    } else if (action.equals(RODAMemberAction.EDIT_PERMISSIONS)) {
      RODAMemberActions.editPermissions(object, callback);
    } else if (action.equals(RODAMemberAction.CHANGE_PASSWORD)) {
      RODAMemberActions.changePassword(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<RODAMember> createActionsBundle() {
    ActionableBundle<RODAMember> actionableBundle = new ActionableBundle<>();

    // ACTIONS
    ActionableGroup<RODAMember> actionableGroup = new ActionableGroup<>(messages.manage(), "btn-edit");
    actionableGroup.addButton(messages.editUserAction(), RODAMemberAction.EDIT, ActionImpact.UPDATED, "btn-edit");
    actionableGroup.addButton(messages.userDataChangePassword(), RODAMemberAction.CHANGE_PASSWORD, ActionImpact.UPDATED, "btn-password");

    actionableGroup.addButton(messages.editUserActivate(), RODAMemberAction.ACTIVATE, ActionImpact.UPDATED,
      "btn-enable-user");
    actionableGroup.addButton(messages.editUserDeactivate(), RODAMemberAction.DEACTIVATE, ActionImpact.UPDATED,
      "btn-disable-user");
    actionableGroup.addButton(messages.addAccessKeyButton(), RODAMemberAction.NEW_ACCESS_KEY, ActionImpact.UPDATED,
      "btn-key");
    actionableGroup.addButton(messages.addToGroupButton(), RODAMemberAction.ADD_NEW_GROUP, ActionImpact.UPDATED,
      "btn-group");
    actionableGroup.addButton(messages.addNewMemberToGroupButton(), RODAMemberAction.ADD_NEW_MEMBER,
      ActionImpact.UPDATED, "btn-user");
    actionableGroup.addButton(messages.editButton(), RODAMemberAction.EDIT_PERMISSIONS, ActionImpact.UPDATED,
      "btn-edit");
    actionableGroup.addButton(messages.editUserRemove(), RODAMemberAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    actionableBundle.addGroup(actionableGroup);
    return actionableBundle;
  }
}
