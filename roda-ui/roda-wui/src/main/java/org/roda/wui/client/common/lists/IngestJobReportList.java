/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.TooltipTextColumn;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
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
public class IngestJobReportList extends AsyncTableCell<IndexedReport> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TooltipTextColumn<IndexedReport> sourceObjectColumn;
  private TooltipTextColumn<IndexedReport> outcomeObjectColumn;
  private TooltipTextColumn<IndexedReport> jobNameColumn;
  private TooltipTextColumn<IndexedReport> pluginNameColumn;
  private Column<IndexedReport, Date> updatedDateColumn;
  private TooltipTextColumn<IndexedReport> lastPluginRunColumn;
  private Column<IndexedReport, SafeHtml> lastPluginRunStateColumn;
  private TextColumn<IndexedReport> completionStatusColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.JOB_REPORT_ID, RodaConstants.JOB_REPORT_TITLE, RodaConstants.JOB_REPORT_JOB_ID,
    RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS, RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID,
    RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS, RodaConstants.JOB_REPORT_SOURCE_OBJECT_LABEL,
    RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME, RodaConstants.JOB_REPORT_OUTCOME_OBJECT_LABEL,
    RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, RodaConstants.JOB_REPORT_DATE_UPDATED, RodaConstants.JOB_REPORT_PLUGIN,
    RodaConstants.JOB_REPORT_PLUGIN_NAME, RodaConstants.JOB_REPORT_PLUGIN_VERSION,
    RodaConstants.JOB_REPORT_PLUGIN_STATE, RodaConstants.JOB_REPORT_STEPS_COMPLETED,
    RodaConstants.JOB_REPORT_TOTAL_STEPS, RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE,
    RodaConstants.JOB_REPORT_UNSUCCESSFUL_PLUGINS);

  private boolean insideJob = false;
  private boolean jobRunning = false;
  private boolean jobComplex = false;

  public IngestJobReportList() {
  }

  public IngestJobReportList(boolean insideJob, boolean jobRunning, boolean jobComplex) {
    this.insideJob = insideJob;
    this.jobRunning = jobRunning;
    this.jobComplex = jobComplex;
  }

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedReport> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedReport> display) {

    sourceObjectColumn = new TooltipTextColumn<IndexedReport>() {
      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          if (report.getSourceObjectOriginalIds().isEmpty()) {
            value = report.getSourceObjectId();
          } else {
            value = StringUtils.prettyPrint(report.getSourceObjectOriginalIds());
          }

          value = report.getSourceObjectOriginalName() + " (" + value + ")";
        }

        return value;
      }
    };

    outcomeObjectColumn = new TooltipTextColumn<IndexedReport>() {
      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          if (StringUtils.isNotBlank(report.getOutcomeObjectLabel())) {
            value = report.getOutcomeObjectLabel() + " (" + report.getOutcomeObjectId() + ")";
          } else if (StringUtils.isNotBlank(report.getOutcomeObjectId())) {
            value = report.getOutcomeObjectId();
          }
        }

        return value;
      }
    };

    jobNameColumn = new TooltipTextColumn<IndexedReport>() {
      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          value = report.getTitle();
        }

        return value;
      }
    };

    pluginNameColumn = new TooltipTextColumn<IndexedReport>() {
      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          value = messages.pluginLabelWithVersion(report.getPluginName(), report.getPluginVersion());
        }

        return value;
      }
    };

    updatedDateColumn = new Column<IndexedReport, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(IndexedReport report) {
        return report != null ? report.getDateUpdated() : null;
      }
    };

    lastPluginRunColumn = new TooltipTextColumn<IndexedReport>() {
      @Override
      public String getValue(IndexedReport report) {
        if (report != null) {
          if (report.getLastRunPlugin() != null) {
            return messages.pluginLabelWithVersion(report.getLastRunPlugin().getPluginName(),
              report.getLastRunPlugin().getPluginVersion());
          }
        }
        return "";
      }
    };

    lastPluginRunStateColumn = new Column<IndexedReport, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedReport report) {
        SafeHtml ret = null;
        if (report != null) {
          switch (report.getPluginState()) {
            case SUCCESS:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-success'>" + messages.pluginStateMessage(PluginState.SUCCESS) + "</span>");
              break;
            case RUNNING:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-default'>" + messages.pluginStateMessage(PluginState.RUNNING) + "</span>");
              break;
            case PARTIAL_SUCCESS:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-warning'>" + messages.pluginStateMessage(PluginState.PARTIAL_SUCCESS) + "</span>");
              break;
            case FAILURE:
            default:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-danger'>" + messages.pluginStateMessage(PluginState.FAILURE) + "</span>");
              break;
          }
        }

        return ret;
      }
    };

    completionStatusColumn = new TextColumn<IndexedReport>() {
      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          value = report.getStepsCompleted() + " " + messages.of() + " " + report.getTotalSteps() + " ("
            + report.getCompletionPercentage() + "%)";
        }

        return value;
      }
    };

    sourceObjectColumn.setSortable(true);
    outcomeObjectColumn.setSortable(true);
    pluginNameColumn.setSortable(true);
    updatedDateColumn.setSortable(true);
    lastPluginRunStateColumn.setSortable(true);
    completionStatusColumn.setSortable(false);

    addColumn(sourceObjectColumn, messages.showSIPExtended(), true, false);
    addColumn(outcomeObjectColumn, messages.showAIPExtended(), true, false);
    if (!insideJob) {
      addColumn(jobNameColumn, messages.jobName(), true, false);
      addColumn(pluginNameColumn, messages.jobPlugin(), true, false);
    }
    addColumn(updatedDateColumn, messages.reportLastUpdatedAt(), true, false, 11);
    addColumn(lastPluginRunStateColumn, messages.reportStatus(), true, false, 8);
    if (jobComplex && jobRunning) {
      addColumn(completionStatusColumn, messages.reportProgress(), true, false, 8);
      if (insideJob) {
        addColumn(lastPluginRunColumn, messages.reportLastRunTask(), true, false, 10);
      }
    }

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(updatedDateColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedReport, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(sourceObjectColumn, Collections.singletonList(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID));
    columnSortingKeyMap.put(outcomeObjectColumn, Collections.singletonList(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID));
    columnSortingKeyMap.put(updatedDateColumn, Collections.singletonList(RodaConstants.JOB_REPORT_DATE_UPDATED));
    columnSortingKeyMap.put(pluginNameColumn, Collections.singletonList(RodaConstants.JOB_REPORT_PLUGIN_NAME));
    columnSortingKeyMap.put(jobNameColumn, Collections.singletonList(RodaConstants.JOB_REPORT_TITLE));
    columnSortingKeyMap.put(lastPluginRunStateColumn, Collections.singletonList(RodaConstants.JOB_REPORT_PLUGIN_STATE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
