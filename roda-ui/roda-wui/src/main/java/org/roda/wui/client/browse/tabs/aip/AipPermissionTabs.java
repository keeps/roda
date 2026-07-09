package org.roda.wui.client.browse.tabs.aip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.EditMemberPermissionsDialog;
import org.roda.wui.client.common.lists.utils.ActionMenuCell;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class AipPermissionTabs extends GenericMetadataCardPanel<IndexedAIP> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final AsyncCallback<Actionable.ActionImpact> parentCallback;
  private final boolean hasDips;
  private IndexedAIP aip;

  public AipPermissionTabs(IndexedAIP aip, boolean hasDips, AsyncCallback<Actionable.ActionImpact> parentCallback) {
    super();
    this.aip = aip;
    this.hasDips = hasDips;
    this.parentCallback = parentCallback;
    setData(aip);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedAIP data) {
    if (data == null) {
      return null;
    }

    AsyncCallback<Actionable.ActionImpact> localCallback = new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        if (parentCallback != null)
          parentCallback.onFailure(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          setData(aip);
        } else if (parentCallback != null) {
          parentCallback.onSuccess(result);
        }
      }
    };

    AipToolbarActions aipToolbarActions = AipToolbarActions.get(data.getId(), data.getState(), data.getPermissions());
    List<Actionable.Action<IndexedAIP>> actions = new ArrayList<>();
    actions.add(AipToolbarActions.AIPAction.ADD_USER_PERMISSION);
    actions.add(AipToolbarActions.AIPAction.ADD_GROUP_PERMISSION);
    actions.add(AipToolbarActions.AIPAction.APPLY_PERMISSIONS_TO_HIERARCHY);

    if (hasDips) {
      actions.add(AipToolbarActions.AIPAction.APPLY_PERMISSIONS_TO_DIPS);
    }

    return new ActionableWidgetBuilder<>(aipToolbarActions).withActionCallback(localCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(data), actions, actions);
  }

  @Override
  protected void buildFields(IndexedAIP data) {
    metadataContainer.clear();

    Permissions permissions = data.getPermissions();

    addSeparator(messages.permissionAssignedGroups());
    metadataContainer.add(createGroupsTable(permissions));

    addSeparator(messages.permissionAssignedUsers());
    metadataContainer.add(createUsersTable(permissions));
  }

  private ScrollPanel createGroupsTable(Permissions permissions) {
    Set<String> groupNames = permissions.getGroupnames();

    if (groupNames == null || groupNames.isEmpty()) {
      SimplePanel panel = new SimplePanel();
      HTML html = new HTML(messages.permissionAssignedGroupsEmpty());
      html.setStyleName("no-permission-assigned");
      panel.setWidget(html);
      return new ScrollPanel(panel);
    }

    BasicTablePanel<String> table = new BasicTablePanel<>(groupNames.iterator(),
      new BasicTablePanel.ColumnInfo<String>(messages.groupName(), 15, getNameColumn()),
      new BasicTablePanel.ColumnInfo<String>(messages.userPermissions(), 20, getGroupPermissionsColumn()),
      new BasicTablePanel.ColumnInfo<String>(messages.actions(), 5, getGroupActionsColumn()));

    table.removeSelectionModel();

    FlowPanel panel = new FlowPanel();
    panel.add(table);
    return new ScrollPanel(panel);
  }

  private ScrollPanel createUsersTable(Permissions permissions) {
    Set<String> usernames = permissions.getUsernames();

    if (usernames == null || usernames.isEmpty()) {
      SimplePanel panel = new SimplePanel();
      HTML html = new HTML(messages.permissionAssignedUsersEmpty());
      html.setStyleName("no-permission-assigned");
      panel.setWidget(html);
      return new ScrollPanel(panel);
    }

    BasicTablePanel<String> table = new BasicTablePanel<>(usernames.iterator(),
      new BasicTablePanel.ColumnInfo<String>(messages.username(), 15, getNameColumn()),
      new BasicTablePanel.ColumnInfo<String>(messages.userPermissions(), 20, getUserPermissionsColumn()),
      new BasicTablePanel.ColumnInfo<String>(messages.actions(), 5, getUserActionsColumn()));

    table.removeSelectionModel();

    FlowPanel panel = new FlowPanel();
    panel.add(table);
    return new ScrollPanel(panel);
  }

  private TextColumn<String> getNameColumn() {
    return new TextColumn<String>() {
      @Override
      public String getValue(String name) {
        return name;
      }
    };
  }

  private TextColumn<String> getUserPermissionsColumn() {
    return new TextColumn<String>() {
      @Override
      public String getValue(String username) {
        return formatPermissions(aip.getPermissions().getUserPermissions(username));
      }
    };
  }

  private TextColumn<String> getGroupPermissionsColumn() {
    return new TextColumn<String>() {
      @Override
      public String getValue(String groupname) {
        return formatPermissions(aip.getPermissions().getGroupPermissions(groupname));
      }
    };
  }

  private Column<String, String> getUserActionsColumn() {
    ActionMenuCell<String> actionCell = new ActionMenuCell<>(this::showUserActionsMenu);

    return new Column<String, String>(actionCell) {
      @Override
      public String getValue(String username) {
        return username;
      }
    };
  }

  private Column<String, String> getGroupActionsColumn() {
    ActionMenuCell<String> actionCell = new ActionMenuCell<>(this::showGroupActionsMenu);

    return new Column<String, String>(actionCell) {
      @Override
      public String getValue(String groupname) {
        return groupname;
      }
    };
  }

  private String formatPermissions(Set<Permissions.PermissionType> permissions) {
    List<String> labels = new ArrayList<>();

    for (Permissions.PermissionType permission : Permissions.PermissionType.values()) {
      if (permissions.contains(permission)) {
        labels.add(messages.objectPermission(permission));
      }
    }

    return String.join(", ", labels);
  }

  private AsyncCallback<Actionable.ActionImpact> createMemberPermissionsCallback() {
    return new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        if (parentCallback != null) {
          parentCallback.onFailure(caught);
        }
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          setData(aip);
        } else if (parentCallback != null) {
          parentCallback.onSuccess(result);
        }
      }
    };
  }

  private void showUserActionsMenu(String username, int left, int top) {
    PopupPanel popup = new PopupPanel(true);
    FlowPanel menuPanel = new FlowPanel();
    menuPanel.addStyleName("groupedActionableDropdown");

    Button editBtn = new Button(messages.editButton());
    editBtn.addStyleName("actionable-button actionable-button-updated actionable-button-label btn-edit");
    editBtn.addClickHandler(e -> {
      popup.hide();
      EditMemberPermissionsDialog.show(aip, username, true, createMemberPermissionsCallback());
    });

    Button removeBtn = new Button(messages.removeButton());
    removeBtn.addStyleName("actionable-button actionable-button-destroyed actionable-button-label btn-ban");
    removeBtn.addClickHandler(e -> {
      popup.hide();
      removeUserPermission(aip, username);
    });

    menuPanel.add(editBtn);
    menuPanel.add(removeBtn);

    popup.setWidget(menuPanel);
    popup.setPopupPosition(left, top);
    popup.show();
  }

  private void showGroupActionsMenu(String groupname, int left, int top) {
    PopupPanel popup = new PopupPanel(true);
    FlowPanel menuPanel = new FlowPanel();
    menuPanel.addStyleName("groupedActionableDropdown");

    Button editBtn = new Button(messages.editButton());
    editBtn.addStyleName("actionable-button actionable-button-updated actionable-button-label btn-edit");
    editBtn.addClickHandler(e -> {
      popup.hide();
      EditMemberPermissionsDialog.show(aip, groupname, false, createMemberPermissionsCallback());
    });

    Button removeBtn = new Button(messages.removeButton());
    removeBtn.addStyleName("actionable-button actionable-button-destroyed actionable-button-label btn-ban");
    removeBtn.addClickHandler(e -> {
      popup.hide();
      removeGroupPermission(aip, groupname);
    });

    menuPanel.add(editBtn);
    menuPanel.add(removeBtn);

    popup.setWidget(menuPanel);
    popup.setPopupPosition(left, top);
    popup.show();
  }

  private void removeUserPermission(IndexedAIP aip, String username) {
    Dialogs.showConfirmDialog(messages.removePermissionConfirmDialogTitle(),
      messages.removeUserPermissionConfirmationMessage(username),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Permissions permissions = new Permissions(aip.getPermissions());
            permissions.setUserPermissions(username, new HashSet<>());
            updateAipPermissions(aip, permissions);
          }
        }
      });
  }

  private void removeGroupPermission(IndexedAIP aip, String groupname) {
    Dialogs.showConfirmDialog(messages.removePermissionConfirmDialogTitle(),
      messages.removeGroupPermissionConfirmationMessage(groupname),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Permissions permissions = new Permissions(aip.getPermissions());
            permissions.setGroupPermissions(groupname, new HashSet<>());
            updateAipPermissions(aip, permissions);
          }
        }
      });
  }

  private void updateAipPermissions(IndexedAIP aip, Permissions permissions) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      com.google.gwt.regexp.shared.RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
      new NoAsyncCallback<String>() {
        @Override
        public void onSuccess(String details) {
          Services services = new Services("Update AIP permissions", "update");

          UpdatePermissionsRequest request = new UpdatePermissionsRequest();
          request.setPermissions(permissions);
          request.setDetails(details);
          request.setRecursive(false);
          request.setSelectedItems(SelectedItemsList.create(IndexedAIP.class.getName(), aip.getId()));

          services.aipResource(s -> s.updatePermissions(request)).whenComplete((job, throwable) -> {
            if (throwable != null) {
              if (parentCallback != null) {
                parentCallback.onFailure(throwable);
              }
            } else {
              Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
              aip.setPermissions(permissions);
              setData(aip);
              if (parentCallback != null) {
                parentCallback.onSuccess(Actionable.ActionImpact.UPDATED);
              }
            }
          });
        }
      });
  }

}
