/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;

import org.roda.wui.client.common.actions.PreservationAgentActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PreservationAgentActionsToolbar extends BrowseObjectActionsToolbar<IndexedPreservationAgent> {
  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();

    PreservationAgentActions preservationAgentActions = PreservationAgentActions.get();
    this.actions
      .add(new ActionableWidgetBuilder<IndexedPreservationAgent>(preservationAgentActions).buildGroupedListWithObjects(
        new ActionableObject<>(object), List.of(PreservationAgentActions.PreservationAgentAction.DOWNLOAD),
        List.of(PreservationAgentActions.PreservationAgentAction.DOWNLOAD)));
  }
}
