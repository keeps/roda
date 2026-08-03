/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
    this.actions.add(
      new ActionableWidgetBuilder<>(riskActions).buildGroupedListWithObjects(new ActionableObject<>(object),
        List.of(RiskActions.IndexedRiskAction.REMOVE, RiskActions.IndexedRiskAction.START_PROCESS),
        List.of(RiskActions.IndexedRiskAction.START_PROCESS)));
  }

}
