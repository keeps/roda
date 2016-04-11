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
import org.roda.wui.client.management.UserManagementService;

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

public class NotificationList extends AsyncTableCell<Message> {

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private TextColumn<Message> fromUser;
  private TextColumn<Message> recipientUser;
  private Column<Message, Date> sentOn;
  private TextColumn<Message> subject;
  private Column<Message, SafeHtml> acknowledged;

  public NotificationList() {
    this(null, null, null, false);
  }

  public NotificationList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<Message> display) {
    fromUser = new TextColumn<Message>() {

      @Override
      public String getValue(Message message) {
        return message != null ? message.getFromUser() : null;
      }
    };

    recipientUser = new TextColumn<Message>() {

      @Override
      public String getValue(Message message) {
        return message != null ? message.getRecipientUser() : null;
      }
    };

    sentOn = new Column<Message, Date>(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(Message message) {
        return message != null ? message.getSentOn() : null;
      }
    };

    subject = new TextColumn<Message>() {

      @Override
      public String getValue(Message message) {
        return message != null ? message.getSubject() : null;
      }
    };

    acknowledged = new Column<Message, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Message message) {
        SafeHtml ret = null;
        if (message != null) {
          if (message.isAcknowledged()) {
            ret = SafeHtmlUtils
              .fromSafeConstant("<span class='label-success'>" + messages.showMessageAcknowledged() + "</span>");
          } else {
            ret = SafeHtmlUtils
              .fromSafeConstant("<span class='label-danger'>" + messages.showMessageNotAcknowledged() + "</span>");
          }
        }

        return ret;
      }
    };

    fromUser.setSortable(true);
    recipientUser.setSortable(true);
    sentOn.setSortable(true);
    subject.setSortable(true);
    acknowledged.setSortable(true);

    // TODO externalize strings into constants

    // display.addColumn(idColumn, "Id");
    display.addColumn(fromUser, "From");
    display.addColumn(recipientUser, "To");
    display.addColumn(sentOn, "Sent On");
    display.addColumn(subject, "Subject");
    display.addColumn(acknowledged, "Acknowledged");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(sentOn, false));

    addStyleName("my-collections-table");
    emptyInfo.addStyleName("my-collections-empty-info");
    // relatedObjectColumn.setCellStyleNames("my-collections-table-cell-link");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Message>> callback) {

    Filter filter = getFilter();

    Map<Column<Message, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Message, ?>, List<String>>();
    columnSortingKeyMap.put(fromUser, Arrays.asList(RodaConstants.MESSAGE_FROM_USER));
    columnSortingKeyMap.put(recipientUser, Arrays.asList(RodaConstants.MESSAGE_RECIPIENT_USER));
    columnSortingKeyMap.put(sentOn, Arrays.asList(RodaConstants.MESSAGE_SENT_ON));
    columnSortingKeyMap.put(subject, Arrays.asList(RodaConstants.MESSAGE_SUBJECT));
    columnSortingKeyMap.put(acknowledged, Arrays.asList(RodaConstants.MESSAGE_IS_ACKNOWLEDGED));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    UserManagementService.Util.getInstance().findMessages(filter, sorter, sublist, getFacets(), callback);

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

}
