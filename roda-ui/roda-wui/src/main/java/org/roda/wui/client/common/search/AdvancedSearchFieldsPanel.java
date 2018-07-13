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

import com.google.gwt.core.client.GWT;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.ListboxUtils;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class AdvancedSearchFieldsPanel extends FlowPanel implements HasValueChangeHandlers<Integer> {
  private String className;
  private final Map<String, SearchField> searchFields = new HashMap<>();
  private ListBox searchAdvancedFieldOptions;

  public AdvancedSearchFieldsPanel(String className) {
    super();

    this.className = className;
    searchAdvancedFieldOptions = new ListBox();

    BrowserService.Util.getInstance().retrieveSearchFields(className, LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<List<SearchField>>() {
        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(List<SearchField> searchFields) {
          AdvancedSearchFieldsPanel.this.searchFields.clear();
          for (SearchField searchField : searchFields) {
            ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptions, searchField.getLabel(),
              searchField.getId());
            AdvancedSearchFieldsPanel.this.searchFields.put(searchField.getId(), searchField);
          }

          for (SearchField searchField : searchFields) {
            if (searchField.isFixed()) {
              addSearchFieldPanel(searchField.getId());
            }
          }
        }
      });

    addStyleName("searchAdvancedFieldsPanel empty");
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

    ClickHandler clickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        AdvancedSearchFieldsPanel.this.remove(searchFieldPanel);
        if (AdvancedSearchFieldsPanel.this.getWidgetCount() == 0) {
          AdvancedSearchFieldsPanel.this.addStyleName("empty");
          AdvancedSearchFieldsPanel.this.onChange();
          addWarningToDuplicateFields();
        }
      }
    };

    searchFieldPanel.addRemoveClickHandler(clickHandler);
    searchFieldPanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        setSuggestions(searchFieldPanel);
      }
    });

    searchFieldPanel.addListBoxChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        addWarningToDuplicateFields();
      }
    });

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
      if (className.equals(RodaConstants.SEARCH_ITEMS)) {
        searchFieldPanel.addInputSearchSuggestBox(new SearchSuggestBox<>(IndexedAIP.class,
          searchField.getSuggestField(), searchField.isSuggestPartial()));
      } else if (className.equals(RodaConstants.SEARCH_REPRESENTATIONS)) {
        searchFieldPanel.addInputSearchSuggestBox(new SearchSuggestBox<>(
          IndexedRepresentation.class, searchField.getSuggestField(), searchField.isSuggestPartial()));
      } else if (className.equals(RodaConstants.SEARCH_FILES)) {
        searchFieldPanel.addInputSearchSuggestBox(new SearchSuggestBox<>(IndexedFile.class,
          searchField.getSuggestField(), searchField.isSuggestPartial()));
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
