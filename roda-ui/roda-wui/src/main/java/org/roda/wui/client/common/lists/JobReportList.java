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

import org.apache.poi.ss.formula.functions.T;
import org.roda.core.data.PluginInfo;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.JobReport;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.BrowseMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class JobReportList extends AsyncTableCell<JobReport> {

  private static final String STATUS_ERROR = "<i class='fa fa-exclamation-triangle'></i>";

  private static final String STATUS_OK = "<i class='fa fa-check-circle'></i>";

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private Column<JobReport, SafeHtml> objectIdColumn;
  private Column<JobReport, Date> updatedDateColumn;
  private TextColumn<JobReport> lastPluginRunColumn;
  private Column<JobReport, SafeHtml> lastPluginRunStateColumn;

  private final Map<String, PluginInfo> pluginsInfo;

  public JobReportList() {
    this(null, null, null, null);
  }

  public JobReportList(Filter filter, Facets facets, String summary, Map<String, PluginInfo> pluginsInfo) {
    super(filter, facets, summary);
    this.pluginsInfo = pluginsInfo;
  }

  @Override
  protected void configureDisplay(CellTable<JobReport> display) {

    objectIdColumn = new Column<JobReport, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(JobReport jobReport) {
        SafeHtml ret = null;
        if (jobReport != null) {
          SafeHtmlBuilder b = new SafeHtmlBuilder();
          String objId = jobReport.getObjectId();
          if (objId != null) {
            // TODO externalise to messages
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='job-report-object-input'>"));
            b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-sign-in'></i>"));
            // TODO escape URI
            b.append(SafeHtmlUtils.fromSafeConstant("<a href='#ingest/transfer/" + objId + "'>"));
            b.append(SafeHtmlUtils.fromString(objId));
            b.append(SafeHtmlUtils.fromSafeConstant("</a>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
          }

          String aipId = jobReport.getAipId();
          if (aipId != null) {
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='job-report-object-output'>"));
            b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-sign-out'></i>"));
            String aipBrowseLink = Tools.createHistoryHashLink(Browse.getViewItemHistoryToken(jobReport.getAipId()));
            b.append(SafeHtmlUtils.fromSafeConstant("<a href='" + aipBrowseLink + "'>"));
            b.append(SafeHtmlUtils.fromString(aipId));
            b.append(SafeHtmlUtils.fromSafeConstant("</a>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
          }
          ret = b.toSafeHtml();

        }
        return ret;
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
        String value = null;
        if (job != null) {
          if (job.getLastPluginRan() != null) {
            PluginInfo pluginInfo = pluginsInfo.get(job.getLastPluginRan());
            if (pluginInfo != null) {
              value = messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion());
            } else {
              value = job.getLastPluginRan();
            }
          }
        }

        return value;
      }
    };

    lastPluginRunStateColumn = new Column<JobReport, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(JobReport jobReport) {
        SafeHtml ret = null;
        if (jobReport != null) {

          switch (jobReport.getLastPluginRanState()) {
            case OK:
              ret = SafeHtmlUtils.fromSafeConstant(STATUS_OK);
              break;
            case ERROR:
            default:
              ret = SafeHtmlUtils.fromSafeConstant(STATUS_ERROR);
              break;
          }
        }
        return ret;
      }
    };

    objectIdColumn.setSortable(true);
    updatedDateColumn.setSortable(true);
    lastPluginRunColumn.setSortable(true);
    lastPluginRunStateColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(objectIdColumn, "Information Packages");
    display.addColumn(updatedDateColumn, "Last updated at");
    display.addColumn(lastPluginRunColumn, "Last run task");
    display.addColumn(lastPluginRunStateColumn, SafeHtmlUtils.fromSafeConstant(STATUS_OK));

    display.setColumnWidth(objectIdColumn, "100%");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(updatedDateColumn, false));

    updatedDateColumn.setCellStyleNames("nowrap");
    lastPluginRunColumn.setCellStyleNames("nowrap");

    display.setRowStyles(new RowStyles<JobReport>() {
      @Override
      public String getStyleNames(JobReport jobReport, int rowIndex) {
        String ret;
        switch (jobReport.getLastPluginRanState()) {
          case OK:
            ret = "row_ok";
            break;
          case ERROR:
          default:
            ret = "row_error";
            break;
        }
        return ret;
      }
    });

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<JobReport>> callback) {

    Filter filter = getFilter();

    Map<Column<JobReport, ?>, String> columnSortingKeyMap = new HashMap<Column<JobReport, ?>, String>();
    columnSortingKeyMap.put(objectIdColumn, RodaConstants.JOB_REPORT_OBJECT_ID);
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

  @Override
  protected CellPreviewEvent.Handler<JobReport> getSelectionEventManager() {
    return DefaultSelectionEventManager.<JobReport> createBlacklistManager(0);
  }

}
