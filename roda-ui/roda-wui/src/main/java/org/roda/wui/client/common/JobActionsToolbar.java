/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.wui.client.common.actions.JobActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.ingest.process.ShowJob;

import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobActionsToolbar extends BrowseObjectActionsToolbar<IndexedJob> {
  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();

    JobActions jobActions = JobActions.get(ShowJob.RESOLVER);

    this.actions.add(new ActionableWidgetBuilder<IndexedJob>(jobActions).buildGroupedListWithObjects(
      new ActionableObject<>(object),
      List.of(JobActions.JobAction.STOP, JobActions.JobAction.INGEST_APPRAISAL, JobActions.JobAction.INGEST_PROCESS),
      List.of(JobActions.JobAction.STOP, JobActions.JobAction.INGEST_APPRAISAL, JobActions.JobAction.INGEST_PROCESS)));
  }
}
