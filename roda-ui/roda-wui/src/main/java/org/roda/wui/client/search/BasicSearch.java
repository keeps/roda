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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
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
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
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
  Button searchAdvancedFieldOptionsAdd;

  @UiField
  Button searchAdvancedGo;

  // FILTERS
  @UiField(provided = true)
  FlowPanel facetDescriptionLevels;
  @UiField(provided = true)
  FlowPanel facetHasRepresentations;

//  @UiField
//  DateBox inputDateInitial;
//  @UiField
//  DateBox inputDateFinal;

  ListBox searchAdvancedFieldOptions;

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();
  // private final Map<String, TextBox> searchFieldTextBoxes = new
  // HashMap<String, TextBox>();

  private BasicSearch() {
    Filter filter = DEFAULT_FILTER;
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    // TODO externalise strings
    searchResultPanel = new AIPList(filter, facets, "Search results");
    facetDescriptionLevels = new FlowPanel();
    facetHasRepresentations = new FlowPanel();

    searchAdvancedFieldOptions = new ListBox();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.AIP_LEVEL, facetDescriptionLevels);
    facetPanels.put(RodaConstants.AIP_HAS_REPRESENTATIONS, facetHasRepresentations);
    FacetUtils.bindFacets(searchResultPanel, facetPanels);

    initWidget(uiBinder.createAndBindUi(this));

    searchDescription.add(new HTMLWidgetWrapper("SearchDescription.html"));

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

//    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
//    ValueChangeHandler<Date> valueChangeHandler = new ValueChangeHandler<Date>() {
//
//      @Override
//      public void onValueChange(ValueChangeEvent<Date> event) {
//        updateDateFilter();
//      }
//
//    };
//
//    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDate());
//    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());
//
//    inputDateInitial.setFormat(dateFormat);
//    inputDateInitial.getDatePicker().setYearArrowsVisible(true);
//    inputDateInitial.setFireNullValues(true);
//    inputDateInitial.addValueChangeHandler(valueChangeHandler);
//
//    inputDateFinal.setFormat(dateFormat);
//    inputDateFinal.getDatePicker().setYearArrowsVisible(true);
//    inputDateFinal.setFireNullValues(true);
//    inputDateFinal.addValueChangeHandler(valueChangeHandler);

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
              SearchAdvancedFieldPanel searchAdvancedFieldPanel = new SearchAdvancedFieldPanel(
                BasicSearch.this.searchFields, searchAdvancedFieldOptions);
              addSearchFieldPanel(searchAdvancedFieldPanel);
              searchAdvancedFieldPanel.selectSearchField(searchField.getField());
            }
          }
        }
      });
  }

  private void showSearchAdvancedPanel() {
    searchAdvancedPanel.setVisible(!searchAdvancedPanel.isVisible());
    if (searchAdvancedPanel.isVisible()) {
      searchAdvancedDisclosureButton.addStyleName("open");
    } else {
      searchAdvancedDisclosureButton.removeStyleName("open");
    }
  }

