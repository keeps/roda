/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.JobReport;
import org.roda.core.data.v2.JobReport.PluginState;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.BrowseMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class JobReportList extends AsyncTableCell<JobReport> {

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private TextColumn<JobReport> objectIdColumn;
  private TextColumn<JobReport> aipIdColumn;
  private Column<JobReport, Date> createdDateColumn;
  private Column<JobReport, Date> updatedDateColumn;
  private TextColumn<JobReport> lastPluginRunColumn;
  private TextColumn<JobReport> lastPluginRunStateColumn;

  public JobReportList() {
    this(null, null, null);
  }

  public JobReportList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
  }

  @Override
  protected void configureDisplay(CellTable<JobReport> display) {

    objectIdColumn = new TextColumn<JobReport>() {

      @Override
      public String getValue(JobReport job) {
        return job != null ? job.getObjectId() : null;
      }
    };

    aipIdColumn = new TextColumn<JobReport>() {

      @Override
      public String getValue(JobReport job) {
        return job != null ? job.getAipId() : null;
      }
    };

    createdDateColumn = new Column<JobReport, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(JobReport job) {
        return job != null ? job.getDateCreated() : null;
      }
    };

    updatedDateColumn = new Column<JobReport, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(JobReport job) {
        return job != null ? job.getDateUpdated() : null;
      }
    };

    lastPluginRunColumn = new TextColumn<JobReport>() {

      @Override
      public String getValue(JobReport job) {
        // TODO get label from server
        return job != null ? job.getLastPluginRan() : null;
      }
    };

    lastPluginRunStateColumn = new TextColumn<JobReport>() {

      @Override
      public String getValue(JobReport job) {
        // TODO make better label from messages
        PluginState pluginState = job.getLastPluginRanState();

        return job != null ? pluginState.toString() : null;
      }
    };

    objectIdColumn.setSortable(true);
    aipIdColumn.setSortable(true);
    createdDateColumn.setSortable(true);
    updatedDateColumn.setSortable(true);
    lastPluginRunColumn.setSortable(true);
    lastPluginRunStateColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(objectIdColumn, "SIP");
    display.addColumn(aipIdColumn, "AIP");
    display.addColumn(createdDateColumn, "Created at");
    display.addColumn(updatedDateColumn, "Last updated at");
    display.addColumn(lastPluginRunColumn, "Last run task");
    display.addColumn(lastPluginRunStateColumn, "Last run task status");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(updatedDateColumn, false));

    objectIdColumn.setCellStyleNames("nowrap");
    aipIdColumn.setCellStyleNames("nowrap");
    createdDateColumn.setCellStyleNames("nowrap");
    updatedDateColumn.setCellStyleNames("nowrap");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<JobReport>> callback) {

    Filter filter = getFilter();

    Map<Column<JobReport, ?>, String> columnSortingKeyMap = new HashMap<Column<JobReport, ?>, String>();
    columnSortingKeyMap.put(objectIdColumn, RodaConstants.JOB_REPORT_OBJECT_ID);
    columnSortingKeyMap.put(aipIdColumn, RodaConstants.JOB_REPORT_AIP_ID);
    columnSortingKeyMap.put(createdDateColumn, RodaConstants.JOB_REPORT_DATE_CREATED);
    columnSortingKeyMap.put(updatedDateColumn, RodaConstants.JOB_REPORT_DATE_UPDATE);
    columnSortingKeyMap.put(lastPluginRunColumn, RodaConstants.JOB_REPORT_LAST_PLUGIN_RAN);
    columnSortingKeyMap.put(lastPluginRunStateColumn, RodaConstants.JOB_REPORT_LAST_PLUGIN_RAN_STATE);

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().findJobReports(filter, sorter, sublist, getFacets(), callback);
  }

  @Override
  protected ProvidesKey<JobReport> getKeyProvider() {
    return new ProvidesKey<JobReport>() {

      @Override
      public Object getKey(JobReport item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
