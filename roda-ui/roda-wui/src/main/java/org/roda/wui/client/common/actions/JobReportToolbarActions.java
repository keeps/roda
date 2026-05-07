package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.common.client.tools.HistoryUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class JobReportToolbarActions extends AbstractActionable<IndexedReport> {
  private static final JobReportToolbarActions INSTANCE = new JobReportToolbarActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private JobReportToolbarActions() {
  }

  public static JobReportToolbarActions get() {
    return INSTANCE;
  }

  @Override
  public Action<IndexedReport>[] getActions() {
    return JobReportAction.values();
  }

  @Override
  public CanActResult userCanAct(Action<IndexedReport> action, IndexedReport object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedReport> action, IndexedReport object) {
    return new CanActResult(true, CanActResult.Reason.CONTEXT, messages.reasonCantActOnGroup());
  }

  @Override
  public void act(Action<IndexedReport> action, IndexedReport object, AsyncCallback<ActionImpact> callback) {
    if (action.equals(JobReportAction.BROWSE_SOURCE)) {
      callback.onSuccess(ActionImpact.NONE);
     HistoryUtils.newHistory(
              HistoryUtils.getHistoryUuidResolver(object.getSourceObjectClass(), object.getSourceObjectId()));
    } else if (action.equals(JobReportAction.BROWSE_OUTCOME)) {
      callback.onSuccess(ActionImpact.NONE);
      HistoryUtils.newHistory(
              HistoryUtils.getHistoryUuidResolver(object.getOutcomeObjectClass(), object.getOutcomeObjectId()));
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<IndexedReport> createActionsBundle() {
    ActionableBundle<IndexedReport> bundle = new ActionableBundle<>();

    ActionableGroup<IndexedReport> managementGroup = new ActionableGroup<>(messages.manage());

    managementGroup.addButton(messages.openSourceObject(), JobReportAction.BROWSE_SOURCE, ActionImpact.NONE, "btn-search");
    managementGroup.addButton(messages.openOutcomeObject(), JobReportAction.BROWSE_OUTCOME, ActionImpact.NONE, "btn-search");

    bundle.addGroup(managementGroup);
    return bundle;
  }

  @Override
  public Action<IndexedReport> actionForName(String name) {
    return JobReportAction.valueOf(name);
  }
}
