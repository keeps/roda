/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.TransferredResourceActions;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;

public class TransferredResourceSearch extends SimplePanel {

  interface MyUiBinder extends UiBinder<Widget, TransferredResourceSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public TransferredResourceSearch() {

  }

  public TransferredResourceSearch(String resourcesListId, Filter filter,
    TransferredResourceActions transferredResourceActions, AsyncCallback<Actionable.ActionImpact> actionCallback) {

    ListBuilder<TransferredResource> transferredResourceListBuilder = new ListBuilder<>(() -> new TransferredResourceList(),
      new AsyncTableCellOptions<>(TransferredResource.class, resourcesListId).withFilter(filter).bindOpener()
        .withActionable(transferredResourceActions).withActionableCallback(actionCallback));

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(transferredResourceListBuilder);
    add(searchWrapper);
  }

  public void setDefaultFilters(Filter filter) {
    if (searchWrapper != null) {
      searchWrapper.setFilter(TransferredResource.class, filter);
    }
  }

  public void refresh() {
    if (searchWrapper != null) {
      searchWrapper.refreshCurrentList();
    }
  }
}
