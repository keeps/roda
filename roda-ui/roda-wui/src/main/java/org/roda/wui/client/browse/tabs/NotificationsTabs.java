/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.management.tabs.NotificationDetailsPanel;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class NotificationsTabs extends Tabs {
  public void init(Notification notification) {
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new NotificationDetailsPanel(notification);
      }
    });
  }
}
