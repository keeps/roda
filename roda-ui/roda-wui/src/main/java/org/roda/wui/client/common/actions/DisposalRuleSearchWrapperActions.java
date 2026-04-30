package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalRuleSearchWrapperActions extends AbstractActionable<DisposalRule> {
  private static final DisposalRuleSearchWrapperActions INSTANCE = new DisposalRuleSearchWrapperActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Set<DisposalRuleAction> POSSIBLE_ACTIONS_WITHOUT_DISPOSAL_RULE = new HashSet<>(
    Arrays.asList(DisposalRuleAction.NEW, DisposalRuleAction.APPLY_RULES));

  private static final Set<DisposalRuleAction> POSSIBLE_ACTIONS_ON_SINGLE_DISPOSAL_RULE = new HashSet<>(
    Arrays.asList(DisposalRuleAction.EDIT, DisposalRuleAction.REMOVE, DisposalRuleAction.CHANGE_ORDER));

  private static final Set<DisposalRuleAction> POSSIBLE_ACTIONS_ON_MULTIPLE_DISPOSAL_RULE = new HashSet<>(
    Arrays.asList(DisposalRuleAction.REMOVE, DisposalRuleAction.CHANGE_ORDER));

  private DisposalRuleSearchWrapperActions() {
  }

  public static DisposalRuleSearchWrapperActions get() {
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
    return new CanActResult(POSSIBLE_ACTIONS_WITHOUT_DISPOSAL_RULE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonNoObjectSelected());
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
    return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_DISPOSAL_RULE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<DisposalRule> action, AsyncCallback<ActionImpact> callback) {
    if (DisposalRuleAction.NEW.equals(action)) {
      DisposalRuleActions.newRule(callback);
    } else if (DisposalRuleAction.APPLY_RULES.equals(action)) {
      DisposalRuleActions.applyDisposalRulesAction();
      callback.onSuccess(ActionImpact.NONE);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<DisposalRule> action, DisposalRule rule, AsyncCallback<ActionImpact> callback) {
    if (DisposalRuleAction.EDIT.equals(action)) {
      DisposalRuleActions.editDisposalRule(rule, callback);
    } else if (DisposalRuleAction.REMOVE.equals(action)) {
      DisposalRuleActions.removeDisposalRule(rule, callback);
    } else if (DisposalRuleAction.CHANGE_ORDER.equals(action)) {
      DisposalRuleActions.changeDisposalRulesOrder(objectToSelectedItems(rule, DisposalRule.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<DisposalRule> action, SelectedItems<DisposalRule> objects,
    AsyncCallback<ActionImpact> callback) {
    if (DisposalRuleAction.REMOVE.equals(action)) {
      DisposalRuleActions.removeMultipleDisposalRules(objects, callback);
    } else if (DisposalRuleAction.CHANGE_ORDER.equals(action)) {
      DisposalRuleActions.changeDisposalRulesOrder(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<DisposalRule> createActionsBundle() {
    ActionableBundle<DisposalRule> disposalRuleActionableBundle = new ActionableBundle<>();

    ActionableGroup<DisposalRule> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newDisposalRuleTitle(), DisposalRuleAction.NEW, ActionImpact.UPDATED,
      "btn-plus-circle");

    managementGroup.addButton(messages.editButton(), DisposalRuleAction.EDIT, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.editRulesOrder(), DisposalRuleAction.CHANGE_ORDER, ActionImpact.UPDATED,
      "btn-edit-order");
    managementGroup.addButton(messages.removeButton(), DisposalRuleAction.REMOVE, ActionImpact.UPDATED, "btn-ban");
    managementGroup.addButton(messages.applyRules(), DisposalRuleAction.APPLY_RULES, ActionImpact.UPDATED, "btn-play");

    disposalRuleActionableBundle.addGroup(managementGroup);
    return disposalRuleActionableBundle;
  }

  @Override
  public Action<DisposalRule> actionForName(String name) {
    return null;
  }

}
