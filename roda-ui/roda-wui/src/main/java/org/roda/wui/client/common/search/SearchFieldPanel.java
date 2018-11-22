/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.wui.client.common.utils.ListboxUtils;
import org.roda.wui.client.common.utils.Tree;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.ClientMessages;

public class SearchFieldPanel extends Composite implements HasValueChangeHandlers<String> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private FlowPanel panel;
  private FlowPanel leftPanel;
  private FlowPanel inputPanel;
  private Button remove = new Button("<i class=\"fa fa-close\"></i>");
  private Label duplicateWarning;

  private SearchField searchField;

  // Simple search field
  private Label fieldLabel;
  private ListBox fieldBox;

  // Complex search field
  private ListBox searchAdvancedFields;
  private Map<String, SearchField> searchFields;

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
  private TextBox inputStorageSizeFrom;
  private TextBox inputStorageSizeTo;
  private ListBox inputStorageSizeList;
  // Boolean
  private CheckBox inputCheckBox;
  // Suggestion
  private SearchSuggestBox<?> inputSearchSuggestBox = null;

  // controlled
  private ListBox inputControlled;

  public SearchFieldPanel() {
    panel = new FlowPanel();
    leftPanel = new FlowPanel();
    inputPanel = new FlowPanel();
    fieldLabel = new Label();
    fieldBox = new ListBox();
    searchAdvancedFields = new ListBox();
    duplicateWarning = new Label();

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));

    inputText = new TextBox();

    inputDateBox = new DateBox();
    inputDateBox.setFormat(dateFormat);
    inputDateBox.getDatePicker().setYearAndMonthDropdownVisible(true);
    inputDateBox.getDatePicker().setYearArrowsVisible(true);
    inputDateBox.setFireNullValues(true);
    inputDateBox.getElement().setPropertyString("placeholder", messages.searchFieldDatePlaceHolder());

    inputDateBoxFrom = new DateBox();
    inputDateBoxFrom.setFormat(dateFormat);
    inputDateBox.getDatePicker().setYearAndMonthDropdownVisible(true);
    inputDateBoxFrom.getDatePicker().setYearArrowsVisible(true);
    inputDateBoxFrom.setFireNullValues(true);
    inputDateBoxFrom.getElement().setPropertyString("placeholder", messages.searchFieldDateFromPlaceHolder());

    inputDateBoxTo = new DateBox();
    inputDateBoxTo.setFormat(dateFormat);
    inputDateBox.getDatePicker().setYearAndMonthDropdownVisible(true);
    inputDateBoxTo.getDatePicker().setYearArrowsVisible(true);
    inputDateBoxTo.setFireNullValues(true);
    inputDateBoxTo.getElement().setPropertyString("placeholder", messages.searchFieldDateToPlaceHolder());

    inputNumeric = new TextBox();
    inputNumeric.getElement().setPropertyString("placeholder", messages.searchFieldNumericPlaceHolder());
    inputNumeric.getElement().setAttribute("type", "number");
    inputNumericFrom = new TextBox();
    inputNumericFrom.getElement().setPropertyString("placeholder", messages.searchFieldNumericFromPlaceHolder());
    inputNumericFrom.getElement().setAttribute("type", "number");
    inputNumericTo = new TextBox();
    inputNumericTo.getElement().setPropertyString("placeholder", messages.searchFieldNumericToPlaceHolder());
    inputNumericTo.getElement().setAttribute("type", "number");

    inputStorageSizeFrom = new TextBox();
    inputStorageSizeFrom.getElement().setPropertyString("placeholder", messages.searchFieldNumericFromPlaceHolder());
    inputStorageSizeFrom.getElement().setAttribute("type", "number");
    inputStorageSizeTo = new TextBox();
    inputStorageSizeTo.getElement().setPropertyString("placeholder", messages.searchFieldNumericToPlaceHolder());
    inputStorageSizeTo.getElement().setAttribute("type", "number");
    inputStorageSizeList = new ListBox();
    for (String unit : Humanize.UNITS) {
      inputStorageSizeList.addItem(unit, unit);
    }
    inputStorageSizeList.getElement().setAttribute("title", messages.inputStorageSizeList());

    inputCheckBox = new CheckBox();

    inputControlled = new ListBox();

    panel.add(leftPanel);

    initWidget(panel);

    searchAdvancedFields.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        listBoxSearchField(searchAdvancedFields.getSelectedValue());
        SearchFieldPanel.this.onChange();
      }
    });

    panel.addStyleName("search-field");
    leftPanel.addStyleName("search-field-left-panel");
    inputPanel.addStyleName("search-field-input-panel");
    inputPanel.addStyleName("full_width");
    remove.addStyleName("search-field-remove");
    fieldLabel.addStyleName("search-field-label");
    fieldBox.addStyleName("form-listbox");
    searchAdvancedFields.addStyleName("form-listbox");
    duplicateWarning.addStyleName("search-field-warning-label");
    duplicateWarning.setVisible(false);

    inputText.addStyleName("form-textbox");
    inputDateBox.addStyleName("form-textbox form-textbox-small");
    inputDateBoxFrom.addStyleName("form-textbox form-textbox-small");
    inputDateBoxTo.addStyleName("form-textbox form-textbox-small");
    inputNumeric.addStyleName("form-textbox form-textbox-small");
    inputNumericFrom.addStyleName("form-textbox form-textbox-small");
    inputNumericTo.addStyleName("form-textbox form-textbox-small");
    inputStorageSizeFrom.addStyleName("form-textbox form-textbox-small");
    inputStorageSizeTo.addStyleName("form-textbox form-textbox-small");
    inputStorageSizeList.addStyleName("form-listbox");
    inputCheckBox.addStyleName("form-checkbox");
    inputControlled.addStyleName("form-controlled");
  }

  public SearchField getSearchField() {
    return searchField;
  }

  public void setSearchField(SearchField searchField) {
    this.searchField = searchField;

    if (searchField.getTerms() != null) {
      for (Tree<String> term : searchField.getTerms().getChildren()) {
        inputControlled.addItem(term.getLabel(), term.getValue());
      }
      // TODO add terms to listbox
    }

  }

  public void setSearchAdvancedFields(ListBox searchAdvancedFieldOptions) {
    ListboxUtils.copyValues(searchAdvancedFieldOptions, searchAdvancedFields);
  }

  public void setSearchFields(Map<String, SearchField> searchFields) {
    this.searchFields = searchFields;
  }

  public void selectSearchField(String field) {
    ListboxUtils.select(searchAdvancedFields, field);
    listBoxSearchField(field);
  }

  public void selectFirstSearchField() {
    if (searchAdvancedFields.getItemCount() > 0) {
      listBoxSearchField(searchAdvancedFields.getValue(0));
    }
  }

  public String getFirstSearchField() {
    String searchField = null;
    if (searchAdvancedFields.getItemCount() > 0) {
      searchField = searchAdvancedFields.getValue(0);
    }
    return searchField;
  }

  public ListBox getAdvancedFieldBox() {
    return searchAdvancedFields;
  }

  public void addRemoveClickHandler(ClickHandler clickHandler) {
    remove.addClickHandler(clickHandler);
  }

  public String getField() {
    return searchAdvancedFields.getSelectedValue();
  }

  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    String type = searchField.getType();
    List<String> searchFields = searchField.getSearchFields();

    if (searchFields != null && !searchFields.isEmpty()) {
      String field = searchFields.get(0);

      if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE) && dateValid(inputDateBox)) {
        filterParameter = new SimpleFilterParameter(field, inputDateBox.getValue().toString());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL)
        && dateIntervalValid(inputDateBoxFrom, inputDateBoxTo) && searchFields.size() >= 2) {
        String fieldTo = searchField.getSearchFields().get(1);
        filterParameter = new DateIntervalFilterParameter(field, fieldTo, inputDateBoxFrom.getValue(),
          inputDateBoxTo.getValue());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL)
        && dateIntervalValid(inputDateBoxFrom, inputDateBoxTo)) {
        filterParameter = new DateIntervalFilterParameter(field, field, inputDateBoxFrom.getValue(),
          inputDateBoxTo.getValue());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_NUMERIC) && valid(inputNumeric)) {
        filterParameter = new BasicSearchFilterParameter(field, inputNumeric.getValue());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL)
        && intervalValid(inputNumericFrom, inputNumericTo)) {
        filterParameter = new LongRangeFilterParameter(field, inputNumericFrom.getValue(), inputNumericTo.getValue());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_STORAGE)
        && intervalValid(inputStorageSizeFrom, inputStorageSizeTo)) {
        filterParameter = new LongRangeFilterParameter(field,
          Humanize.parseFileSize(inputStorageSizeFrom.getValue(), inputStorageSizeList.getSelectedValue()),
          Humanize.parseFileSize(inputStorageSizeTo.getValue(), inputStorageSizeList.getSelectedValue()));
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_BOOLEAN) && valid(inputCheckBox)) {
        filterParameter = new SimpleFilterParameter(field, Boolean.toString(inputCheckBox.getValue()));
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_SUGGEST) && valid(inputSearchSuggestBox)) {
        filterParameter = new SimpleFilterParameter(
          inputSearchSuggestBox.isAllowPartial() ? field + RodaConstants.INDEX_SEARCH_SUFFIX : field,
          inputSearchSuggestBox.getValue());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_CONTROLLED)) {
        filterParameter = new SimpleFilterParameter(field, inputControlled.getSelectedValue());
      } else if (valid(inputText)) {
        filterParameter = new BasicSearchFilterParameter(field, inputText.getValue());
      }
    }

    return filterParameter;
  }

  public void listBoxSearchField(String field) {
    SearchField searchField = searchFields.get(field);
    setSearchField(searchField);

    leftPanel.clear();
    leftPanel.add(searchAdvancedFields);
    leftPanel.add(inputPanel);
    setInputPanel(searchField.getType(), searchField.getLabel());
    panel.add(remove);
    panel.add(duplicateWarning);
    panel.removeStyleName("full_width");
  }

  public void simpleSearchField(String field, String label, String type, Tree<String> terms) {
    List<String> searchFields = new ArrayList<>();
    searchFields.add(field);
    setSearchField(new SearchField(field, searchFields, label, type, terms));

    fieldLabel.setText(label);
    leftPanel.clear();
    leftPanel.add(fieldBox);
    leftPanel.add(inputPanel);
    setInputPanel(type, label);
    panel.add(remove);
    panel.removeStyleName("full_width");
  }

  private void setInputPanel(String type, String label) {
    inputPanel.clear();
    inputPanel.removeStyleName("full_width");

    if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE)) {
      inputPanel.add(inputDateBox);
      inputDateBox.getElement().setAttribute("title", label);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL)) {
      inputPanel.add(inputDateBoxFrom);
      inputPanel.add(inputDateBoxTo);
      inputDateBoxFrom.getElement().setAttribute("title", label + " (" + messages.dateRangeFieldFrom() + ")");
      inputDateBoxTo.getElement().setAttribute("title", label + " (" + messages.dateRangeFieldTo() + ")");
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_NUMERIC)) {
      inputPanel.add(inputNumeric);
      inputPanel.getElement().setAttribute("title", label);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL)) {
      inputPanel.add(inputNumericFrom);
      inputPanel.add(inputNumericTo);
      inputDateBoxFrom.getElement().setAttribute("title", label + " (" + messages.dateRangeFieldFrom() + ")");
      inputDateBoxTo.getElement().setAttribute("title", label + " (" + messages.dateRangeFieldTo() + ")");
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_STORAGE)) {
      inputPanel.add(inputStorageSizeFrom);
      inputPanel.add(inputStorageSizeTo);
      inputPanel.add(inputStorageSizeList);
      inputStorageSizeFrom.getElement().setAttribute("title", label + " (" + messages.dateRangeFieldTo() + ")");
      inputStorageSizeTo.getElement().setAttribute("title", label + " (" + messages.dateRangeFieldTo() + ")");
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_BOOLEAN)) {
      inputPanel.add(inputCheckBox);
      inputCheckBox.getElement().setAttribute("title", label);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_SUGGEST)) {

    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_CONTROLLED)) {
      inputPanel.add(inputControlled);
      inputControlled.getElement().setAttribute("title", label);
    } else {
      inputPanel.add(inputText);
      inputText.getElement().setAttribute("title", label);
      inputPanel.addStyleName("full_width");
    }
  }

  public void addInputSearchSuggestBox(SearchSuggestBox<?> searchSuggestBox) {
    this.inputSearchSuggestBox = searchSuggestBox;
    inputPanel.add(searchSuggestBox);
    inputSearchSuggestBox.addStyleName("form-textbox");
    inputPanel.addStyleName("full_width");
  }

  private boolean dateIntervalValid(DateBox inputFrom, DateBox inputTo) {
    if (inputFrom.getValue() != null && inputTo.getValue() != null) {
      return true;
    }

    if (inputFrom.getValue() != null && inputTo.getTextBox().getText().isEmpty()) {
      return true;
    }

    if (inputFrom.getTextBox().getText().isEmpty() && inputTo.getValue() != null) {
      return true;
    }

    return false;
  }

  private boolean dateValid(DateBox input) {
    return input.getValue() != null;
  }

  private boolean valid(TextBox input) {
    return !input.getValue().isEmpty();
  }

  private boolean valid(SearchSuggestBox<?> input) {
    return !input.getValue().isEmpty();
  }

  private boolean valid(CheckBox input) {
    return input.getValue();
  }

  private boolean intervalValid(TextBox inputFrom, TextBox inputTo) {
    try {
      if (!inputFrom.getValue().isEmpty() || !inputTo.getValue().isEmpty()) {
        return true;
      }
    } catch (Exception e) {
      // do nothing
    }

    return false;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public HandlerRegistration addListBoxChangeHandler(ChangeHandler handler) {
    return searchAdvancedFields.addChangeHandler(handler);
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, searchField.getId());
  }

  public void setWarningVisible(boolean visible) {
    setWarningVisible(visible, null);
  }

  public void setWarningVisible(boolean visible, String field) {
    duplicateWarning.setVisible(visible);

    if (visible) {
      duplicateWarning.setText(messages.searchDuplicateWarningMessage(field));
      duplicateWarning.getElement().getStyle().setMarginLeft(searchAdvancedFields.getOffsetWidth() + 2, Unit.PX);
      panel.removeStyleName("search-field");
      panel.addStyleName("search-field-warning");
    } else {
      panel.removeStyleName("search-field-warning");
      panel.addStyleName("search-field");
    }
  }
}
