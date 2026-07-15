/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.browse.tabs.AbstractPermissionsTab;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import com.google.gwt.user.client.ui.FlowPanel;

import org.roda.wui.client.services.Services;

public class DipPermissionsTab extends AbstractPermissionsTab<IndexedDIP> {
  public DipPermissionsTab(IndexedDIP dip, AsyncCallback<Actionable.ActionImpact> parentCallback) {
    super(dip, parentCallback);
    setData(dip);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedDIP data) {
    if (data == null)
      return null;

    DisseminationActions dipToolbarActions = DisseminationActions.get(data.getPermissions());
    List<Actionable.Action<IndexedDIP>> actions = List.of(DisseminationActions.DisseminationAction.ADD_USER_PERMISSION,
      DisseminationActions.DisseminationAction.ADD_GROUP_PERMISSION);

    return new ActionableWidgetBuilder<>(dipToolbarActions).withActionCallback(createMemberPermissionsCallback())
      .buildGroupedListWithObjects(new ActionableObject<>(data), actions, actions);
  }

  @Override
  protected Permissions getPermissions(IndexedDIP data) {
    return data.getPermissions();
  }

  @Override
  protected void setPermissions(IndexedDIP data, Permissions permissions) {
    data.setPermissions(permissions);
  }

  @Override
  protected String getEntityId(IndexedDIP data) {
    return data.getId();
  }

  @Override
  protected String getRemoveUserConfirmationMessage() {
    return messages.removeUserDIPPermissionConfirmationMessage();
  }

  @Override
  protected String getRemoveGroupConfirmationMessage() {
    return messages.removeGroupDIPPermissionConfirmationMessage();
  }

  @Override
  protected void executePermissionUpdate(IndexedDIP data, Permissions permissions, String details,
    AsyncCallback<Void> callback) {
    Services services = new Services("Update DIP permissions", "update");
    UpdatePermissionsRequest request = new UpdatePermissionsRequest();
    request.setPermissions(permissions);
    request.setDetails(details);
    request.setRecursive(false);
    request.setSelectedItems(SelectedItemsList.create(IndexedDIP.class.getName(), data.getId()));

    services.dipResource(s -> s.updatePermissions(request)).whenComplete((job, throwable) -> {
      if (throwable != null)
        callback.onFailure(throwable);
      else
        callback.onSuccess(null);
    });
  }
}
