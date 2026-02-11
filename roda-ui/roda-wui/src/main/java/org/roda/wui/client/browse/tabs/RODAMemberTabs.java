package org.roda.wui.client.browse.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.management.members.tabs.AccessKeysTab;
import org.roda.wui.client.management.members.tabs.RODAMemberDetailsPanel;
import org.roda.wui.client.management.members.tabs.RODAMemberGroupsTab;
import org.roda.wui.client.management.members.tabs.RODAMemberPermissionsTab;

public class RODAMemberTabs extends Tabs {

  public void init(RODAMember member, AsyncCallback<Actionable.ActionImpact> actionCallback) {

    int activeIndex = this.getSelectedTabIndex();

    this.clear();

    // 1. Clear any existing tabs before building new ones!
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new RODAMemberDetailsPanel(member, actionCallback);
      }
    });

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(member.isUser() ? messages.groups() : messages.membersTabTitle()),
      new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new RODAMemberGroupsTab(member, actionCallback);
        }
      });

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.permissionsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new RODAMemberPermissionsTab(member, actionCallback);
      }
    });

    if (member.isUser() && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_REVOKE_ACCESS_TOKEN,
      RodaConstants.PERMISSION_METHOD_REGENERATE_ACCESS_TOKEN, RodaConstants.PERMISSION_METHOD_DELETE_ACCESS_TOKEN)) {
      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.showAccessKeyTitle()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new AccessKeysTab(member, actionCallback);
        }
      });
    }

    this.selectTabByIndex(activeIndex);
  }
}