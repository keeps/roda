/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.FacetParameter;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ViewRepresentation;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.ListboxUtils;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
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

  FlowPanel itemsSearchAdvancedFieldsPanel;
  FlowPanel filesSearchAdvancedFieldsPanel;
  FlowPanel representationsSearchAdvancedFieldsPanel;

  ListBox searchAdvancedFieldOptionsAIP;
  ListBox searchAdvancedFieldOptionsRepresentation;
  ListBox searchAdvancedFieldOptionsFile;

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();

  private static Map<String, SearchField> representationFields = new HashMap<String, SearchField>();

  static {
    representationFields.put(RodaConstants.REPRESENTATION_ID,
      new SearchField(RodaConstants.REPRESENTATION_ID, Arrays.asList(RodaConstants.REPRESENTATION_ID),
        messages.searchRepresentationFieldIdentifier(), RodaConstants.SEARCH_FIELD_TYPE_TEXT));
    representationFields.put(RodaConstants.REPRESENTATION_TYPE,
      new SearchField(RodaConstants.REPRESENTATION_TYPE, Arrays.asList(RodaConstants.REPRESENTATION_TYPE),
        messages.searchRepresentationFieldType(), RodaConstants.SEARCH_FIELD_TYPE_SUGGEST));
    representationFields.put(RodaConstants.REPRESENTATION_ORIGINAL,
      new SearchField(RodaConstants.REPRESENTATION_ORIGINAL, Arrays.asList(RodaConstants.REPRESENTATION_ORIGINAL),
        messages.searchRepresentationFieldOriginal(), RodaConstants.SEARCH_FIELD_TYPE_BOOLEAN));
    representationFields.put(RodaConstants.REPRESENTATION_SIZE_IN_BYTES,
      new SearchField(RodaConstants.REPRESENTATION_SIZE_IN_BYTES,
        Arrays.asList(RodaConstants.REPRESENTATION_SIZE_IN_BYTES), messages.searchRepresentationFieldSize(),
        RodaConstants.SEARCH_FIELD_TYPE_STORAGE));
    representationFields.put(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES,
      new SearchField(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES,
        Arrays.asList(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES),
        messages.searchRepresentationFieldNumberOfFiles(), RodaConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL));
  }

  private static Map<String, SearchField> fileFields = new HashMap<String, SearchField>();

  static {
    fileFields.put(RodaConstants.FILE_FILE_ID,
      new SearchField(RodaConstants.FILE_FILE_ID, Arrays.asList(RodaConstants.FILE_FILE_ID),
        messages.searchFileFieldFilename(), RodaConstants.SEARCH_FIELD_TYPE_TEXT));
    fileFields.put(RodaConstants.FILE_FILEFORMAT,
      new SearchField(RodaConstants.FILE_FILEFORMAT, Arrays.asList(RodaConstants.FILE_FILEFORMAT),
        messages.searchFileFieldFormat(), RodaConstants.SEARCH_FIELD_TYPE_SUGGEST));
    fileFields.put(RodaConstants.FILE_PRONOM,
      new SearchField(RodaConstants.FILE_PRONOM, Arrays.asList(RodaConstants.FILE_PRONOM),
        messages.searchFileFieldPronom(), RodaConstants.SEARCH_FIELD_TYPE_SUGGEST));
    fileFields.put(RodaConstants.FILE_FORMAT_MIMETYPE,
      new SearchField(RodaConstants.FILE_FORMAT_MIMETYPE, Arrays.asList(RodaConstants.FILE_FORMAT_MIMETYPE),
        messages.searchFileFieldMimetype(), RodaConstants.SEARCH_FIELD_TYPE_SUGGEST));
    fileFields.put(RodaConstants.FILE_SIZE,
      new SearchField(RodaConstants.FILE_SIZE, Arrays.asList(RodaConstants.FILE_SIZE),
        messages.searchFileFieldFilesize(), RodaConstants.SEARCH_FIELD_TYPE_STORAGE));
    fileFields.put(RodaConstants.FILE_FULLTEXT,
      new SearchField(RodaConstants.FILE_FULLTEXT, Arrays.asList(RodaConstants.FILE_FULLTEXT),
        messages.searchFileFieldFulltext(), RodaConstants.SEARCH_FIELD_TYPE_TEXT));
  }

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

    itemsSearchAdvancedFieldsPanel = new FlowPanel();
    filesSearchAdvancedFieldsPanel = new FlowPanel();
    representationsSearchAdvancedFieldsPanel = new FlowPanel();

    defaultFilters();

    searchPanel = new SearchPanel(filter_aips, RodaConstants.AIP_SEARCH, messages.searchPlaceHolder(), true, true,
      false);
    searchPanel.setDefaultFilterIncremental(false);

    searchAdvancedFieldOptionsAIP = new ListBox();
    searchAdvancedFieldOptionsRepresentation = new ListBox();
    searchAdvancedFieldOptionsFile = new ListBox();

    initWidget(uiBinder.createAndBindUi(this));

    searchPanel.setDropdownLabel(messages.searchListBoxItems());
    searchPanel.addDropdownItem(messages.searchListBoxItems(), RodaConstants.SEARCH_LIST_BOX_ITEMS);
    searchPanel.addDropdownItem(messages.searchListBoxRepresentations(), RodaConstants.SEARCH_LIST_BOX_REPRESENTATIONS);
    searchPanel.addDropdownItem(messages.searchListBoxFiles(), RodaConstants.SEARCH_LIST_BOX_FILES);

    searchPanel.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        search();
      }
    });

    searchPanel.addDropdownPopupStyleName("searchInputListBoxPopup");

    BrowserService.Util.getInstance().retrieveSearchFields(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<List<SearchField>>() {
        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(List<SearchField> searchFields) {
          MainSearch.this.searchFields.clear();
          for (SearchField searchField : searchFields) {
            ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptionsAIP, searchField.getLabel(),
              searchField.getId());
            MainSearch.this.searchFields.put(searchField.getId(), searchField);
          }

          for (SearchField searchField : searchFields) {
            if (searchField.isFixed()) {
              addSearchFieldPanel(searchAdvancedFieldOptionsAIP, MainSearch.this.searchFields, searchField.getId(),
                itemsSearchAdvancedFieldsPanel);
            }
          }
        }
      });

    // handler aqui
    searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (selectedItem.equals(Representation.class.getName())) {
          addSearchFieldPanel(searchAdvancedFieldOptionsRepresentation, representationFields, null,
            representationsSearchAdvancedFieldsPanel);
        } else if (selectedItem.equals(File.class.getName())) {
          addSearchFieldPanel(searchAdvancedFieldOptionsFile, fileFields, null, filesSearchAdvancedFieldsPanel);
        } else {
          addSearchFieldPanel(searchAdvancedFieldOptionsAIP, searchFields, null, itemsSearchAdvancedFieldsPanel);
        }
      }
    });

    itemsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");
    filesSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");
    representationsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");

    createItemsSearchResultPanel();
    createRepresentationsSearchAdvancedFieldsPanel();
    createFilesSearchAdvancedFieldsPanel();
  }

  public boolean isJustActive() {
    return justActive;
  }

  private void createRepresentationsSearchAdvancedFieldsPanel() {
    for (SearchField searchField : representationFields.values()) {
      ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptionsRepresentation, searchField.getLabel(),
        searchField.getId());
    }

    for (SearchField searchField : representationFields.values()) {
      addSearchFieldPanel(searchAdvancedFieldOptionsRepresentation, representationFields, searchField.getId(),
        representationsSearchAdvancedFieldsPanel);
    }
  }

  private void createFilesSearchAdvancedFieldsPanel() {
    for (SearchField searchField : fileFields.values()) {
      ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptionsFile, searchField.getLabel(),
        searchField.getId());
    }

    for (SearchField searchField : fileFields.values()) {
      addSearchFieldPanel(searchAdvancedFieldOptionsFile, fileFields, searchField.getId(),
        filesSearchAdvancedFieldsPanel);
    }
  }

  private void addSearchFieldPanel(ListBox searchAdvancedFieldOptions, Map<String, SearchField> searchFields,
    String selectedField, final FlowPanel searchAdvancedFieldPanel) {
    final SearchFieldPanel searchFieldPanel = new SearchFieldPanel();

    searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
    searchFieldPanel.setSearchFields(searchFields);

    if (selectedField == null) {
      selectedField = searchFieldPanel.getFirstSearchField();
    }

    searchFieldPanel.selectSearchField(selectedField);

    ClickHandler clickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        searchAdvancedFieldPanel.remove(searchFieldPanel);
        if (searchAdvancedFieldPanel.getWidgetCount() == 0) {
          searchAdvancedFieldPanel.addStyleName("empty");
          searchPanel.setSearchAdvancedGoEnabled(false);
          addWarningToDuplicateFields(searchAdvancedFieldPanel);
        }
      }
    };

    searchFieldPanel.addRemoveClickHandler(clickHandler);
    searchFieldPanel.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        setSuggestions(event.getValue(), searchFieldPanel);
      }
    });

    searchFieldPanel.addListBoxChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        addWarningToDuplicateFields(searchAdvancedFieldPanel);
      }
    });

    setSuggestions(selectedField, searchFieldPanel);

    searchAdvancedFieldPanel.add(searchFieldPanel);
    searchAdvancedFieldPanel.removeStyleName("empty");

    searchPanel.setSearchAdvancedGoEnabled(true);
    addWarningToDuplicateFields(searchAdvancedFieldPanel);
  }

  private void addWarningToDuplicateFields(final FlowPanel searchAdvancedFieldPanel) {
    List<String> activeFields = new ArrayList<String>();
    for (int i = 0; i < searchAdvancedFieldPanel.getWidgetCount(); i++) {
      Widget widget = searchAdvancedFieldPanel.getWidget(i);

      if (widget instanceof SearchFieldPanel) {
        SearchFieldPanel fieldPanel = (SearchFieldPanel) widget;
        ListBox listBox = fieldPanel.getAdvancedFieldBox();

        if (activeFields.contains(listBox.getSelectedValue())) {
          fieldPanel.setWarningVisible(true, listBox.getSelectedValue());
        } else {
          activeFields.add(listBox.getSelectedValue());
          fieldPanel.setWarningVisible(false);
        }
      }
    }
  }

  private void setSuggestions(String searchFieldId, SearchFieldPanel searchFieldPanel) {
    if (searchFieldId.equals(RodaConstants.FILE_FILEFORMAT)) {
      searchFieldPanel
        .addInputSearchSuggestBox(new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_FILEFORMAT));
    } else if (searchFieldId.equals(RodaConstants.FILE_PRONOM)) {
      searchFieldPanel
        .addInputSearchSuggestBox(new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_PRONOM));
    } else if (searchFieldId.equals(RodaConstants.FILE_FORMAT_MIMETYPE)) {
      searchFieldPanel.addInputSearchSuggestBox(
        new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_FORMAT_MIMETYPE));
    } else if (searchFieldId.equals(RodaConstants.REPRESENTATION_TYPE)) {
      searchFieldPanel.addInputSearchSuggestBox(
        new SearchSuggestBox<IndexedRepresentation>(IndexedRepresentation.class, RodaConstants.REPRESENTATION_TYPE));
    }
  }

  public void showSearchAdvancedFieldsPanel() {
    if (itemsSearchResultPanel == null) {
      createItemsSearchResultPanel();
    }

    searchPanel.setVariables(filter_aips, RodaConstants.AIP_SEARCH, itemsSearchResultPanel,
      itemsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);

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

    searchPanel.setVariables(filter_representations, RodaConstants.REPRESENTATION_SEARCH,
      representationsSearchResultPanel, representationsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);

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

    searchPanel.setVariables(filter_files, RodaConstants.FILE_SEARCH, filesSearchResultPanel,
      filesSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);

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
          Tools.newHistory(Browse.RESOLVER, aip.getId());
        }
      }
    });

    itemsSearchResultPanel.addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedAIP>() {

      @Override
      public void onSelectionChange(SelectedItems<IndexedAIP> selected) {
        setButtonsEnabled(itemsSelectionButtons, !(SelectedItemsUtils.isEmpty(selected)));
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
          Tools.newHistory(ViewRepresentation.RESOLVER, rep.getAipId(), rep.getUUID());
        }
      }
    });

    representationsSearchResultPanel
      .addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedRepresentation>() {

        @Override
        public void onSelectionChange(SelectedItems<IndexedRepresentation> selected) {
          setButtonsEnabled(representationsSelectionButtons, !(SelectedItemsUtils.isEmpty(selected)));
        }
      });
  }

  private void createFilesSearchResultPanel() {
    Facets facets = new Facets(filesFacetsMap.keySet());
    filesSearchResultPanel = new SearchFileList(filter_files, justActive, facets, messages.searchResults(),
      filesSelectable);

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
          ViewRepresentation.jumpTo(file);
        }
      }
    });

    filesSearchResultPanel.addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedFile>() {

      @Override
      public void onSelectionChange(SelectedItems<IndexedFile> selected) {
        setButtonsEnabled(filesSelectionButtons, !(SelectedItemsUtils.isEmpty(selected)));
      }
    });
  }

  public SelectedItems<?> getSelected() {
    SelectedItems<?> selected = null;

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
          searchPanel.setDefaultFilterIncremental(true);
        }
      }
    }

    return successful;
  }

  public void search() {
    if (searchPanel.getDropdownSelectedValue().equals(RodaConstants.SEARCH_LIST_BOX_ITEMS)) {
      showSearchAdvancedFieldsPanel();
    } else if (searchPanel.getDropdownSelectedValue().equals(RodaConstants.SEARCH_LIST_BOX_REPRESENTATIONS)) {
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
}
