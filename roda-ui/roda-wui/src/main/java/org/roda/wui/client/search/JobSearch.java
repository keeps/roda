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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class JobSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, JobSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public JobSearch(String jobsListId, String jobReportsListId, Filter defaultJobFilter, Filter defaultJobReportFilter,
    boolean isIngest) {

    ListBuilder<Job> jobListBuilder = new ListBuilder<>(JobList::new,
      new AsyncTableCellOptions<>(Job.class, jobsListId).withFilter(new Filter(defaultJobFilter)).withAutoUpdate(10000)
        .bindOpener());

    ListBuilder<IndexedReport> jobReportListBuilder = new ListBuilder<>(
      isIngest ? IngestJobReportList::new : SimpleJobReportList::new,
      new AsyncTableCellOptions<>(IndexedReport.class, jobReportsListId).withFilter(new Filter(defaultJobReportFilter))
        .withAutoUpdate(10000).bindOpener());

    searchWrapper = new SearchWrapper(true).createListAndSearchPanel(jobListBuilder)
      .createListAndSearchPanel(jobReportListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    // TODO tmp: remove this if this is not used
    // searchWrapper.setDropdownLabel(messages.searchListBoxJobs());
    // searchWrapper.addDropdownItem(messages.searchListBoxJobs(),
    // RodaConstants.SEARCH_JOBS);
    // searchWrapper.addDropdownItem(messages.searchListBoxJobReports(),
    // RodaConstants.SEARCH_JOB_REPORTS);
  }
}
