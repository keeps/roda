/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
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
import org.roda.wui.client.common.CreateJob;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.SearchPanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.ListboxUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class Search extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "search";
    }
  };

  private static final Filter DEFAULT_FILTER_AIP = new Filter(
    new BasicSearchFilterParameter(RodaConstants.AIP_SEARCH, "*"));
  private static final Filter DEFAULT_FILTER_REPRESENTATIONS = new Filter(
    new BasicSearchFilterParameter(RodaConstants.REPRESENTATION_SEARCH, "*"));
  private static final Filter DEFAULT_FILTER_FILES = new Filter(
    new BasicSearchFilterParameter(RodaConstants.FILE_SEARCH, "*"));

  private static Search instance = null;

  public static Search getInstance() {
    if (instance == null) {
      instance = new Search();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, Search> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();

  @UiField
  FlowPanel searchDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  FlowPanel searchResultPanel;

  // FILTERS
  @UiField
  FlowPanel itemsFacets;
  @UiField(provided = true)
  FlowPanel facetDescriptionLevels;
  @UiField(provided = true)
  FlowPanel facetHasRepresentations;

  @UiField
  FlowPanel filesFacets;
  @UiField
  FlowPanel facetFormats;
  @UiField
  FlowPanel facetPronoms;
  @UiField
  FlowPanel facetMimetypes;

  AIPList itemsSearchResultPanel;
  RepresentationList representationsSearchResultPanel;
  SearchFileList filesSearchResultPanel;

  FlowPanel itemsSearchAdvancedFieldsPanel;
  FlowPanel filesSearchAdvancedFieldsPanel;
  FlowPanel representationsSearchAdvancedFieldsPanel;

  @UiField
  Button newJobButton;

  @UiField
  Button moveItem;

  ListBox searchAdvancedFieldOptionsAIP;
  ListBox searchAdvancedFieldOptionsRepresentation;
  ListBox searchAdvancedFieldOptionsFile;

  private static Map<String, SearchField> representationFields = new HashMap<String, SearchField>();
  static {
    representationFields.put(RodaConstants.REPRESENTATION_ID,
      new SearchField(RodaConstants.REPRESENTATION_ID, Arrays.asList(RodaConstants.REPRESENTATION_ID),
        messages.searchRepresentationFieldIdentifier(), RodaConstants.SEARCH_FIELD_TYPE_TEXT));
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
    fileFields.put(RodaConstants.FILE_FILEID,
      new SearchField(RodaConstants.FILE_FILEID, Arrays.asList(RodaConstants.FILE_FILEID),
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

  boolean selectable = true;
  boolean justActive = true;
  String selectedItem = AIP.class.getName();

  private Search() {
    facetDescriptionLevels = new FlowPanel();
    facetHasRepresentations = new FlowPanel();

    facetFormats = new FlowPanel();
    facetPronoms = new FlowPanel();
    facetMimetypes = new FlowPanel();

    itemsSearchAdvancedFieldsPanel = new FlowPanel();
    filesSearchAdvancedFieldsPanel = new FlowPanel();
    representationsSearchAdvancedFieldsPanel = new FlowPanel();

    searchPanel = new SearchPanel(DEFAULT_FILTER_AIP, RodaConstants.AIP_SEARCH, messages.searchPlaceHolder(), true,
      true);

    searchAdvancedFieldOptionsAIP = new ListBox();
    searchAdvancedFieldOptionsRepresentation = new ListBox();
    searchAdvancedFieldOptionsFile = new ListBox();

    initWidget(uiBinder.createAndBindUi(this));

    searchDescription.add(new HTMLWidgetWrapper("SearchDescription.html"));

    searchPanel.setDropdownLabel(messages.searchListBoxItems());
    searchPanel.addDropdownItem(messages.searchListBoxItems(), RodaConstants.SEARCH_LIST_BOX_ITEMS);
    searchPanel.addDropdownItem(messages.searchListBoxRepresentations(), RodaConstants.SEARCH_LIST_BOX_REPRESENTATIONS);
    searchPanel.addDropdownItem(messages.searchListBoxFiles(), RodaConstants.SEARCH_LIST_BOX_FILES);

    searchPanel.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        if (searchPanel.getDropdownSelectedValue().equals(RodaConstants.SEARCH_LIST_BOX_ITEMS)) {
          showSearchAdvancedFieldsPanel();
        } else if (searchPanel.getDropdownSelectedValue().equals(RodaConstants.SEARCH_LIST_BOX_REPRESENTATIONS)) {
          showRepresentationsSearchAdvancedFieldsPanel();
        } else {
          showFilesSearchAdvancedFieldsPanel();
        }
        searchPanel.doSearch();
      }
    });

    searchPanel.addDropdownPopupStyleName("searchInputListBoxPopup");

    BrowserService.Util.getInstance().getSearchFields(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<List<SearchField>>() {
        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(List<SearchField> searchFields) {
          Search.this.searchFields.clear();
          for (SearchField searchField : searchFields) {
            ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptionsAIP, searchField.getLabel(),
              searchField.getId());
            Search.this.searchFields.put(searchField.getId(), searchField);
          }

          for (SearchField searchField : searchFields) {
            if (searchField.isFixed()) {
              SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
              searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptionsAIP);
              searchFieldPanel.setSearchFields(Search.this.searchFields);
              addSearchFieldPanel(searchFieldPanel, itemsSearchAdvancedFieldsPanel);
              searchFieldPanel.selectSearchField(searchField.getId());
            }
          }
        }
      });

    // handler aqui
    searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (selectedItem.equals(Representation.class.getName())) {
          SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
          searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptionsRepresentation);
          searchFieldPanel.setSearchFields(representationFields);
          searchFieldPanel.selectFirstSearchField();
          addSearchFieldPanel(searchFieldPanel, representationsSearchAdvancedFieldsPanel);
        } else if (selectedItem.equals(File.class.getName())) {
          SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
          searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptionsFile);
          searchFieldPanel.setSearchFields(fileFields);
          searchFieldPanel.selectFirstSearchField();
          addSearchFieldPanel(searchFieldPanel, filesSearchAdvancedFieldsPanel);
        } else {
          SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
          searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptionsAIP);
          searchFieldPanel.setSearchFields(searchFields);
          searchFieldPanel.selectFirstSearchField();
          addSearchFieldPanel(searchFieldPanel, itemsSearchAdvancedFieldsPanel);
        }
      }
    });

    itemsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");
    filesSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");
    representationsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");

    createRepresentationsSearchAdvancedFieldsPanel();
    createFilesSearchAdvancedFieldsPanel();
    showSearchAdvancedFieldsPanel();

    newJobButton.setEnabled(false);
    moveItem.setEnabled(false);
  }

  private void createRepresentationsSearchAdvancedFieldsPanel() {
    for (SearchField searchField : representationFields.values()) {
      ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptionsRepresentation, searchField.getLabel(),
        searchField.getId());
    }

    for (SearchField searchField : representationFields.values()) {
      SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
      searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptionsRepresentation);
      searchFieldPanel.setSearchFields(representationFields);
      addSearchFieldPanel(searchFieldPanel, representationsSearchAdvancedFieldsPanel);
      searchFieldPanel.selectSearchField(searchField.getId());
    }
  }

  private void createFilesSearchAdvancedFieldsPanel() {
    for (SearchField searchField : fileFields.values()) {
      ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptionsFile, searchField.getLabel(),
        searchField.getId());
    }

    for (SearchField searchField : fileFields.values()) {
      SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
      searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptionsFile);
      searchFieldPanel.setSearchFields(fileFields);
      addSearchFieldPanel(searchFieldPanel, filesSearchAdvancedFieldsPanel);
      searchFieldPanel.selectSearchField(searchField.getId());

      if (searchField.getId().equals(RodaConstants.FILE_FILEFORMAT)) {
        searchFieldPanel.addInputSearchSuggestBox(
          new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_FILEFORMAT));
      } else if (searchField.getId().equals(RodaConstants.FILE_PRONOM)) {
        searchFieldPanel
          .addInputSearchSuggestBox(new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_PRONOM));
      } else if (searchField.getId().equals(RodaConstants.FILE_FORMAT_MIMETYPE)) {
        searchFieldPanel.addInputSearchSuggestBox(
          new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_FORMAT_MIMETYPE));
      }
    }
  }

  public Filter buildSearchFilter(String basicQuery, Filter defaultFilter, String allFilter, FlowPanel fieldsPanel) {
    List<FilterParameter> parameters = new ArrayList<FilterParameter>();

    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(allFilter, basicQuery));
    }

    for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
      SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
      FilterParameter filterParameter = searchAdvancedFieldPanel.getFilter();

      if (filterParameter != null) {
        parameters.add(filterParameter);
      }
    }

    Filter filter;
    if (parameters.isEmpty()) {
      filter = defaultFilter;
    } else {
      filter = new Filter(parameters);
    }

    return filter;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      itemsSearchResultPanel.refresh();
      callback.onSuccess(this);
    } else {
      // #search/TYPE/key/value/key/value
      String type = historyTokens.get(0);

      boolean successful = searchPanel.setDropdownSelectedValue(type);
      if (successful) {

        List<FilterParameter> params = new ArrayList<>();
        for (int i = 1; i < historyTokens.size() - 1; i += 2) {
          String key = historyTokens.get(i);
          String value = historyTokens.get(i + 1);

          GWT.log("search " + key + ": " + value);
          params.add(new SimpleFilterParameter(key, value));
        }
        if (!params.isEmpty()) {
          searchPanel.setDefaultFilter(new Filter(params));
          searchPanel.doSearch();
        }

        callback.onSuccess(this);
      } else {
        Toast.showError("Unrecognized search type: " + type);
        Tools.newHistory(RESOLVER);
        callback.onSuccess(null);
      }

    }
  }

  private void addSearchFieldPanel(final SearchFieldPanel searchFieldPanel, final FlowPanel searchAdvancedFieldPanel) {
    searchAdvancedFieldPanel.add(searchFieldPanel);
    searchAdvancedFieldPanel.removeStyleName("empty");

    searchPanel.setSearchAdvancedGoEnabled(true);

    ClickHandler clickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        searchAdvancedFieldPanel.remove(searchFieldPanel);
        if (searchAdvancedFieldPanel.getWidgetCount() == 0) {
          searchAdvancedFieldPanel.addStyleName("empty");
          searchPanel.setSearchAdvancedGoEnabled(false);
        }
      }
    };

    searchFieldPanel.addRemoveClickHandler(clickHandler);
  }

  public void showSearchAdvancedFieldsPanel() {
    if (itemsSearchResultPanel == null) {
      createItemsSearchResultPanel();
    }

    searchPanel.setVariables(DEFAULT_FILTER_AIP, RodaConstants.AIP_SEARCH, itemsSearchResultPanel,
      itemsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);

    searchResultPanel.clear();
    searchResultPanel.add(itemsSearchResultPanel);
    selectedItem = AIP.class.getName();

    itemsFacets.setVisible(true);
    filesFacets.setVisible(false);
    moveItem.setVisible(true);
  }

  public void showRepresentationsSearchAdvancedFieldsPanel() {
    if (representationsSearchResultPanel == null) {
      createRepresentationsSearchResultPanel();
    }

    searchPanel.setVariables(DEFAULT_FILTER_REPRESENTATIONS, RodaConstants.REPRESENTATION_SEARCH,
      representationsSearchResultPanel, representationsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);

    searchResultPanel.clear();
    searchResultPanel.add(representationsSearchResultPanel);
    selectedItem = Representation.class.getName();

    itemsFacets.setVisible(false);
    filesFacets.setVisible(false);
    moveItem.setVisible(false);
  }

  public void showFilesSearchAdvancedFieldsPanel() {
    if (filesSearchResultPanel == null) {
      createFilesSearchResultPanel();
    }

    searchPanel.setVariables(DEFAULT_FILTER_FILES, RodaConstants.FILE_SEARCH, filesSearchResultPanel,
      filesSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);

    searchResultPanel.clear();
    searchResultPanel.add(filesSearchResultPanel);
    selectedItem = File.class.getName();

    itemsFacets.setVisible(false);
    filesFacets.setVisible(true);
    moveItem.setVisible(false);
  }

  private void createItemsSearchResultPanel() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    itemsSearchResultPanel = new AIPList(DEFAULT_FILTER_AIP, justActive, facets, messages.searchResults(), selectable);

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.AIP_LEVEL, facetDescriptionLevels);
    facetPanels.put(RodaConstants.AIP_HAS_REPRESENTATIONS, facetHasRepresentations);
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
        boolean empty = SelectedItemsUtils.isEmpty(selected);
        newJobButton.setEnabled(!empty);
        moveItem.setEnabled(!empty);
      }
    });

  }

  private void createRepresentationsSearchResultPanel() {
    representationsSearchResultPanel = new RepresentationList(DEFAULT_FILTER_REPRESENTATIONS, justActive, Facets.NONE,
      messages.searchResults(), selectable);

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
          boolean empty = SelectedItemsUtils.isEmpty(selected);
          newJobButton.setEnabled(!empty);
        }
      });
  }

  private void createFilesSearchResultPanel() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.FILE_FILEFORMAT),
      new SimpleFacetParameter(RodaConstants.FILE_PRONOM),
      new SimpleFacetParameter(RodaConstants.FILE_FORMAT_MIMETYPE));

    filesSearchResultPanel = new SearchFileList(DEFAULT_FILTER_FILES, justActive, facets, messages.searchResults(),
      selectable);

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.FILE_FILEFORMAT, facetFormats);
    facetPanels.put(RodaConstants.FILE_PRONOM, facetPronoms);
    facetPanels.put(RodaConstants.FILE_FORMAT_MIMETYPE, facetMimetypes);
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
        boolean empty = SelectedItemsUtils.isEmpty(selected);
        newJobButton.setEnabled(!empty);
      }
    });
  }

  @UiHandler("newJobButton")
  void buttonStartIngestHandler(ClickEvent e) {
    Tools.newHistory(CreateJob.RESOLVER, "action");
  }

  @UiHandler("moveItem")
  void buttonMoveItemHandler(ClickEvent e) {
    final SelectedItems<IndexedAIP> selected = (SelectedItems<IndexedAIP>) getSelected();

    // Move all selected
    Filter filter;
    boolean showEmptyParentButton;

    filter = new Filter();
    showEmptyParentButton = false;

    SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, justActive);
    selectAipDialog.setEmptyParentButtonVisible(showEmptyParentButton);
    selectAipDialog.showAndCenter();
    selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
        final IndexedAIP parentAIP = event.getValue();
        final String parentId = (parentAIP != null) ? parentAIP.getId() : null;

        BrowserService.Util.getInstance().moveInHierarchy(selected, parentId, new LoadingAsyncCallback<IndexedAIP>() {

          @Override
          public void onSuccessImpl(IndexedAIP result) {
            if (result != null) {
              Tools.newHistory(Browse.RESOLVER, result.getId());
            } else {
              Tools.newHistory(Search.RESOLVER);
            }
          }

          @Override
          public void onFailureImpl(Throwable caught) {
            if (caught instanceof NotFoundException) {
              Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
            } else if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
              Toast.showError(messages.moveIllegalOperation(caught.getMessage()));
            }
          }
        });
      }
    });

  }

  public SelectedItems<?> getSelected() {
    SelectedItems<?> selected = null;

    if (itemsSearchAdvancedFieldsPanel.isVisible()) {
      selected = itemsSearchResultPanel.getSelected();
    }

    if (representationsSearchResultPanel != null && representationsSearchAdvancedFieldsPanel.isVisible()) {
      selected = representationsSearchResultPanel.getSelected();
    }

    if (filesSearchResultPanel != null && filesSearchAdvancedFieldsPanel.isVisible()) {
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

}
