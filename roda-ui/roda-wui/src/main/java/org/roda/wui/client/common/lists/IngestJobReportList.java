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
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
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
public class IngestJobReportList extends BasicAsyncTableCell<Report> {

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // private Column<Report, SafeHtml> objectIdColumn;
  private TooltipTextColumn<Report> sourceObjectColumn;
  private TooltipTextColumn<Report> outcomeObjectColumn;
  private Column<Report, Date> updatedDateColumn;
  private TextColumn<Report> lastPluginRunColumn;
  private Column<Report, SafeHtml> lastPluginRunStateColumn;
  private TextColumn<Report> completionStatusColumn;

  private final Map<String, PluginInfo> pluginsInfo;

  public IngestJobReportList() {
    this(null, null, null, new HashMap<String, PluginInfo>(), false);
  }

  public IngestJobReportList(Filter filter, Facets facets, String summary, Map<String, PluginInfo> pluginsInfo,
    boolean selectable) {
    super(Report.class, filter, facets, summary, selectable);
    this.pluginsInfo = pluginsInfo;
  }

  @Override
  protected void configureDisplay(CellTable<Report> display) {

    sourceObjectColumn = new TooltipTextColumn<Report>() {

      @Override
      public String getValue(Report report) {
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

    outcomeObjectColumn = new TooltipTextColumn<Report>() {

      @Override
      public String getValue(Report report) {
        String value = "";
        if (report != null) {
          value = report.getOutcomeObjectId();
        }

        return value;
      }
    };

    updatedDateColumn = new Column<Report, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(Report job) {
        return job != null ? job.getDateUpdated() : null;
      }
    };

    lastPluginRunColumn = new TextColumn<Report>() {

      @Override
      public String getValue(Report job) {
        String value = null;
        if (job != null) {
          if (job.getPlugin() != null) {
            PluginInfo pluginInfo = pluginsInfo.get(job.getPlugin());
            String pluginName;
            if (pluginInfo != null) {
              pluginName = pluginInfo.getName();
            } else {
              pluginName = job.getPlugin();
            }
            value = messages.pluginLabel(pluginName, job.getPluginVersion());
          }
        }

        return value;
      }
    };

    lastPluginRunStateColumn = new Column<Report, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Report report) {
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

    completionStatusColumn = new TextColumn<Report>() {

      @Override
      public String getValue(Report report) {
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
    updatedDateColumn.setSortable(true);
    lastPluginRunColumn.setSortable(true);
    lastPluginRunStateColumn.setSortable(true);
    completionStatusColumn.setSortable(false);

    // TODO externalize strings into constants
    addColumn(sourceObjectColumn, messages.showSIPExtended(), true, false);
    addColumn(outcomeObjectColumn, messages.showAIPExtended(), true, false);
    addColumn(updatedDateColumn, messages.reportLastUpdatedAt(), true, false, 11);
    addColumn(lastPluginRunColumn, messages.reportLastRunTask(), true, false);
    addColumn(lastPluginRunStateColumn, messages.reportStatus(), true, false, 8);
    addColumn(completionStatusColumn, messages.reportProgress(), true, false, 8);

    // display.setColumnWidth(sourceObjectColumn, "100%");

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(updatedDateColumn, false));

  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<Report, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Report, ?>, List<String>>();
    columnSortingKeyMap.put(sourceObjectColumn, Arrays.asList(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID));
    columnSortingKeyMap.put(outcomeObjectColumn, Arrays.asList(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID));
    columnSortingKeyMap.put(updatedDateColumn, Arrays.asList(RodaConstants.JOB_REPORT_DATE_UPDATE));
    columnSortingKeyMap.put(lastPluginRunColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN));
    columnSortingKeyMap.put(lastPluginRunStateColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN_STATE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
