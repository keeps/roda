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
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
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
    Arrays.asList(DisposalScheduleAction.EDIT, DisposalScheduleAction.REMOVE, DisposalScheduleAction.SAVE,
      DisposalScheduleAction.UPDATE));

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
      edit(schedule, callback);
    } else if (DisposalScheduleAction.SAVE.equals(action)) {
      create(schedule, callback);
    } else if (DisposalScheduleAction.UPDATE.equals(action)) {
      update(schedule, callback);
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

    managementGroup.addButton(messages.saveButton(), DisposalScheduleAction.SAVE, ActionImpact.UPDATED, "btn-play");

    managementGroup.addButton(messages.updateButton(), DisposalScheduleAction.UPDATE, ActionImpact.UPDATED, "btn-play");

    disposalScheduleActionableBundle.addGroup(managementGroup);
    return disposalScheduleActionableBundle;
  }

  @Override
  public Action<DisposalSchedule> actionForName(String name) {
    return null;
  }

  private void edit(DisposalSchedule schedule, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(EditDisposalSchedule.RESOLVER, schedule.getId());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void create(DisposalSchedule schedule, AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Create disposal schedule", "create");
    services.disposalScheduleResource(s -> s.createDisposalSchedule(schedule)).whenComplete((created, throwable) -> {
      if (throwable != null) {
        Toast.showError(throwable);
        callback.onSuccess(ActionImpact.NONE);
      } else {
        Toast.showInfo("Disposal schedule", "Disposal schedule successfully created");
        HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, created.getId());
        callback.onSuccess(ActionImpact.UPDATED);
      }
    });
  }

  private void update(DisposalSchedule schedule, AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Update disposal schedule", "update");
    services.disposalScheduleResource(s -> s.updateDisposalSchedule(schedule)).whenComplete((updated, throwable) -> {
      if (throwable != null) {
        Toast.showError(throwable);
        callback.onSuccess(ActionImpact.NONE);
      } else {
        Toast.showInfo("Disposal schedule", "Disposal schedule successfully updated");
        HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, updated.getId());
        callback.onSuccess(ActionImpact.UPDATED);
      }
    });
  }

}
