package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.disposal.rule.CreateDisposalRule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;
import org.roda.wui.client.disposal.rule.ShowDisposalRule;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalRuleSearchWrapperActions extends AbstractActionable<DisposalRule> {
  private static final DisposalRuleSearchWrapperActions INSTANCE = new DisposalRuleSearchWrapperActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Set<DisposalRuleAction> POSSIBLE_ACTIONS_WITHOUT_DISPOSAL_RULE = new HashSet<>(
    Arrays.asList(DisposalRuleAction.NEW, DisposalRuleAction.APPLY_RULES));

  private static final Set<DisposalRuleAction> POSSIBLE_ACTIONS_ON_SINGLE_DISPOSAL_RULE = new HashSet<>(
    Arrays.asList(DisposalRuleAction.EDIT, DisposalRuleAction.REMOVE, DisposalRuleAction.SAVE,
      DisposalRuleAction.UPDATE, DisposalRuleAction.CHANGE_ORDER));

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
      newRule(callback);
    } else if (DisposalRuleAction.APPLY_RULES.equals(action)) {
      DisposalRuleActions.applyDisposalRulesAction();
      callback.onSuccess(ActionImpact.NONE);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<DisposalRule> action, DisposalRule rule, AsyncCallback<ActionImpact> callback) {
    if (DisposalRuleAction.SAVE.equals(action)) {
      saveRule(rule, callback);
    } else if (DisposalRuleAction.EDIT.equals(action)) {
      DisposalRuleActions.editDisposalRule(rule, callback);
    } else if (DisposalRuleAction.REMOVE.equals(action)) {
      DisposalRuleActions.removeDisposalRule(rule, callback);
    } else if (DisposalRuleAction.UPDATE.equals(action)) {
      updateRule(rule, callback);
    } else if (DisposalRuleAction.CHANGE_ORDER.equals(action)) {
      DisposalRuleActions.changeDisposalRulesOrder(objectToSelectedItems(rule, DisposalRule.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<DisposalRule> action, SelectedItems<DisposalRule> objects, AsyncCallback<ActionImpact> callback) {
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
    managementGroup.addButton(messages.saveButton(), DisposalRuleAction.SAVE, ActionImpact.UPDATED, "btn-play");
    managementGroup.addButton(messages.updateButton(), DisposalRuleAction.UPDATE, ActionImpact.UPDATED, "btn-play");
    managementGroup.addButton(messages.applyRules(), DisposalRuleAction.APPLY_RULES, ActionImpact.UPDATED, "btn-play");

    disposalRuleActionableBundle.addGroup(managementGroup);
    return disposalRuleActionableBundle;
  }

  @Override
  public Action<DisposalRule> actionForName(String name) {
    return null;
  }

  private void newRule(AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(CreateDisposalRule.RESOLVER.getHistoryPath());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void saveRule(DisposalRule rule, AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Create disposal rule", "create");
    services.disposalRuleResource(s -> s.createDisposalRule(rule)).whenComplete((created, throwable) -> {
      if (throwable != null) {
        Toast.showError(throwable);
        callback.onSuccess(ActionImpact.NONE);
      } else {
        Toast.showInfo(messages.disposalRulesTitle(), messages.disposalRuleSuccessfullyCreated());
        HistoryUtils.newHistory(ShowDisposalRule.RESOLVER, created.getId());
        callback.onSuccess(ActionImpact.UPDATED);
      }
    });
  }

  private void updateRule(DisposalRule rule, AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Update disposal rule", "update");
    services.disposalRuleResource(s -> s.updateDisposalRule(rule)).whenComplete((updated, throwable) -> {
      if (throwable != null) {
        Toast.showError(throwable);
        callback.onSuccess(ActionImpact.NONE);
      } else {
        Toast.showInfo(messages.disposalRulesTitle(), messages.disposalRuleSuccessfullyUpdated());
        HistoryUtils.newHistory(ShowDisposalRule.RESOLVER, updated.getId());
        callback.onSuccess(ActionImpact.UPDATED);
      }
    });
  }
}
