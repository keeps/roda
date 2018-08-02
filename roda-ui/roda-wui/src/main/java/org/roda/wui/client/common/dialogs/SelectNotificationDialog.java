/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.lists.NotificationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;

public class SelectNotificationDialog extends DefaultSelectDialog<Notification> {
  public SelectNotificationDialog(String title, Filter filter) {
    super(title,
      new ListBuilder<>(NotificationList::new,
        new AsyncTableCell.Options<>(Notification.class, "SelectNotificationDialog_notifications").withFilter(filter)
          .withSummary(title)));
  }
}
