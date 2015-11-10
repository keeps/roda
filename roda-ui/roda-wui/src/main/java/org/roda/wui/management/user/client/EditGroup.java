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

import org.roda.core.data.common.NoSuchGroupException;
import org.roda.core.data.v2.Group;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class EditGroup extends WUIWindow {

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final Group group;

  private final WUIButton apply;

  private final WUIButton cancel;

  private TextBox groupName;

  private final TextBox groupFullname;

  // private final GroupSelect groupSelect;

  private final PermissionsPanel permissionsPanel;

  /**
   * Create a new panel to edit a group
   * 
   * @param group
   *          the group to edit
   */
  public EditGroup(Group group) {
    super(constants.editGroupTitle(), 690, 346);
    this.group = group;

    apply = new WUIButton(constants.editGroupApply(), WUIButton.Left.ROUND, WUIButton.Right.REC);

    cancel = new WUIButton(constants.editGroupCancel(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

    apply.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        final String name = groupName.getText();
        String fullname = groupFullname.getText();

        // Set<String> memberGroups = groupSelect.getMemberGroups();
        Set<String> directRoles = permissionsPanel.getDirectRoles();

        EditGroup.this.group.setFullName(fullname);
        // EditGroup.this.group.setDirectGroups(memberGroups);
        EditGroup.this.group.setDirectRoles(directRoles);

        UserManagementService.Util.getInstance().editGroup(EditGroup.this.group, new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            if (caught instanceof NoSuchGroupException) {
              Window.alert(messages.editGroupNotFound(name));
              EditGroup.this.cancel();
            } else {
              Window.alert(messages.editGroupFailure(EditGroup.this.group.getName(), caught.getMessage()));
            }
          }

          public void onSuccess(Void result) {
            EditGroup.this.hide();
            EditGroup.this.onSuccess();
          }

        });
      }

    });

    cancel.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        EditGroup.this.cancel();
      }

    });

    this.addToBottom(apply);
    this.addToBottom(cancel);

    VerticalPanel groupDataPanel = new VerticalPanel();

    VerticalPanel basicInfoPanel = new VerticalPanel();

    groupName = new TextBox();
    groupFullname = new TextBox();

    groupName.setEnabled(false);

    VerticalPanel namePanel = concatInPanel(constants.groupName(), groupName);
    VerticalPanel fullnamePanel = concatInPanel(constants.groupFullname(), groupFullname);

    basicInfoPanel.add(namePanel);
    basicInfoPanel.add(fullnamePanel);

    // groupSelect = new GroupSelect(false);

    groupDataPanel.add(basicInfoPanel);
    // groupDataPanel.add(groupSelect);

    // groupSelect.addChangeListener(new ChangeListener() {
    //
    // public void onChange(Widget sender) {
    // apply.setEnabled(true);
    // }
    //
    // });

    apply.setEnabled(false);

    groupFullname.addKeyboardListener(new KeyboardListener() {

      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
      }

      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
      }

      public void onKeyUp(Widget sender, char keyCode, int modifiers) {
        if (groupFullname.getText().length() > 0) {
          apply.setEnabled(true);
        } else {
          apply.setEnabled(false);
        }

      }

    });

    permissionsPanel = new PermissionsPanel();
    permissionsPanel.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        apply.setEnabled(true);
      }

    });

    this.addTab(groupDataPanel, constants.dataTabTitle());
    this.addTab(permissionsPanel, constants.permissionsTabTitle());

    this.getTabPanel().addTabListener(new TabListener() {

      public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
        if (tabIndex == 1) {
          // permissionsPanel.updateLockedPermissions(groupSelect.getMemberGroups());

        }
        return true;
      }

      public void onTabSelected(SourcesTabEvents sender, int tabIndex) {

      }

    });

    this.selectTab(0);

    this.init();

    getTabPanel().addStyleName("office-edit-group-tabpanel");
    basicInfoPanel.addStyleName("basicInfoPanel");
    namePanel.addStyleName("namePanel");
    fullnamePanel.addStyleName("fullnamePanel");

  }

  protected void init() {
    groupName.setText(group.getName());
    groupFullname.setText(group.getFullName());

    Set<String> superGroups = group.getAllGroups();
    // groupSelect.setMemberGroups(superGroups);
    // groupSelect.exclude(group.getName());
    // groupSelect.setVisible(true);

    permissionsPanel.setEnabled(false);
    Set<String> roles = group.getAllRoles();
    permissionsPanel.checkPermissions(roles, false);
    permissionsPanel.setEnabled(true);
  }

  protected void cancel() {
    this.hide();
    super.onCancel();
  }

  private VerticalPanel concatInPanel(String title, Widget input) {
    VerticalPanel vp = new VerticalPanel();
    Label label = new Label(title);
    vp.add(label);
    vp.add(input);

    vp.addStyleName("office-input-panel");
    label.addStyleName("office-input-title");
    input.addStyleName("office-input-widget");

    return vp;
  }
}
