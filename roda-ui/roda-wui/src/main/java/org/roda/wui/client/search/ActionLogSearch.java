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
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.search.AdvancedSearchFieldsPanel;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.management.ShowLogEntry;
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

public class ActionLogSearch extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, ActionLogSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  Filter filterLogs;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  FlowPanel searchResultPanel;

  LogEntryList logsSearchResultPanel;
  AdvancedSearchFieldsPanel logsSearchAdvancedFieldsPanel;
  String logsListId;

  public ActionLogSearch(String logsListId) {
    this.logsListId = logsListId;

    ValueChangeHandler<Integer> searchAdvancedFieldsPanelHandler = new ValueChangeHandler<Integer>() {
      @Override
      public void onValueChange(ValueChangeEvent<Integer> event) {
        searchPanel.setSearchAdvancedGoEnabled(event.getValue() == 0 ? false : true);
      }
    };

    logsSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_ACTION_LOGS);
    logsSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);
    defaultFilters(Filter.ALL);

    searchPanel = new SearchPanel(filterLogs, RodaConstants.LOG_SEARCH, true, messages.searchPlaceHolder(), false, true,
      false);

    initWidget(uiBinder.createAndBindUi(this));

    searchPanel.setDropdownLabel(messages.searchListBoxActionLogs());
    searchPanel.addDropdownItem(messages.searchListBoxActionLogs(), RodaConstants.SEARCH_ACTION_LOGS);
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
        logsSearchAdvancedFieldsPanel.addSearchFieldPanel();
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    if (logsSearchResultPanel == null) {
      createLogsSearchResultPanel();
    }

    searchPanel.setVariables(filterLogs, RodaConstants.LOG_SEARCH, true, logsSearchResultPanel,
      logsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(logsSearchResultPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(logsSearchResultPanel);
  }

  private void createLogsSearchResultPanel() {
    logsSearchResultPanel = new LogEntryList(logsListId, filterLogs, messages.searchResults(), false);
    ListSelectionUtils.bindBrowseOpener(logsSearchResultPanel);

    logsSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        LogEntry selected = logsSearchResultPanel.getSelectionModel().getSelectedObject();
        if (selected != null) {
          LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
          HistoryUtils.newHistory(ShowLogEntry.RESOLVER, selected.getId());
        }
      }
    });
  }

  public void refresh() {
    if (logsSearchResultPanel != null && logsSearchResultPanel.hasElementsSelected()) {
      logsSearchResultPanel.refresh();
    }
  }

  public void search() {
    showSearchAdvancedFieldsPanel();
    searchPanel.doSearch();
  }

  public void defaultFilters(Filter filter) {
    filterLogs = new Filter(filter);
    if (searchPanel != null) {
      setFilter(filter);
      searchPanel.setDefaultFilterIncremental(false);
    }
  }

  public void setFilter(Filter filter) {
    logsSearchResultPanel.setFilter(filter);
  }
}
