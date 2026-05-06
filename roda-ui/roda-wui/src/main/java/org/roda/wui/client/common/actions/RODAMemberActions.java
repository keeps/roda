package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.common.pekko.messages.events.EventUserUpdated;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
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
import org.roda.wui.client.common.dialogs.AccessKeyDialogs;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RODAMembersDialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.members.CreateGroup;
import org.roda.wui.client.management.members.CreateUser;
import org.roda.wui.client.management.members.EditGroup;
import org.roda.wui.client.management.members.EditUser;
import org.roda.wui.client.management.members.MemberManagement;
import org.roda.wui.client.management.members.tabs.PermissionsPanel;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.HashSet;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RODAMemberActions {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private RODAMemberActions() {
    // private constructor
  }

  public static void createUser(AsyncCallback<Actionable.ActionImpact> callback) {
    HistoryUtils.newHistory(CreateUser.RESOLVER.getHistoryPath());
    callback.onSuccess(Actionable.ActionImpact.UPDATED);
  }

  public static void createGroup(AsyncCallback<Actionable.ActionImpact> callback) {
    HistoryUtils.newHistory(CreateGroup.RESOLVER.getHistoryPath());
    callback.onSuccess(Actionable.ActionImpact.UPDATED);
  }

  public static void edit(RODAMember member, AsyncCallback<Actionable.ActionImpact> callback) {
    if (member.isUser()) {
      HistoryUtils.newHistory(EditUser.RESOLVER, member.getId());
      callback.onSuccess(Actionable.ActionImpact.UPDATED);
    } else {
      HistoryUtils.newHistory(EditGroup.RESOLVER, member.getId());
      callback.onSuccess(Actionable.ActionImpact.UPDATED);
    }
  }

  public static void remove(SelectedItems<RODAMember> objects, AsyncCallback<Actionable.ActionImpact> callback) {
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

  public static void remove(SelectedItems<RODAMember> items, RODAMember object,
    AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(
      object.isUser() ? messages.singleUserRemoveConfirmDialogTitle() : messages.singleGroupRemoveConfirmDialogTitle(),
      object.isUser() ? messages.singleUserRemoveConfirmDialogMessage(object.getId())
        : messages.singleGroupRemoveConfirmDialogMessage(object.getId()),
      messages.dialogNo(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Remove RODA members", "remove");
            services.membersResource(s -> s.deleteMultipleMembers(SelectedItemsUtils.convertToRESTRequest(items)))
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

  public static void activate(SelectedItems<RODAMember> objects, AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.activateUserTitle(), messages.activateUserConfirmationMessage(),
      messages.dialogNo(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Activate RODA member", "activate");
            ChangeUserStatusRequest request = new ChangeUserStatusRequest(
              SelectedItemsUtils.convertToRESTRequest(objects), true);
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
                    callback.onSuccess(Actionable.ActionImpact.NONE);
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

  public static void deactivate(SelectedItems<RODAMember> objects, AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.deactivateUserTitle(), messages.deactivateUserConfirmationMessage(),
      messages.dialogNo(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {

            Services services = new Services("Deactivate RODA member", "deactivate");
            ChangeUserStatusRequest request = new ChangeUserStatusRequest(
              SelectedItemsUtils.convertToRESTRequest(objects), false);
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
                    callback.onSuccess(Actionable.ActionImpact.NONE);
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

  public static void addNewMember(RODAMember object, AsyncCallback<Actionable.ActionImpact> callback) {
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

  public static void addNewGroup(RODAMember object, AsyncCallback<Actionable.ActionImpact> callback) {
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

  public static void createNewAccessKey(AsyncCallback<Actionable.ActionImpact> callback, RODAMember user) {
    callback.onSuccess(Actionable.ActionImpact.NONE);
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
                      callback.onSuccess(Actionable.ActionImpact.UPDATED);
                    }
                  });
              }
            });
        }
      });
  }

  public static void changePassword(RODAMember object, AsyncCallback<Actionable.ActionImpact> callback) {
    RODAMembersDialogs.setUserPassword(messages.userDataChangePassword(), new ActionAsyncCallback<String>(callback) {
      @Override
      public void onSuccess(String result) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUser((User) object);
        request.setPassword(new SecureString(result.toCharArray()));
        request.setValues(((User) object).getExtra());

        Services services = new Services("Update user password", "update");
        services.membersResource(s -> s.updateUser(request)).whenComplete((res, error) -> {
          if (error != null) {
            callback.onSuccess(Actionable.ActionImpact.NONE);
          } else {
            callback.onSuccess(Actionable.ActionImpact.NONE);
            Toast.showError(messages.editUserFailure(object.getFullName(), error.getMessage()));
          }
        });
      }

        @Override
        public void onFailure(Throwable caught) {
            callback.onSuccess(Actionable.ActionImpact.NONE);
        }
    });
  }

  public static void editPermissions(RODAMember object, AsyncCallback<Actionable.ActionImpact> callback) {
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
}
