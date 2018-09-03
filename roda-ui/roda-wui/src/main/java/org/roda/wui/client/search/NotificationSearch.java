/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.lists.NotificationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class NotificationSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, NotificationSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public NotificationSearch(String notificationsListId) {

    ListBuilder<Notification> notificationListBuilder = new ListBuilder<>(() -> new NotificationList(),
      new AsyncTableCellOptions<>(Notification.class, notificationsListId).bindOpener());

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(notificationListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    // TODO tmp
    // searchPanel.setDropdownLabel(messages.searchListBoxNotifications());
    // searchPanel.addDropdownItem(messages.searchListBoxNotifications(),
    // RodaConstants.SEARCH_NOTIFICATIONS);
  }
}
