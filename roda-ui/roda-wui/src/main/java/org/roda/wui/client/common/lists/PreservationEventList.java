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
import org.roda.wui.client.browse.BrowserService;

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

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private Column<IndexedPreservationEvent, Date> eventDateTimeColumn;
  private TextColumn<IndexedPreservationEvent> eventTypeColumn;
  private TextColumn<IndexedPreservationEvent> eventDetailColumn;
  private TextColumn<IndexedPreservationEvent> eventOutcomeColumn;
  private TextColumn<IndexedPreservationEvent> eventTargetType;
  private Column<IndexedPreservationEvent, SafeHtml> eventTarget;
  private Column<IndexedPreservationEvent, SafeHtml> eventAgentColumn;

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

    eventTypeColumn = new TextColumn<IndexedPreservationEvent>() {

      @Override
      public String getValue(IndexedPreservationEvent event) {
        return event != null ? event.getEventType() : null;
      }
    };

    eventDetailColumn = new TextColumn<IndexedPreservationEvent>() {

      @Override
      public String getValue(IndexedPreservationEvent event) {
        return event != null ? event.getEventDetail() : null;
      }
    };

    eventOutcomeColumn = new TextColumn<IndexedPreservationEvent>() {

      @Override
      public String getValue(IndexedPreservationEvent event) {
        return event != null ? event.getEventOutcome() : null;
      }
    };

    eventAgentColumn = new Column<IndexedPreservationEvent, SafeHtml>(new SafeHtmlCell()) {
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

    eventTargetType = new TextColumn<IndexedPreservationEvent>() {

      @Override
      public String getValue(IndexedPreservationEvent event) {
        String ret = null;
        if (event != null) {
          // TODO define link
          if (event.getFileId() != null) {
            ret = "File";
          } else if (event.getRepresentationId() != null) {
            ret = "Representation";
          } else {
            ret = "AIP";
          }
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
            ret = SafeHtmlUtils.fromSafeConstant("<a href='#browse/view/'>" + event.getFileId() + "</a>");
          } else if (event.getRepresentationId() != null) {
            ret = SafeHtmlUtils.fromSafeConstant("<a href='#browse/view/'>" + event.getRepresentationId() + "</a>");
          } else {
            ret = SafeHtmlUtils.fromSafeConstant("<a href='#browse/'>" + event.getAipId() + "</a>");
          }
        }
        return ret;
      }
    };

    eventDateTimeColumn.setSortable(true);
    eventTypeColumn.setSortable(true);
    eventDetailColumn.setSortable(true);
    eventOutcomeColumn.setSortable(true);
    eventAgentColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(eventDateTimeColumn, "Date");
    display.addColumn(eventAgentColumn, "Agent");
    display.addColumn(eventTypeColumn, "Type");
    display.addColumn(eventDetailColumn, "Detail");
    display.addColumn(eventTargetType, "Target type");
    display.addColumn(eventTarget, "Target object");
    display.addColumn(eventOutcomeColumn, "Outcome");

    display.setColumnWidth(eventDetailColumn, "100%");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(eventDateTimeColumn, false));

    eventDateTimeColumn.setCellStyleNames("nowrap");
    eventTypeColumn.setCellStyleNames("nowrap");
    eventOutcomeColumn.setCellStyleNames("nowrap");
    eventTarget.setCellStyleNames("nowrap");
    eventAgentColumn.setCellStyleNames("nowrap");

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedPreservationEvent>> callback) {

    Filter filter = getFilter();

    Map<Column<IndexedPreservationEvent, ?>, String> columnSortingKeyMap = new HashMap<Column<IndexedPreservationEvent, ?>, String>();
    columnSortingKeyMap.put(eventDateTimeColumn, RodaConstants.PRESERVATION_EVENT_DATETIME);
    columnSortingKeyMap.put(eventTypeColumn, RodaConstants.PRESERVATION_EVENT_TYPE);
    columnSortingKeyMap.put(eventDetailColumn, RodaConstants.PRESERVATION_EVENT_DETAIL);
    columnSortingKeyMap.put(eventOutcomeColumn, RodaConstants.PRESERVATION_EVENT_OUTCOME);
    // TODO add agent
    // columnSortingKeyMap.put(eventAgent,
    // RodaConstants.PRESERVATION_EVENT_AGENT);

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().findIndexedPreservationEvent(filter, sorter, sublist, getFacets(), callback);
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
