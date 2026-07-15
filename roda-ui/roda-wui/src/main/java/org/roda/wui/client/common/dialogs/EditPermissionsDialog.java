/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public final class EditPermissionsDialog {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private EditPermissionsDialog() {
    // utility class
  }

  public static <T extends IsIndexed> DialogBox show(T entity, String memberName, boolean user,
    AsyncCallback<Actionable.ActionImpact> callback) {
    if (entity == null) {
      return null;
    }

    if (entity instanceof IndexedDIP) {
      return showDIP((IndexedDIP) entity, memberName, user, callback);
    }

    if (entity instanceof IndexedAIP) {
      return showAIP((IndexedAIP) entity, memberName, user, callback);
    }

    return null;
  }

  private static DialogBox showDIP(IndexedDIP dip, String memberName, boolean user,
    AsyncCallback<Actionable.ActionImpact> callback) {
    Set<PermissionType> currentPermissions = user ? dip.getPermissions().getUserPermissions(memberName)
      : dip.getPermissions().getGroupPermissions(memberName);
    PermissionPanel permissionPanel = new PermissionPanel(currentPermissions);

    DialogBox dialogBox = new DialogBox(false, true);
    FlowPanel layout = new FlowPanel();
    FlowPanel content = new FlowPanel();
    FlowPanel footer = new FlowPanel();
    Button cancelButton = new Button(messages.cancelButton());
    Button saveButton = new Button(messages.saveButton());

    content.add(permissionPanel);
    footer.add(cancelButton);
    footer.add(saveButton);
    layout.add(content);
    layout.add(footer);

    dialogBox.setText(messages.editMemberPermissionsDialogTitle(memberName));
    dialogBox.setWidget(layout);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);
    dialogBox.addStyleName("wui-dialog-prompt");
    dialogBox.addStyleName("edit-member-permissions-dialog");
    layout.addStyleName("wui-dialog-layout");
    content.addStyleName("wui-dialog-layout-content");
    footer.addStyleName("wui-dialog-layout-footer");
    cancelButton.addStyleName("btn btn-link");
    saveButton.addStyleName("pull-right btn btn-play");

    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onSuccess(Actionable.ActionImpact.NONE);
    });

    saveButton
      .addClickHandler(event -> savePermissions(dip, memberName, user, permissionPanel, dialogBox, false, callback));

    dialogBox.center();
    dialogBox.show();

    return dialogBox;
  }

  private static DialogBox showAIP(IndexedAIP aip, String memberName, boolean user,
    AsyncCallback<Actionable.ActionImpact> callback) {
    Set<PermissionType> currentPermissions = user ? aip.getPermissions().getUserPermissions(memberName)
      : aip.getPermissions().getGroupPermissions(memberName);
    PermissionPanel permissionPanel = new PermissionPanel(currentPermissions);

    DialogBox dialogBox = new DialogBox(false, true);
    FlowPanel layout = new FlowPanel();
    FlowPanel content = new FlowPanel();
    FlowPanel footer = new FlowPanel();
    Button cancelButton = new Button(messages.cancelButton());
    Button saveButton = new Button(messages.saveButton());

    content.add(permissionPanel);
    footer.add(cancelButton);
    footer.add(saveButton);
    layout.add(content);
    layout.add(footer);

    dialogBox.setText(messages.editMemberPermissionsDialogTitle(memberName));
    dialogBox.setWidget(layout);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);
    dialogBox.addStyleName("wui-dialog-prompt");
    dialogBox.addStyleName("edit-member-permissions-dialog");
    layout.addStyleName("wui-dialog-layout");
    content.addStyleName("wui-dialog-layout-content");
    footer.addStyleName("wui-dialog-layout-footer");
    cancelButton.addStyleName("btn btn-link");
    saveButton.addStyleName("pull-right btn btn-play");

    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onSuccess(Actionable.ActionImpact.NONE);
    });

    saveButton.addClickHandler(
      event -> askApplyToHierarchyAndSave(aip, memberName, user, permissionPanel, dialogBox, callback));

    dialogBox.center();
    dialogBox.show();

    return dialogBox;
  }

  private static void askApplyToHierarchyAndSave(IndexedAIP aip, String memberName, boolean user,
    PermissionPanel permissionPanel, DialogBox dialogBox, AsyncCallback<Actionable.ActionImpact> callback) {
    dialogBox.hide();

    Dialogs.showConfirmDialog(messages.applyAllButton(), messages.applyPermissionsToHierarchyConfirmationMessage(),
      messages.dialogNo(), messages.dialogYes(), new AsyncCallback<Boolean>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Boolean recursive) {
          savePermissions(aip, memberName, user, permissionPanel, Boolean.TRUE.equals(recursive), callback);
        }
      });
  }

  private static void savePermissions(IndexedDIP dip, String memberName, boolean user, PermissionPanel permissionPanel,
    DialogBox dialogBox, boolean recursive, AsyncCallback<Actionable.ActionImpact> callback) {
    dialogBox.hide();
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
      new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(String details) {
          Permissions permissions = new Permissions(dip.getPermissions());

          if (user) {
            permissions.setUserPermissions(memberName, permissionPanel.getPermissions());
          } else {
            permissions.setGroupPermissions(memberName, permissionPanel.getPermissions());
          }

          UpdatePermissionsRequest request = new UpdatePermissionsRequest();
          request.setPermissions(permissions);
          request.setDetails(details);
          request.setRecursive(recursive);
          request.setSelectedItems(SelectedItemsList.create(IndexedDIP.class.getName(), dip.getId()));

          Services services = new Services("Update DIP permissions", "update");
          services.dipResource(s -> s.updatePermissions(request)).whenComplete((job, throwable) -> {
            if (throwable != null) {
              callback.onFailure(throwable);
            } else {
              dip.setPermissions(permissions);
              Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
              callback.onSuccess(Actionable.ActionImpact.UPDATED);
            }
          });
        }
      });
  }

  private static void savePermissions(IndexedAIP aip, String memberName, boolean user, PermissionPanel permissionPanel,
    boolean recursive, AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
      new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(String details) {
          Permissions permissions = new Permissions(aip.getPermissions());

          if (user) {
            permissions.setUserPermissions(memberName, permissionPanel.getPermissions());
          } else {
            permissions.setGroupPermissions(memberName, permissionPanel.getPermissions());
          }

          UpdatePermissionsRequest request = new UpdatePermissionsRequest();
          request.setPermissions(permissions);
          request.setDetails(details);
          request.setRecursive(recursive);
          request.setSelectedItems(SelectedItemsList.create(IndexedAIP.class.getName(), aip.getId()));

          Services services = new Services("Update AIP permissions", "update");
          services.aipResource(s -> s.updatePermissions(request)).whenComplete((job, throwable) -> {
            if (throwable != null) {
              callback.onFailure(throwable);
            } else {
              aip.setPermissions(permissions);
              Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
              callback.onSuccess(Actionable.ActionImpact.UPDATED);
            }
          });
        }
      });
  }

  private static class PermissionPanel extends Composite {
    private final FlowPanel permissionsPanel;
    private final List<PermissionCheckBox> permissionCheckBoxes;

    public PermissionPanel(Set<PermissionType> permissions) {
      permissionsPanel = new FlowPanel();
      permissionCheckBoxes = new ArrayList<>();

      addSeparator(messages.crudActionsPermissionSection());

      for (PermissionType permissionType : Permissions.PermissionType.values()) {
        if (PermissionType.GRANT.equals(permissionType)) {
          addSeparator(messages.administrativeActionsPermissionSection());
        }

        addPermission(permissionType, permissions);
      }

      permissionsPanel.addStyleName("generic-metadata-panel");
      permissionsPanel.addStyleName("checkbox-group");
      initWidget(permissionsPanel);
    }

    private void addSeparator(String text) {
      Label separator = new Label(text);
      separator.addStyleName("form-separator");
      permissionsPanel.add(separator);
    }

    private void addPermission(PermissionType permissionType, Set<PermissionType> permissions) {
      PermissionCheckBox checkBox = new PermissionCheckBox(permissionType);
      boolean selected = permissions != null && permissions.contains(permissionType);
      checkBox.setValue(selected);
      checkBox.addStyleName("my-custom-checkbox");
      checkBox.addStyleName("permission-edit-checkbox");

      permissionCheckBoxes.add(checkBox);
      permissionsPanel.add(checkBox);
    }

    public Set<PermissionType> getPermissions() {
      Set<PermissionType> permissions = new HashSet<>();

      for (PermissionCheckBox checkBox : permissionCheckBoxes) {
        if (Boolean.TRUE.equals(checkBox.getValue())) {
          permissions.add(checkBox.getPermissionType());
        }
      }

      return permissions;
    }
  }
}
