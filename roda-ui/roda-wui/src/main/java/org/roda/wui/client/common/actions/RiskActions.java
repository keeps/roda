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
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.planning.CreateRisk;
import org.roda.wui.client.planning.EditRisk;
import org.roda.wui.client.planning.RiskHistory;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class RiskActions extends AbstractActionable<IndexedRisk> {
  private static final RiskActions INSTANCE_NO_HISTORY = new RiskActions(false);
  private static final RiskActions INSTANCE_WITH_HISTORY = new RiskActions(true);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<IndexedRiskAction> POSSIBLE_ACTIONS_WITHOUT_RISK = new HashSet<>(
    Arrays.asList(IndexedRiskAction.NEW));

  private static final Set<IndexedRiskAction> POSSIBLE_ACTIONS_ON_SINGLE_RISK = new HashSet<>(
    Arrays.asList(IndexedRiskAction.REMOVE, IndexedRiskAction.START_PROCESS, IndexedRiskAction.EDIT));
  // also HISTORY, but that one also depends on having history

  private static final Set<IndexedRiskAction> POSSIBLE_ACTIONS_ON_MULTIPLE_RISKS = new HashSet<>(
    Arrays.asList(IndexedRiskAction.REMOVE, IndexedRiskAction.START_PROCESS));

  private final boolean hasHistory;

  private RiskActions(boolean hasHistory) {
    this.hasHistory = hasHistory;
  }

  public enum IndexedRiskAction implements Action<IndexedRisk> {
    NEW(RodaConstants.PERMISSION_METHOD_CREATE_RISK), REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_RISK),
    START_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB), EDIT(RodaConstants.PERMISSION_METHOD_UPDATE_RISK),
    HISTORY(RodaConstants.PERMISSION_METHOD_RETRIEVE_RISK_VERSIONS);

    private List<String> methods;

    IndexedRiskAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public IndexedRiskAction[] getActions() {
    return IndexedRiskAction.values();
  }

  @Override
  public IndexedRiskAction actionForName(String name) {
    return IndexedRiskAction.valueOf(name);
  }

  public static RiskActions get() {
    return INSTANCE_NO_HISTORY;
  }

  public static RiskActions getWithHistory() {
    return INSTANCE_WITH_HISTORY;
  }

  @Override
  public CanActResult userCanAct(Action<IndexedRisk> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedRisk> action) {
    return new CanActResult(POSSIBLE_ACTIONS_WITHOUT_RISK.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<IndexedRisk> action, IndexedRisk object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());

  }

  @Override
  public CanActResult contextCanAct(Action<IndexedRisk> action, IndexedRisk object) {
    if (action.equals(IndexedRiskAction.HISTORY)) {
      return new CanActResult(hasHistory, CanActResult.Reason.CONTEXT, messages.reasonRiskHasNoHistory());
    } else {
      return new CanActResult(
        POSSIBLE_ACTIONS_ON_SINGLE_RISK.contains(action) || (action.equals(IndexedRiskAction.HISTORY) && hasHistory),
        CanActResult.Reason.CONTEXT, messages.reasonCantActOnSingleObject());
    }
  }

  @Override
  public CanActResult userCanAct(Action<IndexedRisk> action, SelectedItems<IndexedRisk> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());

  }

  @Override
  public CanActResult contextCanAct(Action<IndexedRisk> action, SelectedItems<IndexedRisk> objects) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_RISKS.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<IndexedRisk> action, AsyncCallback<ActionImpact> callback) {
    if (IndexedRiskAction.NEW.equals(action)) {
      create(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedRisk> action, IndexedRisk object, AsyncCallback<ActionImpact> callback) {
    if (IndexedRiskAction.REMOVE.equals(action)) {
      remove(objectToSelectedItems(object, IndexedRisk.class), callback);
    } else if (IndexedRiskAction.START_PROCESS.equals(action)) {
      startProcess(objectToSelectedItems(object, IndexedRisk.class), callback);
    } else if (IndexedRiskAction.EDIT.equals(action)) {
      edit(object, callback);
    } else if (IndexedRiskAction.HISTORY.equals(action)) {
      history(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedRisk> action, SelectedItems<IndexedRisk> objects,
    AsyncCallback<ActionImpact> callback) {
    if (IndexedRiskAction.REMOVE.equals(action)) {
      remove(objects, callback);
    } else if (IndexedRiskAction.START_PROCESS.equals(action)) {
      startProcess(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void history(IndexedRisk object, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(RiskHistory.RESOLVER, object.getId());
  }

  private void startProcess(SelectedItems<IndexedRisk> objects, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(objects);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void remove(SelectedItems<IndexedRisk> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedRisk.class, objects, new ActionNoAsyncCallback<Long>(callback) {
      Services service = new Services("Remove risks", "remove");

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.riskRemoveConfirmDialogTitle(),
          messages.riskRemoveSelectedConfirmDialogMessage(size), messages.riskRemoveConfirmDialogCancel(),
          messages.riskRemoveConfirmDialogOk(), new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                service.riskResource(s -> s.deleteRisk(SelectedItemsUtils.convertToRESTRequest(objects)))
                  .whenComplete((value, error) -> {
                    if (value != null) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                    } else if (error != null) {

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          Toast.showInfo(messages.riskRemoveSuccessTitle(), messages.riskRemoveSuccessMessage(size));
                          doActionCallbackDestroyed();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }
                  });
              } else {
                doActionCallbackNone();
              }
            }
          });
      }
    });
  }

  private void create(AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(CreateRisk.RESOLVER);
  }

  private void edit(IndexedRisk object, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(EditRisk.RESOLVER, object.getId());
  }

  @Override
  public ActionableBundle<IndexedRisk> createActionsBundle() {
    ActionableBundle<IndexedRisk> formatActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedRisk> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.riskHistoryButton(), IndexedRiskAction.HISTORY, ActionImpact.NONE,
      "btn-history");
    managementGroup.addButton(messages.newButton(), IndexedRiskAction.NEW, ActionImpact.UPDATED, "btn-plus-circle");
    managementGroup.addButton(messages.editButton(), IndexedRiskAction.EDIT, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.removeButton(), IndexedRiskAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionableGroup<IndexedRisk> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), IndexedRiskAction.START_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    formatActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return formatActionableBundle;
  }
}
