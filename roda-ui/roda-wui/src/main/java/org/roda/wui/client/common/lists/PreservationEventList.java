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
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.metadata.IndexedObject;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
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
  private TextColumn<IndexedPreservationEvent> eventAgentColumn;
  private TextColumn<IndexedPreservationEvent> eventTypeColumn;
  private TextColumn<IndexedPreservationEvent> eventDetailColumn;
  private TextColumn<IndexedPreservationEvent> eventSourceObjectColumn;
  private TextColumn<IndexedPreservationEvent> eventOutcomeObjectColumn;
  private TextColumn<IndexedPreservationEvent> eventOutcomeColumn;

  public PreservationEventList() {
    this(null, null, null);
  }

  public PreservationEventList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedPreservationEvent> display) {

    eventDateTimeColumn = new Column<IndexedPreservationEvent, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT))) {
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

    eventAgentColumn = new TextColumn<IndexedPreservationEvent>() {

      @Override
      public String getValue(IndexedPreservationEvent event) {
        String ret = null;
        if (event != null) {
          if(event.getLinkingAgentIds()!=null){
            ret = event.getLinkingAgentIds().size()+ " agents";
          } else {
            ret = "none";
          }
        }
        return ret;
      }
    };

    eventSourceObjectColumn = new TextColumn<IndexedPreservationEvent>() {

      @Override
      public String getValue(IndexedPreservationEvent event) {
        String ret = null;
        // TODO define link
        //TODO handle outcome objects
        if (event != null) {
          if(event.getSourcesObjectIds()!=null){
            ret = event.getSourcesObjectIds().size()+ " objects";
          } else {
            ret = "none";
          }
          
        }

        return ret;
      }

    };

    eventOutcomeObjectColumn = new TextColumn<IndexedPreservationEvent>() {

      @Override
      public String getValue(IndexedPreservationEvent event) {
        String ret = null;
        // TODO define link
        if (event != null) {
          if(event.getOutcomeObjectIds()!=null){
            ret = event.getOutcomeObjectIds().size()+ " objects";
          } else {
            ret = "none";
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
    display.addColumn(eventDateTimeColumn, messages.preservationEventListHeaderDate());
    display.addColumn(eventAgentColumn, messages.preservationEventListHeaderAgent());
    display.addColumn(eventTypeColumn, messages.preservationEventListHeaderType());
    display.addColumn(eventDetailColumn, messages.preservationEventListHeaderDetail());
    display.addColumn(eventSourceObjectColumn, messages.preservationEventListHeaderSourceObject());
    display.addColumn(eventOutcomeObjectColumn, messages.preservationEventListHeaderOutcomeObject());
    display.addColumn(eventOutcomeColumn, messages.preservationEventListHeaderOutcome());

    // display.setColumnWidth(eventDetailColumn, "100%");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(eventDateTimeColumn, false));

    eventDateTimeColumn.setCellStyleNames("nowrap");
    eventAgentColumn.setCellStyleNames("nowrap");
    // eventTypeColumn.setCellStyleNames("nowrap");
    eventOutcomeColumn.setCellStyleNames("nowrap");
    eventSourceObjectColumn.setCellStyleNames("nowrap");
    eventOutcomeObjectColumn.setCellStyleNames("nowrap");
    eventAgentColumn.setCellStyleNames("nowrap");

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedPreservationEvent>> callback) {

    Filter filter = getFilter();

    Map<Column<IndexedPreservationEvent, ?>, String> columnSortingKeyMap = new HashMap<Column<IndexedPreservationEvent, ?>, String>();
    columnSortingKeyMap.put(eventDateTimeColumn, RodaConstants.PRESERVATION_EVENT_DATETIME);
    //TODO an event can now have multiple agents... sort by agent id should maybe be removed...
    columnSortingKeyMap.put(eventAgentColumn, RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER);
    columnSortingKeyMap.put(eventTypeColumn, RodaConstants.PRESERVATION_EVENT_TYPE);
    columnSortingKeyMap.put(eventDetailColumn, RodaConstants.PRESERVATION_EVENT_DETAIL);
    columnSortingKeyMap.put(eventOutcomeColumn, RodaConstants.PRESERVATION_EVENT_OUTCOME);

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
