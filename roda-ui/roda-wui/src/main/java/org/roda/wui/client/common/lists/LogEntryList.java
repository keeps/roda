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
import org.roda.core.data.v2.index.collapse.Collapse;
import org.roda.core.data.v2.index.collapse.NullPolicyEnum;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

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

import config.i18n.client.ClientMessages;

public class LogEntryList extends AsyncTableCell<LogEntry> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<LogEntry, Date> dateColumn;
  private Column<LogEntry, SafeHtml> actionComponentColumn;
  private TextColumn<LogEntry> actionMethodColumn;
  private TextColumn<LogEntry> requestReasonColumn;
  private TextColumn<LogEntry> usernameColumn;
  private TextColumn<LogEntry> durationColumn;
  private TextColumn<LogEntry> addressColumn;
  private Column<LogEntry, SafeHtml> stateColumn;
  private final boolean applyCollapse;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.LOG_ID,
    RodaConstants.LOG_DATETIME, RodaConstants.LOG_ACTION_COMPONENT, RodaConstants.LOG_ACTION_METHOD,
    RodaConstants.LOG_USERNAME, RodaConstants.LOG_DURATION, RodaConstants.LOG_ADDRESS, RodaConstants.LOG_STATE,
    RodaConstants.LOG_REQUEST_HEADER_UUID, RodaConstants.LOG_REQUEST_HEADER_REASON, RodaConstants.LOG_REQUEST_HEADER_TYPE);

  public LogEntryList() {
    this(true);
  }

  public LogEntryList(boolean applyCollapse) {
    super();
    this.applyCollapse = applyCollapse;
  }

  @Override
  protected void adjustOptions(AsyncTableCellOptions<LogEntry> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<LogEntry> display) {
    dateColumn = new Column<LogEntry, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(LogEntry logEntry) {
        return logEntry != null ? logEntry.getDatetime() : null;
      }
    };

    actionComponentColumn = new Column<LogEntry, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(LogEntry logEntry) {
        return SafeHtmlUtils
          .fromSafeConstant(translate(RodaConstants.LOG_ACTION_COMPONENT, logEntry.getActionComponent()));
      }
    };

    actionMethodColumn = new TextColumn<LogEntry>() {
      @Override
      public String getValue(LogEntry logEntry) {
        if (logEntry == null) {
          return null;
        }

        return StringUtils.getPrettifiedActionMethod(logEntry.getActionMethod());
      }
    };

    requestReasonColumn = new TextColumn<LogEntry>() {
      @Override
      public String getValue(LogEntry logEntry) {
        if (logEntry == null) {
          return null;
        }

        return logEntry.getAuditLogRequestHeaders().getReason();
      }
    };

    usernameColumn = new TextColumn<LogEntry>() {
      @Override
      public String getValue(LogEntry logEntry) {
        return logEntry != null ? logEntry.getUsername() : null;
      }
    };

    durationColumn = new TextColumn<LogEntry>() {
      @Override
      public String getValue(LogEntry logEntry) {
        return logEntry != null ? Humanize.durationMillisToShortDHMS(logEntry.getDuration()) : null;
      }
    };

    addressColumn = new TextColumn<LogEntry>() {

      @Override
      public String getValue(LogEntry logEntry) {
        return logEntry != null ? logEntry.getAddress() : null;
      }
    };

    stateColumn = new Column<LogEntry, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(LogEntry logEntry) {
        return HtmlSnippetUtils.getLogEntryStateHtml(logEntry.getState());
      }
    };

    dateColumn.setSortable(true);
    actionComponentColumn.setSortable(true);
    actionMethodColumn.setSortable(true);
    requestReasonColumn.setSortable(true);
    usernameColumn.setSortable(true);
    durationColumn.setSortable(true);
    addressColumn.setSortable(true);
    stateColumn.setSortable(true);

    addColumn(dateColumn, messages.logEntryDate(), true, false, 14);
    addColumn(requestReasonColumn, "Reason", true, false);
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
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<LogEntry, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(dateColumn, Arrays.asList(RodaConstants.LOG_DATETIME));
    columnSortingKeyMap.put(actionComponentColumn, Arrays.asList(RodaConstants.LOG_ACTION_COMPONENT));
    columnSortingKeyMap.put(actionMethodColumn, Arrays.asList(RodaConstants.LOG_ACTION_METHOD));
    columnSortingKeyMap.put(requestReasonColumn, Arrays.asList(RodaConstants.LOG_REQUEST_HEADER_REASON));
    columnSortingKeyMap.put(usernameColumn, Arrays.asList(RodaConstants.LOG_USERNAME));
    columnSortingKeyMap.put(durationColumn, Arrays.asList(RodaConstants.LOG_DURATION));
    columnSortingKeyMap.put(addressColumn, Arrays.asList(RodaConstants.LOG_ADDRESS));
    columnSortingKeyMap.put(stateColumn, Arrays.asList(RodaConstants.LOG_STATE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

  @Override
  public Collapse getCollapse() {
    if (applyCollapse) {
      Collapse collapse = new Collapse();
      collapse.setField(RodaConstants.LOG_REQUEST_HEADER_UUID);
      collapse.setNullPolicy(NullPolicyEnum.EXPAND);

      return collapse;
    }
    return null;
  }
}
