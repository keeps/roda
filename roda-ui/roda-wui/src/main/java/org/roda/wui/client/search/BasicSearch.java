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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.DateIntervalFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;
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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
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
  private static final Filter DEFAULT_FILTER = new Filter(new BasicSearchFilterParameter(RodaConstants.SDO__ALL, "*"));

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

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField(provided = true)
  AIPList searchResultPanel;

//  // ADVANCED SEARCH
//  @UiField
//  DisclosurePanel searchAdvancedDisclosure;

  @UiField
  FlowPanel searchAdvancedFieldsPanel;

  @UiField
  ListBox searchAdvancedFieldOptions;

  @UiField
  Button searchAdvancedFieldOptionsAdd;

  @UiField
  Button searchAdvancedGo;

  // FILTERS
  @UiField(provided = true)
  FlowPanel facetDescriptionLevels;
  @UiField(provided = true)
  FlowPanel facetHasRepresentations;

  @UiField
  DateBox inputDateInitial;
  @UiField
  DateBox inputDateFinal;

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();
  private final Map<String, TextBox> searchFieldTextBoxes = new HashMap<String, TextBox>();

  private BasicSearch() {
    Filter filter = DEFAULT_FILTER;
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.SDO_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    // TODO externalise strings
    searchResultPanel = new AIPList(filter, facets, "Search results");
    facetDescriptionLevels = new FlowPanel();
    facetHasRepresentations = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.SDO_LEVEL, facetDescriptionLevels);
    facetPanels.put(RodaConstants.AIP_HAS_REPRESENTATIONS, facetHasRepresentations);
    FacetUtils.bindFacets(searchResultPanel, facetPanels);

    initWidget(uiBinder.createAndBindUi(this));

    searchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        SimpleDescriptionObject sdo = searchResultPanel.getSelectionModel().getSelectedObject();
        if (sdo != null) {
          view(sdo.getId());
        }
      }
    });

    searchInputBox.getElement().setPropertyString("placeholder", messages.searchPlaceHolder());

    this.searchInputBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        doSearch();
      }
    });

    this.searchInputButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    ValueChangeHandler<Date> valueChangeHandler = new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        updateDateFilter();
      }

    };
    
    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDate());
    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());

    inputDateInitial.setFormat(dateFormat);
    inputDateInitial.getDatePicker().setYearArrowsVisible(true);
    inputDateInitial.setFireNullValues(true);
    inputDateInitial.addValueChangeHandler(valueChangeHandler);

    inputDateFinal.setFormat(dateFormat);
    inputDateFinal.getDatePicker().setYearArrowsVisible(true);
    inputDateFinal.setFireNullValues(true);
    inputDateFinal.addValueChangeHandler(valueChangeHandler);

    BrowserService.Util.getInstance().getSearchFields(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<List<SearchField>>() {
        @Override
        public void onFailure(Throwable caught) {
          GWT.log("Error getting search fields", caught);
        }

        @Override
        public void onSuccess(List<SearchField> searchFields) {
          BasicSearch.this.searchFields.clear();
          searchAdvancedFieldOptions.clear();
          for (SearchField searchField : searchFields) {
            searchAdvancedFieldOptions.addItem(searchField.getLabel(), searchField.getField());
            BasicSearch.this.searchFields.put(searchField.getField(), searchField);
          }
        }

      });

  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateIntervalFilterParameter filterParameter = new DateIntervalFilterParameter(RodaConstants.SDO_DATE_INITIAL,
      RodaConstants.SDO_DATE_FINAL, dateInitial, dateFinal);

    searchResultPanel.setFilter(new Filter(filterParameter));
  }

  protected void view(String id) {
    Tools.newHistory(Browse.RESOLVER, id);
  }

  public void doSearch() {
    List<FilterParameter> parameters = new ArrayList<FilterParameter>();

    // basic query
    String basicQuery = searchInputBox.getText();
    if (!"".equals(basicQuery)) {
      parameters.add(new BasicSearchFilterParameter(RodaConstants.SDO__ALL, basicQuery));
    }

    for (Entry<String, TextBox> entry : searchFieldTextBoxes.entrySet()) {
      String field = entry.getKey();
      String text = entry.getValue().getText();

      if (!"".equals(text)) {
        parameters.add(new BasicSearchFilterParameter(field, text));
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
      searchResultPanel.getSelectionModel().clear();
      callback.onSuccess(this);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("searchAdvancedFieldOptionsAdd")
  void handleSearchAdvancedFieldAdd(ClickEvent e) {
    String selectedValue = searchAdvancedFieldOptions.getSelectedValue();

    SearchField searchField = searchFields.get(selectedValue);
    addSearchFieldPanel(searchField);

  }

  @UiHandler("searchAdvancedGo")
  void handleSearchAdvancedGo(ClickEvent e) {
    doSearch();
  }

  private void addSearchFieldPanel(final SearchField searchField) {
    final FlowPanel panel = new FlowPanel();
    Label label = new Label(searchField.getLabel());
    Anchor remove = new Anchor("remove");
    TextBox input = new TextBox();

    panel.add(label);
    panel.add(remove);
    panel.add(input);

    label.addStyleName("form-label");
    label.addStyleName("search-field-label");
    remove.addStyleName("search-field-remove");
    input.addStyleName("form-textbox");

    searchAdvancedFieldsPanel.add(panel);
    searchFieldTextBoxes.put(searchField.getField(), input);
    searchAdvancedFieldsPanel.removeStyleName("empty");
    searchAdvancedGo.setEnabled(true);

    remove.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        searchAdvancedFieldsPanel.remove(panel);
        searchFieldTextBoxes.remove(searchField.getField());
        if (searchAdvancedFieldsPanel.getWidgetCount() == 0) {
          searchAdvancedFieldsPanel.addStyleName("empty");
          searchAdvancedGo.setEnabled(false);
        }
      }
    });

    input.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        doSearch();
      }
    });

  }

}
