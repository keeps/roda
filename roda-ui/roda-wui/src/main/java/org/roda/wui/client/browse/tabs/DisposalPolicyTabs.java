package org.roda.wui.client.browse.tabs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.wui.client.common.actions.DisposalHoldSearchWrapperActions;
import org.roda.wui.client.common.actions.DisposalRuleSearchWrapperActions;
import org.roda.wui.client.common.actions.DisposalScheduleSearchWrapperActions;
import org.roda.wui.client.common.lists.DisposalHoldList;
import org.roda.wui.client.common.lists.DisposalRuleList;
import org.roda.wui.client.common.lists.DisposalScheduleList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.PermissionClientUtils;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

public class DisposalPolicyTabs extends Tabs {

  public void init() {

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)) {
      ListBuilder<DisposalSchedule> scheduleListBuilder = new ListBuilder<>(() -> new DisposalScheduleList(),
        new AsyncTableCellOptions<>(DisposalSchedule.class, "DisposalPolicyPage_disposalSchedules")
          .withActionable(DisposalScheduleSearchWrapperActions.get()).withCsvDownloadButtonVisibility(false)
          .bindOpener());

      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalSchedulesTitle()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new SearchWrapper(false).createListAndSearchPanel(scheduleListBuilder);
        }
      });
    }

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)) {
      ListBuilder<DisposalHold> holdListBuilder = new ListBuilder<>(() -> new DisposalHoldList(),
        new AsyncTableCellOptions<>(DisposalHold.class, "DisposalPolicyPage_disposalHolds")
          .withActionable(DisposalHoldSearchWrapperActions.get()).withCsvDownloadButtonVisibility(false).bindOpener());

      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalHoldsTitle()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new SearchWrapper(false).createListAndSearchPanel(holdListBuilder);
        }
      });
    }

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_RULES)) {
      ListBuilder<DisposalRule> ruleListBuilder = new ListBuilder<>(() -> new DisposalRuleList(),
        new AsyncTableCellOptions<>(DisposalRule.class, "DisposalPolicyPage_disposalRules")
          .withActionable(DisposalRuleSearchWrapperActions.get()).withCsvDownloadButtonVisibility(false).bindOpener());

      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalRulesTitle()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new SearchWrapper(false).createListAndSearchPanel(ruleListBuilder);
        }
      });
    }
  }
}