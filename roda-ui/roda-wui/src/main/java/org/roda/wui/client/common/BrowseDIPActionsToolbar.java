package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.common.client.tools.ConfigurationManager;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseDIPActionsToolbar extends BrowseObjectActionsToolbar<IndexedDIP> {
  public void buildIcon() {
    setIcon(ConfigurationManager.getString(RodaConstants.UI_ICONS_CLASS, IndexedDIP.class.getSimpleName()));
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();
    // AIP actions
    DisseminationActions dipActions;
    dipActions = DisseminationActions.get(actionPermissions);
    this.actions.add(new ActionableWidgetBuilder<IndexedDIP>(dipActions).withActionCallback(actionCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(object), new ArrayList<>(),
        List.of(DisseminationActions.DisseminationAction.NEW_PROCESS)));

  }
}
