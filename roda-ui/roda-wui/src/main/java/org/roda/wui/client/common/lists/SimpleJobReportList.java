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
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.TooltipTextColumn;
import org.roda.wui.client.common.utils.StringUtils;

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
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class SimpleJobReportList extends AsyncTableCell<IndexedReport> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TooltipTextColumn<IndexedReport> sourceColumn;
  private TooltipTextColumn<IndexedReport> outcomeColumn;
  private Column<IndexedReport, Date> updatedDateColumn;
  private TextColumn<IndexedReport> lastPluginRunColumn;
  private Column<IndexedReport, SafeHtml> lastPluginRunStateColumn;
  private TextColumn<IndexedReport> completionStatusColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.JOB_REPORT_ID, RodaConstants.JOB_REPORT_JOB_ID, RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS,
    RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID, RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS,
    RodaConstants.JOB_REPORT_SOURCE_OBJECT_LABEL, RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME,
    RodaConstants.JOB_REPORT_OUTCOME_OBJECT_LABEL, RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID,
    RodaConstants.JOB_REPORT_DATE_UPDATED, RodaConstants.JOB_REPORT_PLUGIN, RodaConstants.JOB_REPORT_PLUGIN_VERSION,
    RodaConstants.JOB_REPORT_PLUGIN_STATE, RodaConstants.JOB_REPORT_STEPS_COMPLETED,
    RodaConstants.JOB_REPORT_TOTAL_STEPS, RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE);

  private final Map<String, PluginInfo> pluginsInfo;

  public SimpleJobReportList() {
    super();
    this.pluginsInfo = Collections.emptyMap();
  }

  public SimpleJobReportList(Map<String, PluginInfo> pluginsInfo) {
    super();
    this.pluginsInfo = pluginsInfo;
  }

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedReport> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedReport> display) {

    sourceColumn = new TooltipTextColumn<IndexedReport>() {

      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          value = report.getSourceObjectOriginalIds().isEmpty() ? report.getSourceObjectId()
            : StringUtils.prettyPrint(report.getSourceObjectOriginalIds());
        }
        return value;
      }
    };

    outcomeColumn = new TooltipTextColumn<IndexedReport>() {

      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          if (StringUtils.isNotBlank(report.getOutcomeObjectLabel())) {
            value = report.getOutcomeObjectLabel() + " (" + report.getOutcomeObjectId() + ")";
          } else {
            value = report.getOutcomeObjectId();
          }
        }
        return value;
      }
    };

    updatedDateColumn = new Column<IndexedReport, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(IndexedReport job) {
        return job != null ? job.getDateUpdated() : null;
      }
    };

    lastPluginRunColumn = new TextColumn<IndexedReport>() {

      @Override
      public String getValue(IndexedReport job) {
        String value = null;
        if (job != null) {
          String jobPlugin = job.getPlugin();
          if (jobPlugin != null) {
            PluginInfo pluginInfo = pluginsInfo.get(jobPlugin);
            String pluginName;
            if (pluginInfo != null) {
              pluginName = pluginInfo.getName();
            } else {
              pluginName = jobPlugin;
            }

            if (StringUtils.isNotBlank(job.getPluginVersion())) {
              value = messages.pluginLabelWithVersion(pluginName, job.getPluginVersion());
            } else {
              value = messages.pluginLabel(pluginName);
            }
          }
        }

        return value;
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

    sourceColumn.setSortable(true);
    outcomeColumn.setSortable(true);
    updatedDateColumn.setSortable(true);
    lastPluginRunColumn.setSortable(true);
    lastPluginRunStateColumn.setSortable(true);
    completionStatusColumn.setSortable(false);

    addColumn(sourceColumn, messages.reportSource(), true, false);
    addColumn(outcomeColumn, messages.reportOutcome(), true, false);
    addColumn(updatedDateColumn, messages.reportLastUpdatedAt(), true, false, 11);
    addColumn(lastPluginRunColumn, messages.reportLastRunTask(), true, false);
    addColumn(lastPluginRunStateColumn, messages.reportStatus(), true, false, 8);
    addColumn(completionStatusColumn, messages.reportProgress(), true, false, 8);

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(updatedDateColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedReport, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(sourceColumn, Arrays.asList(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID));
    columnSortingKeyMap.put(outcomeColumn, Arrays.asList(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID));
    columnSortingKeyMap.put(updatedDateColumn, Arrays.asList(RodaConstants.JOB_REPORT_DATE_UPDATED));
    columnSortingKeyMap.put(lastPluginRunColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN));
    columnSortingKeyMap.put(lastPluginRunStateColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN_STATE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }
}
