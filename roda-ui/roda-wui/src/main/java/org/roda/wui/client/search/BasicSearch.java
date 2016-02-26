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
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ViewRepresentation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.utils.ListboxUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

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
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class BasicSearch extends Composite {

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
    new BasicSearchFilterParameter(RodaConstants.SRO_SEARCH, "*"));
  private static final Filter DEFAULT_FILTER_FILES = new Filter(
    new BasicSearchFilterParameter(RodaConstants.FILE_SEARCH, "*"));

  private static BasicSearch instance = null;

  public static BasicSearch getInstance() {
    if (instance == null) {
      instance = new BasicSearch();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, BasicSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  FlowPanel searchDescription;

  @UiField
  ListBox searchInputListBox;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  AccessibleFocusPanel searchAdvancedDisclosureButton;

  @UiField
  FlowPanel searchResultPanel;

  @UiField
  FlowPanel searchAdvancedPanel;

  @UiField
  FlowPanel itemsSearchAdvancedFieldsPanel;

  @UiField
  FlowPanel filesSearchAdvancedFieldsPanel;

  @UiField
  FlowPanel representationsSearchAdvancedFieldsPanel;

  @UiField
  Button searchAdvancedFieldOptionsAdd;

  @UiField
  Button searchAdvancedGo;

  // FILTERS
  @UiField(provided = true)
  FlowPanel facetDescriptionLevels;
  @UiField(provided = true)
  FlowPanel facetHasRepresentations;

  AIPList itemsSearchResultPanel;
  RepresentationList representationsSearchResultPanel;
  SearchFileList filesSearchResultPanel;

  ListBox searchAdvancedFieldOptions;

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();

  private BasicSearch() {
    facetDescriptionLevels = new FlowPanel();
    facetHasRepresentations = new FlowPanel();

    searchAdvancedFieldOptions = new ListBox();

    initWidget(uiBinder.createAndBindUi(this));

    searchDescription.add(new HTMLWidgetWrapper("SearchDescription.html"));

    searchInputListBox.addItem(messages.searchListBoxItems(), RodaConstants.SEARCH_LIST_BOX_ITEMS);
    searchInputListBox.addItem(messages.searchListBoxRepresentations(), RodaConstants.SEARCH_LIST_BOX_REPRESENTATIONS);
    searchInputListBox.addItem(messages.searchListBoxFiles(), RodaConstants.SEARCH_LIST_BOX_FILES);

    searchInputListBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        if (searchInputListBox.getSelectedValue().equals(RodaConstants.SEARCH_LIST_BOX_ITEMS)) {
          showSearchAdvancedFieldsPanel();
        } else if (searchInputListBox.getSelectedValue().equals(RodaConstants.SEARCH_LIST_BOX_REPRESENTATIONS)) {
          showRepresentationsSearchAdvancedFieldsPanel();
        } else {
          showFilesSearchAdvancedFieldsPanel();
        }
      }
    });

    searchInputBox.getElement().setPropertyString("placeholder", messages.searchPlaceHolder());
    searchAdvancedPanel.setVisible(false);

    searchInputBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        doSearch();
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });

    searchAdvancedDisclosureButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        showSearchAdvancedPanel();
      }

    });

    BrowserService.Util.getInstance().getSearchFields(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<List<SearchField>>() {
        @Override
        public void onFailure(Throwable caught) {
          GWT.log("Error getting search fields", caught);
        }

        @Override
        public void onSuccess(List<SearchField> searchFields) {
          BasicSearch.this.searchFields.clear();
          for (SearchField searchField : searchFields) {
            ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptions, searchField.getLabel(),
              searchField.getId());
            BasicSearch.this.searchFields.put(searchField.getId(), searchField);
          }

          for (SearchField searchField : searchFields) {
            if (searchField.isFixed()) {
              SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
              searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
              searchFieldPanel.setSearchFields(BasicSearch.this.searchFields);
              addSearchFieldPanel(searchFieldPanel);
              searchFieldPanel.selectSearchField(searchField.getId());
            }
          }
        }
      });

    createRepresentationsSearchAdvancedFieldsPanel();
    createFilesSearchAdvancedFieldsPanel();
    showSearchAdvancedFieldsPanel();
  }

  // TODO define search fields
  private void createRepresentationsSearchAdvancedFieldsPanel() {
    SearchFieldPanel typeField = new SearchFieldPanel();
    typeField.simpleSearchField(RodaConstants.SRO_ORIGINAL, "Original", "boolean");
    representationsSearchAdvancedFieldsPanel.add(typeField);
  }

  // TODO define search fields
  private void createFilesSearchAdvancedFieldsPanel() {
    SearchFieldPanel filenameField = new SearchFieldPanel();
    SearchFieldPanel formatField = new SearchFieldPanel();
    SearchFieldPanel extensionField = new SearchFieldPanel();
    SearchFieldPanel mimetypeField = new SearchFieldPanel();
    SearchFieldPanel sizeField = new SearchFieldPanel();
    SearchFieldPanel fulltextField = new SearchFieldPanel();

    filenameField.simpleSearchField(RodaConstants.FILE_FILEID, "Filename", "text");
    formatField.simpleSearchField(RodaConstants.FILE_FILEFORMAT, "Format", "list");
    extensionField.simpleSearchField(RodaConstants.FILE_EXTENSION, "Extension", "text");
    mimetypeField.simpleSearchField(RodaConstants.FILE_FORMAT_MIMETYPE, "Mimetype", "text");
    sizeField.simpleSearchField(RodaConstants.FILE_SIZE, "Filesize", "storage");
    fulltextField.simpleSearchField(RodaConstants.FILE_FULLTEXT, "Fulltext", "text");

    filesSearchAdvancedFieldsPanel.add(filenameField);
    filesSearchAdvancedFieldsPanel.add(formatField);
    filesSearchAdvancedFieldsPanel.add(extensionField);
    filesSearchAdvancedFieldsPanel.add(mimetypeField);
    filesSearchAdvancedFieldsPanel.add(sizeField);
    filesSearchAdvancedFieldsPanel.add(fulltextField);
  }

  private void showSearchAdvancedPanel() {
    searchAdvancedPanel.setVisible(!searchAdvancedPanel.isVisible());
    if (searchAdvancedPanel.isVisible()) {
      searchAdvancedDisclosureButton.addStyleName("open");
    } else {
      searchAdvancedDisclosureButton.removeStyleName("open");
    }
  }

  public void doSearch() {
    Filter filter = DEFAULT_FILTER_AIP;
    String basicQuery = searchInputBox.getText();

    if (searchInputListBox.getSelectedValue().equals(RodaConstants.SEARCH_LIST_BOX_ITEMS)) {
      filter = buildSearchFilter(basicQuery, DEFAULT_FILTER_AIP, RodaConstants.AIP_SEARCH,
        itemsSearchAdvancedFieldsPanel);
      itemsSearchResultPanel.setFilter(filter);
    } else if (searchInputListBox.getSelectedValue().equals(RodaConstants.SEARCH_LIST_BOX_REPRESENTATIONS)) {
      filter = buildSearchFilter(basicQuery, DEFAULT_FILTER_REPRESENTATIONS, RodaConstants.SRO_SEARCH,
        representationsSearchAdvancedFieldsPanel);
      itemsSearchResultPanel.setFilter(filter);
    } else {
      filter = buildSearchFilter(basicQuery, DEFAULT_FILTER_FILES, RodaConstants.FILE_SEARCH,
        filesSearchAdvancedFieldsPanel);
      filesSearchResultPanel.setFilter(filter);
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

  @UiHandler("searchAdvancedFieldOptionsAdd")
  void handleSearchAdvancedFieldAdd(ClickEvent e) {
    SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
    searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
    searchFieldPanel.setSearchFields(searchFields);
    searchFieldPanel.selectFirstSearchField();
    addSearchFieldPanel(searchFieldPanel);
  }

  @UiHandler("searchAdvancedGo")
  void handleSearchAdvancedGo(ClickEvent e) {
    doSearch();
  }

  private void addSearchFieldPanel(final SearchFieldPanel searchFieldPanel) {
    itemsSearchAdvancedFieldsPanel.add(searchFieldPanel);
    itemsSearchAdvancedFieldsPanel.removeStyleName("empty");
    searchAdvancedGo.setEnabled(true);

    ClickHandler clickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        itemsSearchAdvancedFieldsPanel.remove(searchFieldPanel);
        if (itemsSearchAdvancedFieldsPanel.getWidgetCount() == 0) {
          itemsSearchAdvancedFieldsPanel.addStyleName("empty");
          searchAdvancedGo.setEnabled(false);
        }
      }
    };

    searchFieldPanel.addRemoveClickHandler(clickHandler);
  }

  public void showSearchAdvancedFieldsPanel() {
    if (itemsSearchResultPanel == null) {
      createItemsSearchResultPanel();
    }

    searchResultPanel.clear();
    searchResultPanel.add(itemsSearchResultPanel);

    itemsSearchAdvancedFieldsPanel.setVisible(true);
    filesSearchAdvancedFieldsPanel.setVisible(false);
    representationsSearchAdvancedFieldsPanel.setVisible(false);
  }

  public void showRepresentationsSearchAdvancedFieldsPanel() {
    if (representationsSearchResultPanel == null) {
      createRepresentationsSearchResultPanel();
    }

    searchResultPanel.clear();
    searchResultPanel.add(representationsSearchResultPanel);

    itemsSearchAdvancedFieldsPanel.setVisible(false);
    filesSearchAdvancedFieldsPanel.setVisible(false);
    representationsSearchAdvancedFieldsPanel.setVisible(true);
  }

  public void showFilesSearchAdvancedFieldsPanel() {
    if (filesSearchResultPanel == null) {
      createFilesSearchResultPanel();
    }

    searchResultPanel.clear();
    searchResultPanel.add(filesSearchResultPanel);

    itemsSearchAdvancedFieldsPanel.setVisible(false);
    filesSearchAdvancedFieldsPanel.setVisible(true);
    representationsSearchAdvancedFieldsPanel.setVisible(false);
  }

  private void createItemsSearchResultPanel() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    itemsSearchResultPanel = new AIPList(DEFAULT_FILTER_AIP, facets, messages.searchResults());

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

  }

  private void createRepresentationsSearchResultPanel() {
    representationsSearchResultPanel = new RepresentationList(DEFAULT_FILTER_REPRESENTATIONS, null,
      messages.searchResults());

    // TODO facets

    representationsSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedRepresentation rep = representationsSearchResultPanel.getSelectionModel().getSelectedObject();
        if (rep != null) {
          Tools.newHistory(ViewRepresentation.RESOLVER, rep.getAipId(), rep.getId());
        }
      }
    });
  }

  private void createFilesSearchResultPanel() {
    filesSearchResultPanel = new SearchFileList(DEFAULT_FILTER_FILES, null, messages.searchResults());

    // TODO facets

    filesSearchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedFile file = filesSearchResultPanel.getSelectionModel().getSelectedObject();
        if (file != null) {
          ViewRepresentation.jumpTo(file);
        }
      }
    });
  }
}
