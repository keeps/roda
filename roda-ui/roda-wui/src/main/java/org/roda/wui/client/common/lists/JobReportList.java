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
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
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
public class JobReportList extends AsyncTableCell<Report> {

  private static final String STATUS_ERROR = "<i class='fa fa-exclamation-triangle error'></i>";

  private static final String STATUS_OK = "<i class='fa fa-check-circle'></i>";

  private static final String STATUS_RUNNING = "<i class='fa fa-cog fa-spin'></i>";

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
    this.pluginsInfo = pluginsInfo;
  }

  @Override
  protected void configureDisplay(CellTable<Report> display) {

    // objectIdColumn = new Column<Report, SafeHtml>(new SafeHtmlCell()) {
    // @Override
    // public SafeHtml getValue(Report Report) {
    // SafeHtml ret = null;
    // if (Report != null) {
    // SafeHtmlBuilder b = new SafeHtmlBuilder();
    // String objId = Report.getOtherId();
    // if (objId != null) {
    // b.append(SafeHtmlUtils.fromSafeConstant("<div
    // class='job-report-object-input'>"));
    // b.append(SafeHtmlUtils.fromString(objId));
    // b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    // }
    //
    // String aipId = Report.getItemId();
    // if (aipId != null) {
    // b.append(SafeHtmlUtils.fromSafeConstant("<div
    // class='job-report-object-output'>"));
    // b.append(SafeHtmlUtils.fromSafeConstant("<span
    // class='job-report-object-output-icon'>&#10551;</span>"));
    // b.append(SafeHtmlUtils.fromString(aipId));
    // b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    // }
    // ret = b.toSafeHtml();
    //
    // }
    // return ret;
    // }
    // };

    sourceObjectColumn = new TextColumn<Report>() {

      @Override
      public String getValue(Report report) {
        String value = "";
        if (report != null) {
          value = report.getOtherId();
        }

        return value;
      }
    };

    updatedDateColumn = new Column<Report, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
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
      public SafeHtml getValue(Report Report) {
        SafeHtml ret = null;
        if (Report != null) {

          switch (Report.getPluginState()) {
            case SUCCESS:
              ret = SafeHtmlUtils.fromSafeConstant(STATUS_OK);
              break;
            case RUNNING:
              ret = SafeHtmlUtils.fromSafeConstant(STATUS_RUNNING);
              break;
            case FAILURE:
            default:
              ret = SafeHtmlUtils.fromSafeConstant(STATUS_ERROR);
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
          value = report.getCompletionPercentage() + "% (" + report.getStepsCompleted() + "/" + report.getTotalSteps()
            + ")";
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
    display.addColumn(lastPluginRunStateColumn, SafeHtmlUtils.fromSafeConstant(STATUS_OK));
    display.addColumn(completionStatusColumn, "Completion status");

    display.setColumnWidth(sourceObjectColumn, "100%");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(updatedDateColumn, false));

    updatedDateColumn.setCellStyleNames("nowrap");
    lastPluginRunColumn.setCellStyleNames("nowrap");

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Report>> callback) {

    Filter filter = getFilter();

    Map<Column<Report, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Report, ?>, List<String>>();
    columnSortingKeyMap.put(sourceObjectColumn, Arrays.asList(RodaConstants.JOB_REPORT_OTHER_ID));
    columnSortingKeyMap.put(updatedDateColumn, Arrays.asList(RodaConstants.JOB_REPORT_DATE_UPDATE));
    columnSortingKeyMap.put(lastPluginRunColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN));
    columnSortingKeyMap.put(lastPluginRunStateColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN_STATE));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().find(Report.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }
}
