/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.HasPermissions;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.labels.Header;
import org.roda.wui.client.common.labels.Tag;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class EditPermissionsTab extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  FlowPanel header;
  @UiField
  FlowPanel editPermissionsDescription;
  @UiField
  Header userPermissionsTitle;
  @UiField
  Label userPermissionsEmpty;
  @UiField
  Header groupPermissionsTitle;
  @UiField
  Label groupPermissionsEmpty;
  @UiField
  FlowPanel userPermissionsPanel;
  @UiField
  FlowPanel groupPermissionsPanel;
  @UiField
  ActionsToolbar actionsToolbar;

  private List<HasPermissions> objects = new ArrayList<>();
  private String objectId = null;
  private SelectedItems<?> selectedItems;

  public EditPermissionsTab(SelectedItems<?> items) {
    this.selectedItems = items;
    initWidget(uiBinder.createAndBindUi(this));
    initLabels();
    actionsToolbar.setVisible(false);
    editPermissionsDescription.add(new HTMLWidgetWrapper("EditMultiplePermissionsDescription.html"));
  }

  public EditPermissionsTab(FlowPanel toolbarActionableMenu, String objectClass, HasPermissions object) {
    this.objects.add(object);
    this.objectId = object.getId();
    this.selectedItems = SelectedItemsList.create(objectClass, objectId);
    initWidget(uiBinder.createAndBindUi(this));
    initLabels();
    actionsToolbar.setActionableMenu(toolbarActionableMenu, true);
    header.setVisible(actionsToolbar.isVisible());
    actionsToolbar.setTagsVisible(false);
    actionsToolbar.setLabelVisible(false);
    createPermissionPanel();
  }

  public EditPermissionsTab(String objectClass, SelectedItems<? extends HasPermissions> selectedItems,
    List<? extends HasPermissions> list) {
    this.objects.addAll(list);
    this.selectedItems = selectedItems;
    initWidget(uiBinder.createAndBindUi(this));
    editPermissionsDescription.add(new HTMLWidgetWrapper("EditPermissionsDescription.html"));
    initLabels();
    actionsToolbar.setVisible(false);
    createPermissionPanelList();
  }

  private void initLabels() {
    userPermissionsTitle.setHeaderText(messages.permissionAssignedGroups());
    userPermissionsTitle.setLevel(4);
    groupPermissionsTitle.setHeaderText(messages.permissionAssignedUsers());
    groupPermissionsTitle.setLevel(4);
  }

  private void createPermissionPanelList() {
    Map<String, Set<PermissionType>> userPermissionsToShow = new HashMap<>();
    Map<String, Set<PermissionType>> groupPermissionsToShow = new HashMap<>();

    if (!objects.isEmpty()) {
      Permissions firstAIPPermissions = objects.get(0).getPermissions();

      for (String userName : firstAIPPermissions.getUsernames()) {
        userPermissionsToShow.put(userName, firstAIPPermissions.getUserPermissions(userName));
      }

      for (String groupName : firstAIPPermissions.getGroupnames()) {
        groupPermissionsToShow.put(groupName, firstAIPPermissions.getGroupPermissions(groupName));
      }

      for (int i = 1; i < objects.size(); i++) {
        Permissions permissions = objects.get(i).getPermissions();

        for (Iterator<Entry<String, Set<PermissionType>>> userIterator = userPermissionsToShow.entrySet()
          .iterator(); userIterator.hasNext();) {
          Entry<String, Set<PermissionType>> entry = userIterator.next();
          if (permissions.getUsernames().contains(entry.getKey())) {
            Set<PermissionType> userPermissionType = entry.getValue();

            for (Iterator<PermissionType> permissionTypeIterator = userPermissionType.iterator(); permissionTypeIterator
              .hasNext();) {
              PermissionType permissionType = permissionTypeIterator.next();

              if (!permissions.getUserPermissions(entry.getKey()).contains(permissionType)) {
                permissionTypeIterator.remove();
              }
            }
          } else {
            userIterator.remove();
          }
        }

        for (Iterator<Entry<String, Set<PermissionType>>> groupIterator = groupPermissionsToShow.entrySet()
          .iterator(); groupIterator.hasNext();) {
          Entry<String, Set<PermissionType>> entry = groupIterator.next();
          if (permissions.getGroupnames().contains(entry.getKey())) {
            Set<PermissionType> groupPermissionType = entry.getValue();

            for (Iterator<PermissionType> permissionTypeIterator = groupPermissionType
              .iterator(); permissionTypeIterator.hasNext();) {
              PermissionType permissionType = permissionTypeIterator.next();

              if (!permissions.getGroupPermissions(entry.getKey()).contains(permissionType)) {
                permissionTypeIterator.remove();
              }
            }
          } else {
            groupIterator.remove();
          }
        }

      }
    }

    userPermissionsEmpty.setVisible(userPermissionsToShow.isEmpty());
    groupPermissionsEmpty.setVisible(groupPermissionsToShow.isEmpty());

    for (Entry<String, Set<PermissionType>> entry : userPermissionsToShow.entrySet()) {
      PermissionPanel permissionPanel = new PermissionPanel(entry.getKey(), true, entry.getValue());
      userPermissionsPanel.add(permissionPanel);
      bindUpdateEmptyVisibility(permissionPanel);
    }

    for (Entry<String, Set<PermissionType>> entry : groupPermissionsToShow.entrySet()) {
      PermissionPanel permissionPanel = new PermissionPanel(entry.getKey(), false, entry.getValue());
      groupPermissionsPanel.add(permissionPanel);
      bindUpdateEmptyVisibility(permissionPanel);
    }
  }

  private void createPermissionPanel() {
    Permissions permissions = objects.get(0).getPermissions();

    userPermissionsEmpty.setVisible(permissions.getUsernames().isEmpty());
    groupPermissionsEmpty.setVisible(permissions.getGroupnames().isEmpty());

    for (String username : permissions.getUsernames()) {
      PermissionPanel permissionPanel = new PermissionPanel(username, true, permissions.getUserPermissions(username));
      bindUpdateEmptyVisibility(permissionPanel);
      userPermissionsPanel.add(permissionPanel);
    }

    for (String groupname : permissions.getGroupnames()) {
      PermissionPanel permissionPanel = new PermissionPanel(groupname, false,
        permissions.getGroupPermissions(groupname));
      bindUpdateEmptyVisibility(permissionPanel);
      groupPermissionsPanel.add(permissionPanel);
    }
  }

  public void addPermissionPanel(RODAMember member) {
    PermissionPanel permissionPanel = new PermissionPanel(member);
    if (member.isUser()) {
      bindUpdateEmptyVisibility(permissionPanel);
      userPermissionsPanel.insert(permissionPanel, 0);
    } else {
      bindUpdateEmptyVisibility(permissionPanel);
      groupPermissionsPanel.insert(permissionPanel, 0);
    }
  }

  private void bindUpdateEmptyVisibility(PermissionPanel permissionPanel) {
    permissionPanel.addAttachHandler(new Handler() {

      @Override
      public void onAttachOrDetach(AttachEvent event) {
        // running later to let attach/detach take effect
        Scheduler.get().scheduleDeferred(new Command() {
          @Override
          public void execute() {
            userPermissionsEmpty.setVisible(userPermissionsPanel.getWidgetCount() == 0);
            groupPermissionsEmpty.setVisible(groupPermissionsPanel.getWidgetCount() == 0);
          }
        });

      }
    });
  }

  public List<String> getAssignedUserNames() {
    List<String> ret = new ArrayList<>();
    for (int i = 0; i < userPermissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) userPermissionsPanel.getWidget(i);

      if (permissionPanel.isUser()) {
        ret.add(permissionPanel.getName());
      }
    }

    return ret;
  }

  public List<String> getAssignedGroupNames() {
    List<String> ret = new ArrayList<>();
    for (int i = 0; i < groupPermissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) groupPermissionsPanel.getWidget(i);

      if (!permissionPanel.isUser()) {
        ret.add(permissionPanel.getName());
      }
    }

    return ret;
  }

  public Permissions getPermissions() {
    Permissions permissions = new Permissions();

    for (int i = 0; i < userPermissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) userPermissionsPanel.getWidget(i);

      if (permissionPanel.isUser()) {
        permissions.setUserPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      } else {
        permissions.setGroupPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      }
    }

    for (int i = 0; i < groupPermissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) groupPermissionsPanel.getWidget(i);

      if (permissionPanel.isUser()) {
        permissions.setUserPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      } else {
        permissions.setGroupPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      }
    }

    return permissions;
  }

  interface MyUiBinder extends UiBinder<Widget, EditPermissionsTab> {
  }

  public class PermissionPanel extends Composite {
    private FlowPanel panel;
    private FlowPanel panelBody;
    private FlowPanel rightPanel;
    private HTML type;
    private Label nameLabel;
    private FlowPanel permissionTagsPanel;

    private String name;
    private boolean isUser;

    public PermissionPanel(RODAMember member) {
      this(member.getName(), member.isUser(), new HashSet<>());
    }

    public PermissionPanel(String name, boolean isUser, Set<PermissionType> permissions) {
      this.name = name;
      this.isUser = isUser;

      panel = new FlowPanel();
      panelBody = new FlowPanel();

      type = new HTML(
        SafeHtmlUtils.fromSafeConstant(isUser ? "<i class='fa fa-user'></i>" : "<i class='fa fa-users'></i>"));
      nameLabel = new Label(name);

      rightPanel = new FlowPanel();
      permissionTagsPanel = new FlowPanel();

      Map<PermissionType, Tag.TagStyle> tagStyles = new HashMap<>();
      tagStyles.put(PermissionType.GRANT, Tag.TagStyle.BORDER_BLACK);
      tagStyles.put(PermissionType.READ, Tag.TagStyle.BORDER_BLACK);
      tagStyles.put(PermissionType.DELETE, Tag.TagStyle.BORDER_DANGER);
      tagStyles.put(PermissionType.CREATE, Tag.TagStyle.BORDER_BLACK);
      tagStyles.put(PermissionType.UPDATE, Tag.TagStyle.BORDER_BLACK);
      for (PermissionType permissionType : permissions) {
        Tag permissionTag = Tag.fromText(messages.objectPermission(permissionType), tagStyles.get(permissionType));
        permissionTagsPanel.add(permissionTag);
        permissionTag.addStyleName("permission-tag");
      }

      panelBody.add(type);
      panelBody.add(nameLabel);
      panelBody.add(rightPanel);

      rightPanel.add(permissionTagsPanel);

      panel.add(panelBody);

      initWidget(panel);

      panel.addStyleName("panel permission");
      panel.addStyleName(isUser ? "permission-user" : "permission-group");
      panelBody.addStyleName("panel-body");
      type.addStyleName("permission-type");
      nameLabel.addStyleName("permission-name");
      rightPanel.addStyleName("pull-right");
      permissionTagsPanel.addStyleName("permission-tags");
    }

    public Set<PermissionType> getPermissions() {
      HashSet<PermissionType> permissions = new HashSet<>();
      for (int i = 0; i < permissionTagsPanel.getWidgetCount(); i++) {
        ValueLabel valueCheckBox = (ValueLabel) permissionTagsPanel.getWidget(i);
        permissions.add(valueCheckBox.getPermissionType());
      }
      return permissions;
    }

    public String getName() {
      return name;
    }

    public boolean isUser() {
      return isUser;
    }

    public class ValueLabel extends Label {
      private PermissionType permissionType;

      public ValueLabel(PermissionType permissionType) {
        super(messages.objectPermission(permissionType));
        this.permissionType = permissionType;
      }

      public PermissionType getPermissionType() {
        return permissionType;
      }
    }
  }
}
