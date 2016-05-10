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
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Report.PluginState;
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
public class PreservationEventList extends BasicAsyncTableCell<IndexedPreservationEvent> {

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private Column<IndexedPreservationEvent, Date> eventDateTimeColumn;
  private TextColumn<IndexedPreservationEvent> eventTypeColumn;
  private TextColumn<IndexedPreservationEvent> eventDetailColumn;

  private Column<IndexedPreservationEvent, SafeHtml> eventOutcomeColumn;

  public PreservationEventList() {
    this(null, null, null, false);
  }

  public PreservationEventList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(IndexedPreservationEvent.class);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedPreservationEvent> display) {

    eventDateTimeColumn = new Column<IndexedPreservationEvent, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
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

    eventOutcomeColumn = new Column<IndexedPreservationEvent, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedPreservationEvent event) {
        SafeHtml ret = null;
        if (event != null) {
          String outcome = event.getEventOutcome();
          if (PluginState.SUCCESS.toString().equalsIgnoreCase(outcome)) {
            ret = SafeHtmlUtils.fromSafeConstant("<span class='label-success'>" + outcome.toLowerCase() + "</span>");
          } else if (PluginState.FAILURE.toString().equalsIgnoreCase(outcome)) {
            ret = SafeHtmlUtils.fromSafeConstant("<span class='label-danger'>" + outcome.toLowerCase() + "</span>");
          } else {
            ret = SafeHtmlUtils.fromString(outcome);
          }
        }
        return ret;
      }
    };

    eventDateTimeColumn.setSortable(true);
    eventTypeColumn.setSortable(true);
    eventDetailColumn.setSortable(true);
    eventOutcomeColumn.setSortable(true);
    // eventAgentColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(eventDateTimeColumn, messages.preservationEventListHeaderDate());
    // display.addColumn(eventAgentColumn,
    // messages.preservationEventListHeaderAgent());
    display.addColumn(eventTypeColumn, messages.preservationEventListHeaderType());
    display.addColumn(eventDetailColumn, messages.preservationEventListHeaderDetail());
    // display.addColumn(eventSourceObjectColumn,
    // messages.preservationEventListHeaderSourceObject());
    // display.addColumn(eventOutcomeObjectColumn,
    // messages.preservationEventListHeaderOutcomeObject());
    display.addColumn(eventOutcomeColumn, messages.preservationEventListHeaderOutcome());

    // display.setColumnWidth(eventDetailColumn, "100%");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(eventDateTimeColumn, false));

    eventDateTimeColumn.setCellStyleNames("nowrap");
    // eventAgentColumn.setCellStyleNames("nowrap");
    // eventTypeColumn.setCellStyleNames("nowrap");
    eventOutcomeColumn.setCellStyleNames("nowrap");
    // eventSourceObjectColumn.setCellStyleNames("nowrap");
    // eventOutcomeObjectColumn.setCellStyleNames("nowrap");
    // eventAgentColumn.setCellStyleNames("nowrap");

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedPreservationEvent>> callback) {

    Filter filter = getFilter();

    Map<Column<IndexedPreservationEvent, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<IndexedPreservationEvent, ?>, List<String>>();
    columnSortingKeyMap.put(eventDateTimeColumn, Arrays.asList(RodaConstants.PRESERVATION_EVENT_DATETIME));
    // TODO an event can now have multiple agents... sort by agent id should
    // maybe be removed...
    // columnSortingKeyMap.put(eventAgentColumn,
    // RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER);
    columnSortingKeyMap.put(eventTypeColumn, Arrays.asList(RodaConstants.PRESERVATION_EVENT_TYPE));
    columnSortingKeyMap.put(eventDetailColumn, Arrays.asList(RodaConstants.PRESERVATION_EVENT_DETAIL));
    columnSortingKeyMap.put(eventOutcomeColumn, Arrays.asList(RodaConstants.PRESERVATION_EVENT_OUTCOME));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    boolean showInactive = true;
    BrowserService.Util.getInstance().find(IndexedPreservationEvent.class.getName(), filter, sorter, sublist,
      getFacets(), LocaleInfo.getCurrentLocale().getLocaleName(), showInactive, callback);
  }

}
