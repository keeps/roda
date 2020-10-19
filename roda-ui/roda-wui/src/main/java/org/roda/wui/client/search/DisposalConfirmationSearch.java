package org.roda.wui.client.search;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
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
      new AsyncTableCellOptions<>(DisposalConfirmationMetadata.class, resourcesListId).bindOpener());

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(disposalConfirmationListBuilder);
    add(searchWrapper);
  }

  public void refresh() {
    if (searchWrapper != null) {
      searchWrapper.refreshCurrentList();
    }
  }
}
