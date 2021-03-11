/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.disposal.confirmations.CreateDisposalConfirmation;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationActions extends AbstractActionable<DisposalConfirmation> {
  private static final DisposalConfirmationActions INSTANCE = new DisposalConfirmationActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public enum DisposalConfirmationAction implements Action<DisposalConfirmation> {
    NEW(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION);

    private List<String> methods;

    DisposalConfirmationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  private DisposalConfirmationActions() {
  }

  public static DisposalConfirmationActions get() {
    return INSTANCE;
  }

  @Override
  public DisposalConfirmationAction[] getActions() {
    return DisposalConfirmationAction.values();
  }

  @Override
  public Action<DisposalConfirmation> actionForName(String name) {
    return DisposalConfirmationAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<DisposalConfirmation> action) {
    return hasPermissions(action);
  }

  @Override
  public void act(Action<DisposalConfirmation> action, AsyncCallback<ActionImpact> callback) {
    if (DisposalConfirmationAction.NEW.equals(action)) {
      newConfirmation(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void newConfirmation(AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(CreateDisposalConfirmation.RESOLVER);
  }

  @Override
  public ActionableBundle<DisposalConfirmation> createActionsBundle() {
    ActionableBundle<DisposalConfirmation> confirmationActionableBundle = new ActionableBundle<>();

    // management
    ActionableGroup<DisposalConfirmation> actionsGroup = new ActionableGroup<>(messages.sidebarActionsTitle());

    actionsGroup.addButton(messages.newDisposalConfirmationButton(), DisposalConfirmationAction.NEW,
      ActionImpact.UPDATED, "btn-plus-circle");

    confirmationActionableBundle.addGroup(actionsGroup);

    return confirmationActionableBundle;
  }
}
