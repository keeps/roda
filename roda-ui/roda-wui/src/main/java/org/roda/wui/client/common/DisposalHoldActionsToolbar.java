/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.wui.client.common.actions.DisposalHoldAction;
import org.roda.wui.client.common.actions.DisposalHoldToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldActionsToolbar extends BrowseObjectActionsToolbar<DisposalHold> {
  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();

    DisposalHoldToolbarActions toolbarActions = DisposalHoldToolbarActions.get();
    this.actions.add(new ActionableWidgetBuilder<DisposalHold>(toolbarActions).withActionCallback(actionCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(object), List.of(DisposalHoldAction.LIFT), List.of()));
  }
}
