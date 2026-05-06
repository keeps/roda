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
import org.roda.wui.client.common.panels.PermissionPanel;
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

  public EditPermissionsTab(FlowPanel toolbarActionableMenu, String objectClass, HasPermissions object) {
    this.objects.add(object);
    initWidget(uiBinder.createAndBindUi(this));
    initLabels();
    actionsToolbar.setActionableMenu(toolbarActionableMenu, true);
    header.setVisible(actionsToolbar.isVisible());
    actionsToolbar.setTagsVisible(false);
    actionsToolbar.setLabelVisible(false);
    createPermissionPanel();
  }

  private void initLabels() {
    userPermissionsTitle.setHeaderText(messages.permissionAssignedGroups());
    userPermissionsTitle.setLevel(4);
    groupPermissionsTitle.setHeaderText(messages.permissionAssignedUsers());
    groupPermissionsTitle.setLevel(4);
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

}
