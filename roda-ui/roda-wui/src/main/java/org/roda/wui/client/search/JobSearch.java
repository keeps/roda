/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import java.util.HashMap;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.common.lists.IngestJobReportList;
import org.roda.wui.client.common.lists.JobList;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.search.AdvancedSearchFieldsPanel;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

public class JobSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, JobSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  Filter filterJobs;
  Filter filterJobReports;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  FlowPanel searchResultPanel;

  JobList jobsSearchResultPanel;
  BasicAsyncTableCell<IndexedReport> jobReportsSearchResultPanel;

  AdvancedSearchFieldsPanel jobsSearchAdvancedFieldsPanel;
  AdvancedSearchFieldsPanel jobReportsSearchAdvancedFieldsPanel;

  String selectedItem = Job.class.getName();
  String jobsListId;
  String jobReportsListId;

  boolean isIngest = false;

  public JobSearch(String jobsListId, String jobReportsListId, Filter defaultFilter, boolean isIngest) {
    this.jobsListId = jobsListId;
    this.jobReportsListId = jobReportsListId;
    this.isIngest = isIngest;

    ValueChangeHandler<Integer> searchAdvancedFieldsPanelHandler = new ValueChangeHandler<Integer>() {
      @Override
      public void onValueChange(ValueChangeEvent<Integer> event) {
        searchPanel.setSearchAdvancedGoEnabled(event.getValue() == 0 ? false : true);
      }
    };

    jobsSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_JOBS);
    jobReportsSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_JOB_REPORTS);

    jobsSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);
    jobReportsSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);

    defaultFilters(defaultFilter);

    searchPanel = new SearchPanel(filterJobs, RodaConstants.JOB_SEARCH, true, messages.searchPlaceHolder(), true, true,
      true);

    initWidget(uiBinder.createAndBindUi(this));

    searchPanel.setDropdownLabel(messages.searchListBoxJobs());
    searchPanel.addDropdownItem(messages.searchListBoxJobs(), RodaConstants.SEARCH_JOBS);
    searchPanel.addDropdownItem(messages.searchListBoxJobReports(), RodaConstants.SEARCH_JOB_REPORTS);
    search();

    searchPanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        search();
      }
    });

    searchPanel.addDropdownPopupStyleName("searchInputListBoxPopup");

    // handler aqui
    searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (selectedItem.equals(Report.class.getName())) {
          jobReportsSearchAdvancedFieldsPanel.addSearchFieldPanel();
        } else {
          jobsSearchAdvancedFieldsPanel.addSearchFieldPanel();
        }
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    if (jobsSearchResultPanel == null) {
      createJobsSearchResultPanel();
    }

    jobsSearchResultPanel.setVisible(true);

    searchPanel.setVariables(filterJobs, RodaConstants.JOB_SEARCH, true, jobsSearchResultPanel,
      jobsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(jobsSearchAdvancedFieldsPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(jobsSearchResultPanel);
    selectedItem = Job.class.getName();
  }

  public void showJobReportsSearchAdvancedFieldsPanel() {
    if (jobReportsSearchResultPanel == null) {
      createJobReportsSearchResultPanel();
    }

    jobReportsSearchResultPanel.setVisible(true);

    searchPanel.setVariables(filterJobReports, RodaConstants.JOB_REPORT_SEARCH, true, jobReportsSearchResultPanel,
      jobReportsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(jobReportsSearchAdvancedFieldsPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(jobReportsSearchResultPanel);
    selectedItem = Report.class.getName();
  }

  private void createJobsSearchResultPanel() {
    jobsSearchResultPanel = new JobList(jobsListId, filterJobs, messages.searchResults(), true);
    ListSelectionUtils.bindBrowseOpener(jobsSearchResultPanel);

    jobsSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Job job = jobsSearchResultPanel.getSelectionModel().getSelectedObject();
        if (job != null) {
          HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
        }
      }
    });

    jobsSearchResultPanel.autoUpdate(10000);
  }

  private void createJobReportsSearchResultPanel() {
    if (isIngest) {
      jobReportsSearchResultPanel = new IngestJobReportList(jobReportsListId, filterJobReports,
        messages.searchResults(), new HashMap<>(), false);
    } else {
      jobReportsSearchResultPanel = new SimpleJobReportList(jobReportsListId, filterJobReports,
        messages.searchResults(), false);
    }

    jobReportsSearchResultPanel.autoUpdate(10000);
    ListSelectionUtils.bindBrowseOpener(jobReportsSearchResultPanel);
  }

  public SelectedItems<? extends IsIndexed> getSelected() {
    SelectedItems<? extends IsIndexed> selected = null;

    if (jobsSearchResultPanel != null && jobsSearchResultPanel.hasElementsSelected()) {
      selected = jobsSearchResultPanel.getSelected();
    } else if (jobReportsSearchResultPanel != null && jobReportsSearchResultPanel.hasElementsSelected()) {
      selected = jobReportsSearchResultPanel.getSelected();
    }

    if (selected == null) {
      selected = new SelectedItemsList<>();
    }

    return selected;
  }

  public void refresh() {
    if (jobsSearchResultPanel != null && jobsSearchResultPanel.hasElementsSelected()) {
      jobsSearchResultPanel.refresh();
    } else if (jobReportsSearchResultPanel != null && jobReportsSearchResultPanel.hasElementsSelected()) {
      jobReportsSearchResultPanel.refresh();
    }
  }

  public void search() {
    if (searchPanel.getDropdownSelectedValue().equals(RodaConstants.SEARCH_JOB_REPORTS)) {
      showJobReportsSearchAdvancedFieldsPanel();
    } else {
      showSearchAdvancedFieldsPanel();
    }

    searchPanel.doSearch();
  }

  public void defaultFilters(Filter filter) {
    filterJobs = new Filter(filter);
    filterJobReports = new Filter(filter);
    if (searchPanel != null) {
      searchPanel.setDefaultFilterIncremental(false);
    }
  }

  public void setFilter(Filter filter) {
    jobsSearchResultPanel.setFilter(filter);
  }
}
