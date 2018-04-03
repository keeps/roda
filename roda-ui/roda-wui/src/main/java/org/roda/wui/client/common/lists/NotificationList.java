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
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.StringUtils;

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
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

public class NotificationList extends BasicAsyncTableCell<Notification> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<Notification> fromUser;
  private TextColumn<Notification> recipientUsers;
  private Column<Notification, Date> sentOn;
  private TextColumn<Notification> subject;
  private Column<Notification, SafeHtml> acknowledged;
  private Column<Notification, SafeHtml> state;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.NOTIFICATION_ID, RodaConstants.NOTIFICATION_FROM_USER, RodaConstants.NOTIFICATION_RECIPIENT_USERS,
    RodaConstants.NOTIFICATION_SENT_ON, RodaConstants.NOTIFICATION_SUBJECT, RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED,
    RodaConstants.NOTIFICATION_STATE);

  public NotificationList() {
    this(null, null, null, false);
  }

  public NotificationList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(Notification.class, filter, false, facets, summary, selectable, fieldsToReturn);
  }

  public NotificationList(Filter filter, Facets facets, String summary, boolean selectable, int pageSize,
    int incrementPage) {
    super(Notification.class, filter, false, facets, summary, selectable, pageSize, incrementPage, fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<Notification> display) {
    fromUser = new TextColumn<Notification>() {
      @Override
      public String getValue(Notification notification) {
        return notification != null ? notification.getFromUser() : null;
      }
    };

    recipientUsers = new TextColumn<Notification>() {
      @Override
      public String getValue(Notification notification) {
        return notification != null ? StringUtils.prettyPrint(notification.getRecipientUsers()) : null;
      }
    };

    sentOn = new Column<Notification, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
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

    state = new Column<Notification, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Notification notification) {
        SafeHtml ret = null;
        if (notification != null) {
          ret = HtmlSnippetUtils.getNotificationStateHTML(notification.getState());
        }
        return ret;
      }
    };

    fromUser.setSortable(true);
    sentOn.setSortable(true);
    subject.setSortable(true);
    acknowledged.setSortable(true);
    state.setSortable(true);

    addColumn(fromUser, messages.notificationFrom(), true, false);
    addColumn(recipientUsers, messages.notificationTo(), true, false);
    addColumn(sentOn, messages.notificationSentOn(), true, false, 13);
    addColumn(subject, messages.notificationSubject(), true, false);
    addColumn(state, messages.notificationState(), true, false, 7);
    addColumn(acknowledged, messages.notificationAck(), true, false, 7);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(sentOn, false));

    addStyleName("my-collections-table");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<Notification, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(fromUser, Arrays.asList(RodaConstants.NOTIFICATION_FROM_USER));
    columnSortingKeyMap.put(recipientUsers, Arrays.asList(RodaConstants.NOTIFICATION_RECIPIENT_USERS));
    columnSortingKeyMap.put(sentOn, Arrays.asList(RodaConstants.NOTIFICATION_SENT_ON));
    columnSortingKeyMap.put(subject, Arrays.asList(RodaConstants.NOTIFICATION_SUBJECT));
    columnSortingKeyMap.put(acknowledged, Arrays.asList(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED));
    columnSortingKeyMap.put(state, Arrays.asList(RodaConstants.NOTIFICATION_STATE));
    return createSorter(columnSortList, columnSortingKeyMap);
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

}
