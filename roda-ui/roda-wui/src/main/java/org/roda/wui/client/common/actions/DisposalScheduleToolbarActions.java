/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;
import org.roda.wui.client.disposal.schedule.EditDisposalSchedule;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalScheduleToolbarActions extends AbstractActionable<DisposalSchedule> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisposalScheduleAction> POSSIBLE_ACTIONS_ON_DISPOSAL_SCHEDULE = new HashSet<>(
    Arrays.asList(DisposalScheduleAction.EDIT, DisposalScheduleAction.REMOVE));

  private static final DisposalScheduleToolbarActions GENERAL_INSTANCE = new DisposalScheduleToolbarActions();

  private DisposalScheduleToolbarActions() {
  }

  public static DisposalScheduleToolbarActions get() {
    return GENERAL_INSTANCE;
  }

  @Override
  public Action<DisposalSchedule>[] getActions() {
    return DisposalScheduleAction.values();
  }

  @Override
  public CanActResult userCanAct(Action<DisposalSchedule> action, ActionableObject<DisposalSchedule> object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalSchedule> action, ActionableObject<DisposalSchedule> object) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_DISPOSAL_SCHEDULE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnGroup());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalSchedule> action, DisposalSchedule object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalSchedule> action, DisposalSchedule object) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_DISPOSAL_SCHEDULE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnSingleObject());
  }

  @Override
  public void act(Action<DisposalSchedule> action, DisposalSchedule schedule, AsyncCallback<ActionImpact> callback) {
    if (DisposalScheduleAction.EDIT.equals(action)) {
      DisposalScheduleActions.editDisposalSchedule(schedule, callback);
    } else if (DisposalScheduleAction.REMOVE.equals(action)) {
      DisposalScheduleActions.removeDisposalSchedule(schedule, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<DisposalSchedule> createActionsBundle() {
    ActionableBundle<DisposalSchedule> disposalScheduleActionableBundle = new ActionableBundle<>();

    ActionableGroup<DisposalSchedule> managementGroup = new ActionableGroup<>(messages.manage(), "btn-edit");
    managementGroup.addButton(messages.editButton(), DisposalScheduleAction.EDIT, ActionImpact.UPDATED,
      "fas fa-pencil");
    managementGroup.addButton(messages.removeButton(), DisposalScheduleAction.REMOVE, ActionImpact.UPDATED,
      "fas fa-trash");

    disposalScheduleActionableBundle.addGroup(managementGroup);
    return disposalScheduleActionableBundle;
  }

  @Override
  public Action<DisposalSchedule> actionForName(String name) {
    return null;
  }
}
