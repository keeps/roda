/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.tabs;

import org.roda.core.data.v2.user.User;
import org.roda.wui.client.management.members.tabs.AccessKeysTab;
import org.roda.wui.client.management.members.tabs.RODAMemberDetailsPanel;
import org.roda.wui.client.management.members.tabs.RODAMemberGroupsTab;
import org.roda.wui.client.management.members.tabs.RODAMemberPermissionsTab;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

public class ProfileTabs extends Tabs {

  public void init(User user) {
    this.clear();

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new RODAMemberDetailsPanel(user);
      }
    });

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.groups()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new RODAMemberGroupsTab(user, null, true);
      }
    });

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.permissionsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new RODAMemberPermissionsTab(user, null, true);
      }
    });

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.showAccessKeyTitle()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new AccessKeysTab(user, null);
      }
    });
  }
}
