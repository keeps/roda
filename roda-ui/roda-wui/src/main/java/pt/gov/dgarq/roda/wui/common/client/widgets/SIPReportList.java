package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.IngestListConstants;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.ingest.list.client.IngestListService;

public class SIPReportList extends AsyncTableCell<SIPReport> {

  private static final int PAGE_SIZE = 20;

  private static IngestListConstants constants = GWT.create(IngestListConstants.class);

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  // private TextColumn<SIPReport> idColumn;
  private TextColumn<SIPReport> originalFilenameColumn;
  private Column<SIPReport, Date> submissionDateColumn;
  private TextColumn<SIPReport> currentStateColumn;
  private TextColumn<SIPReport> percentageColumn;
  private TextColumn<SIPReport> producerColumn;

  public SIPReportList() {
    this(null, null, null);
  }

  public SIPReportList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
  }

  @Override
  protected void configureDisplay(CellTable<SIPReport> display) {
    // idColumn = new TextColumn<SIPReport>() {
    //
    // @Override
    // public String getValue(SIPReport sip) {
    // return sip != null ? sip.getId() : null;
    // }
    // };

    originalFilenameColumn = new TextColumn<SIPReport>() {

      @Override
      public String getValue(SIPReport sip) {
        return sip != null ? sip.getOriginalFilename() : null;
      }
    };

    submissionDateColumn = new Column<SIPReport, Date>(
      new DateCell(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS"))) {
      @Override
      public Date getValue(SIPReport sip) {
        return sip != null ? sip.getDatetime() : null;
      }
    };
    currentStateColumn = new TextColumn<SIPReport>() {

      @Override
      public String getValue(SIPReport sip) {
        return sip != null ? sip.getState() : null;
      }
    };
    percentageColumn = new TextColumn<SIPReport>() {

      @Override
      public String getValue(SIPReport sip) {
        return sip != null ? NumberFormat.getPercentFormat().format(sip.getCompletePercentage()) : null;
      }
    };
    producerColumn = new TextColumn<SIPReport>() {

      @Override
      public String getValue(SIPReport sip) {
        return sip != null ? sip.getUsername() : null;
      }
    };

    // idColumn.setSortable(true);
    originalFilenameColumn.setSortable(true);
    submissionDateColumn.setSortable(true);
    currentStateColumn.setSortable(true);
    percentageColumn.setSortable(true);
    producerColumn.setSortable(true);

    // TODO externalize strings into constants
    // display.addColumn(idColumn, "Id");
    display.addColumn(originalFilenameColumn, constants.headerFilename());
    display.addColumn(submissionDateColumn, constants.headerStartDate());
    display.addColumn(currentStateColumn, constants.headerState());
    display.addColumn(percentageColumn, constants.headerPercentage());
    display.addColumn(producerColumn, constants.headerProducer());

    // display.setAutoHeaderRefreshDisabled(true);
    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(originalFilenameColumn, "100%");

    addStyleName("my-list-SIPReport");
    emptyInfo.addStyleName("my-list-SIPReport-empty-info");

    // idColumn.setCellStyleNames("nowrap");

    submissionDateColumn.setCellStyleNames("nowrap my-collections-table-cell-alignright");
    currentStateColumn.setCellStyleNames("nowrap my-collections-table-cell-alignright");
    percentageColumn.setCellStyleNames("nowrap my-collections-table-cell-alignright");
    producerColumn.setCellStyleNames("nowrap my-collections-table-cell-alignright");
  }

  @Override
  protected void getData(int start, int length, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<SIPReport>> callback) {

    Filter filter = getFilter();

    // calculate sorter
    Sorter sorter = new Sorter();
    for (int i = 0; i < columnSortList.size(); i++) {
      ColumnSortInfo columnSortInfo = columnSortList.get(i);
      String sortParameterKey;
      // if (columnSortInfo.getColumn().equals(idColumn)) {
      // sortParameterKey = RodaConstants.SIP_REPORT_ID;
      // } else
      if (columnSortInfo.getColumn().equals(originalFilenameColumn)) {
        sortParameterKey = RodaConstants.SIP_REPORT_ORIGINAL_FILENAME;
      } else if (columnSortInfo.getColumn().equals(submissionDateColumn)) {
        sortParameterKey = RodaConstants.SIP_REPORT_DATETIME;
      } else if (columnSortInfo.getColumn().equals(currentStateColumn)) {
        sortParameterKey = RodaConstants.SIP_REPORT_STATE;
      } else if (columnSortInfo.getColumn().equals(percentageColumn)) {
        sortParameterKey = RodaConstants.SIP_REPORT_COMPLETE_PERCENTAGE;
      } else if (columnSortInfo.getColumn().equals(producerColumn)) {
        sortParameterKey = RodaConstants.SIP_REPORT_USERNAME;
      } else {
        sortParameterKey = null;
      }

      if (sortParameterKey != null) {
        sorter.add(new SortParameter(sortParameterKey, !columnSortInfo.isAscending()));
      } else {
        logger.warn("Selecting a sorter that is not mapped");
      }
    }

    // define sublist
    Sublist sublist = new Sublist(start, length);

    GWT.log("Requesting ingest getSIPs");

    IngestListService.Util.getInstance().findSipReports(filter, sorter, sublist, getFacets(), callback);

  }

  @Override
  protected ProvidesKey<SIPReport> getKeyProvider() {
    return new ProvidesKey<SIPReport>() {

      @Override
      public Object getKey(SIPReport item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
