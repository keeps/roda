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
import org.roda.core.data.v2.messages.Message;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.LocaleInfo;
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
public class MessageList extends AsyncTableCell<Message> {

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private TextColumn<Message> fromColumn;
  private TextColumn<Message> toColumn;
  private Column<Message, Date> sentOnColumn;
  private TextColumn<Message> subjectColumn;
  private TextColumn<Message> acknowledgedColumn;

  public MessageList() {
    this(null, null, null, false);
  }

  public MessageList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<Message> display) {

    fromColumn = new TextColumn<Message>() {

      @Override
      public String getValue(Message message) {
        return message != null ? message.getFromUser() : null;
      }
    };

    toColumn = new TextColumn<Message>() {

      @Override
      public String getValue(Message message) {
        return message != null ? message.getRecipientUser() : null;
      }
    };

    sentOnColumn = new Column<Message, Date>(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(Message message) {
        return message != null ? message.getSentOn() : null;
      }
    };

    subjectColumn = new TextColumn<Message>() {

      @Override
      public String getValue(Message message) {
        return message != null ? message.getSubject() : null;
      }
    };

    acknowledgedColumn = new TextColumn<Message>() {

      @Override
      public String getValue(Message message) {
        return message != null ? Boolean.toString(message.isAcknowledged()) : null;
      }
    };

    fromColumn.setSortable(true);
    toColumn.setSortable(true);
    sentOnColumn.setSortable(true);
    subjectColumn.setSortable(true);
    acknowledgedColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(fromColumn, "From");
    display.addColumn(toColumn, "To");
    display.addColumn(sentOnColumn, "Sent On");
    display.addColumn(subjectColumn, "Subject");
    display.addColumn(acknowledgedColumn, "Acknowledged");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(subjectColumn, "100%");

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(sentOnColumn, false));

    fromColumn.setCellStyleNames("nowrap");
    toColumn.setCellStyleNames("nowrap");
    sentOnColumn.setCellStyleNames("nowrap");
    acknowledgedColumn.setCellStyleNames("nowrap");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Message>> callback) {

    Filter filter = getFilter();

    Map<Column<Message, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Message, ?>, List<String>>();
    columnSortingKeyMap.put(fromColumn, Arrays.asList(RodaConstants.MESSAGE_FROM_USER));
    columnSortingKeyMap.put(toColumn, Arrays.asList(RodaConstants.MESSAGE_RECIPIENT_USER));
    columnSortingKeyMap.put(sentOnColumn, Arrays.asList(RodaConstants.MESSAGE_SENT_ON));
    columnSortingKeyMap.put(subjectColumn, Arrays.asList(RodaConstants.MESSAGE_SUBJECT));
    columnSortingKeyMap.put(acknowledgedColumn, Arrays.asList(RodaConstants.MESSAGE_IS_ACKNOWLEDGED));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().find(Risk.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

  @Override
  protected ProvidesKey<Message> getKeyProvider() {
    return new ProvidesKey<Message>() {

      @Override
      public Object getKey(Message item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
