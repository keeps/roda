package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.common.actions.RiskActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class BrowseRiskActionsToolbar extends BrowseObjectActionsToolbar<IndexedRisk> {
  public void buildIcon() {
    setIcon("fa-solid fa-triangle-exclamation");
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();
    if (object == null)
      return;

    RiskActions riskActions = RiskActions.get();
    if (object.hasVersions()) {
      riskActions = RiskActions.getWithHistory();
    }
    this.actions
      .add(new ActionableWidgetBuilder<IndexedRisk>(riskActions).buildGroupedListWithObjects(
        new ActionableObject<>(object), List.of(RiskActions.IndexedRiskAction.EDIT,
          RiskActions.IndexedRiskAction.REMOVE, RiskActions.IndexedRiskAction.START_PROCESS),
        List.of(RiskActions.IndexedRiskAction.START_PROCESS)));
  }

}
