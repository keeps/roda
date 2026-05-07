/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.wui.client.common.actions.CreateJobToolbarActionable;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.process.JobCreationDataProvider;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateJobActionsToolbar extends BrowseObjectActionsToolbar<IndexedJob> {
  private JobCreationDataProvider dataProvider;

  public void setDataProvider(JobCreationDataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();

    CreateJobToolbarActionable createJobToolbarActionable = CreateJobToolbarActionable.get(dataProvider);

    this.actions.add(new ActionableWidgetBuilder<IndexedJob>(createJobToolbarActionable).buildGroupedListWithObjects(
      new ActionableObject<>(IndexedJob.class),
      List.of(CreateJobToolbarActionable.CreateJobAction.OBTAIN_COMMAND),
      List.of(CreateJobToolbarActionable.CreateJobAction.OBTAIN_COMMAND)));
  }
}
