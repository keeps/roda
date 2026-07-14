package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.actions.RiskIncidenceActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class BrowseRiskIncidenceActionsToolbar extends BrowseObjectActionsToolbar<RiskIncidence> {
  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();
    if (object == null)
      return;

    RiskIncidenceActions riskIncidenceActions = RiskIncidenceActions.get();

    this.actions.add(
      new ActionableWidgetBuilder<>(riskIncidenceActions).buildGroupedListWithObjects(new ActionableObject<>(object),
        List.of(RiskIncidenceActions.RiskIncidenceAction.REMOVE,
          RiskIncidenceActions.RiskIncidenceAction.START_PROCESS),
        List.of(RiskIncidenceActions.RiskIncidenceAction.REMOVE,
          RiskIncidenceActions.RiskIncidenceAction.START_PROCESS)));
  }

}