//  private void updateDateFilter() {
//    Date dateInitial = inputDateInitial.getDatePicker().getValue();
//    Date dateFinal = inputDateFinal.getDatePicker().getValue();
//
//    DateIntervalFilterParameter filterParameter = new DateIntervalFilterParameter(RodaConstants.AIP_DATE_INITIAL,
//      RodaConstants.AIP_DATE_FINAL, dateInitial, dateFinal);
//
//    searchResultPanel.setFilter(new Filter(filterParameter));
//  }

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
      SearchAdvancedFieldPanel searchAdvancedFieldPanel = (SearchAdvancedFieldPanel) searchAdvancedFieldsPanel
        .getWidget(i);
      String field = searchAdvancedFieldPanel.getField();
      String value = searchAdvancedFieldPanel.getValue();

      if (value != null && value.trim().length() > 0) {
        parameters.add(new BasicSearchFilterParameter(field, value));
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
    SearchAdvancedFieldPanel searchAdvancedFieldPanel = new SearchAdvancedFieldPanel(searchFields,
      searchAdvancedFieldOptions);
    searchAdvancedFieldPanel.selectFirstSearchField();
    addSearchFieldPanel(searchAdvancedFieldPanel);
  }

  @UiHandler("searchAdvancedGo")
  void handleSearchAdvancedGo(ClickEvent e) {
    doSearch();
  }

  private void addSearchFieldPanel(final SearchAdvancedFieldPanel searchAdvancedFieldPanel) {
    searchAdvancedFieldsPanel.add(searchAdvancedFieldPanel);
    searchAdvancedFieldsPanel.removeStyleName("empty");
    searchAdvancedGo.setEnabled(true);

    ClickHandler clickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        searchAdvancedFieldsPanel.remove(searchAdvancedFieldPanel);
        if (searchAdvancedFieldsPanel.getWidgetCount() == 0) {
          searchAdvancedFieldsPanel.addStyleName("empty");
          searchAdvancedGo.setEnabled(false);
        }
      }
    };

    searchAdvancedFieldPanel.addRemoveClickHandler(clickHandler);
  }

  public class SearchAdvancedFieldPanel extends Composite {
    private FlowPanel panel;
    private FlowPanel leftPanel;
    private ListBox searchAdvancedFields;
    private Map<String, SearchField> searchFields;
    private Button remove = new Button("<i class=\"fa fa-close\"></i>");

    // Text
    private TextBox inputText;
    // Date
    private DateBox inputDateBox;
    // Date interval
    private DateBox inputDateBoxFrom;
    private DateBox inputDateBoxTo;
    // Numeric
    private TextBox inputNumeric;
    // Numeric interval
    private TextBox inputNumericFrom;
    private TextBox inputNumericTo;
    // Storage

    public SearchAdvancedFieldPanel(Map<String, SearchField> searchFields, ListBox searchAdvancedFieldOptions) {
      panel = new FlowPanel();
      leftPanel = new FlowPanel();
      searchAdvancedFields = new ListBox();

      DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));

      inputText = new TextBox();

      inputDateBox = new DateBox();
      inputDateBox.setFormat(dateFormat);
      inputDateBox.getDatePicker().setYearArrowsVisible(true);
      inputDateBox.setFireNullValues(true);
      inputDateBox.getElement().setPropertyString("placeholder", "yyyy-mm-dd");

      inputDateBoxFrom = new DateBox();
      inputDateBoxFrom.setFormat(dateFormat);
      inputDateBoxFrom.getDatePicker().setYearArrowsVisible(true);
      inputDateBoxFrom.setFireNullValues(true);
      inputDateBoxFrom.getElement().setPropertyString("placeholder", "yyyy-mm-dd");

      inputDateBoxTo = new DateBox();
      inputDateBoxTo.setFormat(dateFormat);
      inputDateBoxTo.getDatePicker().setYearArrowsVisible(true);
      inputDateBoxTo.setFireNullValues(true);
      inputDateBoxTo.getElement().setPropertyString("placeholder", "yyyy-mm-dd");

      inputNumeric = new TextBox();
      inputNumeric.getElement().setPropertyString("placeholder", "Ex: 1000");
      inputNumericFrom = new TextBox();
      inputNumericFrom.getElement().setPropertyString("placeholder", "Ex: 0");
      inputNumericTo = new TextBox();
      inputNumericTo.getElement().setPropertyString("placeholder", "Ex: 1000");

      this.searchFields = searchFields;

      ListboxUtils.copyValues(searchAdvancedFieldOptions, searchAdvancedFields);

      panel.add(leftPanel);
      panel.add(remove);

      initWidget(panel);

      searchAdvancedFields.addStyleName("form-listbox");

      searchAdvancedFields.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          setValue(searchAdvancedFields.getSelectedValue());
        }
      });

      panel.addStyleName("search-field");
      leftPanel.addStyleName("search-field-left-panel");
      leftPanel.addStyleName("full_width");
      remove.addStyleName("search-field-remove");

      inputText.addStyleName("form-textbox");
      inputDateBox.addStyleName("form-textbox form-textbox-small");
      inputDateBoxFrom.addStyleName("form-textbox form-textbox-small");
      inputDateBoxTo.addStyleName("form-textbox form-textbox-small");
      inputNumeric.addStyleName("form-textbox form-textbox-small");
      inputNumericFrom.addStyleName("form-textbox form-textbox-small");
      inputNumericTo.addStyleName("form-textbox form-textbox-small");
    }

    public void selectSearchField(String field) {
      ListboxUtils.select(searchAdvancedFields, field);
      setValue(field);
    }

    public void selectFirstSearchField() {
      if (searchAdvancedFields.getItemCount() > 0) {
        setValue(searchAdvancedFields.getValue(0));
      }
    }

    public void addRemoveClickHandler(ClickHandler clickHandler) {
      remove.addClickHandler(clickHandler);
    }

    public String getField() {
      return searchAdvancedFields.getSelectedValue();
    }

    public String getValue() {
      SearchField searchField = searchFields.get(searchAdvancedFields.getSelectedValue());

      if (searchField.getType().equals("date")) {
      } else if (searchField.getType().equals("date_interval")) {
      } else if (searchField.getType().equals("numeric")) {
      } else if (searchField.getType().equals("numeric_interval")) {
      } else if (searchField.getType().equals("storage")) {
      } else {
      }

      return inputText.getValue();
    }

    public void setValue(String field) {
      SearchField searchField = searchFields.get(field);
      leftPanel.clear();
      leftPanel.add(searchAdvancedFields);
      leftPanel.removeStyleName("full_width");

      if (searchField.getType().equals("date")) {
        leftPanel.add(inputDateBox);
      } else if (searchField.getType().equals("date_interval")) {
        leftPanel.add(inputDateBoxFrom);
        leftPanel.add(inputDateBoxTo);
      } else if (searchField.getType().equals("numeric")) {
        leftPanel.add(inputNumeric);
      } else if (searchField.getType().equals("numeric_interval")) {
        leftPanel.add(inputNumericFrom);
        leftPanel.add(inputNumericTo);
      } else if (searchField.getType().equals("storage")) {
      } else {
        leftPanel.add(inputText);
        leftPanel.addStyleName("full_width");
      }
    }
  }
}
