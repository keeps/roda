package org.roda.wui.client.browse.tabs.aip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.browse.EditPermissionsTab;
import org.roda.wui.client.common.actions.AipToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.panels.PermissionPanel;

import java.util.List;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class AipPermissionTabs extends GenericMetadataCardPanel<IndexedAIP> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public AipPermissionTabs(IndexedAIP aip) {
    setData(aip);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedAIP data) {
    if (data == null) {
      return null;
    }

    AipToolbarActions aipToolbarActions = AipToolbarActions.get(data.getId(), data.getState(), data.getPermissions());
    return new ActionableWidgetBuilder<>(aipToolbarActions).buildGroupedListWithObjects(new ActionableObject<>(data),
      List.of(AipToolbarActions.AIPAction.UPDATE_PERMISSIONS), List.of(AipToolbarActions.AIPAction.UPDATE_PERMISSIONS));
  }

  @Override
  protected void buildFields(IndexedAIP data) {

    Permissions permissions = data.getPermissions();
    Set<String> groupNames = permissions.getGroupnames();
    Set<String> usernames = permissions.getUsernames();

    addSeparator(messages.permissionAssignedGroups());
    if (groupNames != null && groupNames.isEmpty()) {
      SimplePanel panel = new SimplePanel();
      HTML html = new HTML(messages.permissionAssignedGroupsEmpty());
      html.setStyleName("basicTableEmpty");
      panel.setWidget(html);
      metadataContainer.add(panel);
    } else {
      FlowPanel panel = new FlowPanel();
      panel.addStyleName("permissionsTab permission-box-padding");
      for (String groupName : groupNames) {
        PermissionPanel permissionPanel = new PermissionPanel(groupName, false,
          permissions.getGroupPermissions(groupName));
        panel.add(permissionPanel);
      }
      metadataContainer.add(panel);
    }

    addSeparator(messages.permissionAssignedUsers());
    if (usernames != null && usernames.isEmpty()) {
      SimplePanel panel = new SimplePanel();
      HTML html = new HTML(messages.permissionAssignedUsersEmpty());
      html.setStyleName("basicTableEmpty");
      panel.setWidget(html);
      metadataContainer.add(panel);
    } else {
      FlowPanel panel = new FlowPanel();
      panel.addStyleName("permissionsTab permission-box-padding");
      for (String userName : usernames) {
        PermissionPanel permissionPanel = new PermissionPanel(userName, true, permissions.getUserPermissions(userName));
        panel.add(permissionPanel);
      }
      metadataContainer.add(panel);
    }
  }
}
