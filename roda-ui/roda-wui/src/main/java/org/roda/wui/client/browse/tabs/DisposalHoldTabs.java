package org.roda.wui.client.browse.tabs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.IndexedAIPDisposalHoldSearchWrapperActions;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.hold.tabs.DisposalHoldDetailsPanel;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DisposalHoldTabs extends Tabs {

  public void init(DisposalHold hold, AsyncCallback<Actionable.ActionImpact> actionCallback) {

    int activeIndex = this.getSelectedTabIndex();

    this.clear();

    // 1. Clear any existing tabs before building new ones!
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new DisposalHoldDetailsPanel(hold, actionCallback);
      }
    });

    // Check if user has permissions to see the AIP
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_AIP)) {

      ListBuilder<IndexedAIP> aipsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "ShowDisposalSchedule_aips")
          .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_HOLDS_ID, hold.getId()),
            new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.name())))
          .withActionable(new IndexedAIPDisposalHoldSearchWrapperActions(hold)).withSummary(messages.listOfAIPs()).bindOpener());

      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.disposalScheduleListAips()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new SearchWrapper(false).createListAndSearchPanel(aipsListBuilder);
        }
      });
    }

    this.selectTabByIndex(activeIndex);
  }
}