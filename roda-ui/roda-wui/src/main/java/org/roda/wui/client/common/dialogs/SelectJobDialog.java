/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.wui.client.common.lists.JobList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;

public class SelectJobDialog extends DefaultSelectDialog<IndexedJob> {
  public SelectJobDialog(String title, Filter filter) {
    super(title, new ListBuilder<>(() -> new JobList(),
      new AsyncTableCellOptions<>(IndexedJob.class, "SelectJobDialog_jobs").withFilter(filter).withSummary(title)));
  }
}
