/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.lists.NotificationList;

public class SelectNotificationDialog extends DefaultSelectDialog<Notification, Void> {

  private static final Filter DEFAULT_FILTER_NOTIFICATION = new Filter(
    new BasicSearchFilterParameter(RodaConstants.NOTIFICATION_SEARCH, "*"));

  public SelectNotificationDialog(String title) {
    this(title, DEFAULT_FILTER_NOTIFICATION);
  }

  public SelectNotificationDialog(String title, Filter filter) {
    this(title, filter, false);
  }

  public SelectNotificationDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.NOTIFICATION_SEARCH, new NotificationList(filter, null, title, selectable),
      false);
  }

}
