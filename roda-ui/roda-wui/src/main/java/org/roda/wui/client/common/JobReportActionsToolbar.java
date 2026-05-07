/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.client.common.actions.JobReportAction;
import org.roda.wui.client.common.actions.JobReportToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import java.util.List;

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

    JobReportToolbarActions jobReportActions = JobReportToolbarActions.get();

    this.actions.add(new ActionableWidgetBuilder<IndexedReport>(jobReportActions).buildGroupedListWithObjects(
      new ActionableObject<>(object), List.of(),
      List.of(JobReportAction.BROWSE_SOURCE, JobReportAction.BROWSE_OUTCOME)));
  }
}
