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
package org.roda.wui.management.user.client;

import java.util.List;

import org.roda.core.common.EmailAlreadyExistsException;
import org.roda.core.common.NoSuchUserException;
import org.roda.core.data.v2.User;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.UserLogin;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.management.client.Management;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementMessages;

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
        UserManagementService.Util.getInstance().getUser(username, new AsyncCallback<User>() {

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
        Tools.newHistory(MemberManagement.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(MemberManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "edit_user";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditUser> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final User user;

  private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  UserDataPanel userDataPanel;

  // private final PermissionsPanel permissionsPanel;

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
    // this.permissionsPanel = new PermissionsPanel();
    initWidget(uiBinder.createAndBindUi(this));

    buttonApply.setEnabled(false);

    userDataPanel.setUsernameReadOnly(true);

    userDataPanel.addValueChangeHandler(new ValueChangeHandler<User>() {

      @Override
      public void onValueChange(ValueChangeEvent<User> event) {
        buttonApply.setEnabled(userDataPanel.isValid());
      }
    });

    // permissionsPanel.addChangeListener(new ChangeListener() {
    // public void onChange(Widget sender) {
    // apply.setEnabled(userDataPanel.isValid());
    // }
    // // });

    // this.addTab(userDataPanel, constants.dataTabTitle());
    // this.addTab(permissionsPanel, constants.permissionsTabTitle());

    // this.getTabPanel().addTabListener(new TabListener() {
    //
    // public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex)
    // {
    // if (tabIndex == 1) {
    // permissionsPanel.updateLockedPermissions(userDataPanel.getMemberGroups());
    // }
    // return true;
    // }
    //
    // public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
    // }
    //
    // });
    //
    // this.selectTab(0);

    // getTabPanel().addStyleName("office-edit-user-tabpanel");
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    final User user = userDataPanel.getUser();
    final String password = userDataPanel.getPassword();

    // final Set<String> specialroles = permissionsPanel.getDirectRoles();
    // user.setDirectRoles(specialroles);

    UserManagementService.Util.getInstance().editUser(user, password, new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {
        if (caught instanceof NoSuchUserException) {
          Window.alert(messages.editUserNotFound(user.getName()));
          cancel();
        } else if (caught instanceof EmailAlreadyExistsException) {
          Window.alert(messages.editUserEmailAlreadyExists(user.getEmail()));
          cancel();
        } else {
          Window.alert(messages.editUserFailure(EditUser.this.user.getName(), caught.getMessage()));
        }
      }

      public void onSuccess(Void result) {
        Tools.newHistory(MemberManagement.RESOLVER);
      }

    });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(MemberManagement.RESOLVER);
  }

}
