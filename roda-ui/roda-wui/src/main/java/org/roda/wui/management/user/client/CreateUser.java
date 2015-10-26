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

import java.util.Set;

import org.roda.core.common.EmailAlreadyExistsException;
import org.roda.core.common.UserAlreadyExistsException;
import org.roda.core.data.v2.User;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateUser extends WUIWindow {

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

  private final WUIButton create;

  private final WUIButton cancel;

  private final UserDataPanel userDataPanel;

  private final PermissionsPanel permissionsPanel;

  /**
   * Create new panel to create a user
   */
  public CreateUser() {
    super(constants.createUserTitle(), 699, 630);

    create = new WUIButton(constants.createUserCreate(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_DOWN);

    cancel = new WUIButton(constants.createUserCancel(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

    create.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        final User user = userDataPanel.getValue();
        final String password = userDataPanel.getPassword();
        final Set<String> specialroles = permissionsPanel.getDirectRoles();
        user.setDirectRoles(specialroles);

        UserManagementService.Util.getInstance().createUser(user, password, new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            if (caught instanceof UserAlreadyExistsException) {
              Window.alert(messages.createUserAlreadyExists(user.getName()));

            } else if (caught instanceof EmailAlreadyExistsException) {
              Window.alert(messages.createUserEmailAlreadyExists(user.getEmail()));

            } else {
              Window.alert(messages.createUserFailure(caught.getMessage()));
            }
          }

          public void onSuccess(Void result) {
            CreateUser.this.hide();
            CreateUser.this.onSuccess();
          }

        });
      }

    });

    cancel.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        CreateUser.this.cancel();
      }

    });

    this.addToBottom(create);
    this.addToBottom(cancel);

    this.userDataPanel = new UserDataPanel(false, true);
    this.permissionsPanel = new PermissionsPanel();

    create.setEnabled(userDataPanel.isValid());

    userDataPanel.addValueChangeHandler(new ValueChangeHandler<User>() {

      @Override
      public void onValueChange(ValueChangeEvent<User> event) {
        create.setEnabled(userDataPanel.isValid());
      }
    });

    this.addTab(userDataPanel, constants.dataTabTitle());

    this.addTab(permissionsPanel, constants.permissionsTabTitle());
    this.getTabPanel().addTabListener(new TabListener() {

      public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
        if (tabIndex == 1) {
          permissionsPanel.updateLockedPermissions(userDataPanel.getMemberGroups());
        }
        return true;
      }

      public void onTabSelected(SourcesTabEvents sender, int tabIndex) {

      }

    });

    this.selectTab(0);

    getTabPanel().addStyleName("office-create-user-tabpanel");
  }

  private void cancel() {
    this.hide();
    super.onCancel();
  }

}
