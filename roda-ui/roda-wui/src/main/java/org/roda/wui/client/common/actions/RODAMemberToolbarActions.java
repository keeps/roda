package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.accessKey.CreateAccessKeyRequest;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.ChangeUserStatusRequest;
import org.roda.core.data.v2.user.requests.UpdateUserRequest;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.AccessKeyDialogs;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RODAMembersDialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.members.tabs.PermissionsPanel;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
          || (action.equals(RODAMemberAction.REMOVE)
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
      activate(object, callback);
    } else if (action.equals(RODAMemberAction.DEACTIVATE)) {
      deactivate(object, callback);
    } else if (action.equals(RODAMemberAction.EDIT)) {
      edit(object, callback);
    } else if (action.equals(RODAMemberAction.REMOVE)) {
      remove(object, callback);
    } else if (action.equals(RODAMemberAction.NEW_ACCESS_KEY)) {
      createNewAccessKey(callback, object);
    } else if (action.equals(RODAMemberAction.ADD_NEW_GROUP)) {
      addNewGroup(object, callback);
    } else if (action.equals(RODAMemberAction.ADD_NEW_MEMBER)) {
      addNewMember(object, callback);
    } else if (action.equals(RODAMemberAction.EDIT_PERMISSIONS)) {
      editPermissions(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void editPermissions(RODAMember object, AsyncCallback<ActionImpact> callback) {
    PermissionsPanel permissionsPanel = new PermissionsPanel(object, false, true);

    RODAMembersDialogs.showEditRODAMemberPermissionsPanel(messages.editPermissionsModalTitle(), messages.cancelButton(),
      messages.updateButton(), permissionsPanel, new ActionAsyncCallback<List<String>>(callback) {
        @Override
        public void onSuccess(List<String> result) {
          Services services = new Services("Edit RODA member permissions", "update");
          if (object.isUser()) {
            UpdateUserRequest request = new UpdateUserRequest();
            User user = (User) object;
            user.setDirectRoles(new HashSet<>(result));
            request.setPassword(null);
            request.setUser(user);
            services.membersResource(s -> s.updateUser(request)).whenComplete((res, error) -> {
              if (error != null) {
                doActionCallbackUpdated();
                Toast.showError(error.getMessage());
              } else {
                doActionCallbackUpdated();
                Toast.showInfo(messages.userPermissions(), messages.permissionsUpdateWithSuccess());
              }
            });
          } else {
            Group group = (Group) object;
            group.setDirectRoles(new HashSet<>(result));
            services.membersResource(s -> s.updateGroup(group)).whenComplete((res, error) -> {
              if (error != null) {
                doActionCallbackUpdated();
                Toast.showError(error.getMessage());
              } else {
                doActionCallbackUpdated();
                Toast.showInfo(messages.groupPermissions(), messages.permissionsUpdateWithSuccess());
              }
            });
          }
        }
      });
  }

  private void addNewMember(RODAMember object, AsyncCallback<ActionImpact> callback) {
    Filter filter = new Filter(new BasicSearchFilterParameter(RodaConstants.MEMBERS_IS_USER, "true"));
    ((Group) object).getUsers()
      .forEach(user -> filter.add(new NotSimpleFilterParameter(RodaConstants.MEMBERS_NAME, user)));

    RODAMembersDialogs.showAddGroupsToRODAMember(SafeHtmlUtils.fromSafeConstant(messages.addNewMemberToGroupTitle()),
      messages.cancelButton(), messages.confirmButton(), filter,
      new ActionAsyncCallback<SelectedItems<RODAMember>>(callback) {
        @Override
        public void onSuccess(SelectedItems<RODAMember> result) {
          Services services = new Services("Add member to RODA group", "update");
          services
            .membersResource(s -> s.addMembersToGroup(object.getId(), SelectedItemsUtils.convertToRESTRequest(result)))
            .whenComplete((group, error) -> {
              if (error != null) {
                doActionCallbackUpdated();
                Toast.showError(error.getMessage());
              } else {
                doActionCallbackUpdated();
                Toast.showInfo(messages.membersTabTitle(), messages.memberSuccessfullyAdded());
              }
            });
        }
      });
  }

  private void addNewGroup(RODAMember object, AsyncCallback<ActionImpact> callback) {
    Filter filter = new Filter(new BasicSearchFilterParameter(RodaConstants.MEMBERS_IS_USER, "false"));
    ((User) object).getGroups()
      .forEach(group -> filter.add(new NotSimpleFilterParameter(RodaConstants.MEMBERS_NAME, group)));

    RODAMembersDialogs.showAddGroupsToRODAMember(SafeHtmlUtils.fromSafeConstant(messages.addNewGroupModalTitle()),
      messages.cancelButton(), messages.confirmButton(), filter,
      new ActionAsyncCallback<SelectedItems<RODAMember>>(callback) {
        @Override
        public void onSuccess(SelectedItems<RODAMember> result) {
          Services services = new Services("Add groups to RODA member", "update");
          services
            .membersResource(s -> s.addGroupsToUser(object.getId(), SelectedItemsUtils.convertToRESTRequest(result)))
            .whenComplete((res, error) -> {
              if (error != null) {
                doActionCallbackUpdated();
                Toast.showError(error.getMessage());
              } else {
                doActionCallbackUpdated();
                Toast.showInfo(messages.groups(), messages.groupSuccessfullyAdded());
              }
            });
        }
      });
  }

  private void activate(RODAMember object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.activateUserTitle(), messages.activateUserConfirmationMessage(),
      messages.dialogNo(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Activate RODA member", "activate");
            ChangeUserStatusRequest request = new ChangeUserStatusRequest(
              SelectedItemsUtils.convertToRESTRequest(objectToSelectedItems(object, RODAMember.class)), true);
            services.membersResource(s -> s.changeActive(request)).whenComplete((res, error) -> {
              if (error == null) {
                Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
                Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    doActionCallbackUpdated();
                  }

                  @Override
                  public void onSuccess(final Void nothing) {
                    callback.onSuccess(ActionImpact.NONE);
                    HistoryUtils.newHistory(ShowJob.RESOLVER, res.getId());
                  }
                });
              }
            });
          } else {
            doActionCallbackNone();
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }
      });
  }

  private void edit(RODAMember object, AsyncCallback<ActionImpact> callback) {
    if (object.isUser()) {
      RODAMembersDialogs.editUserInformation(messages.editUserTitle(), messages.cancelButton(), messages.updateButton(),
        (User) object, new ActionAsyncCallback<UpdateUserRequest>(callback) {
          @Override
          public void onSuccess(UpdateUserRequest result) {
            Services services = new Services("Update User", "update");
            services.membersResource(s -> s.updateUser(result)).whenComplete((res, error) -> {
              if (error == null) {
                Toast.showInfo(messages.users(), "User successfully updated");
                doActionCallbackUpdated();
              } else {
                doActionCallbackNone();
              }
            });
          }
        });
    } else {
      RODAMembersDialogs.editGroupInformation(messages.editGroupTitle(), messages.cancelButton(),
        messages.updateButton(), (Group) object, new ActionAsyncCallback<Group>(callback) {
          @Override
          public void onSuccess(Group result) {
            Services services = new Services("Update group", "update");
            services.membersResource(s -> s.updateGroup(result)).whenComplete((res, error) -> {
              if (error == null) {
                Toast.showInfo(messages.groups(), "Group successfully updated");
                doActionCallbackUpdated();
              } else {
                doActionCallbackNone();
              }
            });
          }
        });
    }
  }

  private void deactivate(RODAMember object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.deactivateUserTitle(), messages.deactivateUserConfirmationMessage(),
      messages.dialogNo(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Deactivate RODA member", "deactivate");
            ChangeUserStatusRequest request = new ChangeUserStatusRequest(
              SelectedItemsUtils.convertToRESTRequest(objectToSelectedItems(object, RODAMember.class)), false);
            services.membersResource(s -> s.changeActive(request)).whenComplete((res, error) -> {
              if (error == null) {
                Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
                Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    doActionCallbackUpdated();
                  }

                  @Override
                  public void onSuccess(final Void nothing) {
                    callback.onSuccess(ActionImpact.NONE);
                    HistoryUtils.newHistory(ShowJob.RESOLVER, res.getId());
                  }
                });
              }
            });
          } else {
            doActionCallbackNone();
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          doActionCallbackNone();
          callback.onFailure(caught);
        }
      });
  }

  private void remove(RODAMember object, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(object.isUser() ? messages.singleUserRemoveConfirmDialogTitle() : messages.singleGroupRemoveConfirmDialogTitle(),
      object.isUser() ? messages.singleUserRemoveConfirmDialogMessage(object.getId()) : messages.singleGroupRemoveConfirmDialogMessage(object.getId()), messages.dialogNo(), messages.dialogYes(),
      new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Remove RODA members", "remove");
            services
              .membersResource(s -> s.deleteMultipleMembers(
                SelectedItemsUtils.convertToRESTRequest(objectToSelectedItems(object, RODAMember.class))))
              .whenComplete((res, error) -> {
                if (error == null) {
                  callback.onSuccess(Actionable.ActionImpact.DESTROYED);
                } else {
                  Toast.showError(error.getMessage());
                  callback.onSuccess(Actionable.ActionImpact.NONE);
                }
              });
          } else {
            doActionCallbackNone();
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }
      });
  }

  private void createNewAccessKey(AsyncCallback<ActionImpact> callback, RODAMember user) {
    callback.onSuccess(ActionImpact.NONE);
    AccessKeyDialogs.createAccessKeyDialog(messages.createAccessKeyTitle(), null, true,
      new ActionNoAsyncCallback<CreateAccessKeyRequest>(callback) {
        @Override
        public void onSuccess(CreateAccessKeyRequest keyRequest) {
          Services services = new Services("Create access key", "create");
          CreateAccessKeyRequest createAccessKeyRequest = new CreateAccessKeyRequest(keyRequest.getName(),
            keyRequest.getExpirationDate());
          services.membersResource(s -> s.createAccessKey(user.getId(), createAccessKeyRequest))
            .whenComplete((response, error) -> {
              if (response != null) {
                AccessKeyDialogs.showAccessKeyDialog(messages.accessKeyLabel(), response,
                  new NoAsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                      callback.onSuccess(ActionImpact.UPDATED);
                    }
                  });
              }
            });
        }
      });
  }

  @Override
  public ActionableBundle<RODAMember> createActionsBundle() {
    ActionableBundle<RODAMember> actionableBundle = new ActionableBundle<>();

    // ACTIONS
    ActionableGroup<RODAMember> actionableGroup = new ActionableGroup<>(messages.manage(), "btn-edit");
    actionableGroup.addButton(messages.editUserAction(), RODAMemberAction.EDIT, ActionImpact.UPDATED, "btn-edit");

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
