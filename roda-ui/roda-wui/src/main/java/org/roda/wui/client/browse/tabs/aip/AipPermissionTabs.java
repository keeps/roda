package org.roda.wui.client.browse.tabs.aip;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.browse.tabs.AbstractPermissionsTab;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.services.Services;

public class AipPermissionTabs extends AbstractPermissionsTab<IndexedAIP> {
  private final boolean hasDips;

  public AipPermissionTabs(IndexedAIP aip, boolean hasDips, AsyncCallback<Actionable.ActionImpact> parentCallback) {
    super(aip, parentCallback);
    this.hasDips = hasDips;
    setData(aip);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedAIP data) {
    if (data == null)
      return null;

    AipToolbarActions aipToolbarActions = AipToolbarActions.get(data.getId(), data.getState(), data.getPermissions());
    List<Actionable.Action<IndexedAIP>> actions = new ArrayList<>();
    actions.add(AipToolbarActions.AIPAction.ADD_USER_PERMISSION);
    actions.add(AipToolbarActions.AIPAction.ADD_GROUP_PERMISSION);
    actions.add(AipToolbarActions.AIPAction.APPLY_PERMISSIONS_TO_HIERARCHY);

    if (hasDips) {
      actions.add(AipToolbarActions.AIPAction.APPLY_PERMISSIONS_TO_DIPS);
    }

    return new ActionableWidgetBuilder<>(aipToolbarActions).withActionCallback(createMemberPermissionsCallback())
      .buildGroupedListWithObjects(new ActionableObject<>(data), actions, actions);
  }

  @Override
  protected Permissions getPermissions(IndexedAIP data) {
    return data.getPermissions();
  }

  @Override
  protected void setPermissions(IndexedAIP data, Permissions permissions) {
    data.setPermissions(permissions);
  }

  @Override
  protected String getEntityId(IndexedAIP data) {
    return data.getId();
  }

  @Override
  protected String getRemoveUserConfirmationMessage() {
    return messages.removeUserPermissionConfirmationMessage();
  }

  @Override
  protected String getRemoveGroupConfirmationMessage() {
    return messages.removeGroupPermissionConfirmationMessage();
  }

  @Override
  protected void executePermissionUpdate(IndexedAIP data, Permissions permissions, String details,
    AsyncCallback<Void> callback) {
    Services services = new Services("Update AIP permissions", "update");
    UpdatePermissionsRequest request = new UpdatePermissionsRequest();
    request.setPermissions(permissions);
    request.setDetails(details);
    request.setRecursive(false);
    request.setSelectedItems(SelectedItemsList.create(IndexedAIP.class.getName(), data.getId()));

    services.aipResource(s -> s.updatePermissions(request)).whenComplete((job, throwable) -> {
      if (throwable != null)
        callback.onFailure(throwable);
      else
        callback.onSuccess(null);
    });
  }
}