/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.utils.ListboxUtils;
import org.roda.wui.client.common.utils.Tree;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class AdvancedSearchFieldsPanel extends FlowPanel implements HasValueChangeHandlers<Integer> {
  private String classSimpleName;
  private final Map<String, SearchField> searchFields = new HashMap<>();
  private ListBox searchAdvancedFieldOptions;

  private static List<SearchField> getSearchFieldsFromConfig(String className) {
    List<SearchField> searchFields = new ArrayList<>();
    List<String> fields = ConfigurationManager.getStringList(RodaConstants.SEARCH_FIELD_PREFIX, className);

    for (String field : fields) {
      String fieldPrefix = RodaConstants.SEARCH_FIELD_PREFIX + '.' + className + '.' + field;

      SearchField searchField = new SearchField();
      String fieldsNames = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_FIELDS);
      String fieldType = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_TYPE);
      String fieldLabelI18N = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_I18N);
      String fieldI18NPrefix = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_I18N_PREFIX);
      List<String> fieldsValues = ConfigurationManager.getStringList(fieldPrefix, RodaConstants.SEARCH_FIELD_VALUES);
      String suggestField = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_TYPE_SUGGEST_FIELD);

      boolean fieldFixed = ConfigurationManager.getBoolean(false, fieldPrefix, RodaConstants.SEARCH_FIELD_FIXED);
      boolean suggestPartial = ConfigurationManager.getBoolean(false, fieldPrefix,
        RodaConstants.SEARCH_FIELD_TYPE_SUGGEST_PARTIAL);

      if (fieldsNames != null && fieldType != null && fieldLabelI18N != null) {
        List<String> fieldsNamesList = Arrays.asList(fieldsNames.split(","));

        searchField.setId(field);
        searchField.setSearchFields(fieldsNamesList);
        searchField.setType(fieldType);
        try {
          searchField.setLabel(ConfigurationManager.getTranslation(fieldLabelI18N));
        } catch (MissingResourceException e) {
          searchField.setLabel(fieldLabelI18N);
        }
        searchField.setFixed(fieldFixed);

        if (fieldsValues != null) {

          Map<String, String> labels = new HashMap<>();
          for (String fieldValue : fieldsValues) {
            labels.put(fieldValue, ConfigurationManager.getTranslation(fieldI18NPrefix, fieldValue));
          }

          Tree<String> terms = new Tree<>(field, field);
          for (String value : fieldsValues) {
            terms.addChild(labels.get(value), value);
          }
          searchField.setTerms(terms);
        }

        if (suggestField != null) {
          searchField.setSuggestField(suggestField);
        }

        searchField.setSuggestPartial(suggestPartial);
        searchFields.add(searchField);
      }
    }

    return searchFields;
  }

  public AdvancedSearchFieldsPanel(String classSimpleName) {
    super();

    this.classSimpleName = classSimpleName;

    searchAdvancedFieldOptions = new ListBox();

    addStyleName("searchAdvancedFieldsPanel empty");

    List<SearchField> searchFieldsList = getSearchFieldsFromConfig(this.classSimpleName);

    searchFields.clear();
    for (SearchField searchField : searchFieldsList) {
      String id = searchField.getId();
      String label = StringUtils.isNotBlank(searchField.getLabel()) ? searchField.getLabel() : id;
      ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptions, label, id);
      searchFields.put(id, searchField);
    }

    for (SearchField searchField : searchFieldsList) {
      if (searchField.isFixed()) {
        addSearchFieldPanel(searchField.getId());
      }
    }
  }

  public void addSearchFieldPanel() {
    addSearchFieldPanel(null);
  }

  public void addSearchFieldPanel(String field) {
    final SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
    String selectedField = field;

    searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
    searchFieldPanel.setSearchFields(searchFields);

    if (selectedField == null) {
      selectedField = searchFieldPanel.getFirstSearchField();
    }

    searchFieldPanel.selectSearchField(selectedField);

    ClickHandler clickHandler = event -> {
      AdvancedSearchFieldsPanel.this.remove(searchFieldPanel);
      if (AdvancedSearchFieldsPanel.this.getWidgetCount() == 0) {
        AdvancedSearchFieldsPanel.this.addStyleName("empty");
        AdvancedSearchFieldsPanel.this.onChange();
        addWarningToDuplicateFields();
      }
    };

    searchFieldPanel.addRemoveClickHandler(clickHandler);
    searchFieldPanel.addValueChangeHandler(event -> setSuggestions(searchFieldPanel));

    searchFieldPanel.addListBoxChangeHandler(event -> addWarningToDuplicateFields());

    setSuggestions(searchFieldPanel);
    add(searchFieldPanel);
    removeStyleName("empty");

    onChange();
    addWarningToDuplicateFields();
  }

  private void addWarningToDuplicateFields() {
    List<String> activeFields = new ArrayList<>();
    for (int i = 0; i < getWidgetCount(); i++) {
      Widget widget = getWidget(i);

      if (widget instanceof SearchFieldPanel) {
        SearchFieldPanel fieldPanel = (SearchFieldPanel) widget;
        ListBox listBox = fieldPanel.getAdvancedFieldBox();

        if (activeFields.contains(listBox.getSelectedValue())) {
          fieldPanel.setWarningVisible(true, listBox.getSelectedValue());
        } else {
          activeFields.add(listBox.getSelectedValue());
          fieldPanel.setWarningVisible(false);
        }
      }
    }
  }

  private void setSuggestions(SearchFieldPanel searchFieldPanel) {
    SearchField searchField = searchFieldPanel.getSearchField();

    if (searchField.getType().equals(RodaConstants.SEARCH_FIELD_TYPE_SUGGEST)) {
      if (classSimpleName.equals(IndexedAIP.class.getSimpleName())) {
        searchFieldPanel.addInputSearchSuggestBox(
          new SearchSuggestBox<>(IndexedAIP.class, searchField.getSuggestField(), searchField.isSuggestPartial()));
      } else if (classSimpleName.equals(IndexedRepresentation.class.getSimpleName())) {
        searchFieldPanel.addInputSearchSuggestBox(new SearchSuggestBox<>(IndexedRepresentation.class,
          searchField.getSuggestField(), searchField.isSuggestPartial()));
      } else if (classSimpleName.equals(IndexedFile.class.getSimpleName())) {
        searchFieldPanel.addInputSearchSuggestBox(
          new SearchSuggestBox<>(IndexedFile.class, searchField.getSuggestField(), searchField.isSuggestPartial()));
      }
    }
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getWidgetCount());
  }
}
