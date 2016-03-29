/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.BrowseMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class JobList extends AsyncTableCell<Job> {

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private TextColumn<Job> nameColumn;
  private TextColumn<Job> usernameColumn;
  private Column<Job, Date> startDateColumn;
  private TextColumn<Job> statusColumn;
  private TextColumn<Job> objectsTotalCountColumn;
  private TextColumn<Job> objectsSuccessCountColumn;
  private TextColumn<Job> objectsFailureCountColumn;
  private TextColumn<Job> objectsWaitingCountColumn;

  public JobList() {
    this(null, null, null, false);
  }

  public JobList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<Job> display) {

    nameColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        return job != null ? job.getName() : null;
      }
    };

    usernameColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        return job != null ? job.getUsername() : null;
      }
    };

    startDateColumn = new Column<Job, Date>(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(Job job) {
        return job != null ? job.getStartDate() : null;
      }
    };

    statusColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        String ret = null;
        if (job != null) {
          JOB_STATE state = job.getState();
          if (JOB_STATE.COMPLETED.equals(state)) {
            ret = messages.showJobStatusCompleted(job.getEndDate());
          } else if (JOB_STATE.FAILED_DURING_CREATION.equals(state)) {
            ret = messages.showJobStatusFailedDuringCreation(job.getEndDate());
          } else if (JOB_STATE.CREATED.equals(state)) {
            ret = messages.showJobStatusCreated();
          } else if (JOB_STATE.STARTED.equals(state)) {
            ret = messages.showJobStatusStarted(job.getCompletionPercentage());
          } else {
            ret = state.toString();
          }
        }

        return ret;
      }
    };

    objectsTotalCountColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        String ret = null;
        if (job != null) {
          ret = job.getObjectsCount() + "";
        }
        return ret;
      }
    };

    objectsSuccessCountColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        String ret = null;
        if (job != null) {
          ret = job.getObjectsProcessedWithSuccess() + "";
        }
        return ret;
      }
    };

    objectsFailureCountColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        String ret = null;
        if (job != null) {
          ret = job.getObjectsProcessedWithFailure() + "";
        }
        return ret;
      }
    };

    objectsWaitingCountColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        String ret = null;
        if (job != null) {
          ret = job.getObjectsWaitingToBeProcessed() + "";
        }
        return ret;
      }
    };

    nameColumn.setSortable(true);
    usernameColumn.setSortable(true);
    startDateColumn.setSortable(true);
    statusColumn.setSortable(true);
    objectsTotalCountColumn.setSortable(true);
    objectsSuccessCountColumn.setSortable(true);
    objectsFailureCountColumn.setSortable(true);
    objectsWaitingCountColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(nameColumn, "Name");
    display.addColumn(usernameColumn, "Creator");
    display.addColumn(startDateColumn, "Start date");
    display.addColumn(objectsTotalCountColumn, "Total");
    display.addColumn(objectsSuccessCountColumn, "Success");
    display.addColumn(objectsFailureCountColumn, "Failure");
    display.addColumn(objectsWaitingCountColumn, "Waiting");
    display.addColumn(statusColumn, "Status");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(nameColumn, "100%");

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(startDateColumn, false));

    startDateColumn.setCellStyleNames("nowrap");
    statusColumn.setCellStyleNames("nowrap");
    usernameColumn.setCellStyleNames("nowrap");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Job>> callback) {

    Filter filter = getFilter();

    Map<Column<Job, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Job, ?>, List<String>>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.JOB_NAME));
    columnSortingKeyMap.put(startDateColumn, Arrays.asList(RodaConstants.JOB_START_DATE));
    columnSortingKeyMap.put(statusColumn, Arrays.asList(RodaConstants.JOB_COMPLETION_PERCENTAGE));
    columnSortingKeyMap.put(objectsTotalCountColumn, Arrays.asList(RodaConstants.JOB_OBJECTS_COUNT));
    columnSortingKeyMap.put(objectsSuccessCountColumn, Arrays.asList(RodaConstants.JOB_OBJECTS_PROCESSED_WITH_SUCCESS));
    columnSortingKeyMap.put(objectsFailureCountColumn, Arrays.asList(RodaConstants.JOB_OBJECTS_PROCESSED_WITH_FAILURE));
    columnSortingKeyMap.put(objectsWaitingCountColumn,
      Arrays.asList(RodaConstants.JOB_OBJECTS_WAITING_TO_BE_PROCESSED));
    columnSortingKeyMap.put(usernameColumn, Arrays.asList(RodaConstants.JOB_USERNAME));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().find(Job.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

}
