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
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

public class NotificationList extends BasicAsyncTableCell<Notification> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final int PAGE_SIZE = 20;

  private TextColumn<Notification> fromUser;
  // private TextColumn<Message> recipientUser;
  private Column<Notification, Date> sentOn;
  private TextColumn<Notification> subject;
  private Column<Notification, SafeHtml> acknowledged;

  public NotificationList() {
    this(null, null, null, false);
  }

  public NotificationList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(Notification.class);
  }

  @Override
  protected void configureDisplay(CellTable<Notification> display) {
    fromUser = new TextColumn<Notification>() {

      @Override
      public String getValue(Notification notification) {
        return notification != null ? notification.getFromUser() : null;
      }
    };

    /*
     * recipientUser = new TextColumn<Message>() {
     * 
     * @Override public String getValue(Message message) { return message !=
     * null ? message.getRecipientUsers().toArray().toString() : null; } };
     */

    sentOn = new Column<Notification, Date>(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(Notification notification) {
        return notification != null ? notification.getSentOn() : null;
      }
    };

    subject = new TextColumn<Notification>() {

      @Override
      public String getValue(Notification notification) {
        return notification != null ? notification.getSubject() : null;
      }
    };

    acknowledged = new Column<Notification, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Notification notification) {
        SafeHtml ret = null;
        if (notification != null) {
          if (notification.isAcknowledged()) {
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
    // recipientUser.setSortable(true);
    sentOn.setSortable(true);
    subject.setSortable(true);
    acknowledged.setSortable(true);

    // TODO externalize strings into constants

    addColumn(fromUser, messages.notificationFrom(), true, false);
    addColumn(sentOn, messages.notificationSentOn(), true, false, 13);
    addColumn(subject, messages.notificationSubject(), true, false);
    addColumn(acknowledged, messages.notificationAck(), true, false, 5);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(sentOn, false));

    addStyleName("my-collections-table");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<Notification>> callback) {

    Filter filter = getFilter();

    Map<Column<Notification, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Notification, ?>, List<String>>();
    columnSortingKeyMap.put(fromUser, Arrays.asList(RodaConstants.NOTIFICATION_FROM_USER));
    // columnSortingKeyMap.put(recipientUser,
    // Arrays.asList(RodaConstants.MESSAGE_RECIPIENT_USERS));
    columnSortingKeyMap.put(sentOn, Arrays.asList(RodaConstants.NOTIFICATION_SENT_ON));
    columnSortingKeyMap.put(subject, Arrays.asList(RodaConstants.NOTIFICATION_SUBJECT));
    columnSortingKeyMap.put(acknowledged, Arrays.asList(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    boolean justActive = false;
    BrowserService.Util.getInstance().find(Notification.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), justActive, callback);

  }

  @Override
  protected ProvidesKey<Notification> getKeyProvider() {
    return new ProvidesKey<Notification>() {

      @Override
      public Object getKey(Notification item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
