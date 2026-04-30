package org.roda.wui.client.browse.tabs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationState;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.confirmations.tabs.DisposalConfirmationDetailsPanel;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

public class DisposalConfirmationTabs extends Tabs {

  public void init(DisposalConfirmation confirmation) {

    int activeIndex = this.getSelectedTabIndex();

    this.clear();

    // 1. Clear any existing tabs before building new ones!
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new DisposalConfirmationDetailsPanel(confirmation);
      }
    });

    // Check if user has permissions to see the AIP
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_AIP) && DisposalConfirmationState.PENDING.equals(confirmation.getState())) {
      ListBuilder<IndexedAIP> aipsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "ShowDisposalSchedule_aips")
          .withFilter(
            new Filter(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_CONFIRMATION_ID, confirmation.getId()),
              new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.name())))
          .withSummary(messages.listOfAIPs()).bindOpener());

      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.allIntellectualEntities()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new SearchWrapper(false).createListAndSearchPanel(aipsListBuilder);
        }
      });
    }

    this.selectTabByIndex(activeIndex);
  }
}