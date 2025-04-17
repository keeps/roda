package org.roda.wui.client.browse.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.actions.TransferredResourceActions;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

public class BrowseTransferredResourceTabs extends Tabs {
  public void init(TransferredResource resource) {
    if (!resource.isFile()) {
      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.filesTab()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {

          ListBuilder<TransferredResource> transferredResourceListBuilder = new ListBuilder<>(
            () -> new TransferredResourceList(),
            new AsyncTableCellOptions<>(TransferredResource.class, "IngestTransfer_transferredResources")
              .withFilter(new Filter(
                new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, resource.getRelativePath())))
              .bindOpener().withActionable(TransferredResourceActions.get(resource)));

          return new SearchWrapper(false).createListAndSearchPanel(transferredResourceListBuilder);
        }
      });
    }

    // Details
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new DetailsTab(resource);
      }
    });
  }
}
