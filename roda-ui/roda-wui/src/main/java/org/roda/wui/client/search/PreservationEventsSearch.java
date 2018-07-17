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
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.wui.client.browse.ShowPreservationEvent;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.search.AdvancedSearchFieldsPanel;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.StringUtils;
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

public class PreservationEventsSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, PreservationEventsSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  Filter filterEvents;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  FlowPanel searchResultPanel;

  PreservationEventList eventsSearchResultPanel;
  AdvancedSearchFieldsPanel eventsSearchAdvancedFieldsPanel;
  String eventsListId;

  public PreservationEventsSearch(String eventsListId) {
    this.eventsListId = eventsListId;

    ValueChangeHandler<Integer> searchAdvancedFieldsPanelHandler = new ValueChangeHandler<Integer>() {
      @Override
      public void onValueChange(ValueChangeEvent<Integer> event) {
        searchPanel.setSearchAdvancedGoEnabled(event.getValue() == 0 ? false : true);
      }
    };

    eventsSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_PRESERVATION_EVENTS);
    eventsSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);
    defaultFilters(Filter.ALL);

    searchPanel = new SearchPanel(filterEvents, RodaConstants.PRESERVATION_EVENT_SEARCH, true,
      messages.searchPlaceHolder(), false, true, false);

    initWidget(uiBinder.createAndBindUi(this));

    searchPanel.setDropdownLabel(messages.searchListBoxPreservationEvents());
    searchPanel.addDropdownItem(messages.searchListBoxPreservationEvents(), RodaConstants.SEARCH_PRESERVATION_EVENTS);
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
        eventsSearchAdvancedFieldsPanel.addSearchFieldPanel();
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    if (eventsSearchResultPanel == null) {
      createEventsSearchResultPanel();
    }

    searchPanel.setVariables(filterEvents, RodaConstants.PRESERVATION_EVENT_SEARCH, true, eventsSearchResultPanel,
      eventsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(eventsSearchResultPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(eventsSearchResultPanel);
  }

  private void createEventsSearchResultPanel() {
    eventsSearchResultPanel = new PreservationEventList(eventsListId, filterEvents, messages.searchResults(), false);
    ListSelectionUtils.bindBrowseOpener(eventsSearchResultPanel);

    eventsSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedPreservationEvent selected = eventsSearchResultPanel.getSelectionModel().getSelectedObject();
        if (selected != null) {
          String fileUUID = selected.getFileUUID();
          String representationUUID = selected.getRepresentationUUID();
          String aipUUID = selected.getAipID();

          if (StringUtils.isNotBlank(fileUUID)) {
            HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, aipUUID, representationUUID, fileUUID,
              selected.getId());
          } else if (StringUtils.isNotBlank(representationUUID)) {
            HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, aipUUID, representationUUID, selected.getId());
          } else if (StringUtils.isNotBlank(aipUUID)) {
            HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, aipUUID, selected.getId());
          } else {
            HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, selected.getId());
          }
        }
      }
    });

    eventsSearchResultPanel.autoUpdate(10000);
  }

  public void refresh() {
    if (eventsSearchResultPanel != null && eventsSearchResultPanel.hasElementsSelected()) {
      eventsSearchResultPanel.refresh();
    }
  }

  public void search() {
    showSearchAdvancedFieldsPanel();
    searchPanel.doSearch();
  }

  public void defaultFilters(Filter filter) {
    filterEvents = new Filter(filter);
    if (searchPanel != null) {
      searchPanel.setDefaultFilterIncremental(false);
    }
  }

  public void setFilter(Filter filter) {
    eventsSearchResultPanel.setFilter(filter);
  }
}
