package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalCreateConfirmationReviewActions extends AbstractActionable<IndexedAIP> {
  private static final DisposalCreateConfirmationReviewActions INSTANCE = new DisposalCreateConfirmationReviewActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalCreateConfirmationReviewAction> POSSIBLE_ACTIONS_WITH_RECORDS = new HashSet<>(
    Collections.singletonList(DisposalCreateConfirmationReviewAction.CHANGE_SCHEDULE));

  public enum DisposalCreateConfirmationReviewAction implements Action<IndexedAIP> {
    CHANGE_SCHEDULE(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION);

    private List<String> methods;

    DisposalCreateConfirmationReviewAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  private DisposalCreateConfirmationReviewActions() {
  }

  public static DisposalCreateConfirmationReviewActions get() {
    return INSTANCE;
  }

  @Override
  public DisposalCreateConfirmationReviewAction[] getActions() {
    return DisposalCreateConfirmationReviewAction.values();
  }

  @Override
  public Action<IndexedAIP> actionForName(String name) {
    return DisposalCreateConfirmationReviewAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, IndexedAIP object) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_WITH_RECORDS.contains(action);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_WITH_RECORDS.contains(action);
  }

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> confirmationActionableBundle = new ActionableBundle<>();

    // management
    ActionableGroup<IndexedAIP> actionsGroup = new ActionableGroup<>(messages.sidebarActionsTitle());

    actionsGroup.addButton(messages.changeDisposalScheduleActionTitle(), DisposalCreateConfirmationReviewAction.CHANGE_SCHEDULE, ActionImpact.NONE,
      "btn-edit");

    confirmationActionableBundle.addGroup(actionsGroup);

    return confirmationActionableBundle;
  }
}
