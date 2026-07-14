package org.roda.wui.client.browse.tabs.aip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
  private final IndexedAIP aip;

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
      new BasicTablePanel.ColumnInfo<String>("", 0.8, getGroupTypeColumn()),
      new BasicTablePanel.ColumnInfo<String>(messages.groupName(), 15, getGroupNameColumn()),
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
      new BasicTablePanel.ColumnInfo<String>("", 0.8, getUserTypeColumn()),
      new BasicTablePanel.ColumnInfo<String>(messages.username(), 15, getUserNameColumn()),
      new BasicTablePanel.ColumnInfo<String>(messages.userPermissions(), 20, getUserPermissionsColumn()),
      new BasicTablePanel.ColumnInfo<String>(messages.actions(), 5, getUserActionsColumn()));

    table.removeSelectionModel();

    FlowPanel panel = new FlowPanel();
    panel.add(table);
    return new ScrollPanel(panel);
  }

  private Column<String, SafeHtml> getGroupTypeColumn() {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String object) {
        return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-users'></i>");
      }
    };
  }

  private Column<String, SafeHtml> getUserTypeColumn() {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String object) {
        return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>");
      }
    };
  }

  private Column<String, SafeHtml> getGroupNameColumn() {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String groupName) {
        // Generate a unique ID for the DOM element to update later
        String elementId = "user-cell-" + groupName + "-" + System.currentTimeMillis();

        // Fire the async request using RODA's Services wrapper
        Services services = new Services("Get group", "get");
        services.membersResource(s -> s.getGroup(groupName)).whenComplete((group, throwable) -> {
          if (throwable == null && group != null) {
            Element element = Document.get().getElementById(elementId);
            if (element != null) {
              // Update the cell with the group's full name (or fallback to username)
              String displayName = group.getFullName() != null && !group.getFullName().isEmpty()
                ? group.getFullName() + " (" + groupName + ")"
                : groupName;
              element.setInnerText(displayName);
            }
          }
        });

        // Render a placeholder immediately while the request happens in the background
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<span id=\"" + elementId + "\">");
        builder.appendEscaped(groupName); // Fallback text while loading
        builder.appendHtmlConstant("</span>");

        return builder.toSafeHtml();
      }
    };
  }

  private Column<String, SafeHtml> getUserNameColumn() {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String username) {
        // Generate a unique ID for the DOM element to update later
        String elementId = "user-cell-" + username + "-" + System.currentTimeMillis();

        // Fire the async request using RODA's Services wrapper
        Services services = new Services("Get user", "get");
        services.membersResource(s -> s.getUser(username)).whenComplete((user, throwable) -> {
          if (throwable == null && user != null) {
            Element element = Document.get().getElementById(elementId);
            if (element != null) {
              // Update the cell with the user's full name (or fallback to username)
              String displayName = user.getFullName() != null && !user.getFullName().isEmpty()
                ? user.getFullName() + " (" + username + ")"
                : username;
              element.setInnerText(displayName);
            }
          }
        });

        // Render a placeholder immediately while the request happens in the background
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<span id=\"" + elementId + "\">");
        builder.appendEscaped(username); // Fallback text while loading
        builder.appendHtmlConstant("</span>");

        return builder.toSafeHtml();
      }
    };
  }

  private Column<String, SafeHtml> getUserPermissionsColumn() {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String username) {
        return formatPermissions(aip.getPermissions().getUserPermissions(username));
      }
    };
  }

  private Column<String, SafeHtml> getGroupPermissionsColumn() {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String groupname) {
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

  private SafeHtml formatPermissions(Set<Permissions.PermissionType> permissions) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();

    for (Permissions.PermissionType permission : Permissions.PermissionType.values()) {
      if (permissions.contains(permission)) {
        if (Permissions.PermissionType.DELETE.equals(permission)
          || Permissions.PermissionType.GRANT.equals(permission)) {
          builder.appendHtmlConstant("<span class=\"label-danger\">");
        } else {
          builder.appendHtmlConstant("<span class=\"label-info\">");
        }
        builder.appendEscaped(messages.objectPermission(permission));
        builder.appendHtmlConstant("</span> ");
      }
    }

    return builder.toSafeHtml();
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
      messages.removeUserPermissionConfirmationMessage(username), messages.cancelButton(), messages.confirmButton(),
      new NoAsyncCallback<Boolean>() {
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
      messages.removeGroupPermissionConfirmationMessage(groupname), messages.cancelButton(), messages.confirmButton(),
      new NoAsyncCallback<Boolean>() {
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
