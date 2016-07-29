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
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.management.UserManagementService;

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
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;

import config.i18n.client.ClientMessages;

public class LogEntryList extends BasicAsyncTableCell<LogEntry> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<LogEntry, Date> dateColumn;
  private TextColumn<LogEntry> actionComponentColumn;
  private TextColumn<LogEntry> actionMethodColumn;
  // private Column<LogEntry, String> relatedObjectColumn;
  private TextColumn<LogEntry> usernameColumn;
  private TextColumn<LogEntry> durationColumn;
  private TextColumn<LogEntry> addressColumn;
  private TextColumn<LogEntry> stateColumn;

  public LogEntryList() {
    this(null, null, null, false);
  }

  public LogEntryList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(LogEntry.class);
  }

  @Override
  protected void configureDisplay(CellTable<LogEntry> display) {
    dateColumn = new Column<LogEntry, Date>(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(LogEntry logEntry) {
        return logEntry != null ? logEntry.getDatetime() : null;
      }
    };

    actionComponentColumn = new TextColumn<LogEntry>() {

      @Override
      public String getValue(LogEntry logEntry) {
        return logEntry != null ? logEntry.getActionComponent() : null;
      }
    };

    actionMethodColumn = new TextColumn<LogEntry>() {

      @Override
      public String getValue(LogEntry logEntry) {
        if (logEntry == null) {
          return null;
        }

        String method = logEntry.getActionMethod().substring(0, 1).toUpperCase()
          + logEntry.getActionMethod().substring(1);
        return method.replaceAll("([A-Z])", " $1").trim();
      }
    };

    // relatedObjectColumn = new Column<LogEntry, String>(new
    // ClickableTextCell()) {
    //
    // @Override
    // public String getValue(LogEntry logEntry) {
    // return logEntry != null ? logEntry.getRelatedObjectID() : null;
    // }
    // };
    //
    // relatedObjectColumn.setFieldUpdater(new FieldUpdater<LogEntry, String>()
    // {
    //
    // @Override
    // public void update(int index, LogEntry logEntry, String value) {
    // if (logEntry != null && logEntry.getRelatedObjectID() != null) {
    // Tools.newHistory(Browse.getViewItemHistoryToken(logEntry.getRelatedObjectID()));
    // }
    // }
    // });

    usernameColumn = new TextColumn<LogEntry>() {

      @Override
      public String getValue(LogEntry logEntry) {
        return logEntry != null ? logEntry.getUsername() : null;
      }
    };

    durationColumn = new TextColumn<LogEntry>() {

      @Override
      public String getValue(LogEntry logEntry) {
        // FIXME
        return logEntry != null ? logEntry.getDuration() + " ms" : null;
      }
    };

    addressColumn = new TextColumn<LogEntry>() {

      @Override
      public String getValue(LogEntry logEntry) {
        return logEntry != null ? logEntry.getAddress() : null;
      }
    };

    stateColumn = new TextColumn<LogEntry>() {

      @Override
      public String getValue(LogEntry logEntry) {
        return logEntry != null ? logEntry.getState().toString() : null;
      }
    };

    dateColumn.setSortable(true);
    actionComponentColumn.setSortable(true);
    actionMethodColumn.setSortable(true);
    // relatedObjectColumn.setSortable(true);
    usernameColumn.setSortable(true);
    durationColumn.setSortable(true);
    addressColumn.setSortable(true);
    

    // TODO externalize strings into constants

    addColumn(dateColumn, messages.logEntryDatetimeExtended(), true, false, 14);
    addColumn(actionComponentColumn, messages.logEntryComponent(), true, false);
    addColumn(actionMethodColumn, messages.logEntryMethod(), true, false);
    addColumn(usernameColumn, messages.logEntryUser(), true, false);
    addColumn(durationColumn, messages.logEntryDuration(), true, true, 5);
    addColumn(addressColumn, messages.logEntryAddress(), true, false);
    addColumn(stateColumn, messages.logEntryState(), true, false);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(dateColumn, false));

    addStyleName("my-collections-table");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<LogEntry>> callback) {

    Filter filter = getFilter();

    Map<Column<LogEntry, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<LogEntry, ?>, List<String>>();
    columnSortingKeyMap.put(dateColumn, Arrays.asList(RodaConstants.LOG_DATETIME));
    columnSortingKeyMap.put(actionComponentColumn, Arrays.asList(RodaConstants.LOG_ACTION_COMPONENT));
    columnSortingKeyMap.put(actionMethodColumn, Arrays.asList(RodaConstants.LOG_ACTION_METHOD));
    columnSortingKeyMap.put(usernameColumn, Arrays.asList(RodaConstants.LOG_USERNAME));
    columnSortingKeyMap.put(durationColumn, Arrays.asList(RodaConstants.LOG_DURATION));
    columnSortingKeyMap.put(addressColumn, Arrays.asList(RodaConstants.LOG_ADDRESS));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    UserManagementService.Util.getInstance().findLogEntries(filter, sorter, sublist, getFacets(), callback);

  }

  @Override
  protected CellPreviewEvent.Handler<LogEntry> getSelectionEventManager() {
    return DefaultSelectionEventManager.<LogEntry> createBlacklistManager(3);
  }

}
