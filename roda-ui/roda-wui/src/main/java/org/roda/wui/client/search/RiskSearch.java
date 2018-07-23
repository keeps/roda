/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.RiskList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.search.AdvancedSearchFieldsPanel;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.planning.RiskRegister;
import org.roda.wui.client.planning.ShowRisk;
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

public class RiskSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, RiskSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  Filter filterRisks;
  Filter filterIncidences;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  FlowPanel searchResultPanel;

  RiskList risksSearchResultPanel;
  RiskIncidenceList incidencesSearchResultPanel;

  AdvancedSearchFieldsPanel risksSearchAdvancedFieldsPanel;
  AdvancedSearchFieldsPanel incidencesSearchAdvancedFieldsPanel;

  String selectedItem = Risk.class.getName();
  String risksListId;
  String incidencesListId;

  public RiskSearch(String risksListId, String incidencesListId) {
    this.risksListId = risksListId;
    this.incidencesListId = incidencesListId;

    ValueChangeHandler<Integer> searchAdvancedFieldsPanelHandler = new ValueChangeHandler<Integer>() {
      @Override
      public void onValueChange(ValueChangeEvent<Integer> event) {
        searchPanel.setSearchAdvancedGoEnabled(event.getValue() == 0 ? false : true);
      }
    };

    risksSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_RISKS);
    incidencesSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_INCIDENCES);

    risksSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);
    incidencesSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);

    defaultFilters();

    searchPanel = new SearchPanel(filterRisks, RodaConstants.AIP_SEARCH, true, messages.searchPlaceHolder(), true, true,
      false);

    initWidget(uiBinder.createAndBindUi(this));

    searchPanel.setDropdownLabel(messages.searchListBoxRisks());
    searchPanel.addDropdownItem(messages.searchListBoxRisks(), RodaConstants.SEARCH_RISKS);
    searchPanel.addDropdownItem(messages.searchListBoxIncidences(), RodaConstants.SEARCH_INCIDENCES);
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
        if (selectedItem.equals(RiskIncidence.class.getName())) {
          incidencesSearchAdvancedFieldsPanel.addSearchFieldPanel();
        } else {
          risksSearchAdvancedFieldsPanel.addSearchFieldPanel();
        }
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    if (risksSearchResultPanel == null) {
      createRisksSearchResultPanel();
    }

    risksSearchResultPanel.setVisible(true);

    searchPanel.setVariables(filterRisks, RodaConstants.RISK_SEARCH, true, risksSearchResultPanel,
      risksSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(risksSearchAdvancedFieldsPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(risksSearchResultPanel);
    selectedItem = Risk.class.getName();
  }

  public void showIncidencesSearchAdvancedFieldsPanel() {
    if (incidencesSearchResultPanel == null) {
      createIncidencesSearchResultPanel();
    }

    incidencesSearchResultPanel.setVisible(true);

    searchPanel.setVariables(filterIncidences, RodaConstants.RISK_INCIDENCE_SEARCH, true, incidencesSearchResultPanel,
      incidencesSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(incidencesSearchAdvancedFieldsPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(incidencesSearchResultPanel);
    selectedItem = RiskIncidence.class.getName();
  }

  private void createRisksSearchResultPanel() {
    risksSearchResultPanel = new RiskList(risksListId, filterRisks, messages.searchResults(), true);
    ListSelectionUtils.bindBrowseOpener(risksSearchResultPanel);

    risksSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedRisk selected = risksSearchResultPanel.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.newHistory(RiskRegister.RESOLVER, ShowRisk.RESOLVER.getHistoryToken(), selected.getId());
        }
      }
    });
  }

  private void createIncidencesSearchResultPanel() {
    incidencesSearchResultPanel = new RiskIncidenceList(incidencesListId, filterIncidences, messages.searchResults(),
      true);
    ListSelectionUtils.bindBrowseOpener(incidencesSearchResultPanel);
  }

  public SelectedItems<? extends IsIndexed> getSelected() {
    SelectedItems<? extends IsIndexed> selected = null;

    if (risksSearchResultPanel != null && risksSearchResultPanel.hasElementsSelected()) {
      selected = risksSearchResultPanel.getSelected();
    } else if (incidencesSearchResultPanel != null && incidencesSearchResultPanel.hasElementsSelected()) {
      selected = incidencesSearchResultPanel.getSelected();
    }

    if (selected == null) {
      selected = new SelectedItemsList<>();
    }

    return selected;
  }

  public void refresh() {
    if (risksSearchResultPanel != null && risksSearchResultPanel.hasElementsSelected()) {
      risksSearchResultPanel.refresh();
    } else if (incidencesSearchResultPanel != null && incidencesSearchResultPanel.hasElementsSelected()) {
      incidencesSearchResultPanel.refresh();
    }
  }

  public void search() {
    if (searchPanel.getDropdownSelectedValue().equals(RodaConstants.SEARCH_INCIDENCES)) {
      showIncidencesSearchAdvancedFieldsPanel();
    } else {
      showSearchAdvancedFieldsPanel();
    }

    searchPanel.doSearch();
  }

  public void defaultFilters() {
    filterRisks = new Filter(Filter.ALL);
    filterIncidences = new Filter(Filter.ALL);
    if (searchPanel != null) {
      searchPanel.setDefaultFilterIncremental(false);
    }
  }

  public void setFilter(Filter filter) {
    risksSearchResultPanel.setFilter(filter);
  }

  public RiskList getList() {
    return risksSearchResultPanel;
  }
}
