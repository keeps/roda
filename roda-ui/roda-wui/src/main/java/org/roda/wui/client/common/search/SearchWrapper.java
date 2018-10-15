package org.roda.wui.client.common.search;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.wui.client.common.lists.pagination.ListSelectionState;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.common.client.tools.ConfigurationManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchWrapper extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final boolean hasMultipleSearchPanels;
  private final String preselectedDropdownValue;

  private final FlowPanel rootPanel;

  // this being not null means that lists should be created inside a ScrollPanel
  // and that the ScrollPanel should be using the specified CSS classes
  private String scrollPanelCssClasses = null;

  private Dropdown searchPanelSelectionDropdown;
  private String listClassForSingleSearchPanel;

  private final Components components;

  public SearchWrapper(boolean hasMultipleSearchPanels, String preselectedDropdownValue) {
    this.searchPanelSelectionDropdown = null;
    this.listClassForSingleSearchPanel = null;
    this.hasMultipleSearchPanels = hasMultipleSearchPanels;
    this.preselectedDropdownValue = preselectedDropdownValue;
    this.components = new Components();

    rootPanel = new FlowPanel();
    initWidget(rootPanel);
  }

  public SearchWrapper withListsInsideScrollPanel(String scrollPanelCssClasses) {
    this.scrollPanelCssClasses = scrollPanelCssClasses != null ? scrollPanelCssClasses : "";
    return this;
  }

  public SearchWrapper(boolean hasMultipleSearchPanels) {
    this(hasMultipleSearchPanels, null);
  }

  public <T extends IsIndexed> SearchWrapper createListAndSearchPanel(ListBuilder<T> listBuilder) {
    AsyncTableCell<T> list = listBuilder.build();

    SearchPanel<T> searchPanel;

    Filter filter = list.getFilter();
    String allFilter = SearchFilters.searchField();
    boolean incremental = SearchFilters.shouldBeIncremental(filter);

    // get configuration

    boolean searchEnabled = ConfigurationManager.getBoolean(true, RodaConstants.UI_LISTS_PROPERTY, list.getListId(),
      RodaConstants.UI_LISTS_SEARCH_ENABLED_PROPERTY);

    String defaultLabelText = ConfigurationManager.resolveTranslation(RodaConstants.UI_LISTS_PROPERTY, list.getListId(),
      RodaConstants.UI_LISTS_SEARCH_SELECTEDINFO_LABEL_DEFAULT_I18N_PROPERTY);
    if (defaultLabelText == null) {
      defaultLabelText = messages.someOfAObject(list.getClassToReturn().getName());
    }

    String dropdownValue = list.getClassToReturn().getSimpleName();

    // create
    searchPanel = new SearchPanel<>(list, filter, allFilter, incremental,
      listBuilder.getOptions().getSearchPlaceholder(), hasMultipleSearchPanels,
      listBuilder.getOptions().getActionable(), listBuilder.getOptions().getActionableCallback());
    if (hasMultipleSearchPanels) {
      initSearchPanelSelectionDropdown();
      searchPanelSelectionDropdown.addItem(defaultLabelText, dropdownValue,
        SelectedPanel.getIconForList(list.getListId(), list.getClassToReturn().getSimpleName()));
    } else {
      listClassForSingleSearchPanel = listBuilder.getOptions().getClassToReturn().getSimpleName();
    }
    searchPanel.setVisible(searchEnabled);

    components.put(list.getClassToReturn(), searchPanel, list);

    // add search panel if none has been added yet, note that if there is a
    // preselectedDropdownValue then only the corresponding search panel should be
    // used as the default search panel
    if (rootPanel.getWidgetCount() == 0) {
      if (preselectedDropdownValue != null) {
        if (preselectedDropdownValue.equals(dropdownValue)) {
          attachComponents(dropdownValue);
        }
      } else {
        attachComponents(dropdownValue);
      }
    }

    return this;
  }

  private <T extends IsIndexed> void addShortcutButtons(SearchPanel<T> searchPanel) {

  }

  public SelectedItems<? extends IsIndexed> getSelectedItemsInCurrentList() {
    String lookupClassSimpleName = searchPanelSelectionDropdown != null
      ? searchPanelSelectionDropdown.getSelectedValue()
      : listClassForSingleSearchPanel;
    AsyncTableCell<IsIndexed> list = components.getList(lookupClassSimpleName);
    return list != null ? list.getSelected() : new SelectedItemsNone<>();
  }

  public <T extends IsIndexed> SelectedItems<T> getSelectedItems(Class<T> objectClass) {
    AsyncTableCell<T> list = components.getList(objectClass);
    return list != null ? list.getSelected() : new SelectedItemsNone<>();
  }

  public <T extends IsIndexed> ListSelectionState<T> getListSelectionState(Class<T> objectClass) {
    AsyncTableCell<T> list = components.getList(objectClass);
    return list != null ? list.getListSelectionState() : null;
  }

  public boolean changeDropdownSelectedValue(String objectClassSimpleName) {
    return searchPanelSelectionDropdown.setSelectedValue(objectClassSimpleName, true);
  }

  public void refreshAllLists() {
    components.forEachList(AsyncTableCell::refresh);
  }

  public void refreshCurrentList() {
    if (hasMultipleSearchPanels) {
      refreshList(searchPanelSelectionDropdown.getSelectedValue());
    } else {
      refreshAllLists();
    }
  }

  private <T extends IsIndexed> void refreshList(String objectClassSimpleName) {
    AsyncTableCell<T> list = components.getList(objectClassSimpleName);
    if (list != null) {
      list.refresh();
    }
  }

  private <T extends IsIndexed> void refreshList(Class<T> objectClass) {
    AsyncTableCell<T> list = components.getList(objectClass);
    if (list != null) {
      list.refresh();
    }
  }

  public <T extends IsIndexed> void setFilter(String objectClassSimpleName, Filter filter) {
    SearchPanel<T> searchPanel = components.getSearchPanel(objectClassSimpleName);
    if (searchPanel != null) {
      searchPanel.setDefaultFilter(filter, SearchFilters.shouldBeIncremental(filter));
    }
  }

  public <T extends IsIndexed> void setFilter(Class<T> objectClass, Filter filter) {
    SearchPanel<T> searchPanel = components.getSearchPanel(objectClass);
    if (searchPanel != null) {
      searchPanel.setDefaultFilter(filter, SearchFilters.shouldBeIncremental(filter));
    }
  }

  public <T extends IsIndexed> void resetToDefaultFilter(String objectClassSimpleName) {
    SearchPanel<T> searchPanel = components.getSearchPanel(objectClassSimpleName);
    if (searchPanel != null) {
      searchPanel.setDefaultFilter(SearchFilters.allFilter(), false);
    }
  }

  public <T extends IsIndexed> void resetToDefaultFilter(Class<T> objectClass) {
    SearchPanel<T> searchPanel = components.getSearchPanel(objectClass);
    if (searchPanel != null) {
      searchPanel.setDefaultFilter(SearchFilters.allFilter(), false);
    }
  }

  public <T extends IsIndexed> void addSearchFieldTextValueChangeHandler(String objectClassSimpleName,
    ValueChangeHandler<String> handler) {
    SearchPanel<T> searchPanel = components.getSearchPanel(objectClassSimpleName);
    if (searchPanel != null) {
      searchPanel.addValueChangeHandler(handler);
    }
  }

  public <T extends IsIndexed> void addSearchFieldTextValueChangeHandler(Class<T> objectClass,
    ValueChangeHandler<String> handler) {
    SearchPanel<T> searchPanel = components.getSearchPanel(objectClass);
    if (searchPanel != null) {
      searchPanel.addValueChangeHandler(handler);
    }
  }

  public <T extends IsIndexed> void addListDataChangeHandler(String objectClassSimpleName,
    ValueChangeHandler<IndexResult<T>> handler) {
    AsyncTableCell<T> list = components.getList(objectClassSimpleName);
    if (list != null) {
      list.addValueChangeHandler(handler);
    }
  }

  public <T extends IsIndexed> void addListDataChangeHandler(Class<T> objectClass,
    ValueChangeHandler<IndexResult<T>> handler) {
    AsyncTableCell<T> list = components.getList(objectClass);
    if (list != null) {
      list.addValueChangeHandler(handler);
    }
  }

  private void initSearchPanelSelectionDropdown() {
    if (searchPanelSelectionDropdown == null) {
      searchPanelSelectionDropdown = new Dropdown();
      searchPanelSelectionDropdown.addStyleName("searchInputListBox");
      searchPanelSelectionDropdown.addPopupStyleName("searchInputListBoxPopup");
      searchPanelSelectionDropdown.addValueChangeHandler(event -> attachComponents(event.getValue()));
    }
  }

  private <T extends IsIndexed> void attachComponents(String objectClassSimpleName) {
    SearchPanel<T> searchPanel = components.getSearchPanel(objectClassSimpleName);
    AsyncTableCell<T> list = components.getList(objectClassSimpleName);

    rootPanel.clear();
    rootPanel.add(searchPanel);
    if (scrollPanelCssClasses != null) {
      ScrollPanel scrollPanel = new ScrollPanel(list);
      scrollPanel.addStyleName(scrollPanelCssClasses);
      rootPanel.add(scrollPanel);
    } else {
      rootPanel.add(list);
    }

    if (hasMultipleSearchPanels) {
      searchPanelSelectionDropdown.setSelectedValue(objectClassSimpleName, false);
      searchPanel.attachSearchPanelSelectionDropdown(searchPanelSelectionDropdown);
    }
  }

  /**
   * Auxiliary manager for inner components (groups of one searchWrapper and one
   * BasicAsyncTableCell, at least for now) that is used to enforce type coherence
   */
  @SuppressWarnings("unchecked")
  private class Components {
    private final Map<Class<? extends IsIndexed>, SearchPanel<? extends IsIndexed>> searchPanels = new LinkedHashMap<>();
    private final Map<Class<? extends IsIndexed>, AsyncTableCell<? extends IsIndexed>> lists = new LinkedHashMap<>();

    /**
     * Add a new set of components associated with a class.
     *
     * @param objectClass
     *          the classe to associate the components with
     * @param searchPanel
     *          the searchWrapper component
     * @param list
     *          the BasicAsyncTableCell component
     * @param <T>
     *          extends IsIndexed, type parameter shared by this set of components
     */
    public <T extends IsIndexed> void put(Class<T> objectClass, SearchPanel<T> searchPanel, AsyncTableCell<T> list) {
      searchPanels.put(objectClass, searchPanel);
      lists.put(objectClass, list);
    }

    <T extends IsIndexed> SearchPanel<T> getSearchPanel(Class<T> objectClass) {
      return (SearchPanel<T>) searchPanels.get(objectClass);
    }

    <T extends IsIndexed> AsyncTableCell<T> getList(Class<T> objectClass) {
      return (AsyncTableCell<T>) lists.get(objectClass);
    }

    <T extends IsIndexed> SearchPanel<T> getSearchPanel(String className) {
      return (SearchPanel<T>) searchPanels.get(classForName(className));
    }

    <T extends IsIndexed> AsyncTableCell<T> getList(String className) {
      return (AsyncTableCell<T>) lists.get(classForName(className));
    }

    <T extends IsIndexed> void forEachList(Consumer<AsyncTableCell<T>> action) {
      for (AsyncTableCell<? extends IsIndexed> value : lists.values()) {
        AsyncTableCell<T> list = (AsyncTableCell<T>) value;
        action.accept(list);
      }
    }

    // auxiliary
    private <T extends IsIndexed> Class<T> classForName(String classSimpleName) {
      for (Class<? extends IsIndexed> associatedClass : searchPanels.keySet()) {
        if (associatedClass.getSimpleName().equals(classSimpleName)) {
          return (Class<T>) associatedClass;
        }
      }
      return null;
    }
  }
}
