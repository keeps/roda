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
package org.roda.wui.client.ingest.appraisal;

import java.util.ArrayList;
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
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ViewRepresentation;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.SearchPanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.utils.ListboxUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.search.SearchField;
import org.roda.wui.client.search.SearchFieldPanel;
import org.roda.wui.client.search.SearchSuggestBox;
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
import com.google.gwt.regexp.shared.RegExp;
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

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class IngestAppraisal extends Composite {

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
      return Tools.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "appraisal";
    }
  };

  private static final Filter DEFAULT_FILTER_AIP = new Filter(
    new SimpleFilterParameter(RodaConstants.STATE, AIPState.UNDER_APPRAISAL.toString()));
  private static final Filter DEFAULT_FILTER_REPRESENTATIONS = new Filter(
    new SimpleFilterParameter(RodaConstants.STATE, AIPState.UNDER_APPRAISAL.toString()));
  private static final Filter DEFAULT_FILTER_FILES = new Filter(
    new SimpleFilterParameter(RodaConstants.STATE, AIPState.UNDER_APPRAISAL.toString()));

  private static IngestAppraisal instance = null;

  public static IngestAppraisal getInstance() {
    if (instance == null) {
      instance = new IngestAppraisal();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, IngestAppraisal> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  FlowPanel ingestAppraisalDescription;

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
  Button acceptButton, rejectButton;

  ListBox searchAdvancedFieldOptions;

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();

  // cannot let reps and files to be selectable for now
  // boolean selectable = true;
  boolean itemsSelectable = true;
  boolean representationsSelectable = false;
  boolean filesSelectable = false;
  boolean justActive = false;

  private IngestAppraisal() {
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

    searchAdvancedFieldOptions = new ListBox();

    initWidget(uiBinder.createAndBindUi(this));

    ingestAppraisalDescription.add(new HTMLWidgetWrapper("IngestAppraisalDescription.html"));

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
          GWT.log("Error getting search fields", caught);
        }

        @Override
        public void onSuccess(List<SearchField> searchFields) {
          IngestAppraisal.this.searchFields.clear();
          for (SearchField searchField : searchFields) {
            ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptions, searchField.getLabel(),
              searchField.getId());
            IngestAppraisal.this.searchFields.put(searchField.getId(), searchField);
          }

          for (SearchField searchField : searchFields) {
            if (searchField.isFixed()) {
              SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
              searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
              searchFieldPanel.setSearchFields(IngestAppraisal.this.searchFields);
              addSearchFieldPanel(searchFieldPanel);
              searchFieldPanel.selectSearchField(searchField.getId());
            }
          }
        }
      });

    searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
        searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
        searchFieldPanel.setSearchFields(searchFields);
        searchFieldPanel.selectFirstSearchField();
        addSearchFieldPanel(searchFieldPanel);
      }
    });

    itemsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");
    filesSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");
    representationsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");

    createRepresentationsSearchAdvancedFieldsPanel();
    createFilesSearchAdvancedFieldsPanel();
    showSearchAdvancedFieldsPanel();

    acceptButton.setEnabled(false);
    rejectButton.setEnabled(false);
  }

  private void createRepresentationsSearchAdvancedFieldsPanel() {
    SearchFieldPanel idField = new SearchFieldPanel();
    SearchFieldPanel sizeField = new SearchFieldPanel();
    SearchFieldPanel numberOfFilesField = new SearchFieldPanel();
    SearchFieldPanel typeField = new SearchFieldPanel();

    idField.simpleSearchField(RodaConstants.REPRESENTATION_ID, messages.searchRepresentationFieldIdentifier(),
      RodaConstants.SEARCH_FIELD_TYPE_TEXT);
    typeField.simpleSearchField(RodaConstants.REPRESENTATION_ORIGINAL, messages.searchRepresentationFieldOriginal(),
      RodaConstants.SEARCH_FIELD_TYPE_BOOLEAN);
    sizeField.simpleSearchField(RodaConstants.REPRESENTATION_SIZE_IN_BYTES, messages.searchRepresentationFieldSize(),
      RodaConstants.SEARCH_FIELD_TYPE_STORAGE);
    numberOfFilesField.simpleSearchField(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES,
      messages.searchRepresentationFieldNumberOfFiles(), RodaConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL);

    representationsSearchAdvancedFieldsPanel.add(idField);
    representationsSearchAdvancedFieldsPanel.add(typeField);
    representationsSearchAdvancedFieldsPanel.add(sizeField);
    representationsSearchAdvancedFieldsPanel.add(numberOfFilesField);
  }

  private void createFilesSearchAdvancedFieldsPanel() {
    SearchFieldPanel filenameField = new SearchFieldPanel();
    SearchFieldPanel formatField = new SearchFieldPanel();
    SearchFieldPanel pronomField = new SearchFieldPanel();
    SearchFieldPanel mimetypeField = new SearchFieldPanel();
    SearchFieldPanel sizeField = new SearchFieldPanel();
    SearchFieldPanel fulltextField = new SearchFieldPanel();

    filenameField.simpleSearchField(RodaConstants.FILE_FILEID, messages.searchFileFieldFilename(),
      RodaConstants.SEARCH_FIELD_TYPE_TEXT);
    formatField.simpleSearchField(RodaConstants.FILE_FILEFORMAT, messages.searchFileFieldFormat(),
      RodaConstants.SEARCH_FIELD_TYPE_SUGGEST);
    pronomField.simpleSearchField(RodaConstants.FILE_PRONOM, messages.searchFileFieldPronom(),
      RodaConstants.SEARCH_FIELD_TYPE_SUGGEST);
    mimetypeField.simpleSearchField(RodaConstants.FILE_FORMAT_MIMETYPE, messages.searchFileFieldMimetype(),
      RodaConstants.SEARCH_FIELD_TYPE_SUGGEST);
    sizeField.simpleSearchField(RodaConstants.FILE_SIZE, messages.searchFileFieldFilesize(),
      RodaConstants.SEARCH_FIELD_TYPE_STORAGE);
    fulltextField.simpleSearchField(RodaConstants.FILE_FULLTEXT, messages.searchFileFieldFulltext(),
      RodaConstants.SEARCH_FIELD_TYPE_TEXT);

    formatField
      .addInputSearchSuggestBox(new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_FILEFORMAT));
    pronomField
      .addInputSearchSuggestBox(new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_PRONOM));
    mimetypeField.addInputSearchSuggestBox(
      new SearchSuggestBox<IndexedFile>(IndexedFile.class, RodaConstants.FILE_FORMAT_MIMETYPE));

    filesSearchAdvancedFieldsPanel.add(filenameField);
    filesSearchAdvancedFieldsPanel.add(formatField);
    filesSearchAdvancedFieldsPanel.add(pronomField);
    filesSearchAdvancedFieldsPanel.add(mimetypeField);
    filesSearchAdvancedFieldsPanel.add(sizeField);
    filesSearchAdvancedFieldsPanel.add(fulltextField);
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
    if (parameters.size() == 0) {
      filter = defaultFilter;
    } else {
      filter = new Filter(parameters);
    }

    return filter;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      itemsSearchResultPanel.refresh();
      callback.onSuccess(this);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  private void addSearchFieldPanel(final SearchFieldPanel searchFieldPanel) {
    itemsSearchAdvancedFieldsPanel.add(searchFieldPanel);
    itemsSearchAdvancedFieldsPanel.removeStyleName("empty");

    searchPanel.setSearchAdvancedGoEnabled(true);

    ClickHandler clickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        itemsSearchAdvancedFieldsPanel.remove(searchFieldPanel);
        if (itemsSearchAdvancedFieldsPanel.getWidgetCount() == 0) {
          itemsSearchAdvancedFieldsPanel.addStyleName("empty");
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

    searchPanel.setVariables(DEFAULT_FILTER_FILES, RodaConstants.REPRESENTATION_SEARCH, itemsSearchResultPanel,
      itemsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);

    searchResultPanel.clear();
    searchResultPanel.add(itemsSearchResultPanel);

    itemsFacets.setVisible(true);
    filesFacets.setVisible(false);
  }

  public void showRepresentationsSearchAdvancedFieldsPanel() {
    if (representationsSearchResultPanel == null) {
      createRepresentationsSearchResultPanel();
    }

    searchPanel.setVariables(DEFAULT_FILTER_REPRESENTATIONS, RodaConstants.REPRESENTATION_SEARCH,
      representationsSearchResultPanel, representationsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(false);

    searchResultPanel.clear();
    searchResultPanel.add(representationsSearchResultPanel);

    itemsFacets.setVisible(false);
    filesFacets.setVisible(false);
  }

  public void showFilesSearchAdvancedFieldsPanel() {
    if (filesSearchResultPanel == null) {
      createFilesSearchResultPanel();
    }

    searchPanel.setVariables(DEFAULT_FILTER_FILES, RodaConstants.FILE_SEARCH, filesSearchResultPanel,
      filesSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(false);

    searchResultPanel.clear();
    searchResultPanel.add(filesSearchResultPanel);

    itemsFacets.setVisible(false);
    filesFacets.setVisible(true);
  }

  private void createItemsSearchResultPanel() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));

    itemsSearchResultPanel = new AIPList(DEFAULT_FILTER_AIP, justActive, facets, messages.searchResults(),
      itemsSelectable);

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
        acceptButton.setEnabled(!empty);
        rejectButton.setEnabled(!empty);
      }
    });

  }

  private void createRepresentationsSearchResultPanel() {
    representationsSearchResultPanel = new RepresentationList(DEFAULT_FILTER_REPRESENTATIONS, justActive, Facets.NONE,
      messages.searchResults(), representationsSelectable);

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
          acceptButton.setEnabled(!empty);
          rejectButton.setEnabled(!empty);
        }
      });
  }

  private void createFilesSearchResultPanel() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.FILE_FILEFORMAT),
      new SimpleFacetParameter(RodaConstants.FILE_PRONOM),
      new SimpleFacetParameter(RodaConstants.FILE_FORMAT_MIMETYPE));

    filesSearchResultPanel = new SearchFileList(DEFAULT_FILTER_FILES, justActive, facets, messages.searchResults(),
      filesSelectable);

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
        acceptButton.setEnabled(!empty);
        rejectButton.setEnabled(!empty);
      }
    });
  }

  @UiHandler("acceptButton")
  void buttonAcceptHandler(ClickEvent e) {
    boolean accept = true;
    SelectedItems<?> selected = getSelected();
    String rejectReason = null;
    // not supporting accept of reps and files for now
    BrowserService.Util.getInstance().appraisal((SelectedItems<IndexedAIP>) selected, accept, rejectReason,
      new LoadingAsyncCallback<Void>() {

        @Override
        public void onSuccessImpl(Void result) {
          Toast.showInfo("Done", "All selected items were accepted");
          refresh();
        }
      });
  }

  @UiHandler("rejectButton")
  void buttonRejectHandler(ClickEvent e) {
    final boolean accept = false;
    final SelectedItems<?> selected = getSelected();
    Dialogs.showPromptDialog("Reject message", "What is the reason for rejecting these SIPs?", RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }

        @Override
        public void onSuccess(final String rejectReason) {
          // TODO support accept of reps and files
          BrowserService.Util.getInstance().appraisal((SelectedItems<IndexedAIP>) selected, accept, rejectReason,
            new LoadingAsyncCallback<Void>() {

              @Override
              public void onSuccessImpl(Void result) {
                Toast.showInfo("Done", "All selected items were rejected");
                refresh();
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

  public void refresh() {

    if (itemsSearchAdvancedFieldsPanel.isVisible()) {
      itemsSearchResultPanel.refresh();
    }

    if (representationsSearchResultPanel != null && representationsSearchAdvancedFieldsPanel.isVisible()) {
      representationsSearchResultPanel.refresh();
    }

    if (filesSearchResultPanel != null && filesSearchAdvancedFieldsPanel.isVisible()) {
      filesSearchResultPanel.refresh();
    }

  }

}
