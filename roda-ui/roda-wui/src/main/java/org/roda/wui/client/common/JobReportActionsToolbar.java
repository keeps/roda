/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.v2.jobs.IndexedReport;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobReportActionsToolbar extends BrowseObjectActionsToolbar<IndexedReport> {
  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();

    //RODAMemberToolbarActions rodaMemberActions = RODAMemberToolbarActions.get();
    //this.actions.add(new ActionableWidgetBuilder<RODAMember>(rodaMemberActions).withActionCallback(actionCallback)
     // .buildGroupedListWithObjects(new ActionableObject<>(object),
      //  List.of(RODAMemberAction.ACTIVATE, RODAMemberAction.DEACTIVATE, RODAMemberAction.REMOVE),
       // List.of()));
  }
}
