package org.roda.wui.client.browse.tabs;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.aip.MembersLookupRequest;
import org.roda.core.data.v2.aip.MembersLookupResponse;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.EditPermissionsDialog;
import org.roda.wui.client.common.lists.utils.ActionMenuCell;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.widgets.Toast;

import config.i18n.client.ClientMessages;

public abstract class AbstractPermissionsTab<T extends IsIndexed> extends GenericMetadataCardPanel<T> {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  protected final AsyncCallback<Actionable.ActionImpact> parentCallback;
  protected final T entity;

  protected AbstractPermissionsTab(T entity, AsyncCallback<Actionable.ActionImpact> parentCallback) {
    super();
    this.entity = entity;
    this.parentCallback = parentCallback;
  }

  // --- Abstract Methods for Entity-Specific Behavior ---
  protected abstract Permissions getPermissions(T data);

  protected abstract void setPermissions(T data, Permissions permissions);

  protected abstract String getEntityId(T data);

  protected abstract String getRemoveUserConfirmationMessage();

  protected abstract String getRemoveGroupConfirmationMessage();

  protected abstract void executePermissionUpdate(T data, Permissions permissions, String details,
    AsyncCallback<Void> callback);

  @Override
  protected void buildFields(T data) {
    metadataContainer.clear();
    Permissions permissions = getPermissions(data);

    Set<String> usernames = permissions.getUsernames();
    Set<String> groupnames = permissions.getGroupnames();

    MembersLookupRequest request = new MembersLookupRequest(usernames, groupnames);
    Services services = new Services("Get members display names", "post");

    services.membersResource(s -> s.getMembersDisplayNames(request)).whenComplete((response, throwable) -> {
      metadataContainer.clear();

      // Safety fallback: if the request fails, use an empty response so raw IDs are
      // displayed
      MembersLookupResponse safeResponse = (throwable == null && response != null) ? response
        : new MembersLookupResponse();

      addSeparator(messages.permissionAssignedGroups());
      metadataContainer.add(createGroupsTable(permissions, safeResponse));

      addSeparator(messages.permissionAssignedUsers());
      metadataContainer.add(createUsersTable(permissions, safeResponse));
    });
  }

  private ScrollPanel createGroupsTable(Permissions permissions, MembersLookupResponse response) {
    Set<String> groupNames = permissions.getGroupnames();
    if (groupNames == null || groupNames.isEmpty()) {
      return createEmptyPanel(messages.permissionAssignedGroupsEmpty());
    }

    BasicTablePanel<String> table = new BasicTablePanel<>(groupNames.iterator(),
      new BasicTablePanel.ColumnInfo<>("", 0.8, getGroupTypeColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.groupName(), 15, getGroupNameColumn(response)),
      new BasicTablePanel.ColumnInfo<>(messages.userPermissions(), 20, getGroupPermissionsColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.actions(), 5, getGroupActionsColumn()));

    table.removeSelectionModel();
    FlowPanel panel = new FlowPanel();
    panel.add(table);
    return new ScrollPanel(panel);
  }

