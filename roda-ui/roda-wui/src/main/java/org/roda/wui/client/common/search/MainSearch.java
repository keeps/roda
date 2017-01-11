/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

public class MainSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, MainSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final Filter DEFAULT_FILTER_AIP = new Filter(
    new BasicSearchFilterParameter(RodaConstants.AIP_SEARCH, "*"));
  private static final Filter DEFAULT_FILTER_REPRESENTATIONS = new Filter(
    new BasicSearchFilterParameter(RodaConstants.REPRESENTATION_SEARCH, "*"));
  private static final Filter DEFAULT_FILTER_FILES = new Filter(
    new BasicSearchFilterParameter(RodaConstants.FILE_SEARCH, "*"));

  Filter filter_aips;
  Filter filter_representations;
  Filter filter_files;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  FlowPanel searchResultPanel;

  AIPList itemsSearchResultPanel;
  RepresentationList representationsSearchResultPanel;
  SearchFileList filesSearchResultPanel;

  AdvancedSearchFieldsPanel itemsSearchAdvancedFieldsPanel;
  AdvancedSearchFieldsPanel filesSearchAdvancedFieldsPanel;
  AdvancedSearchFieldsPanel representationsSearchAdvancedFieldsPanel;

  boolean justActive = true;
  boolean itemsSelectable = true;
  boolean representationsSelectable = true;
  boolean filesSelectable = true;
  String selectedItem = AIP.class.getName();

  FlowPanel itemsFacets;
  Map<FacetParameter, FlowPanel> itemsFacetsMap;
  Map<Button, Boolean> itemsButtons;
  List<Button> itemsSelectionButtons;
  FlowPanel representationsFacets;
  Map<FacetParameter, FlowPanel> representationsFacetsMap;
  Map<Button, Boolean> representationsButtons;
  List<Button> representationsSelectionButtons;
  FlowPanel filesFacets;
  Map<FacetParameter, FlowPanel> filesFacetsMap;
  Map<Button, Boolean> filesButtons;
  List<Button> filesSelectionButtons;

  public MainSearch(boolean justActive, boolean itemsSelectable, boolean representationsSelectable,
    boolean filesSelectable, FlowPanel itemsFacets, Map<FacetParameter, FlowPanel> itemsFacetsMap,
    Map<Button, Boolean> itemsButtons, List<Button> itemsSelectionButtons, FlowPanel representationsFacets,
    Map<FacetParameter, FlowPanel> representationsFacetsMap, Map<Button, Boolean> representationsButtons,
    List<Button> representationsSelectionButtons, FlowPanel filesFacets, Map<FacetParameter, FlowPanel> filesFacetsMap,
    Map<Button, Boolean> filesButtons, List<Button> filesSelectionButtons) {
    this.justActive = justActive;
    this.itemsSelectable = itemsSelectable;
    this.representationsSelectable = representationsSelectable;
    this.filesSelectable = filesSelectable;

    this.itemsFacets = itemsFacets;
    this.itemsFacetsMap = itemsFacetsMap;
    this.itemsButtons = itemsButtons;
    this.itemsSelectionButtons = itemsSelectionButtons;

    this.representationsFacets = representationsFacets;
    this.representationsFacetsMap = representationsFacetsMap;
    this.representationsButtons = representationsButtons;
    this.representationsSelectionButtons = representationsSelectionButtons;

    this.filesFacets = filesFacets;
    this.filesFacetsMap = filesFacetsMap;
    this.filesButtons = filesButtons;
    this.filesSelectionButtons = filesSelectionButtons;

    ValueChangeHandler<Integer> searchAdvancedFieldsPanelHandler = new ValueChangeHandler<Integer>() {

      @Override
      public void onValueChange(ValueChangeEvent<Integer> event) {
        searchPanel.setSearchAdvancedGoEnabled(event.getValue() == 0 ? false : true);
      }
    };

    itemsSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_ITEMS);
    representationsSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_REPRESENTATIONS);
    filesSearchAdvancedFieldsPanel = new AdvancedSearchFieldsPanel(RodaConstants.SEARCH_FILES);

    itemsSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);
    representationsSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);
    filesSearchAdvancedFieldsPanel.addValueChangeHandler(searchAdvancedFieldsPanelHandler);

    defaultFilters();

    searchPanel = new SearchPanel(filter_aips, RodaConstants.AIP_SEARCH, true, messages.searchPlaceHolder(), true, true,
      false);

    initWidget(uiBinder.createAndBindUi(this));

    searchPanel.setDropdownLabel(messages.searchListBoxItems());
    searchPanel.addDropdownItem(messages.searchListBoxItems(), RodaConstants.SEARCH_ITEMS);
    searchPanel.addDropdownItem(messages.searchListBoxRepresentations(), RodaConstants.SEARCH_REPRESENTATIONS);
    searchPanel.addDropdownItem(messages.searchListBoxFiles(), RodaConstants.SEARCH_FILES);

    searchPanel.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        search();
      }
    });

    searchPanel.addDropdownPopupStyleName("searchInputListBoxPopup");

    // handler aqui
    searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (selectedItem.equals(Representation.class.getName())) {
          representationsSearchAdvancedFieldsPanel.addSearchFieldPanel();
        } else if (selectedItem.equals(File.class.getName())) {
          filesSearchAdvancedFieldsPanel.addSearchFieldPanel();
        } else {
          itemsSearchAdvancedFieldsPanel.addSearchFieldPanel();
        }
      }
    });
  }

  public boolean isJustActive() {
    return justActive;
  }

  public void showSearchAdvancedFieldsPanel() {
    if (itemsSearchResultPanel == null) {
      createItemsSearchResultPanel();
    }

    searchPanel.setVariables(filter_aips, RodaConstants.AIP_SEARCH, true, itemsSearchResultPanel,
      itemsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(itemsSearchAdvancedFieldsPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(itemsSearchResultPanel);
    selectedItem = AIP.class.getName();

    itemsFacets.setVisible(true);
    representationsFacets.setVisible(false);
    filesFacets.setVisible(false);

    setButtons(itemsButtons);
    setButtonsEnabled(itemsSelectionButtons, false);
  }

  public void showRepresentationsSearchAdvancedFieldsPanel() {
    if (representationsSearchResultPanel == null) {
      createRepresentationsSearchResultPanel();
    }

    searchPanel.setVariables(filter_representations, RodaConstants.REPRESENTATION_SEARCH, true,
      representationsSearchResultPanel, representationsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel
      .setSearchAdvancedGoEnabled(representationsSearchAdvancedFieldsPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(representationsSearchResultPanel);
    selectedItem = Representation.class.getName();

    itemsFacets.setVisible(false);
    representationsFacets.setVisible(true);
    filesFacets.setVisible(false);

    setButtons(representationsButtons);
    setButtonsEnabled(representationsSelectionButtons, false);
  }

  public void showFilesSearchAdvancedFieldsPanel() {
    if (filesSearchResultPanel == null) {
      createFilesSearchResultPanel();
    }

    searchPanel.setVariables(filter_files, RodaConstants.FILE_SEARCH, true, filesSearchResultPanel,
      filesSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
    searchPanel.setSearchAdvancedGoEnabled(filesSearchAdvancedFieldsPanel.getWidgetCount() == 0 ? false : true);

    searchResultPanel.clear();
    searchResultPanel.add(filesSearchResultPanel);
    selectedItem = File.class.getName();

    itemsFacets.setVisible(false);
    representationsFacets.setVisible(false);
    filesFacets.setVisible(true);

    setButtons(filesButtons);
    setButtonsEnabled(filesSelectionButtons, false);
  }

  private void createItemsSearchResultPanel() {
    Facets facets = new Facets(itemsFacetsMap.keySet());
    itemsSearchResultPanel = new AIPList(filter_aips, justActive, facets, messages.searchResults(), itemsSelectable);

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    for (FacetParameter facetParameter : itemsFacetsMap.keySet()) {
      facetPanels.put(facetParameter.getName(), itemsFacetsMap.get(facetParameter));
    }
    FacetUtils.bindFacets(itemsSearchResultPanel, facetPanels);

    itemsSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedAIP aip = itemsSearchResultPanel.getSelectionModel().getSelectedObject();
        if (aip != null) {
          HistoryUtils.openBrowse(aip);
        }
      }
    });

    itemsSearchResultPanel.addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedAIP>() {

      @Override
      public void onSelectionChange(SelectedItems<IndexedAIP> selected) {
        setButtonsEnabled(itemsSelectionButtons, !(ClientSelectedItemsUtils.isEmpty(selected)));
      }
    });
  }

  private void createRepresentationsSearchResultPanel() {
    Facets facets = new Facets(representationsFacetsMap.keySet());
    representationsSearchResultPanel = new RepresentationList(filter_representations, justActive, facets,
      messages.searchResults(), representationsSelectable);

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    for (FacetParameter facetParameter : representationsFacetsMap.keySet()) {
      facetPanels.put(facetParameter.getName(), representationsFacetsMap.get(facetParameter));
    }
    FacetUtils.bindFacets(representationsSearchResultPanel, facetPanels);

    representationsSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedRepresentation rep = representationsSearchResultPanel.getSelectionModel().getSelectedObject();
        if (rep != null) {
          HistoryUtils.openBrowse(rep);
        }
      }
    });

    representationsSearchResultPanel
      .addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedRepresentation>() {

        @Override
        public void onSelectionChange(SelectedItems<IndexedRepresentation> selected) {
          setButtonsEnabled(representationsSelectionButtons, !(ClientSelectedItemsUtils.isEmpty(selected)));
        }
      });
  }

  private void createFilesSearchResultPanel() {
    Facets facets = new Facets(filesFacetsMap.keySet());
    boolean showFilesPath = false;
    filesSearchResultPanel = new SearchFileList(filter_files, justActive, facets, messages.searchResults(),
      filesSelectable, showFilesPath);

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    for (FacetParameter facetParameter : filesFacetsMap.keySet()) {
      facetPanels.put(facetParameter.getName(), filesFacetsMap.get(facetParameter));
    }
    FacetUtils.bindFacets(filesSearchResultPanel, facetPanels);

    filesSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedFile file = filesSearchResultPanel.getSelectionModel().getSelectedObject();
        if (file != null) {
          HistoryUtils.openBrowse(file);
        }
      }
    });

    filesSearchResultPanel.addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedFile>() {

      @Override
      public void onSelectionChange(SelectedItems<IndexedFile> selected) {
        setButtonsEnabled(filesSelectionButtons, !(ClientSelectedItemsUtils.isEmpty(selected)));
      }
    });
  }

  public SelectedItems<? extends IsIndexed> getSelected() {
    SelectedItems<? extends IsIndexed> selected = null;

    if (itemsSearchResultPanel != null && itemsSearchResultPanel.hasElementsSelected()) {
      selected = itemsSearchResultPanel.getSelected();
    } else if (representationsSearchResultPanel != null && representationsSearchResultPanel.hasElementsSelected()) {
      selected = representationsSearchResultPanel.getSelected();
    } else if (filesSearchResultPanel != null && filesSearchResultPanel.hasElementsSelected()) {
      selected = filesSearchResultPanel.getSelected();
    }

    if (selected == null) {
      selected = new SelectedItemsList<>();
    }

    return selected;
  }

  public void clearSelected() {

    if (itemsSearchResultPanel != null) {
      itemsSearchResultPanel.clearSelected();
    }

    if (representationsSearchResultPanel != null) {
      representationsSearchResultPanel.clearSelected();
    }

    if (filesSearchResultPanel != null) {
      filesSearchResultPanel.clearSelected();
    }
  }

  public void refresh() {
    if (itemsSearchResultPanel != null && itemsSearchResultPanel.hasElementsSelected()) {
      itemsSearchResultPanel.refresh();
    } else if (representationsSearchResultPanel != null && representationsSearchResultPanel.hasElementsSelected()) {
      representationsSearchResultPanel.refresh();
    } else if (filesSearchResultPanel != null && filesSearchResultPanel.hasElementsSelected()) {
      filesSearchResultPanel.refresh();
    }
  }

  private void setButtons(Map<Button, Boolean> buttonsMap) {
    for (Button button : buttonsMap.keySet()) {
      button.setVisible(buttonsMap.get(button));
    }
  }

  private void setButtonsEnabled(List<Button> buttons, boolean active) {
    for (Button button : buttons) {
      button.setEnabled(active);
    }
  }

  public boolean setSearch(List<String> historyTokens) {
    // #search/TYPE/key/value/key/value
    String type = historyTokens.get(0);

    boolean successful = searchPanel.setDropdownSelectedValue(type, false);
    if (successful) {
      List<FilterParameter> params = new ArrayList<>();
      for (int i = 1; i < historyTokens.size() - 1; i += 2) {
        String key = historyTokens.get(i);
        String value = historyTokens.get(i + 1);

        params.add(new SimpleFilterParameter(key, value));
      }

      if (!params.isEmpty()) {
        if (searchPanel.isDefaultFilterIncremental()) {
          filter_aips.add(params);
          filter_representations.add(params);
          filter_files.add(params);
        } else {
          Filter filter = new Filter(params);
          filter_aips = filter;
          filter_representations = filter;
          filter_files = filter;
          searchPanel.setDefaultFilter(filter, true);
        }
      }
    }

    return successful;
  }

  public void search() {
    if (searchPanel.getDropdownSelectedValue().equals(RodaConstants.SEARCH_ITEMS)) {
      showSearchAdvancedFieldsPanel();
    } else if (searchPanel.getDropdownSelectedValue().equals(RodaConstants.SEARCH_REPRESENTATIONS)) {
      showRepresentationsSearchAdvancedFieldsPanel();
    } else {
      showFilesSearchAdvancedFieldsPanel();
    }
    searchPanel.doSearch();
  }

  public void defaultFilters() {
    filter_aips = new Filter(DEFAULT_FILTER_AIP);
    filter_representations = new Filter(DEFAULT_FILTER_REPRESENTATIONS);
    filter_files = new Filter(DEFAULT_FILTER_FILES);
    if (searchPanel != null) {
      searchPanel.setDefaultFilterIncremental(false);
    }
  }

  public void setDefaultFilters(Filter defaultFilter) {
    filter_aips = new Filter(defaultFilter);
    filter_representations = new Filter(defaultFilter);
    filter_files = new Filter(defaultFilter);
    if (searchPanel != null) {
      searchPanel.setDefaultFilterIncremental(true);
    }
  }

  public void setAipFilter(Filter filter) {
    filter_aips = filter;
  }
}
