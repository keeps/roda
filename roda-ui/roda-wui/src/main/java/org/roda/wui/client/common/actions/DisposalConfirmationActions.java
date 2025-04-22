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
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationState;
import org.roda.core.data.v2.index.select.SelectedItems;
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<Action<DisposalConfirmation>> POSSIBLE_ACTIONS_WITHOUT_OBJECT_SELECTED = new HashSet<>(
    List.of(DisposalConfirmationAction.NEW));
  private static final Set<Action<DisposalConfirmation>> POSSIBLE_ACTIONS_FOR_PENDING = new HashSet<>(
    List.of(DisposalConfirmationAction.DESTROY));
  private static final Set<Action<DisposalConfirmation>> POSSIBLE_ACTIONS_FOR_APPROVED = new HashSet<>(
    Arrays.asList(DisposalConfirmationAction.PERM_DELETE, DisposalConfirmationAction.RESTORE));

  private DisposalConfirmationActions() {
    // Singleton
  }

  private static class SingletonHelper {
    private static final DisposalConfirmationActions INSTANCE = new DisposalConfirmationActions();
  }

  public static DisposalConfirmationActions getInstance() {
    return SingletonHelper.INSTANCE;
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
  public CanActResult userCanAct(Action<DisposalConfirmation> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalConfirmation> action) {
    return new CanActResult(POSSIBLE_ACTIONS_WITHOUT_OBJECT_SELECTED.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<DisposalConfirmation> action, DisposalConfirmation disposalConfirmation) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalConfirmation> action, DisposalConfirmation disposalConfirmation) {
    if (DisposalConfirmationState.PENDING.equals(disposalConfirmation.getState())) {
      return new CanActResult(POSSIBLE_ACTIONS_FOR_PENDING.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonInvalidContext());
    } else if (DisposalConfirmationState.APPROVED.equals(disposalConfirmation.getState())) {
      return new CanActResult(POSSIBLE_ACTIONS_FOR_APPROVED.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonInvalidContext());
    } else if (DisposalConfirmationState.PERMANENTLY_DELETED.equals(disposalConfirmation.getState())
      || DisposalConfirmationState.RESTORED.equals(disposalConfirmation.getState())) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonInvalidContext());
    } else {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonInvalidContext());
    }
  }

  @Override
  public CanActResult userCanAct(Action<DisposalConfirmation> action, SelectedItems<DisposalConfirmation> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DisposalConfirmation> action, SelectedItems<DisposalConfirmation> objects) {
    if (DisposalConfirmationAction.NEW.equals(action)) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonInvalidContext());
    }
    return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<DisposalConfirmation> action, AsyncCallback<ActionImpact> callback) {
    if (DisposalConfirmationAction.NEW.equals(action)) {
      newConfirmation(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<DisposalConfirmation> action, DisposalConfirmation object,
    AsyncCallback<ActionImpact> callback) {
    if (DisposalConfirmationAction.RESTORE.equals(action)) {
      DisposalConfirmationActionsUtils.restoreDestroyedRecord(object, callback);
    } else if (DisposalConfirmationAction.DESTROY.equals(action)) {
      DisposalConfirmationActionsUtils.destroyDisposalConfirmationContent(object, callback);
    } else if (DisposalConfirmationAction.PERM_DELETE.equals(action)) {
      DisposalConfirmationActionsUtils.permanentlyDeleteDisposalConfirmationReport(object, callback);
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
    ActionableGroup<DisposalConfirmation> actionsGroup = new ActionableGroup<>("Disposal confirmation");
    actionsGroup.addButton(messages.newDisposalConfirmationButton(), DisposalConfirmationAction.NEW,
      ActionImpact.UPDATED, "btn-plus-circle");
    actionsGroup.addButton(messages.applyDisposalScheduleButton(), DisposalConfirmationAction.DESTROY,
      ActionImpact.DESTROYED, "btn-trash-alt");

    // disposal bin
    ActionableGroup<DisposalConfirmation> disposalBinGroup = new ActionableGroup<>("Disposal bin");
    disposalBinGroup.addButton(messages.permanentlyDeleteFromBinButton(), DisposalConfirmationAction.PERM_DELETE,
      ActionImpact.UPDATED, "btn-delete-forever");

    disposalBinGroup.addButton(messages.restoreFromBinButton(), DisposalConfirmationAction.RESTORE,
      ActionImpact.DESTROYED, "btn-restore");

    confirmationActionableBundle.addGroup(actionsGroup).addGroup(disposalBinGroup);

    return confirmationActionableBundle;
  }

  public enum DisposalConfirmationAction implements Action<DisposalConfirmation> {
    NEW(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION),
    DESTROY(RodaConstants.PERMISSION_METHOD_DESTROY_RECORDS_DISPOSAL_CONFIRMATION),
    PERM_DELETE(RodaConstants.PERMISSION_METHOD_PERMANENTLY_DELETE_RECORDS_DISPOSAL_CONFIRMATION),
    RESTORE(RodaConstants.PERMISSION_METHOD_RESTORE_RECORDS_DISPOSAL_CONFIRMATION);

    private final List<String> methods;

    DisposalConfirmationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
