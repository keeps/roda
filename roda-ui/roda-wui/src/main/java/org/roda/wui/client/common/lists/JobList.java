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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.TooltipTextColumn;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Humanize.DHMSFormat;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class JobList extends AsyncTableCell<Job> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TooltipTextColumn<Job> nameColumn;
  private TextColumn<Job> usernameColumn;
  private Column<Job, Date> startDateColumn;
  private TextColumn<Job> durationColumn;
  private Column<Job, SafeHtml> statusColumn;
  private TextColumn<Job> progressColumn;
  private TextColumn<Job> objectsTotalCountColumn;
  private Column<Job, SafeHtml> objectsSuccessCountColumn;
  private Column<Job, SafeHtml> objectsFailureCountColumn;


  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.JOB_NAME,
    RodaConstants.JOB_USERNAME, RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE, RodaConstants.JOB_STATE,
    RodaConstants.JOB_SOURCE_OBJECTS_COUNT, RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS,
    RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE, RodaConstants.JOB_COMPLETION_PERCENTAGE);

  @Override
  protected void adjustOptions(AsyncTableCellOptions<Job> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<Job> display) {
    nameColumn = new TooltipTextColumn<Job>() {

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

    startDateColumn = new Column<Job, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(Job job) {
        return job != null ? job.getStartDate() : null;
      }
    };

    durationColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        if (job == null) {
          return null;
        }
        Date end = job.getEndDate() != null ? job.getEndDate() : getDate();
        return Humanize.durationInDHMS(job.getStartDate(), end, DHMSFormat.SHORT);
      }
    };

    statusColumn = new Column<Job, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Job job) {
        return HtmlSnippetUtils.getJobStateHtml(job);
      }
    };

    objectsTotalCountColumn = new TextColumn<Job>() {

      @Override
      public String getValue(Job job) {
        String ret = "";
        if (job != null && job.getJobStats().getSourceObjectsCount() > 0) {
          ret = Integer.toString(job.getJobStats().getSourceObjectsCount());
        }
        return ret;
      }
    };

    objectsSuccessCountColumn = new Column<Job, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(Job job) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        if (job != null) {
          b.append(
            job.getJobStats().getSourceObjectsProcessedWithSuccess() > 0 ? SafeHtmlUtils.fromSafeConstant("<span>")
              : SafeHtmlUtils.fromSafeConstant("<span class='ingest-process-counter-0'>"));
          b.append(job.getJobStats().getSourceObjectsProcessedWithSuccess());
          b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
        }
        return b.toSafeHtml();
      }
    };

    objectsFailureCountColumn = new Column<Job, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Job job) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        if (job != null) {
          b.append(SafeHtmlUtils.fromSafeConstant("<span"));
          if (job.getJobStats().getSourceObjectsProcessedWithFailure() > 0) {
            b.append(SafeHtmlUtils.fromSafeConstant(" class='ingest-process-failed-column'"));
          } else {
            b.append(SafeHtmlUtils.fromSafeConstant(" class='ingest-process-counter-0'"));
          }
          b.append(SafeHtmlUtils.fromSafeConstant(">"));
          b.append(job.getJobStats().getSourceObjectsProcessedWithFailure());
          b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
        }
        return b.toSafeHtml();
      }
    };

    progressColumn = new TextColumn<Job>() {
      @Override
      public String getValue(Job job) {
        return job != null ? job.getJobStats().getCompletionPercentage() + "%" : null;
      }
    };

    nameColumn.setSortable(true);
    usernameColumn.setSortable(true);
    startDateColumn.setSortable(true);
    statusColumn.setSortable(true);
    objectsTotalCountColumn.setSortable(true);
    objectsSuccessCountColumn.setSortable(true);
    objectsFailureCountColumn.setSortable(true);
    progressColumn.setSortable(true);

    addColumn(nameColumn, messages.jobName(), true, false, 11);
    addColumn(usernameColumn, messages.jobCreator(), true, false, 6);
    addColumn(startDateColumn, messages.jobStartDate(), true, false, 11);
    addColumn(durationColumn, messages.jobDuration(), true, true, 6);
    addColumn(statusColumn, messages.jobStatus(), true, false, 7);
    addColumn(progressColumn, messages.jobProgress(), true, true);
    addColumn(objectsTotalCountColumn, messages.jobTotalCountMessage(), true, true, 5);
    addColumn(objectsSuccessCountColumn, messages.jobSuccessCountMessage(), true, true);
    addColumn(objectsFailureCountColumn, messages.jobFailureCountMessage(), true, true, 5);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(startDateColumn, false));

  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<Job, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.JOB_NAME));
    columnSortingKeyMap.put(startDateColumn, Arrays.asList(RodaConstants.JOB_START_DATE));
    columnSortingKeyMap.put(statusColumn, Arrays.asList(RodaConstants.JOB_STATE));
    columnSortingKeyMap.put(progressColumn, Arrays.asList(RodaConstants.JOB_COMPLETION_PERCENTAGE));
    columnSortingKeyMap.put(objectsTotalCountColumn, Arrays.asList(RodaConstants.JOB_SOURCE_OBJECTS_COUNT));
    columnSortingKeyMap.put(objectsSuccessCountColumn,
      Arrays.asList(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS));
    columnSortingKeyMap.put(objectsFailureCountColumn,
      Arrays.asList(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE));
    columnSortingKeyMap.put(usernameColumn, Arrays.asList(RodaConstants.JOB_USERNAME));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
