package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationActions extends AbstractActionable<DisposalConfirmationMetadata> {
  private static final DisposalConfirmationActions INSTANCE = new DisposalConfirmationActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public enum DisposalConfirmationAction implements Action<DisposalConfirmationMetadata> {
    NEW_CONFIRMATION(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION);

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
  public Action<DisposalConfirmationMetadata> actionForName(String name) {
    return DisposalConfirmationAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<DisposalConfirmationMetadata> action) {
    return hasPermissions(action) && DisposalConfirmationAction.NEW_CONFIRMATION.equals(action);
  }

  @Override
  public ActionableBundle<DisposalConfirmationMetadata> createActionsBundle() {
    ActionableBundle<DisposalConfirmationMetadata> confirmationActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<DisposalConfirmationMetadata> managementGroup = new ActionableGroup<>(
      messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newProcessPreservation(), DisposalConfirmationAction.NEW_CONFIRMATION,
      ActionImpact.UPDATED, "btn-plus-circle");

    confirmationActionableBundle.addGroup(managementGroup);

    return confirmationActionableBundle;
  }
}
