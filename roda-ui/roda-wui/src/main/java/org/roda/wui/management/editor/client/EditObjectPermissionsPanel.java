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
package org.roda.wui.management.editor.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.LoadingPopup;
import org.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
public class EditObjectPermissionsPanel extends Composite {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final IndexedAIP aip;
  private Map<RODAMember, ObjectPermissions> permissions;

  private DockPanel layout;
  private Label title;
  private ScrollPanel memberListScroll;
  private VerticalPanel memberListPanel;
  private CheckBox applyRecursivelly;
  private List<WUIButton> actionButtons;
  private WUIButton addUser;
  private WUIButton addGroup;
  private WUIButton apply;

  private LoadingPopup loading;

  private List<UserMiniPermissionPanel> userMiniPermissionPanels;
  private List<GroupMiniPermissionPanel> groupMiniPermissionPanels;

  /**
   * Create a new edit producers panel
   * 
   * @param aip
   */
  public EditObjectPermissionsPanel(IndexedAIP aip) {
    this.aip = aip;
    this.permissions = new HashMap<RODAMember, ObjectPermissions>();
    userMiniPermissionPanels = new Vector<UserMiniPermissionPanel>();
    groupMiniPermissionPanels = new Vector<GroupMiniPermissionPanel>();

    layout = new DockPanel();
    title = new Label(constants.editProducersTitle());
    memberListPanel = new VerticalPanel();
    memberListScroll = new ScrollPanel(memberListPanel);

    applyRecursivelly = new CheckBox(constants.objectPermissionsApplyRecursivelly());

    actionButtons = new ArrayList<WUIButton>();
    addUser = new WUIButton(constants.objectPermissionsAddUser(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    addGroup = new WUIButton(constants.objectPermissionsAddGroup(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    apply = new WUIButton(constants.objectPermissionsSave(), WUIButton.Left.ROUND, WUIButton.Right.REC);

    actionButtons.add(addUser);
    actionButtons.add(addGroup);
    actionButtons.add(apply);

    layout.add(title, DockPanel.NORTH);
    layout.add(memberListScroll, DockPanel.CENTER);
    layout.add(applyRecursivelly, DockPanel.SOUTH);

    initWidget(layout);

    addUser.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        addUser();
      }

    });

    addGroup.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        addGroup();
      }

    });

    apply.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        apply();

      }

    });

    apply.setEnabled(false);

    applyRecursivelly.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        apply.setEnabled(true);
      }

    });

    loading = new LoadingPopup(this);

    layout.addStyleName("wui-edit-permissions");
    title.addStyleName("title");
    memberListScroll.addStyleName("member-list-scroll");
    memberListPanel.addStyleName("member-list-panel");

  }

  private boolean initialized = false;

  /**
   * Initialize producers panel
   */
  public void init() {
    if (!initialized) {
      initialized = true;
      initializePermissionsList();
    }

  }

  /**
   * Add user
   */
  public void addUser() {
    // final SelectUserWindow selectUser = new SelectUserWindow();
    // selectUser.addSuccessListener(new SuccessListener() {
    //
    // public void onCancel() {
    // // do nothing
    //
    // }
    //
    // public void onSuccess() {
    // selectUser.getSelected(new AsyncCallback<RODAMember>() {
    //
    // public void onFailure(Throwable caught) {
    // logger.error("Error adding user", caught);
    // }
    //
    // public void onSuccess(RODAMember member) {
    // setMemberPermissions(member, ObjectPermissions.ReadOnly);
    // }
    //
    // });
    // }
    //
    // });
    // selectUser.show();
  }

  /**
   * Add group
   */
  public void addGroup() {
    // final SelectGroupWindow selectGroup = new SelectGroupWindow();
    // selectGroup.addSuccessListener(new SuccessListener() {
    //
    // public void onCancel() {
    // // do nothing
    //
    // }
    //
    // public void onSuccess() {
    // selectGroup.getSelected(new AsyncCallback<RODAMember>() {
    //
    // public void onFailure(Throwable caught) {
    // logger.error("Error adding group", caught);
    // }
    //
    // public void onSuccess(RODAMember member) {
    // setMemberPermissions(member, ObjectPermissions.ReadOnly);
    // }
    //
    // });
    // }
    //
    // });
    // selectGroup.show();
  }

  /**
   * Get buttons witch allow edition control
   * 
   * @return the WUI Buttons
   */
  public List<WUIButton> getActionButtons() {
    return actionButtons;
  }

  private void initializePermissionsList() {
    loading.show();
    // EditorService.Util.getInstance().getObjectPermissions(aip.getId(),
    // new AsyncCallback<Map<RODAMember, ObjectPermissions>>() {
    //
    // public void onFailure(Throwable caught) {
    // loading.hide();
    // logger.error("Error initializing permissions list", caught);
    // }
    //
    // public void onSuccess(Map<RODAMember, ObjectPermissions> permissions) {
    // EditObjectPermissionsPanel.this.permissions = permissions;
    // updateMemberList();
    // loading.hide();
    // }
    //
    // });
  }

  /**
   * Set member permissions
   * 
   * @param member
   * @param permission
   */
  private void setMemberPermissions(RODAMember member, ObjectPermissions permission) {
    loading.show();
    // EditorService.Util.getInstance().setPermission(aip.getId(), member,
    // permission,
    // new AsyncCallback<Map<RODAMember, ObjectPermissions>>() {
    //
    // public void onFailure(Throwable caught) {
    // loading.hide();
    // logger.error("Error setting member permissions", caught);
    //
    // }
    //
    // public void onSuccess(Map<RODAMember, ObjectPermissions> permissions) {
    // EditObjectPermissionsPanel.this.permissions = permissions;
    // updateMemberList();
    // loading.hide();
    // }
    //
    // });
  }

  protected void apply() {
    loading.show();
    apply.setEnabled(false);
    boolean recursivelly = applyRecursivelly.isChecked();
    Map<RODAMember, ObjectPermissions> permissions = new LinkedHashMap<RODAMember, ObjectPermissions>();
    for (UserMiniPermissionPanel userMiniPanel : userMiniPermissionPanels) {
      permissions.put(userMiniPanel.getUser(), userMiniPanel.getPermissions());
    }
    for (GroupMiniPermissionPanel groupMiniPanel : groupMiniPermissionPanels) {
      permissions.put(groupMiniPanel.getGroup(), groupMiniPanel.getPermissions());
    }
    // EditorService.Util.getInstance().setObjectPermissions(aip.getId(),
    // permissions, recursivelly,
    // new AsyncCallback<Map<RODAMember, ObjectPermissions>>() {
    //
    // public void onFailure(Throwable caught) {
    // loading.hide();
    // logger.error("Error adding applying permissions", caught);
    //
    // }
    //
    // public void onSuccess(Map<RODAMember, ObjectPermissions> permissions) {
    // EditObjectPermissionsPanel.this.permissions = permissions;
    // updateMemberList();
    // loading.hide();
    // }
    //
    // });

  }

  private void updateMemberList() {
    memberListPanel.clear();
    userMiniPermissionPanels.clear();
    groupMiniPermissionPanels.clear();
    for (Entry<RODAMember, ObjectPermissions> entry : permissions.entrySet()) {
      RODAMember member = entry.getKey();
      ObjectPermissions metaPermissions = entry.getValue();
      if (member instanceof User) {
        User user = (User) member;
        UserMiniPermissionPanel userMiniPanel = new UserMiniPermissionPanel(user, metaPermissions);
        memberListPanel.add(userMiniPanel);
        userMiniPermissionPanels.add(userMiniPanel);
        userMiniPanel.addChangeListener(new ChangeListener() {

          public void onChange(Widget sender) {
            apply.setEnabled(true);
          }

        });
      } else if (member instanceof Group) {
        Group group = (Group) member;
        GroupMiniPermissionPanel groupMiniPanel = new GroupMiniPermissionPanel(group, metaPermissions);
        memberListPanel.add(groupMiniPanel);
        groupMiniPermissionPanels.add(groupMiniPanel);
        groupMiniPanel.addChangeListener(new ChangeListener() {

          public void onChange(Widget sender) {
            apply.setEnabled(true);
          }

        });
      }
    }
  }

}
