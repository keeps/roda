/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.management;

import java.util.List;

import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.access.AccessKeyTablePanel;
import org.roda.wui.client.management.access.CreateAccessKey;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class EditUser extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String username = historyTokens.get(0);
        UserManagementService.Util.getInstance().retrieveUser(username, new AsyncCallback<User>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(User user) {
            EditUser editUser = new EditUser(user);
            callback.onSuccess(editUser);
          }
        });
      } else {
        HistoryUtils.newHistory(MemberManagement.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(MemberManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_user";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditUser> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final User user;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonDeActivate;

  @UiField
  Button buttonRemove;

  @UiField
  Button buttonAddAccessKey;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  UserDataPanel userDataPanel;

  @UiField
  FlowPanel accessKeyTablePanel;

  /**
   * Create a new panel to edit a user
   *
   * @param user
   *          the user to edit
   */
  public EditUser(User user) {
    this.user = user;

    this.userDataPanel = new UserDataPanel(true, true, true);
    this.userDataPanel.setUser(user);

    initWidget(uiBinder.createAndBindUi(this));

    accessKeyTablePanel.add(new AccessKeyTablePanel(user.getId()));
    userDataPanel.setUsernameReadOnly(true);

    buttonDeActivate.setEnabled(true);
    if (user.isActive()) {
      buttonDeActivate.setText(messages.editUserDeactivate());
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private SecureString getPassword() {
    if(userDataPanel.getPassword() != null) {
      return new SecureString(userDataPanel.getPassword().toCharArray());
    } else {
      return null;
    }
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (userDataPanel.isChanged()) {
      if (userDataPanel.isValid()) {
        final User updatedUser = userDataPanel.getUser();
        try (SecureString password = getPassword()) {

          UserManagementService.Util.getInstance().updateUser(updatedUser, password, userDataPanel.getExtra(),
            new AsyncCallback<Void>() {

              @Override
              public void onFailure(Throwable caught) {
                errorMessage(caught, updatedUser);
              }

              @Override
              public void onSuccess(Void result) {
                HistoryUtils.newHistory(MemberManagement.RESOLVER);
              }
            });
        }
      } else {
        HistoryUtils.newHistory(MemberManagement.RESOLVER);
      }
    }
  }

  @UiHandler("buttonDeActivate")
  void buttonDeActivateHandler(ClickEvent e) {
    user.setActive(!user.isActive());

    UserManagementService.Util.getInstance().updateUser(user, null, userDataPanel.getExtra(),
      new AsyncCallback<Void>() {

        @Override
        public void onSuccess(Void result) {
          BrowserService.Util.getInstance().deactivateUserAccessKeys(user.getId(), new NoAsyncCallback<Void>());
          HistoryUtils.newHistory(MemberManagement.RESOLVER);
        }

        @Override
        public void onFailure(Throwable caught) {
          user.setActive(!user.isActive());
          errorMessage(caught, null);
        }
      });
  }

  @UiHandler("buttonAddAccessKey")
  void buttonAddAccessKeyHandler(ClickEvent e) {
    HistoryUtils.newHistory(CreateAccessKey.RESOLVER, user.getName());
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.userRemoveConfirmDialogTitle(), messages.userRemoveConfirmDialogMessage(),
      messages.dialogNo(), messages.dialogYes(), new AsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            UserManagementService.Util.getInstance().deleteUser(user.getId(), new AsyncCallback<Void>() {

              @Override
              public void onSuccess(Void result) {
                BrowserService.Util.getInstance().deleteUserAccessKeys(user.getId(), new NoAsyncCallback<Void>());
                HistoryUtils.newHistory(MemberManagement.RESOLVER);
              }

              @Override
              public void onFailure(Throwable caught) {
                errorMessage(caught, null);
              }
            });
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          errorMessage(caught, null);
        }
      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(MemberManagement.RESOLVER);
  }

  private void errorMessage(Throwable caught, User modifiedUser) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editUserNotFound(user.getName()));
      cancel();
    } else if (caught instanceof AlreadyExistsException) {
      String email = (modifiedUser != null) ? modifiedUser.getEmail() : user.getEmail();
      Toast.showError(messages.editUserEmailAlreadyExists(email));
    } else {
      Toast.showError(messages.editUserFailure(EditUser.this.user.getName(), caught.getMessage()));
    }
  }
}
