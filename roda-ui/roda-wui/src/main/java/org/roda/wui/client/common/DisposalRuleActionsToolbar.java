/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.wui.client.common.actions.DisposalRuleAction;
import org.roda.wui.client.common.actions.DisposalRuleToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalRuleActionsToolbar extends BrowseObjectActionsToolbar<DisposalRule> {
  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();

    this.actions.add(
      new ActionableWidgetBuilder<DisposalRule>(DisposalRuleToolbarActions.get()).withActionCallback(actionCallback)
        .buildGroupedListWithObjects(new ActionableObject<>(object), List.of(DisposalRuleAction.REMOVE), List.of()));
  }
}
