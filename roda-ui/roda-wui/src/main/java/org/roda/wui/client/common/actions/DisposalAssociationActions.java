package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class DisposalAssociationActions extends AbstractActionable<IndexedAIP> {
  private static final DisposalAssociationActions INSTANCE = new DisposalAssociationActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public enum DisposalAssociationAction implements Action<IndexedAIP> {
    EDIT();

    private List<String> methods;

    DisposalAssociationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  public DisposalAssociationActions() {
  }

  public static DisposalAssociationActions get() {
    return INSTANCE;
  }

  @Override
  public Action<IndexedAIP>[] getActions() {
    return DisposalAssociationAction.values();
  }

  @Override
  public Action<IndexedAIP> actionForName(String name) {
    return DisposalAssociationAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, ActionableObject<IndexedAIP> object) {
    return hasPermissions(action);
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (DisposalAssociationAction.EDIT.equals(action)) {
      edit(aip, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void edit(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    GWT.log("EDIT action");
  }

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> disposalAssociationActionableBundle = new ActionableBundle<>();
    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.editButton(), DisposalAssociationAction.EDIT, ActionImpact.UPDATED, "btn-edit");

    disposalAssociationActionableBundle.addGroup(managementGroup);
    return disposalAssociationActionableBundle;
  }
}
