/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementMessages;
import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.management.client.Management;

/**
 * @author Luis Faria
 * 
 */
public class EditUser extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(String[] historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.length == 1) {
        String username = historyTokens[0];
        UserManagementService.Util.getInstance().getUser(username, new AsyncCallback<RodaUser>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(RodaUser user) {
            EditUser editUser = new EditUser(user);
            callback.onSuccess(editUser);
          }
        });
      } else {
        History.newItem(MemberManagement.RESOLVER.getHistoryPath());
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public String getHistoryPath() {
      return Management.RESOLVER.getHistoryPath() + "." + getHistoryToken();
    }

    public String getHistoryToken() {
      return "user";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditUser> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final RodaUser user;

  private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  private final UserDataPanel userDataPanel;

  private final PermissionsPanel permissionsPanel;

  /**
   * Create a new panel to edit a user
   * 
   * @param user
   *          the user to edit
   */
  public EditUser(RodaUser user) {
    this.user = user;

    initWidget(uiBinder.createAndBindUi(this));

    this.userDataPanel = new UserDataPanel(false, true, true);
    this.permissionsPanel = new PermissionsPanel();

    buttonApply.setEnabled(false);

    // userDataPanel.setUsernameReadOnly(true);
    // userDataPanel.addChangeListener(new ChangeListener() {
    //
    // public void onChange(Widget sender) {
    // buttonApply.setEnabled(userDataPanel.isValid());
    // }
    //
    // });

    // permissionsPanel.addChangeListener(new ChangeListener() {
    // public void onChange(Widget sender) {
    // apply.setEnabled(userDataPanel.isValid());
    // }
    // });

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

    this.init();

    // getTabPanel().addStyleName("office-edit-user-tabpanel");
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    final User user = userDataPanel.getUser();
    final String password = userDataPanel.getPassword();

    final Set<String> specialroles = permissionsPanel.getDirectRoles();
    user.setDirectRoles(specialroles);

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
        History.newItem(MemberManagement.RESOLVER.getHistoryPath());
      }

    });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    History.newItem(MemberManagement.RESOLVER.getHistoryPath());
  }

  private void init() {
    // userDataPanel.setUser(user);
    // userDataPanel.setVisible(true);

    // UserManagementService.Util.getInstance().getUserDirectRoles(user.getName(),
    // new AsyncCallback<Set<String>>() {
    //
    // public void onFailure(Throwable caught) {
    // logger.error("Error while getting " + EditUser.this.user.getName() + "
    // roles", caught);
    // }
    //
    // public void onSuccess(Set<String> directRoles) {
    // permissionsPanel.checkPermissions(directRoles, false);
    // permissionsPanel.setEnabled(true);
    // }
    //
    // });

  }

}
