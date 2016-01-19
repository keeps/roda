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
import org.roda.core.data.v2.IndexedPreservationEvent;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
public class PreservationEventList extends AsyncTableCell<IndexedPreservationEvent> {

  private static final String OUTCOME_SUCESS = "<i class='fa fa-check-circle'></i>";

  private static final String OUTCOME_FAILURE = "<i class='fa fa-exclamation-triangle error'></i>";

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private Column<IndexedPreservationEvent, Date> eventDateTimeColumn;
  private TextColumn<IndexedPreservationEvent> eventDetail;
  private Column<IndexedPreservationEvent, SafeHtml> eventOutcome;
  private Column<IndexedPreservationEvent, SafeHtml> eventTarget;
  private Column<IndexedPreservationEvent, SafeHtml> eventAgent;

  public PreservationEventList() {
    this(null, null, null);
  }

  public PreservationEventList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedPreservationEvent> display) {

    eventDateTimeColumn = new Column<IndexedPreservationEvent, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(IndexedPreservationEvent event) {
        return event != null ? event.getEventDateTime() : null;
      }
    };

    eventDetail = new TextColumn<IndexedPreservationEvent>() {

      @Override
      public String getValue(IndexedPreservationEvent event) {
        return event != null ? event.getEventDetail() : null;
      }
    };

    eventOutcome = new Column<IndexedPreservationEvent, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedPreservationEvent event) {
        SafeHtml ret = null;
        if (event != null) {

          if (event.getEventOutcome().equalsIgnoreCase("success")) {
            ret = SafeHtmlUtils.fromSafeConstant(OUTCOME_SUCESS);
          } else if (event.getEventOutcome().equalsIgnoreCase("failure")) {
            ret = SafeHtmlUtils.fromSafeConstant(OUTCOME_FAILURE);
          } else {
            ret = SafeHtmlUtils.fromString(event.getEventOutcome());
          }
        }
        return ret;
      }
    };

    eventAgent = new Column<IndexedPreservationEvent, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedPreservationEvent event) {
        SafeHtml ret = null;
        if (event != null) {
          // TODO add agent
          // TODO add link
          ret = SafeHtmlUtils.fromSafeConstant("<a href='#agent/1'>agent1</a>");
        }
        return ret;
      }
    };

    eventTarget = new Column<IndexedPreservationEvent, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedPreservationEvent event) {
        SafeHtml ret = null;
        if (event != null) {
          // TODO define link
          if (event.getFileId() != null) {
            ret = SafeHtmlUtils.fromString(event.getFileId());
          } else if (event.getRepresentationId() != null) {
            ret = SafeHtmlUtils.fromString(event.getRepresentationId());
          } else {
            ret = SafeHtmlUtils.fromString(event.getAipId());
          }
        }
        return ret;
      }
    };

    eventDateTimeColumn.setSortable(true);
    eventDetail.setSortable(true);
    eventOutcome.setSortable(true);
    eventAgent.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(eventDateTimeColumn, "Date");
    display.addColumn(eventDetail, "Detail");
    display.addColumn(eventOutcome, "Outcome");
    display.addColumn(eventTarget, "Target");
    display.addColumn(eventAgent, "Agent");

    display.setColumnWidth(eventDetail, "100%");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(eventDateTimeColumn, false));

    eventDateTimeColumn.setCellStyleNames("nowrap");
    eventOutcome.setCellStyleNames("nowrap");
    eventTarget.setCellStyleNames("nowrap");
    eventAgent.setCellStyleNames("nowrap");

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedPreservationEvent>> callback) {

    Filter filter = getFilter();

    Map<Column<IndexedPreservationEvent, ?>, String> columnSortingKeyMap = new HashMap<Column<IndexedPreservationEvent, ?>, String>();
    columnSortingKeyMap.put(eventDateTimeColumn, RodaConstants.PRESERVATION_EVENT_DATETIME);
    columnSortingKeyMap.put(eventDetail, RodaConstants.PRESERVATION_EVENT_DETAIL);
    columnSortingKeyMap.put(eventOutcome, RodaConstants.PRESERVATION_EVENT_OUTCOME);
    // TODO add agent
    // columnSortingKeyMap.put(eventAgent,
    // RodaConstants.PRESERVATION_EVENT_AGENT);

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    // BrowserService.Util.getInstance().findJobReports(filter, sorter, sublist,
    // getFacets(), callback);
    // TODO call service
  }

  @Override
  protected ProvidesKey<IndexedPreservationEvent> getKeyProvider() {
    return new ProvidesKey<IndexedPreservationEvent>() {

      @Override
      public Object getKey(IndexedPreservationEvent event) {
        return event.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
