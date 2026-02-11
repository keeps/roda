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

import com.google.gwt.i18n.client.LocaleInfo;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.requests.ChangeUserStatusRequest;
import org.roda.core.data.v2.user.requests.CreateGroupRequest;
import org.roda.core.data.v2.user.requests.CreateUserRequest;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RODAMembersDialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.members.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class RODAMemberActions extends AbstractActionable<RODAMember> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final RODAMemberActions GENERAL_INSTANCE = new RODAMemberActions();

  private static final Set<RODAMemberAction> POSSIBLE_ACTIONS_WITHOUT_MEMBER = new HashSet<>(
    Arrays.asList(RODAMemberAction.NEW_USER, RODAMemberAction.NEW_GROUP));

  private static final Set<RODAMemberAction> POSSIBLE_ACTIONS_ON_USER = new HashSet<>(
    Arrays.asList(RODAMemberAction.ACTIVATE, RODAMemberAction.DEACTIVATE, RODAMemberAction.REMOVE));

  private static final Set<RODAMemberAction> POSSIBLE_ACTIONS_ON_GROUP = new HashSet<>(
    Arrays.asList(RODAMemberAction.REMOVE));

  private static final Set<RODAMemberAction> POSSIBLE_ACTIONS_ON_MEMBERS = new HashSet<>(
    Arrays.asList(RODAMemberAction.ACTIVATE, RODAMemberAction.DEACTIVATE, RODAMemberAction.REMOVE));

  private RODAMemberActions() {
    // do nothing
  }

  public static RODAMemberActions get() {
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
      createUser(callback);
    } else if (action.equals(RODAMemberAction.NEW_GROUP)) {
      createGroup(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<RODAMember> action, RODAMember object, AsyncCallback<ActionImpact> callback) {
    if (action.equals(RODAMemberAction.ACTIVATE)) {
      activate(objectToSelectedItems(object, RODAMember.class), callback);
    } else if (action.equals(RODAMemberAction.DEACTIVATE)) {
      deactivate(objectToSelectedItems(object, RODAMember.class), callback);
    } else if (action.equals(RODAMemberAction.REMOVE)) {
      remove(objectToSelectedItems(object, RODAMember.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<RODAMember> action, SelectedItems<RODAMember> objects, AsyncCallback<ActionImpact> callback) {
    if (action.equals(RODAMemberAction.ACTIVATE)) {
      activate(objects, callback);
    } else if (action.equals(RODAMemberAction.DEACTIVATE)) {
      deactivate(objects, callback);
    } else if (action.equals(RODAMemberAction.REMOVE)) {
      remove(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void activate(SelectedItems<RODAMember> objects, AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Activate RODA member", "activate");
    ChangeUserStatusRequest request = new ChangeUserStatusRequest(SelectedItemsUtils.convertToRESTRequest(objects),
      true);
    services.membersResource(s -> s.changeActive(request)).whenComplete((res, error) -> {
      if (error == null) {
        Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
        Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onSuccess(Actionable.ActionImpact.UPDATED);
            HistoryUtils.newHistory(MemberManagement.RESOLVER.getHistoryPath());
          }

          @Override
          public void onSuccess(final Void nothing) {
            callback.onSuccess(ActionImpact.NONE);
            HistoryUtils.newHistory(ShowJob.RESOLVER, res.getId());
          }
        });
      }
    });
  }

  private void deactivate(SelectedItems<RODAMember> objects, AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Deactivate RODA member", "deactivate");
    ChangeUserStatusRequest request = new ChangeUserStatusRequest(SelectedItemsUtils.convertToRESTRequest(objects),
      false);
    services.membersResource(s -> s.changeActive(request)).whenComplete((res, error) -> {
      if (error == null) {
        Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
        Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onSuccess(Actionable.ActionImpact.UPDATED);
            HistoryUtils.newHistory(MemberManagement.RESOLVER.getHistoryPath());
          }

          @Override
          public void onSuccess(final Void nothing) {
            callback.onSuccess(ActionImpact.NONE);
            HistoryUtils.newHistory(ShowJob.RESOLVER, res.getId());
          }
        });
      }
    });
  }

  private void remove(SelectedItems<RODAMember> objects, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.userRemoveConfirmDialogTitle(), messages.userRemoveConfirmDialogMessage(),
      messages.dialogNo(), messages.dialogYes(), new AsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Remove RODA members", "remove");
            services.membersResource(s -> s.deleteMultipleMembers(SelectedItemsUtils.convertToRESTRequest(objects)))
              .whenComplete((res, error) -> {
                if (error == null) {
                  callback.onSuccess(Actionable.ActionImpact.DESTROYED);
                  HistoryUtils.newHistory(MemberManagement.RESOLVER.getHistoryPath());
                }
              });
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }
      });
  }

  private void createUser(AsyncCallback<ActionImpact> callback) {
    RODAMembersDialogs.createUser(messages.addUserButton(), messages.cancelButton(), messages.saveButton(),
      new ActionAsyncCallback<CreateUserRequest>(callback) {
        @Override
        public void onSuccess(CreateUserRequest result) {
          Services services = new Services("Create User", "create");
          services.membersResource(s -> s.createUser(result, LocaleInfo.getCurrentLocale().getLocaleName()))
            .whenComplete((res, error) -> {
              if (error == null) {
                Toast.showInfo(messages.users(), "User successfully created");
                doActionCallbackUpdated();
              } else {
                doActionCallbackNone();
              }
            });
        }

        @Override
        public void onFailure(Throwable caught) {
          doActionCallbackNone();
        }
      });
  }

  private void createGroup(AsyncCallback<ActionImpact> callback) {
    RODAMembersDialogs.createGroup(messages.addGroupButton(), messages.cancelButton(), messages.saveButton(),
      new ActionAsyncCallback<CreateGroupRequest>(callback) {

        @Override
        public void onSuccess(CreateGroupRequest createGroupRequest) {
          Services services = new Services("Create Group", "create");
          services.membersResource(s -> s.createGroup(createGroupRequest)).whenComplete((res, error) -> {
            if (error == null) {
              Toast.showInfo(messages.groups(), "Group successfully created");
              doActionCallbackUpdated();
            } else {
              doActionCallbackNone();
            }
          });
        }

        @Override
        public void onFailure(Throwable caught) {
          doActionCallbackNone();
        }
      });
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
