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
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.popup.CalloutPopup;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class SearchPanel<T extends IsIndexed> extends Composite implements HasValueChangeHandlers<String> {
  private static final String FILTER_ICON = "<i class='fa fa-filter' aria-hidden='true'></i>";
  private static final String WITH_FILTERS_CSS = "with-prefilters";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SearchPanel> {
  }

  @UiField
  FlowPanel searchPanel;

  @UiField(provided = true)
  SelectedPanel<T> searchSelectedPanel;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  AccessibleFocusPanel searchAdvancedDisclosureButton;

  @UiField
  FlowPanel searchAdvancedPanel;

  @UiField
  FlowPanel searchAdvancedPanelButtons;

  @UiField
  Button searchAdvancedFieldOptionsAdd;

  @UiField
  Button searchAdvancedGo;

  @UiField
  Button searchAdvancedClean;

  @UiField
  Button searchAdvancedSave;

  @UiField
  FlowPanel searchPreFilters;

  @UiField
  SimplePanel searchPanelSelectionDropdownWrapper;

  private Filter defaultFilter;
  private String allFilter;
  private boolean defaultFilterIncremental = false;

  private AdvancedSearchFieldsPanel advancedSearchFieldsPanel;
  private AsyncTableCell<T> list;

  private boolean showPreFilters;
  private boolean hideListAfterClear;

  private SearchPanel() {
    // private constructor to forbid its usage
  }

  SearchPanel(AsyncTableCell<T> list, Filter defaultFilter, String allFilter, boolean incremental, String placeholder,
    boolean showSearchInputListBox, boolean showSaveButton, boolean hideListAfterClear) {
    this.defaultFilter = defaultFilter;
    this.allFilter = allFilter;
    this.defaultFilterIncremental = incremental;
    this.list = list;
    this.hideListAfterClear = hideListAfterClear;

    this.showPreFilters = ConfigurationManager.getBoolean(false, RodaConstants.UI_LISTS_PROPERTY, list.getListId(),
      RodaConstants.UI_LISTS_SEARCH_PREFILTERS_VISIBLE_PROPERTY);

    searchSelectedPanel = new SelectedPanel<>(list);

    initWidget(binder.createAndBindUi(this));

    // setup search input textfield and search button
    searchInputButton.addClickHandler(event -> doSearch());
    searchInputBox.getElement().setPropertyString("placeholder", placeholder == null ? "" : placeholder);
    searchInputBox.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        doSearch();
      }
    });

    // setup actions
    final CalloutPopup popup = new CalloutPopup();
    popup.addStyleName("actionable-popup");
    popup.addStyleName("ActionableStyleMenu");

    list.addCheckboxSelectionListener(selectedItems -> {
      if (!list.isVisible()) {
        doSearch();
      }
    });

    // setup advanced search panel and button
    setupAdvancedSearch(false);

    // bind searchSelectedPanel to show the number of selected items from the list,
    // and also to show/hide itself and the searchPanelSelectionDropdown
    boolean searchSelectedPanelVisibleByDefault = ConfigurationManager.getBoolean(false,
      RodaConstants.UI_LISTS_PROPERTY, list.getListId(),
      RodaConstants.UI_LISTS_SEARCH_SELECTEDINFO_ALWAYSVISIBLE_PROPERTY);

    searchSelectedPanel.addValueChangeHandler(event -> {
      // if something is selected, show the selectedPanel and hide the dropdown.
      // otherwise if there is a dropdown then show it, if there is no dropdown then
      // use the configuration value for the selectedPanel being shown by default
      boolean selectedPanelVisible = event.getValue()
        || (!showSearchInputListBox && searchSelectedPanelVisibleByDefault);

      searchSelectedPanel.setVisible(selectedPanelVisible);
      searchPanelSelectionDropdownWrapper.setVisible(!selectedPanelVisible);
    });

    boolean selectedPanelVisible = !showSearchInputListBox && searchSelectedPanelVisibleByDefault;
    searchPanelSelectionDropdownWrapper.setVisible(!selectedPanelVisible);
    searchSelectedPanel.setVisible(selectedPanelVisible);

    drawSearchPreFilters();

    if (!showSaveButton) {
      searchAdvancedSave.setVisible(false);
    }
  }

  private void setupAdvancedSearch(boolean makeAdvancedPanelVisible) {
    boolean advancedSearchEnabled = ConfigurationManager.getBoolean(false, RodaConstants.UI_LISTS_PROPERTY,
      list.getListId(), RodaConstants.UI_LISTS_SEARCH_ADVANCED_ENABLED_PROPERTY);

    if (advancedSearchFieldsPanel != null) {
      searchAdvancedPanel.remove(advancedSearchFieldsPanel);
    }

    searchAdvancedDisclosureButton.setVisible(advancedSearchEnabled);
    searchAdvancedPanel.setVisible(makeAdvancedPanelVisible);
    if (advancedSearchEnabled) {
      searchAdvancedDisclosureButton.addClickHandler(event -> toggleAdvancedSearchPanel());

      advancedSearchFieldsPanel = new AdvancedSearchFieldsPanel(list.getClassToReturn().getSimpleName(), keyCode -> {
        if (KeyCodes.KEY_ENTER == keyCode && searchAdvancedGo.isEnabled()) {
          searchAdvancedGo.click();
        }
      });
      advancedSearchFieldsPanel.addValueChangeHandler(event -> searchAdvancedGo.setEnabled(event.getValue() != 0));
      searchAdvancedPanel.insert(advancedSearchFieldsPanel, 0);
    }

    if (advancedSearchEnabled) {
      searchPanel.addStyleName("searchPanelAdvanced");
    }
  }

  /**
   * Sets the default filter and incremental boolean for the search, shows the
   * preFilters in the UI if they should be shown according to the constructor
   * parameters, and triggers a new search in the list with the new default filter
   * and the existing search fields
   *
   * @param defaultFilter
   *          the new default filters
   * @param incremental
   *          if subsequent searches should add to or replace the existing filter
   */
  public void setDefaultFilter(Filter defaultFilter, boolean incremental) {
    clearSearchInputBox();
    this.defaultFilter = defaultFilter;
    this.defaultFilterIncremental = incremental;
    doSearch(false);
  }

  public void clearSearchInputBox() {
    searchInputBox.setText("");
  }

  @UiHandler("searchAdvancedFieldOptionsAdd")
  void handleSearchAdvancedAdd(ClickEvent e) {
    advancedSearchFieldsPanel.addSearchFieldPanel();
  }

  @UiHandler("searchAdvancedClean")
  void handleSearchAdvancedClean(ClickEvent e) {
    setupAdvancedSearch(true);
    clearSearchInputBox();
    if (hideListAfterClear) {
      list.setVisible(false);
    }
    doSearch(false);
  }

  @UiHandler("searchAdvancedSave")
  void handleSearchAdvancedSave(ClickEvent e) {
    String classSimpleName;
    if (searchPanelSelectionDropdownWrapper.getWidget() instanceof Dropdown) {
      classSimpleName = ((Dropdown) searchPanelSelectionDropdownWrapper.getWidget()).getSelectedValue();
    } else {
      classSimpleName = IndexedAIP.class.getSimpleName();
    }

    Dialogs.showPromptDialog(messages.saveSearchTitle(), messages.saveSearchDescription(), "", "",
      RegExp.compile("^[^/]+$"), messages.promptDialogErrorMessageForSavedSearchTitle(), messages.cancelButton(),
      messages.saveButton(), true, false, new NoAsyncCallback<String>() {
        @Override
        public void onSuccess(String title) {
          SavedSearchMapper mapper = GWT.create(SavedSearchMapper.class);
          String json = mapper.write(new RODASavedSearch(classSimpleName, title, buildSearchFilter()));
          String base64String = JavascriptUtils.encodeBase64(json);
          HistoryUtils.newHistory(Search.RESOLVER, RodaConstants.SEARCH_WITH_SAVED_HANDLER, base64String);
        }
      });
  }

  @UiHandler("searchAdvancedGo")
  void handleSearchAdvancedGo(ClickEvent e) {
    doSearch();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  private void toggleAdvancedSearchPanel() {
    searchAdvancedPanel.setVisible(!searchAdvancedPanel.isVisible());
    if (searchAdvancedPanel.isVisible()) {
      searchAdvancedDisclosureButton.addStyleName("open");
    } else {
      searchAdvancedDisclosureButton.removeStyleName("open");
    }
  }

  private void doSearch() {
    doSearch(true);
  }

  private void doSearch(boolean makeListVisible) {
    if (makeListVisible) {
      list.setVisible(true);
    }
    list.setFilter(buildSearchFilter());
    onChange(searchInputBox.getValue());
  }

  private void onChange(String value) {
    ValueChangeEvent.fire(this, value);
  }

  private void drawSearchPreFilters() {
    if (showPreFilters && defaultFilter != null && !defaultFilter.getParameters().isEmpty()) {
      searchPreFilters.clear();

      boolean effectivelyVisible = false;
      List<FilterParameter> parameters = new ArrayList<>(defaultFilter.getParameters());
      parameters.removeIf(f -> f instanceof AllFilterParameter);
      for (int i = 0; i < parameters.size(); i++) {
        SafeHtml filterHTML = SearchPreFilterUtils.getFilterParameterHTML(parameters.get(i));

        if (filterHTML != null) {
          effectivelyVisible = true;
          if (i == 0) {
            HTML header = new HTML(SafeHtmlUtils.fromSafeConstant(FILTER_ICON));
            header.addStyleName("inline gray");
            searchPreFilters.add(header);
          } else {
            InlineHTML andSeparator = new InlineHTML(messages.searchPreFilterAnd());
            andSeparator.addStyleName("gray");
            searchPreFilters.add(andSeparator);
          }

          HTML html = new HTML(filterHTML);
          html.addStyleName("gray inline nowrap");
          searchPreFilters.add(html);
        }
      }

      if (effectivelyVisible) {
        searchPreFilters.setVisible(true);
        addStyleName(WITH_FILTERS_CSS);
      } else {
        removeStyleName(WITH_FILTERS_CSS);
        searchPreFilters.setVisible(false);
      }
    } else {
      removeStyleName(WITH_FILTERS_CSS);
      searchPreFilters.setVisible(false);
    }
  }

  void attachSearchPanelSelectionDropdown(Dropdown dropdown) {
    searchPanelSelectionDropdownWrapper.setWidget(dropdown);
  }

  /**
   * @return the effective search filter based on all search options and inputs
   */
  private Filter buildSearchFilter() {
    String basicQuery = searchInputBox.getText();

    List<FilterParameter> parameters = new ArrayList<>();
    Map<String, FilterParameter> advancedSearchFilters = new HashMap<>();

    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(allFilter, basicQuery));
    }

    // transform advanced search fields into filter parameters
    if (this.searchAdvancedPanel != null && this.searchAdvancedPanel.isVisible()) {
      for (int i = 0; i < advancedSearchFieldsPanel.getWidgetCount(); i++) {
        if (advancedSearchFieldsPanel.getWidget(i) instanceof SearchFieldPanel) {
          SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) advancedSearchFieldsPanel.getWidget(i);
          String searchFieldId = searchAdvancedFieldPanel.getSearchField().getId();
          FilterParameter oldFilterParameter = advancedSearchFilters.get(searchFieldId);
          FilterParameter filterParameter = searchAdvancedFieldPanel.getFilter();

          if (filterParameter instanceof SimpleFilterParameter) {
            SimpleFilterParameter parameter = (SimpleFilterParameter) filterParameter;
            if (RodaConstants.SEARCH_FIELD_TYPE_CONTROLLED.equals(searchAdvancedFieldPanel.getSearchField().getType())
              && RodaConstants.NONE_SELECTED_LEVEL.equals(parameter.getValue())) {
              filterParameter = null;
            }
          }

          if (filterParameter != null) {
            if (oldFilterParameter != null) {
              if (oldFilterParameter instanceof OrFiltersParameters) {
                List<FilterParameter> filterParameters = ((OrFiltersParameters) oldFilterParameter).getValues();
                filterParameters.add(filterParameter);
                ((OrFiltersParameters) oldFilterParameter).setValues(filterParameters);
                advancedSearchFilters.put(searchFieldId, oldFilterParameter);
              } else {
                List<FilterParameter> filterParameters = new ArrayList<>();
                filterParameters.add(oldFilterParameter);
                filterParameters.add(filterParameter);
                advancedSearchFilters.put(searchFieldId, new OrFiltersParameters(filterParameters));
              }
            } else {
              advancedSearchFilters.put(searchFieldId, filterParameter);
            }
          }
        }
      }

      parameters.addAll(advancedSearchFilters.values());
    }

    Filter filter;
    if (defaultFilterIncremental) {
      filter = defaultFilter != null ? new Filter(defaultFilter) : new Filter();
      filter.add(parameters);
    } else if (parameters.isEmpty()) {
      filter = defaultFilter;
    } else {
      filter = new Filter(parameters);
    }
    drawSearchPreFilters();
    return filter;
  }
}
