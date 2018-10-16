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

import com.google.gwt.user.client.ui.SimplePanel;

public class NotificationSearch extends SimplePanel {
  public NotificationSearch(String notificationsListId) {
    ListBuilder<Notification> notificationListBuilder = new ListBuilder<>(() -> new NotificationList(),
      new AsyncTableCellOptions<>(Notification.class, notificationsListId).bindOpener());

    setWidget(new SearchWrapper(false).createListAndSearchPanel(notificationListBuilder));
  }
}