  private ScrollPanel createUsersTable(Permissions permissions, MembersLookupResponse response) {
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_UPDATE_AIP_PERMISSIONS)) {
      return createUsersTableFullPermissions(permissions, response);
    } else {
      return createUsersTableReadOnly(permissions, response);
    }
  }

  private ScrollPanel createUsersTableFullPermissions(Permissions permissions, MembersLookupResponse response) {
    Set<String> usernames = permissions.getUsernames();
    if (usernames == null || usernames.isEmpty()) {
      return createEmptyPanel(messages.permissionAssignedUsersEmpty());
    }

    BasicTablePanel<String> table = new BasicTablePanel<>(usernames.iterator(),
      new BasicTablePanel.ColumnInfo<>("", 0.8, getUserTypeColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.username(), 15, getUserNameColumn(response)),
      new BasicTablePanel.ColumnInfo<>(messages.userPermissions(), 20, getUserPermissionsColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.actions(), 5, getUserActionsColumn()));

    table.removeSelectionModel();
    FlowPanel panel = new FlowPanel();
    panel.add(table);
    return new ScrollPanel(panel);
  }

  private ScrollPanel createUsersTableReadOnly(Permissions permissions, MembersLookupResponse response) {
    Set<String> usernames = permissions.getUsernames();
    if (usernames == null || usernames.isEmpty()) {
      return createEmptyPanel(messages.permissionAssignedUsersEmpty());
    }

    BasicTablePanel<String> table = new BasicTablePanel<>(usernames.iterator(),
      new BasicTablePanel.ColumnInfo<>("", 0.8, getUserTypeColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.username(), 15, getUserNameColumn(response)),
      new BasicTablePanel.ColumnInfo<>(messages.userPermissions(), 20, getUserPermissionsColumn()));

    table.removeSelectionModel();
    FlowPanel panel = new FlowPanel();
    panel.add(table);
    return new ScrollPanel(panel);
  }

  private ScrollPanel createEmptyPanel(String message) {
    SimplePanel panel = new SimplePanel();
    HTML html = new HTML(message);
    html.setStyleName("no-permission-assigned");
    panel.setWidget(html);
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

  private Column<String, SafeHtml> getGroupNameColumn(final MembersLookupResponse response) {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String groupName) {
        String displayName = response.getGroupDisplayName(groupName);
        return SafeHtmlUtils.fromString(displayName);
      }
    };
  }

  private Column<String, SafeHtml> getUserNameColumn(final MembersLookupResponse response) {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String username) {
        String displayName = response.getUserDisplayName(username);
        return SafeHtmlUtils.fromString(displayName);
      }
    };
  }

  private Column<String, SafeHtml> getUserPermissionsColumn() {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String username) {
        return formatPermissions(getPermissions(entity).getUserPermissions(username));
      }
    };
  }

  private Column<String, SafeHtml> getGroupPermissionsColumn() {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String groupName) {
        return formatPermissions(getPermissions(entity).getGroupPermissions(groupName));
      }
    };
  }

  private Column<String, String> getUserActionsColumn() {
    return new Column<String, String>(new ActionMenuCell<>(this::showUserActionsMenu)) {
      @Override
      public String getValue(String username) {
        return username;
      }
    };
  }

  private Column<String, String> getGroupActionsColumn() {
    return new Column<String, String>(new ActionMenuCell<>(this::showGroupActionsMenu)) {
      @Override
      public String getValue(String groupName) {
        return groupName;
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

  protected AsyncCallback<Actionable.ActionImpact> createMemberPermissionsCallback() {
    return new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        if (parentCallback != null)
          parentCallback.onFailure(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          setData(entity);
        } else if (parentCallback != null) {
          parentCallback.onSuccess(result);
        }
      }
    };
  }

  private void showUserActionsMenu(String username, int left, int top) {
    showActionsMenu(username, true, left, top);
  }

  private void showGroupActionsMenu(String groupName, int left, int top) {
    showActionsMenu(groupName, false, left, top);
  }

  private void showActionsMenu(String targetName, boolean isUser, int left, int top) {
    PopupPanel popup = new PopupPanel(true);
    FlowPanel menuPanel = new FlowPanel();
    menuPanel.addStyleName("groupedActionableDropdown");

    Button editBtn = new Button(messages.editButton());
    editBtn.addStyleName("actionable-button actionable-button-updated actionable-button-label btn-edit");
    editBtn.addClickHandler(e -> {
      popup.hide();
      EditPermissionsDialog.show(entity, targetName, isUser, createMemberPermissionsCallback());
    });

    Button removeBtn = new Button(messages.removeButton());
    removeBtn.addStyleName("actionable-button actionable-button-destroyed actionable-button-label btn-ban");
    removeBtn.addClickHandler(e -> {
      popup.hide();
      if (isUser)
        removeUserPermission(targetName);
      else
        removeGroupPermission(targetName);
    });

    menuPanel.add(editBtn);
    menuPanel.add(removeBtn);
    popup.setWidget(menuPanel);
    popup.setPopupPosition(left, top);
    popup.show();
  }

  private void removeUserPermission(String username) {
    Dialogs.showConfirmDialog(messages.removePermissionConfirmDialogTitle(username), getRemoveUserConfirmationMessage(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Permissions permissions = new Permissions(getPermissions(entity));
            permissions.setUserPermissions(username, new HashSet<>());
            updatePermissions(permissions);
          }
        }
      });
  }

  private void removeGroupPermission(String groupName) {
    Dialogs.showConfirmDialog(messages.removePermissionConfirmDialogTitle(groupName),
      getRemoveGroupConfirmationMessage(), messages.cancelButton(), messages.confirmButton(),
      new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Permissions permissions = new Permissions(getPermissions(entity));
            permissions.setGroupPermissions(groupName, new HashSet<>());
            updatePermissions(permissions);
          }
        }
      });
  }

  private void updatePermissions(Permissions permissions) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      com.google.gwt.regexp.shared.RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
      new NoAsyncCallback<String>() {
        @Override
        public void onSuccess(String details) {
          executePermissionUpdate(entity, permissions, details, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
              if (parentCallback != null)
                parentCallback.onFailure(throwable);
            }

            @Override
            public void onSuccess(Void v) {
              Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
              setPermissions(entity, permissions);
              setData(entity);
              if (parentCallback != null) {
                parentCallback.onSuccess(Actionable.ActionImpact.UPDATED);
              }
            }
          });
        }
      });
  }
}