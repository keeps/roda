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
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
public class JobReportList extends BasicAsyncTableCell<Report> {

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  // private Column<Report, SafeHtml> objectIdColumn;
  private TextColumn<Report> sourceObjectColumn;
  private Column<Report, Date> updatedDateColumn;
  private TextColumn<Report> lastPluginRunColumn;
  private Column<Report, SafeHtml> lastPluginRunStateColumn;
  private TextColumn<Report> completionStatusColumn;

  private final Map<String, PluginInfo> pluginsInfo;

  public JobReportList() {
    this(null, null, null, null, false);
  }

  public JobReportList(Filter filter, Facets facets, String summary, Map<String, PluginInfo> pluginsInfo,
    boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(Report.class);
    this.pluginsInfo = pluginsInfo;
  }

  @Override
  protected void configureDisplay(CellTable<Report> display) {

    sourceObjectColumn = new TextColumn<Report>() {

      @Override
      public String getValue(Report report) {
        String value = "";
        if (report != null) {
          value = report.getSourceObjectId();
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
            if (pluginInfo != null) {
              value = messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion());
            } else {
              value = job.getPlugin();
            }
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
                "<span class='label-success'>" + report.getPluginState().toString().toLowerCase() + "</span>");
              break;
            case RUNNING:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-default'>" + report.getPluginState().toString().toLowerCase() + "</span>");
              break;
            case FAILURE:
            default:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-danger'>" + report.getPluginState().toString().toLowerCase() + "</span>");
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
          value = report.getStepsCompleted() + " of " + report.getTotalSteps() + " (" + report.getCompletionPercentage()
            + "%)";
        }

        return value;
      }
    };

    sourceObjectColumn.setSortable(true);
    updatedDateColumn.setSortable(true);
    lastPluginRunColumn.setSortable(true);
    lastPluginRunStateColumn.setSortable(true);
    completionStatusColumn.setSortable(false);

    // TODO externalize strings into constants
    display.addColumn(sourceObjectColumn, "Submission Information Package");
    display.addColumn(updatedDateColumn, "Last updated at");
    display.addColumn(lastPluginRunColumn, "Last run task");
    display.addColumn(lastPluginRunStateColumn, "Status");
    display.addColumn(completionStatusColumn, "Progress");

    display.setColumnWidth(sourceObjectColumn, "100%");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(updatedDateColumn, false));

    updatedDateColumn.setCellStyleNames("nowrap");
    lastPluginRunColumn.setCellStyleNames("nowrap");
    completionStatusColumn.setCellStyleNames("nowrap");

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Report>> callback) {

    Filter filter = getFilter();

    Map<Column<Report, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Report, ?>, List<String>>();
    columnSortingKeyMap.put(sourceObjectColumn, Arrays.asList(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID));
    columnSortingKeyMap.put(updatedDateColumn, Arrays.asList(RodaConstants.JOB_REPORT_DATE_UPDATE));
    columnSortingKeyMap.put(lastPluginRunColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN));
    columnSortingKeyMap.put(lastPluginRunStateColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN_STATE));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().find(Report.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }
}
