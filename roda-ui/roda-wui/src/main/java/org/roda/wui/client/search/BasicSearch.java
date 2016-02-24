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
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AIPList;
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

  private static final Filter DEFAULT_FILTER = new Filter(new BasicSearchFilterParameter(RodaConstants.AIP__ALL, "*"));

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

  @UiField(provided = true)
  AIPList searchResultPanel;

  @UiField
  FlowPanel searchAdvancedPanel;

  @UiField
  FlowPanel searchAdvancedFieldsPanel;

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

  ListBox searchAdvancedFieldOptions;

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();

  private BasicSearch() {
    Filter filter = DEFAULT_FILTER;
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    searchResultPanel = new AIPList(filter, facets, messages.searchResults());
    facetDescriptionLevels = new FlowPanel();
    facetHasRepresentations = new FlowPanel();

    searchAdvancedFieldOptions = new ListBox();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.AIP_LEVEL, facetDescriptionLevels);
    facetPanels.put(RodaConstants.AIP_HAS_REPRESENTATIONS, facetHasRepresentations);
    FacetUtils.bindFacets(searchResultPanel, facetPanels);

    initWidget(uiBinder.createAndBindUi(this));

    searchDescription.add(new HTMLWidgetWrapper("SearchDescription.html"));

    searchInputListBox.addItem(messages.searchListBoxObjects(), RodaConstants.SEARCH_LIST_BOX_ITEMS);
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

    searchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedAIP aip = searchResultPanel.getSelectionModel().getSelectedObject();
        if (aip != null) {
          view(aip.getId());
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
              searchField.getField());
            BasicSearch.this.searchFields.put(searchField.getField(), searchField);
          }

          for (SearchField searchField : searchFields) {
            if (searchField.isFixed()) {
              SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
              searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
              searchFieldPanel.setSearchFields(BasicSearch.this.searchFields);
              addSearchFieldPanel(searchFieldPanel);
              searchFieldPanel.selectSearchField(searchField.getField());
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
    SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
    searchFieldPanel.simpleSearchField("1", "1", "text");
    
    SearchFieldPanel searchFieldPanel2 = new SearchFieldPanel();
    searchFieldPanel2.simpleSearchField("2", "2", "date_interval");
    
    SearchFieldPanel searchFieldPanel3 = new SearchFieldPanel();
    searchFieldPanel3.simpleSearchField("3", "3", "numeric");
    
    SearchFieldPanel searchFieldPanel4 = new SearchFieldPanel();
    searchFieldPanel4.simpleSearchField("4", "4", "storage");
    
    representationsSearchAdvancedFieldsPanel.add(searchFieldPanel);
    representationsSearchAdvancedFieldsPanel.add(searchFieldPanel2);
    representationsSearchAdvancedFieldsPanel.add(searchFieldPanel3);
    representationsSearchAdvancedFieldsPanel.add(searchFieldPanel4);
  }

  // TODO define search fields
  private void createFilesSearchAdvancedFieldsPanel() {
    SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
    searchFieldPanel.simpleSearchField("filename", "Filename", "text");
    
    SearchFieldPanel searchFieldPanel2 = new SearchFieldPanel();
    searchFieldPanel2.simpleSearchField("2", "2", "date_interval");
    
    SearchFieldPanel searchFieldPanel3 = new SearchFieldPanel();
    searchFieldPanel3.simpleSearchField("3", "3", "numeric");
    
    SearchFieldPanel searchFieldPanel4 = new SearchFieldPanel();
    searchFieldPanel4.simpleSearchField("filesize", "Filesize", "storage");
    
    SearchFieldPanel searchFieldPanel5 = new SearchFieldPanel();
    searchFieldPanel5.simpleSearchField("fulltext", "Fulltext", "text");
    
    filesSearchAdvancedFieldsPanel.add(searchFieldPanel);
    filesSearchAdvancedFieldsPanel.add(searchFieldPanel2);
    filesSearchAdvancedFieldsPanel.add(searchFieldPanel3);
    filesSearchAdvancedFieldsPanel.add(searchFieldPanel4);
    filesSearchAdvancedFieldsPanel.add(searchFieldPanel5);
  }

  private void showSearchAdvancedPanel() {
    searchAdvancedPanel.setVisible(!searchAdvancedPanel.isVisible());
    if (searchAdvancedPanel.isVisible()) {
      searchAdvancedDisclosureButton.addStyleName("open");
    } else {
      searchAdvancedDisclosureButton.removeStyleName("open");
    }
  }

  protected void view(String id) {
    Tools.newHistory(Browse.RESOLVER, id);
  }

  public void doSearch() {
    List<FilterParameter> parameters = new ArrayList<FilterParameter>();

    // basic query
    String basicQuery = searchInputBox.getText();
    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(RodaConstants.AIP__ALL, basicQuery));
    }

    for (int i = 0; i < searchAdvancedFieldsPanel.getWidgetCount(); i++) {
      SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) searchAdvancedFieldsPanel.getWidget(i);
      FilterParameter filterParameter = searchAdvancedFieldPanel.getFilter();

      if (filterParameter != null) {
        parameters.add(filterParameter);
      }
    }

    Filter filter;
    if (parameters.size() == 0) {
      filter = DEFAULT_FILTER;
    } else {
      filter = new Filter(parameters);
    }
    searchResultPanel.setFilter(filter);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      searchResultPanel.refresh();
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
    searchAdvancedFieldsPanel.add(searchFieldPanel);
    searchAdvancedFieldsPanel.removeStyleName("empty");
    searchAdvancedGo.setEnabled(true);

    ClickHandler clickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        searchAdvancedFieldsPanel.remove(searchFieldPanel);
        if (searchAdvancedFieldsPanel.getWidgetCount() == 0) {
          searchAdvancedFieldsPanel.addStyleName("empty");
          searchAdvancedGo.setEnabled(false);
        }
      }
    };

    searchFieldPanel.addRemoveClickHandler(clickHandler);
  }

  public void showSearchAdvancedFieldsPanel() {
    searchAdvancedFieldsPanel.setVisible(true);
    filesSearchAdvancedFieldsPanel.setVisible(false);
    representationsSearchAdvancedFieldsPanel.setVisible(false);
  }

  public void showRepresentationsSearchAdvancedFieldsPanel() {
    searchAdvancedFieldsPanel.setVisible(false);
    filesSearchAdvancedFieldsPanel.setVisible(false);
    representationsSearchAdvancedFieldsPanel.setVisible(true);
  }

  public void showFilesSearchAdvancedFieldsPanel() {
    searchAdvancedFieldsPanel.setVisible(false);
    filesSearchAdvancedFieldsPanel.setVisible(true);
    representationsSearchAdvancedFieldsPanel.setVisible(false);
  }
}
