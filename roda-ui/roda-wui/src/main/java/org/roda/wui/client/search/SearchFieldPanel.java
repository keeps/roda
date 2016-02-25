package org.roda.wui.client.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.DateIntervalFilterParameter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.LongRangeFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.common.utils.ListboxUtils;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

import config.i18n.client.BrowseMessages;

public class SearchFieldPanel extends Composite {
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private FlowPanel panel;
  private FlowPanel leftPanel;
  private FlowPanel inputPanel;
  private Button remove = new Button("<i class=\"fa fa-close\"></i>");

  private SearchField searchField;

  // Simple search field
  private Label fieldLabel;

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
  // Enum
  private ListBox inputListBox;

  public SearchFieldPanel() {
    panel = new FlowPanel();
    leftPanel = new FlowPanel();
    inputPanel = new FlowPanel();
    fieldLabel = new Label();
    searchAdvancedFields = new ListBox();

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));

    inputText = new TextBox();

    inputDateBox = new DateBox();
    inputDateBox.setFormat(dateFormat);
    inputDateBox.getDatePicker().setYearArrowsVisible(true);
    inputDateBox.setFireNullValues(true);
    inputDateBox.getElement().setPropertyString("placeholder", messages.searchFieldDatePlaceHolder());

    inputDateBoxFrom = new DateBox();
    inputDateBoxFrom.setFormat(dateFormat);
    inputDateBoxFrom.getDatePicker().setYearArrowsVisible(true);
    inputDateBoxFrom.setFireNullValues(true);
    inputDateBoxFrom.getElement().setPropertyString("placeholder", messages.searchFieldDateFromPlaceHolder());

    inputDateBoxTo = new DateBox();
    inputDateBoxTo.setFormat(dateFormat);
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
    // TODO
    inputStorageSizeList.addItem("B", "bytes");
    inputStorageSizeList.addItem("KB", "kbytes");
    inputStorageSizeList.addItem("MB", "megabytes");
    inputStorageSizeList.addItem("GB", "gigabytes");

    inputCheckBox = new CheckBox();

    inputListBox = new ListBox();

    panel.add(leftPanel);

    initWidget(panel);

    searchAdvancedFields.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        listBoxSearchField(searchAdvancedFields.getSelectedValue());
      }
    });

    panel.addStyleName("search-field");
    leftPanel.addStyleName("search-field-left-panel");
    inputPanel.addStyleName("search-field-input-panel");
    inputPanel.addStyleName("full_width");
    remove.addStyleName("search-field-remove");
    fieldLabel.addStyleName("search-field-label");
    searchAdvancedFields.addStyleName("form-listbox");

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
    inputListBox.addStyleName("form-listbox");
  }

  public SearchField getSearchField() {
    return searchField;
  }

  public void setSearchField(SearchField searchField) {
    this.searchField = searchField;
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

  public void addRemoveClickHandler(ClickHandler clickHandler) {
    remove.addClickHandler(clickHandler);
  }

  public String getField() {
    return searchAdvancedFields.getSelectedValue();
  }

  // TODO validate inputs!
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    String type = searchField.getType();
    List<String> searchFields = searchField.getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);

      if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE) && inputDateBox.getValue() != null) {
        filterParameter = new BasicSearchFilterParameter(field, inputDateBox.getValue().toString());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL) && inputDateBoxFrom.getValue() != null
        && inputDateBoxTo.getValue() != null && searchFields.size() >= 2) {
        String fieldTo = searchField.getSearchFields().get(1);
        filterParameter = new DateIntervalFilterParameter(field, fieldTo, inputDateBoxFrom.getValue(),
          inputDateBoxTo.getValue());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL) && inputDateBoxFrom.getValue() != null
        && inputDateBoxTo.getValue() != null) {
        filterParameter = new DateIntervalFilterParameter(field, field, inputDateBoxFrom.getValue(),
          inputDateBoxTo.getValue());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_NUMERIC) && !inputNumeric.getValue().isEmpty()) {
        filterParameter = new BasicSearchFilterParameter(field, inputNumeric.getValue());
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL)) {
        filterParameter = new LongRangeFilterParameter(field, Long.valueOf(inputNumericFrom.getValue()),
          Long.valueOf(inputNumericTo.getValue()));
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_STORAGE)) {
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_BOOLEAN)) {
      } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_LIST)) {
      } else if (!inputText.getValue().isEmpty()) {
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
    setInputPanel(searchField.getType());
    panel.add(remove);
    panel.removeStyleName("full_width");
  }

  public void simpleSearchField(String field, String label, String type) {
    List<String> searchFields = new ArrayList<String>();
    searchFields.add(field);
    setSearchField(new SearchField(field, searchFields, label, type));

    fieldLabel.setText(label);
    leftPanel.clear();
    leftPanel.add(fieldLabel);
    leftPanel.add(inputPanel);
    setInputPanel(type);
    panel.addStyleName("full_width");
  }

  private void setInputPanel(String type) {
    inputPanel.clear();
    inputPanel.removeStyleName("full_width");

    if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE)) {
      inputPanel.add(inputDateBox);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL)) {
      inputPanel.add(inputDateBoxFrom);
      inputPanel.add(inputDateBoxTo);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_NUMERIC)) {
      inputPanel.add(inputNumeric);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL)) {
      inputPanel.add(inputNumericFrom);
      inputPanel.add(inputNumericTo);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_STORAGE)) {
      inputPanel.add(inputStorageSizeFrom);
      inputPanel.add(inputStorageSizeTo);
      inputPanel.add(inputStorageSizeList);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_BOOLEAN)) {
      inputPanel.add(inputCheckBox);
    } else if (type.equals(RodaConstants.SEARCH_FIELD_TYPE_LIST)) {
      inputPanel.add(inputListBox);
    } else {
      inputPanel.add(inputText);
      inputPanel.addStyleName("full_width");
    }
  }
}
