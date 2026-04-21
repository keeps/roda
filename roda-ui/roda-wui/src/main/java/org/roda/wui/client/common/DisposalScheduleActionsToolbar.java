/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.wui.client.common.actions.DisposalScheduleAction;
import org.roda.wui.client.common.actions.DisposalScheduleToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.labels.Tag;

import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalScheduleActionsToolbar extends BrowseObjectActionsToolbar<DisposalSchedule> {
  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    this.tags.clear();

    if (object.isUsedInDisposalRule()) {
      Tag tag = Tag.fromText(messages.disposalScheduleUsedInRule(), Tag.TagStyle.WARNING_LIGHT);
      tags.add(tag);
    }
  }

  public void buildActions() {
    this.actions.clear();

    DisposalScheduleToolbarActions toolbarActions = DisposalScheduleToolbarActions.get();
    this.actions.add(new ActionableWidgetBuilder<DisposalSchedule>(toolbarActions).withActionCallback(actionCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(object), List.of(DisposalScheduleAction.REMOVE), List.of()));
  }
}
