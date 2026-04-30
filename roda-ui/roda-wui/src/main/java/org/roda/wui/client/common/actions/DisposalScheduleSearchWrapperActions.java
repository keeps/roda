package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalScheduleSearchWrapperActions extends AbstractActionable<DisposalSchedule> {
  private static final DisposalScheduleSearchWrapperActions INSTANCE = new DisposalScheduleSearchWrapperActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Set<DisposalScheduleAction> POSSIBLE_ACTIONS_WITHOUT_SCHEDULE = new HashSet<>(
    Arrays.asList(DisposalScheduleAction.NEW));

  private static final Set<DisposalScheduleAction> POSSIBLE_ACTIONS_ON_SINGLE_DISPOSAL_SCHEDULE = new HashSet<>(
    Arrays.asList(DisposalScheduleAction.EDIT, DisposalScheduleAction.REMOVE));

  private DisposalScheduleSearchWrapperActions() {
  }

  public static DisposalScheduleSearchWrapperActions get() {
    return INSTANCE;
  }

  @Override
  public Action<DisposalSchedule>[] getActions() {
    return DisposalScheduleAction.values();
  }

  @Override
  public CanActResult userCanAct(Action<DisposalSchedule> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalSchedule> action) {
    return new CanActResult(POSSIBLE_ACTIONS_WITHOUT_SCHEDULE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalSchedule> action, DisposalSchedule object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalSchedule> action, DisposalSchedule object) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_DISPOSAL_SCHEDULE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnSingleObject());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalSchedule> action, SelectedItems<DisposalSchedule> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalSchedule> action, SelectedItems<DisposalSchedule> objects) {
    return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<DisposalSchedule> action, AsyncCallback<ActionImpact> callback) {
    if (DisposalScheduleAction.NEW.equals(action)) {
      DisposalScheduleActions.newDisposalSchedule(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<DisposalSchedule> action, DisposalSchedule schedule, AsyncCallback<ActionImpact> callback) {
    if (DisposalScheduleAction.REMOVE.equals(action)) {
      DisposalScheduleActions.removeDisposalSchedule(schedule, callback);
    } else if (DisposalScheduleAction.EDIT.equals(action)) {
      DisposalScheduleActions.editDisposalSchedule(schedule, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<DisposalSchedule> createActionsBundle() {
    ActionableBundle<DisposalSchedule> disposalScheduleActionableBundle = new ActionableBundle<>();

    ActionableGroup<DisposalSchedule> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newDisposalScheduleTitle(), DisposalScheduleAction.NEW, ActionImpact.UPDATED,
      "btn-plus-circle");

    managementGroup.addButton(messages.editButton(), DisposalScheduleAction.EDIT, ActionImpact.UPDATED, "btn-edit");

    managementGroup.addButton(messages.removeButton(), DisposalScheduleAction.REMOVE, ActionImpact.DESTROYED,
      "btn-ban");

    disposalScheduleActionableBundle.addGroup(managementGroup);
    return disposalScheduleActionableBundle;
  }

  @Override
  public Action<DisposalSchedule> actionForName(String name) {
    return null;
  }
}
