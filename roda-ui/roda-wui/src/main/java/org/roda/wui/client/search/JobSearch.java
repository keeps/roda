/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.common.lists.IngestJobReportList;
import org.roda.wui.client.common.lists.JobList;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.user.client.ui.SimplePanel;

public class JobSearch extends SimplePanel {
  public JobSearch(String jobsListId, String jobReportsListId, Filter defaultJobFilter, Filter defaultJobReportFilter,
    boolean isIngest) {

    ListBuilder<Job> jobListBuilder = new ListBuilder<>(() -> new JobList(),
      new AsyncTableCellOptions<>(Job.class, jobsListId).withFilter(new Filter(defaultJobFilter)).withAutoUpdate(5000)
        .bindOpener());

    ListBuilder<IndexedReport> jobReportListBuilder = new ListBuilder<>(
      isIngest ? () -> new IngestJobReportList() : () -> new SimpleJobReportList(),
      new AsyncTableCellOptions<>(IndexedReport.class, jobReportsListId).withFilter(new Filter(defaultJobReportFilter))
        .withAutoUpdate(5000).bindOpener());

    SearchWrapper searchWrapper = new SearchWrapper(true).createListAndSearchPanel(jobListBuilder)
      .createListAndSearchPanel(jobReportListBuilder);

    setWidget(searchWrapper);
  }
}
