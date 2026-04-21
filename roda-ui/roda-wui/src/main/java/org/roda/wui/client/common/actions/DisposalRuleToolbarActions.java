package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalRuleToolbarActions extends AbstractActionable<DisposalRule> {
  private static final DisposalRuleToolbarActions INSTANCE = new DisposalRuleToolbarActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalRuleAction> POSSIBLE_ACTIONS_ON_SINGLE_DISPOSAL_RULE = new HashSet<>(
    Arrays.asList(DisposalRuleAction.EDIT, DisposalRuleAction.REMOVE));

  private DisposalRuleToolbarActions() {
  }

  public static DisposalRuleToolbarActions get() {
    return INSTANCE;
  }

  @Override
  public Action<DisposalRule>[] getActions() {
    return DisposalRuleAction.values();
  }

  @Override
  public CanActResult userCanAct(Action<DisposalRule> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalRule> action) {
    return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalRule> action, DisposalRule object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalRule> action, DisposalRule object) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_DISPOSAL_RULE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnSingleObject());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalRule> action, SelectedItems<DisposalRule> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalRule> action, SelectedItems<DisposalRule> objects) {
    return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<DisposalRule> action, DisposalRule rule, AsyncCallback<ActionImpact> callback) {
    if (DisposalRuleAction.REMOVE.equals(action)) {
      DisposalRuleActions.removeDisposalRule(rule, callback);
    } else if (DisposalRuleAction.EDIT.equals(action)) {
      DisposalRuleActions.editDisposalRule(rule, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<DisposalRule> createActionsBundle() {
    ActionableBundle<DisposalRule> disposalRuleActionableBundle = new ActionableBundle<>();

    ActionableGroup<DisposalRule> managementGroup = new ActionableGroup<>(messages.manage());

    managementGroup.addButton(messages.editButton(), DisposalRuleAction.EDIT, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.removeButton(), DisposalRuleAction.REMOVE, ActionImpact.UPDATED, "btn-ban");

    disposalRuleActionableBundle.addGroup(managementGroup);
    return disposalRuleActionableBundle;
  }

  @Override
  public Action<DisposalRule> actionForName(String name) {
    return null;
  }
}
