/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.NotSimpleFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.wui.client.common.lists.AsyncTableCell;
import org.roda.wui.client.search.Dropdown;
import org.roda.wui.client.search.SearchFieldPanel;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SearchPanel extends Composite implements HasValueChangeHandlers<String> {
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SearchPanel> {
  }

  @UiField
  FlowPanel searchPanel;

  @UiField
  Dropdown searchInputListBox;

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
  FlowPanel searchPreFilters;

  private Filter defaultFilter;
  private String allFilter;
  private boolean defaultFilterIncremental = false;

  private FlowPanel fieldsPanel;
  private AsyncTableCell<?, ?> list;

  public SearchPanel(Filter defaultFilter, String allFilter, String placeholder, boolean showSearchInputListBox,
    boolean showSearchAdvancedDisclosureButton) {
    this.defaultFilter = defaultFilter;
    this.allFilter = allFilter;

    initWidget(binder.createAndBindUi(this));

    if (placeholder != null) {
      searchInputBox.getElement().setPropertyString("placeholder", placeholder);
    }

    searchInputListBox.setVisible(showSearchInputListBox);
    searchAdvancedDisclosureButton.setVisible(showSearchAdvancedDisclosureButton);
    searchAdvancedPanel.setVisible(false);

    searchInputBox.addKeyDownHandler(new KeyDownHandler() {
      
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          doSearch();
        }
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

    searchInputListBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        onChange();
      }
    });

    if (showSearchAdvancedDisclosureButton) {
      searchPanel.addStyleName("searchPanelAdvanced");
    }

    searchPreFilters.setVisible(!defaultFilter.getParameters().isEmpty());
    drawSearchPreFilters();
  }

  private void drawSearchPreFilters() {
    searchPreFilters.clear();
    HTML header = new HTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-filter' aria-hidden='true'></i>"));
    header.addStyleName("inline gray");
    searchPreFilters.add(header);
        
    for (FilterParameter parameter : defaultFilter.getParameters()) {
      if (parameter instanceof SimpleFilterParameter) {
        SimpleFilterParameter sfp = (SimpleFilterParameter) parameter;
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromString(sfp.getName()));
        b.append(SafeHtmlUtils.fromSafeConstant(": "));
        b.append(SafeHtmlUtils.fromString(sfp.getValue()));
        HTML html = new HTML(b.toSafeHtml());
        html.addStyleName("xsmall gray inline");
        searchPreFilters.add(html);
      } else if (parameter instanceof BasicSearchFilterParameter) {
        BasicSearchFilterParameter bsfp = (BasicSearchFilterParameter) parameter;
        // TODO put '*' in some constant, see Search
        if (!"*".equals(bsfp.getValue())) {
          SafeHtmlBuilder b = new SafeHtmlBuilder();
          b.append(SafeHtmlUtils.fromString(bsfp.getName()));
          b.append(SafeHtmlUtils.fromSafeConstant(": "));
          b.append(SafeHtmlUtils.fromString(bsfp.getValue()));
          HTML html = new HTML(b.toSafeHtml());
          html.addStyleName("xsmall gray inline");
          searchPreFilters.add(html);
        }
      } else if (parameter instanceof NotSimpleFilterParameter) {
        NotSimpleFilterParameter nsfp = (NotSimpleFilterParameter) parameter;
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromSafeConstant("NOT "));
        b.append(SafeHtmlUtils.fromString(nsfp.getName()));
        b.append(SafeHtmlUtils.fromSafeConstant(": "));
        b.append(SafeHtmlUtils.fromString(nsfp.getValue()));
        HTML html = new HTML(b.toSafeHtml());
        html.addStyleName("xsmall gray inline");
        searchPreFilters.add(html);
      } else if (parameter instanceof EmptyKeyFilterParameter) {
        EmptyKeyFilterParameter ekfp = (EmptyKeyFilterParameter) parameter;
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromSafeConstant("NO "));
        b.append(SafeHtmlUtils.fromString(ekfp.getName()));
        HTML html = new HTML(b.toSafeHtml());
        html.addStyleName("xsmall gray inline");
        searchPreFilters.add(html);
      } else {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromString(parameter.getClass().getSimpleName()));
        HTML html = new HTML(b.toSafeHtml());
        html.addStyleName("xsmall gray inline");
        searchPreFilters.add(html);
      }
    }
  }

  public void doSearch() {
    Filter filter = buildSearchFilter(searchInputBox.getText(), defaultFilter, allFilter, fieldsPanel,
      defaultFilterIncremental);
    list.setFilter(filter);
  }

  private Filter buildSearchFilter(String basicQuery, Filter defaultFilter, String allFilter, FlowPanel fieldsPanel,
    boolean defaultFilterIncremental) {
    List<FilterParameter> parameters = new ArrayList<FilterParameter>();

    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(allFilter, basicQuery));
    }

    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
        FilterParameter filterParameter = searchAdvancedFieldPanel.getFilter();

        if (filterParameter != null) {
          parameters.add(filterParameter);
        }
      }
    }

    Filter filter;
    if (defaultFilterIncremental) {
      filter = new Filter(defaultFilter);
      filter.add(parameters);
      searchPreFilters.setVisible(defaultFilter.getParameters().size() > 0);
      GWT.log("Incremental filter: " + filter);
    } else if (parameters.size() == 0) {
      filter = defaultFilter;
      searchPreFilters.setVisible(defaultFilter.getParameters().size() > 0);
      GWT.log("Default filter: " + filter);
    } else {
      filter = new Filter(parameters);
      searchPreFilters.setVisible(false);
      GWT.log("New filter: " + filter);
    }

    return filter;
  }

  public String getDropdownSelectedValue() {
    return searchInputListBox.getSelectedValue();
  }

  public void setDropdownLabel(String label) {
    searchInputListBox.setLabel(label);
  }

  public void addDropdownItem(String label, String value) {
    searchInputListBox.addItem(label, value);
  }

  private void showSearchAdvancedPanel() {
    searchAdvancedPanel.setVisible(!searchAdvancedPanel.isVisible());
    if (searchAdvancedPanel.isVisible()) {
      searchAdvancedDisclosureButton.addStyleName("open");
    } else {
      searchAdvancedDisclosureButton.removeStyleName("open");
    }
  }

  public void addDropdownPopupStyleName(String styleName) {
    searchInputListBox.addPopupStyleName(styleName);
  }

  public void setFieldsPanel(FlowPanel fieldsPanel) {
    this.fieldsPanel = fieldsPanel;
    searchAdvancedPanel.clear();
    searchAdvancedPanel.add(fieldsPanel);
    searchAdvancedPanel.add(searchAdvancedPanelButtons);
  }

  public void setList(AsyncTableCell<?, ?> list) {
    this.list = list;
  }

  public void setDefaultFilter(Filter defaultFilter) {
    this.defaultFilter = defaultFilter;
    drawSearchPreFilters();
  }

  public void setAllFilter(String allFilter) {
    this.allFilter = allFilter;
  }

  public void setVariables(Filter defaultFilter, String allFilter, AsyncTableCell<?, ?> list, FlowPanel fieldsPanel) {
    setDefaultFilter(defaultFilter);
    setAllFilter(allFilter);
    setList(list);
    setFieldsPanel(fieldsPanel);
  }

  public void setDefaultFilterIncremental(boolean defaultFilterIncremental) {
    this.defaultFilterIncremental = defaultFilterIncremental;
  }

  public void clearSearchInputBox() {
    searchInputBox.setText("");
  }

  public void setSearchAdvancedFieldOptionsAddVisible(boolean visible) {
    searchAdvancedFieldOptionsAdd.setVisible(visible);
  }

  public void setSearchAdvancedGoEnabled(boolean enabled) {
    searchAdvancedGo.setEnabled(enabled);
  }

  public void addSearchAdvancedFieldAddHandler(ClickHandler handler) {
    searchAdvancedFieldOptionsAdd.addClickHandler(handler);
  }

  @UiHandler("searchAdvancedGo")
  void handleSearchAdvancedGo(ClickEvent e) {
    doSearch();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, searchInputListBox.getSelectedValue());
  }
}
