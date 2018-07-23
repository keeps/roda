/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.search.AdvancedSearchFieldsPanel;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

public class TransferredResourceSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, TransferredResourceSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  Filter filterResources;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  FlowPanel searchResultPanel;

  TransferredResourceList resourcesSearchResultPanel;
  AdvancedSearchFieldsPanel resourcesSearchAdvancedFieldsPanel;
  String resourcesListId;

  public TransferredResourceSearch(String resourcesListId) {
    this.resourcesListId = resourcesListId;

    ValueChangeHandler<Integer> searchAdvancedFieldsPanelHandler = new ValueChangeHandler<Integer>() {
      @Override
      public void onValueChange(ValueChangeEvent<Integer> event) {
        searchPanel.setSearchAdvancedGoEnabled(event.getValue() == 0 ? false : true);
      }
    };

    resourcesSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_TRANSFERRED_RESOURCES);
    resourcesSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);
    defaultFilters(Filter.ALL);

    searchPanel = new SearchPanel(filterResources, RodaConstants.TRANSFERRED_RESOURCE_SEARCH, true,
      messages.searchPlaceHolder(), false, true, false);

    initWidget(uiBinder.createAndBindUi(this));

    searchPanel.setDropdownLabel(messages.searchListBoxTransferredResources());
    searchPanel.addDropdownItem(messages.searchListBoxTransferredResources(),
      RodaConstants.SEARCH_TRANSFERRED_RESOURCES);
    search();

    searchPanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        search();
      }
    });

    searchPanel.addDropdownPopupStyleName("searchInputListBoxPopup");

    searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        resourcesSearchAdvancedFieldsPanel.addSearchFieldPanel();
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    if (resourcesSearchResultPanel == null) {
      createTransferredResourcesSearchResultPanel();
    }

    searchPanel.setVariables(filterResources, RodaConstants.NOTIFICATION_SEARCH, true, resourcesSearchResultPanel,
      resourcesSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(resourcesSearchResultPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(resourcesSearchResultPanel);
  }

  private void createTransferredResourcesSearchResultPanel() {
    resourcesSearchResultPanel = new TransferredResourceList(resourcesListId, filterResources, messages.searchResults(),
      true);

    ListSelectionUtils.bindBrowseOpener(resourcesSearchResultPanel);

    resourcesSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        TransferredResource r = resourcesSearchResultPanel.getSelectionModel().getSelectedObject();
        if (r != null) {
          searchPanel.clearSearchInputBox();
          HistoryUtils.newHistory(IngestTransfer.RESOLVER, r.getUUID());
        }
      }
    });
  }

  public SelectedItems<TransferredResource> getSelected() {
    if (resourcesSearchResultPanel != null && resourcesSearchResultPanel.hasElementsSelected()) {
      return resourcesSearchResultPanel.getSelected();
    } else {
      return new SelectedItemsList<>();
    }
  }

  public void refresh() {
    if (resourcesSearchResultPanel != null && resourcesSearchResultPanel.hasElementsSelected()) {
      resourcesSearchResultPanel.refresh();
    }
  }

  public void search() {
    showSearchAdvancedFieldsPanel();
    searchPanel.doSearch();
  }

  public void defaultFilters(Filter filter) {
    filterResources = new Filter(filter);
    if (searchPanel != null) {
      searchPanel.setDefaultFilter(filter, false);
      resourcesSearchResultPanel.setFilter(filterResources);
    }
  }

  public void setFilter(Filter filter) {
    resourcesSearchResultPanel.setFilter(filter);
  }

  public TransferredResourceList getList() {
    return resourcesSearchResultPanel;
  }
}
