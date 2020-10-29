package org.roda.wui.client.search;

import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.lists.DisposalConfirmationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationSearch extends SimplePanel {
  SearchWrapper searchWrapper;

  public DisposalConfirmationSearch() {
    super();
  }

  public DisposalConfirmationSearch(String resourcesListId) {

    ListBuilder<DisposalConfirmationMetadata> disposalConfirmationListBuilder = new ListBuilder<>(
      () -> new DisposalConfirmationList(),
      new AsyncTableCellOptions<>(DisposalConfirmationMetadata.class, resourcesListId).bindOpener().withAutoUpdate(5000)
        .withActionableCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
          @Override
          public void onSuccess(Actionable.ActionImpact result) {
            if (Actionable.ActionImpact.DESTROYED.equals(result) || Actionable.ActionImpact.UPDATED.equals(result)) {
              searchWrapper.refreshAllLists();
            }
          }
        }));

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(disposalConfirmationListBuilder);
    add(searchWrapper);
  }

  public void refresh() {
    if (searchWrapper != null) {
      searchWrapper.refreshCurrentList();
    }
  }
}
