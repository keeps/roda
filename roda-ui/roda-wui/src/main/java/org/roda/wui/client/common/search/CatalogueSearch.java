/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class CatalogueSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, CatalogueSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  // state
  final String parentAipId;
  final AIPState parentAipState;
  final Permissions permissions;

  public CatalogueSearch(boolean justActive, String itemsListId, String representationsListId, String filesListId,
    String parentAipId, AIPState parentAipState, Permissions permissions) {

    this.parentAipId = parentAipId;
    this.parentAipState = parentAipState;
    this.permissions = permissions;

    // prepare lists
    ListBuilder<IndexedAIP> aipListBuilder = new ListBuilder<>(AIPList::new,
      new AsyncTableCell.Options<>(IndexedAIP.class, itemsListId).withJustActive(justActive).bindOpener()
        .withStartHidden(true));

    ListBuilder<IndexedRepresentation> representationListBuilder = new ListBuilder<>(RepresentationList::new,
      new AsyncTableCell.Options<>(IndexedRepresentation.class, representationsListId).withJustActive(justActive)
        .bindOpener().withStartHidden(true));

    ListBuilder<IndexedFile> fileListBuilder = new ListBuilder<>(() -> new SearchFileList(true),
      new AsyncTableCell.Options<>(IndexedFile.class, filesListId).withJustActive(justActive).bindOpener()
        .withStartHidden(true));

    // add lists to search

    // TODO tmp check why aip has actions with a parent but representation doesnt.
    // bug?
    searchWrapper = new SearchWrapper(true, IndexedAIP.class.getSimpleName())
      .createListAndSearchPanel(aipListBuilder, AipActions.get(parentAipId, parentAipState, permissions))
      .createListAndSearchPanel(representationListBuilder, RepresentationActions.get())
      .createListAndSearchPanel(fileListBuilder, FileActions.get());

    initWidget(uiBinder.createAndBindUi(this));

    // searchWrapper.setDropdownLabel(messages.searchListBoxItems());
    // searchWrapper.addDropdownItem(messages.searchListBoxItems(),
    // RodaConstants.SEARCH_ITEMS);
    // searchWrapper.addDropdownItem(messages.searchListBoxRepresentations(),
    // RodaConstants.SEARCH_REPRESENTATIONS);
    // searchWrapper.addDropdownItem(messages.searchListBoxFiles(),
    // RodaConstants.SEARCH_FILES);
    // searchWrapper.addDropdownPopupStyleName("searchInputListBoxPopup");
  }

  // TODO this is used in appraisal, but it should be using actionable
  @Deprecated
  public SelectedItems<? extends IsIndexed> getSelected() {
    return searchWrapper.getSelectedItemsInCurrentList();
  }

  public void refresh() {
    searchWrapper.refreshCurrentList();
  }

  public void setFilters(List<String> historyTokens) {
    Filter filter = SearchFilters.createFilterFromHistoryTokens(ListUtils.tail(historyTokens));
    searchWrapper.setFilter(IndexedRepresentation.class, filter);
    searchWrapper.setFilter(IndexedFile.class, filter);

    // handle aipId VS parentAipId
    if (historyTokens.contains(RodaConstants.REPRESENTATION_AIP_ID)) {
      List<String> tokensForAip = new ArrayList<>(historyTokens);
      tokensForAip.set(historyTokens.indexOf(RodaConstants.REPRESENTATION_AIP_ID), RodaConstants.AIP_PARENT_ID);
      searchWrapper.setFilter(IndexedAIP.class,
        SearchFilters.createFilterFromHistoryTokens(ListUtils.tail(historyTokens)));
    } else {
      searchWrapper.setFilter(IndexedAIP.class, filter);
    }
  }
}
