/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;

public class SelectReportDialog extends DefaultSelectDialog<IndexedReport> {
  public SelectReportDialog(String title, Filter filter) {
    super(title,
      new ListBuilder<>(SimpleJobReportList::new,
        new AsyncTableCell.Options<>(IndexedReport.class, "SelectReportDialog_simpleJobReports").withFilter(filter)
          .withSummary(title)));
  }
}
