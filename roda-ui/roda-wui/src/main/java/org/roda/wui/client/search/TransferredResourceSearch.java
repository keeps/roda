/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.actions.TransferredResourceActions;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class TransferredResourceSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, TransferredResourceSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public TransferredResourceSearch(String resourcesListId, Filter filter,
    TransferredResourceActions transferredResourceActions) {

    ListBuilder<TransferredResource> transferredResourceListBuilder = new ListBuilder<>(TransferredResourceList::new,
      new AsyncTableCell.Options<>(TransferredResource.class, resourcesListId).withFilter(filter).bindOpener());

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(transferredResourceListBuilder,
      transferredResourceActions);

    initWidget(uiBinder.createAndBindUi(this));

    // TODO tmp
    // searchPanel.setDropdownLabel(messages.searchListBoxTransferredResources());
    // searchPanel.addDropdownItem(messages.searchListBoxTransferredResources(),
    // RodaConstants.SEARCH_TRANSFERRED_RESOURCES);
  }

  public SelectedItems<TransferredResource> getSelected() {
    return searchWrapper.getSelectedItems(TransferredResource.class);
  }

  public void setDefaultFilters(Filter filter) {
    searchWrapper.setFilter(TransferredResource.class, filter);
  }

  public void refresh() {
    searchWrapper.refreshCurrentList();
  }
}
