/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.lists.pagination.ListSelectionState;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.MyCellTableResources;
import org.roda.wui.common.client.widgets.wcag.AccessibleCellTable;
import org.roda.wui.common.client.widgets.wcag.AccessibleSimplePager;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;

import config.i18n.client.ClientMessages;

public abstract class AsyncTableCell<T extends IsIndexed, O> extends FlowPanel
  implements HasValueChangeHandlers<IndexResult<T>> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final Class<T> classToReturn;
  private final O object;

  private final MyAsyncDataProvider<T> dataProvider;
  private final SingleSelectionModel<T> selectionModel;
  private final AsyncHandler columnSortHandler;

  private final AccessibleSimplePager resultsPager;
  private final RodaPageSizePager pageSizePager;
  private Button csvDownloadButton;
  private Button actionsButton;
  private final CellTable<T> display;

  private FlowPanel selectAllPanel;
  private FlowPanel selectAllPanelBody;
  private Label selectAllLabel;
  private CheckBox selectAllCheckBox;

  private Column<T, Boolean> selectColumn;
  private Set<T> selected = new HashSet<T>();
  private final List<CheckboxSelectionListener<T>> listeners = new ArrayList<AsyncTableCell.CheckboxSelectionListener<T>>();

  private Filter filter;
  private boolean justActive;
  private Facets facets;
  private boolean selectable;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private int initialPageSize = 20;
  private int pageSizeIncrement = 100;

  private Timer autoUpdateTimer = null;
  private int autoUpdateTimerMillis = 0;

  private IndexResult<T> result;

  private Actionable<T> actionable = null;
  private final PopupPanel actionsPopup = new PopupPanel(true, true);

  public AsyncTableCell(Class<T> classToReturn) {
    this(classToReturn, null, false, null, null, false, 20, 100, null);
  }

  public AsyncTableCell(Class<T> classToReturn, Filter filter, boolean justActive, Facets facets, String summary,
    boolean selectable, O object) {
    this(classToReturn, filter, justActive, facets, summary, selectable, 20, 100, object);
  }

  public AsyncTableCell(final Class<T> classToReturn, final Filter filter, final boolean justActive,
    final Facets facets, final String summary, final boolean selectable, final int initialPageSize,
    final int pageSizeIncrement, final O object) {
    super();

    this.classToReturn = classToReturn;
    this.initialPageSize = initialPageSize;
    this.pageSizeIncrement = pageSizeIncrement;
    this.object = object;

    final String notNullSummary = StringUtils.isNotBlank(summary) ? summary : "summary" + Random.nextInt(1000);

    this.filter = filter;
    this.justActive = justActive;
    this.facets = facets;
    this.selectable = selectable;

    display = new AccessibleCellTable<T>(getInitialPageSize(),
      (MyCellTableResources) GWT.create(MyCellTableResources.class), getKeyProvider(), summary);
    display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
    display.setLoadingIndicator(new HTML(HtmlSnippetUtils.LOADING));

    configure(display);

    this.dataProvider = new MyAsyncDataProvider<T>(display, new IndexResultDataProvider<T>() {

      @Override
      public void getData(Sublist sublist, Sorter sorter, final AsyncCallback<IndexResult<T>> callback) {
        AsyncTableCell.this.getData(AsyncTableCell.this.getFilter(), sublist, sorter,
          new AsyncCallback<IndexResult<T>>() {

            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);

            }

            @Override
            public void onSuccess(IndexResult<T> result) {
              setResult(result);
              callback.onSuccess(result);
            }
          });
      }

      @Override
      public Sorter getSorter(ColumnSortList columnSortList) {
        return AsyncTableCell.this.getSorter(columnSortList);
      }
    }) {

      @Override
      protected void fireChangeEvent(IndexResult<T> result) {
        ValueChangeEvent.fire(AsyncTableCell.this, result);
      }
    };

    dataProvider.addDataDisplay(display);

    resultsPager = new AccessibleSimplePager(AccessibleSimplePager.TextLocation.LEFT,
      (SimplePager.Resources) GWT.create(SimplePager.Resources.class), false, initialPageSize, false, false,
      (SimplePager.ImageButtonsConstants) GWT.create(SimplePager.ImageButtonsConstants.class));
    resultsPager.setDisplay(display);

    pageSizePager = new RodaPageSizePager(getPageSizePagerIncrement());
    pageSizePager.setDisplay(display);

    csvDownloadButton = new Button(messages.tableDownloadCSV());
    csvDownloadButton.addStyleName("btn btn-link csvDownloadButton");

    actionsButton = new Button(messages.tableAction());
    actionsButton.addStyleName("btn btn-link actionsButton");
    actionsButton.setVisible(actionable != null);
    actionsPopup.setStyleName("actions-popup");

    createSelectAllPanel();

    add(selectAllPanel);
    add(display);
    add(resultsPager);
    add(pageSizePager);
    add(csvDownloadButton);
    add(actionsButton);

    csvDownloadButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        RestUtils.requestCSVExport(getClassToReturn(), getFilter(), dataProvider.getSorter(), dataProvider.getSublist(),
          getFacets(), getJustActive(), false, notNullSummary + ".csv");
      }
    });

    actionsButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        showActions();
      }
    });

    selectionModel = new SingleSelectionModel<>(getKeyProvider());

    Handler<T> selectionEventManager = getSelectionEventManager();
    if (selectionEventManager != null) {
      display.setSelectionModel(selectionModel, selectionEventManager);
    } else {
      display.setSelectionModel(selectionModel);
    }

    columnSortHandler = new AsyncHandler(display);
    display.addColumnSortHandler(columnSortHandler);

    addStyleName("my-asyncdatagrid");
    resultsPager.addStyleName("my-asyncdatagrid-pager-results");
    pageSizePager.addStyleName("my-asyncdatagrid-pager-pagesize");
    display.addStyleName("my-asyncdatagrid-display");

    addValueChangeHandler(new ValueChangeHandler<IndexResult<T>>() {
      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<T>> event) {
        selected = new HashSet<T>();
        hideSelectAllPanel();
      }
    });

    updateEmptyTableWidget();
  }

  private void updateEmptyTableWidget() {
    if (FacetUtils.hasSelected(getFacets())) {
      FlowPanel layout = new FlowPanel();
      Label l = new Label(messages.noItemsToDisplayButFacetsActive());
      Button resetFacets = new Button(messages.disableFacets());

      layout.addStyleName("table-empty");
      resetFacets.addStyleName("table-empty-clear-facets btn btn-primary btn-ban");

      resetFacets.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          FacetUtils.clearFacets(getFacets());
          refresh();
        }

      });
      layout.add(l);
      layout.add(resetFacets);
      display.setEmptyTableWidget(layout);
    } else {
      Label layout = new Label(messages.noItemsToDisplay());
      layout.addStyleName("table-empty");
      display.setEmptyTableWidget(layout);
    }
  }

  private void configure(final CellTable<T> display) {
    if (selectable) {
      selectColumn = new Column<T, Boolean>(new CheckboxCell(true, false)) {
        @Override
        public Boolean getValue(T object) {
          return selected.contains(object);
        }
      };

      selectColumn.setFieldUpdater(new FieldUpdater<T, Boolean>() {
        @Override
        public void update(int index, T object, Boolean isSelected) {
          if (isSelected) {
            selected.add(object);
          } else {
            selected.remove(object);
          }

          // update header
          display.redrawHeaders();
          fireOnCheckboxSelectionChanged();
        }
      });

      Header<Boolean> selectHeader = new Header<Boolean>(new CheckboxCell(true, true)) {

        @Override
        public Boolean getValue() {
          Boolean ret;

          if (selected.isEmpty()) {
            ret = false;
          } else if (selected.containsAll(getVisibleItems())) {
            ret = true;
            showSelectAllPanel();
          } else {
            // some are selected
            ret = false;
            hideSelectAllPanel();
          }

          return ret;
        }
      };

      selectHeader.setUpdater(new ValueUpdater<Boolean>() {

        @Override
        public void update(Boolean value) {
          if (value) {
            selected.addAll(getVisibleItems());
            showSelectAllPanel();
          } else {
            selected.clear();
            hideSelectAllPanel();
          }
          redraw();
          fireOnCheckboxSelectionChanged();
        }
      });

      display.addColumn(selectColumn, selectHeader);
      display.setColumnWidth(selectColumn, "45px");
    }
    configureDisplay(display);
  }

  public Class<T> getClassToReturn() {
    return classToReturn;
  }

  protected abstract void configureDisplay(CellTable<T> display);

  protected int getInitialPageSize() {
    return initialPageSize;
  }

  protected ProvidesKey<T> getKeyProvider() {
    return new ProvidesKey<T>() {

      @Override
      public Object getKey(T item) {
        return item.getUUID();
      }
    };
  }

  private void getData(Filter filter, Sublist sublist, Sorter sorter, AsyncCallback<IndexResult<T>> callback) {
    if (filter == null) {
      callback.onSuccess(null);
    } else {
      getData(sublist, sorter, callback);
    }
  }

  protected void getData(Sublist sublist, Sorter sorter, AsyncCallback<IndexResult<T>> callback) {
    BrowserService.Util.getInstance().find(getClassToReturn().getName(), getFilter(), sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), getJustActive(), callback);
  }

  protected abstract Sorter getSorter(ColumnSortList columnSortList);

  public Sorter getSorter() {
    return dataProvider.getSorter();
  }

  protected int getPageSizePagerIncrement() {
    return pageSizeIncrement;
  }

  protected CellPreviewEvent.Handler<T> getSelectionEventManager() {
    if (selectable) {
      return DefaultSelectionEventManager.<T> createBlacklistManager(0);
    } else {
      return null;
    }
  }

  public SingleSelectionModel<T> getSelectionModel() {
    return selectionModel;
  }

  public void refresh() {
    selected = new HashSet<T>();
    hideSelectAllPanel();
    display.setVisibleRangeAndClearData(new Range(0, getInitialPageSize()), true);
    getSelectionModel().clear();
    updateEmptyTableWidget();
  }

  public void update() {
    dataProvider.update();
  }

  public void autoUpdate(int periodMillis) {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }

    autoUpdateTimer = new Timer() {

      @Override
      public void run() {
        dataProvider.update(new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            // disable auto-update
            autoUpdateTimer.cancel();
          }

          @Override
          public void onSuccess(Void result) {
            // do nothing
          }
        });
      }
    };

    autoUpdateTimerMillis = periodMillis;
    if (this.isAttached()) {
      autoUpdateTimer.scheduleRepeating(periodMillis);
    }

  }

  @Override
  protected void onDetach() {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
    super.onDetach();
  }

  @Override
  protected void onLoad() {
    if (autoUpdateTimer != null && autoUpdateTimerMillis > 0 && !autoUpdateTimer.isRunning()) {
      autoUpdateTimer.scheduleRepeating(autoUpdateTimerMillis);
    }
    super.onLoad();
  }

  public void redraw() {
    display.redraw();
  }

  public Filter getFilter() {
    return filter;
  }

  public boolean getJustActive() {
    return justActive;
  }

  public void setJustActive(boolean justActive) {
    this.justActive = justActive;
    refresh();
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
    refresh();
  }

  public Facets getFacets() {
    return facets;
  }

  public void setFacets(Facets facets) {
    this.facets = facets;
    refresh();
  }

  public void set(Filter filter, boolean justActive, Facets facets) {
    this.filter = filter;
    this.justActive = justActive;
    this.facets = facets;
    refresh();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<IndexResult<T>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public List<T> getVisibleItems() {
    return display.getVisibleItems();
  }

  protected Sorter createSorter(ColumnSortList columnSortList, Map<Column<T, ?>, List<String>> columnSortingKeyMap) {
    Sorter sorter = new Sorter();
    for (int i = 0; i < columnSortList.size(); i++) {
      ColumnSortInfo columnSortInfo = columnSortList.get(i);

      List<String> sortParameterKeys = columnSortingKeyMap.get(columnSortInfo.getColumn());

      if (sortParameterKeys != null) {
        for (String sortParameterKey : sortParameterKeys) {
          sorter.add(new SortParameter(sortParameterKey, !columnSortInfo.isAscending()));
        }
      } else {
        logger.warn("Selecting a sorter that is not mapped");
      }
    }
    return sorter;
  }

  public void nextItemSelection() {
    nextItemSelection(false);
  }

  public void nextItemSelection(boolean nextPageJump) {
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      int selectedIndex = getVisibleItems().indexOf(selectedItem);

      if (nextPageJump) {
        if (selectedIndex == -1) {
          getSelectionModel().setSelected(getVisibleItems().get(0), true);
        } else {
          getSelectionModel().setSelected(getVisibleItems().get(selectedIndex + 1), true);
        }
      } else {
        if (selectedIndex < getVisibleItems().size() - 1) {
          getSelectionModel().setSelected(getVisibleItems().get(selectedIndex + 1), true);
        }
      }
    } else {
      getSelectionModel().setSelected(getVisibleItems().get(0), true);
    }
  }

  public void previousItemSelection() {
    previousItemSelection(false);
  }

  public void previousItemSelection(boolean previousPageJump) {
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      int selectedIndex = getVisibleItems().indexOf(selectedItem);

      if (previousPageJump) {
        if (selectedIndex == -1) {
          getSelectionModel().setSelected(getVisibleItems().get(getVisibleItems().size() - 1), true);
        } else {
          getSelectionModel().setSelected(getVisibleItems().get(selectedIndex - 1), true);
        }
      } else {
        if (selectedIndex > 0) {
          getSelectionModel().setSelected(getVisibleItems().get(selectedIndex - 1), true);
        }
      }
    } else {
      getSelectionModel().setSelected(getVisibleItems().get(0), true);
    }
  }

  public boolean nextPageOnNextFile() {
    boolean nextPage = false;
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      if (getVisibleItems().indexOf(selectedItem) == (resultsPager.getPageSize() - 1) && resultsPager.hasNextPage()) {
        nextPage = true;
      }
    }
    return nextPage;
  }

  public boolean previousPageOnPreviousFile() {
    boolean previousPage = false;
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      if (getVisibleItems().indexOf(selectedItem) == 0 && resultsPager.hasPreviousPage()) {
        previousPage = true;
      }
    }
    return previousPage;
  }

  public void nextPage() {
    resultsPager.nextPage();
  }

  public void previousPage() {
    resultsPager.previousPage();
  }

  public boolean isSelectable() {
    return selectable;
  }

  public void setSelectable(boolean selectable) {
    this.selectable = selectable;
  }

  public SelectedItems<T> getSelected() {
    SelectedItems<T> ret;
    if (isAllSelected()) {
      Filter filterPlusFacets = new Filter(getFilter());
      if (getFacets() != null) {
        for (FacetParameter facetParameter : getFacets().getParameters().values()) {
          if (!facetParameter.getValues().isEmpty()) {
            filterPlusFacets.add(new OneOfManyFilterParameter(facetParameter.getName(), facetParameter.getValues()));
          }
        }
      }

      ret = new SelectedItemsFilter<T>(filterPlusFacets, getClassToReturn().getName(), getJustActive());
    } else {
      List<String> ids = new ArrayList<>();

      for (T item : selected) {
        ids.add(item.getUUID());
      }

      ret = new SelectedItemsList<T>(ids, getClassToReturn().getName());
    }

    return ret;
  }

  public void setSelected(Set<T> newSelected) {
    selected.clear();
    selected.addAll(newSelected);
    redraw();
    fireOnCheckboxSelectionChanged();
  }

  public void clearSelected() {
    selected.clear();
    redraw();
    fireOnCheckboxSelectionChanged();
  }

  public int getIndexOfVisibleObject(T object) {
    int visibleIndex = getVisibleItems().indexOf(object);
    int visibleStartIndex = display.getVisibleRange().getStart();
    return visibleStartIndex + visibleIndex;
  }

  public ListSelectionState<T> getListSelectionState() {
    ListSelectionState<T> ret = null;
    T selectedObject = getSelectionModel().getSelectedObject();
    if (selectedObject != null) {
      int index = getIndexOfVisibleObject(selectedObject);
      ret = ListSelectionUtils.create(selectedObject, getFilter(), getJustActive(), getFacets(), getSorter(), index);
    }
    return ret;
  }

  // LISTENER

  public interface CheckboxSelectionListener<T extends IsIndexed> {
    public void onSelectionChange(SelectedItems<T> selected);
  }

  public void addCheckboxSelectionListener(CheckboxSelectionListener<T> checkboxSelectionListener) {
    listeners.add(checkboxSelectionListener);
  }

  public void removeCheckboxSelectionListener(CheckboxSelectionListener<T> listener) {
    listeners.remove(listener);
  }

  public void fireOnCheckboxSelectionChanged() {
    for (CheckboxSelectionListener<T> listener : listeners) {
      listener.onSelectionChange(getSelected());
    }
  }

  // SELECT ALL PANEL

  public void createSelectAllPanel() {
    selectAllPanel = new FlowPanel();
    selectAllPanelBody = new FlowPanel();
    selectAllCheckBox = new CheckBox();
    selectAllLabel = new Label("Select all");

    selectAllPanelBody.add(selectAllCheckBox);
    selectAllPanelBody.add(selectAllLabel);
    selectAllPanel.add(selectAllPanelBody);
    selectAllPanel.setVisible(false);

    selectAllPanel.addStyleName("panel");
    selectAllPanelBody.addStyleName("panel-body");

    selectAllCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        fireOnCheckboxSelectionChanged();
      }
    });

  }

  public void showSelectAllPanel() {
    if (!selectAllPanel.isVisible() && resultsPager.hasNextPage() || resultsPager.hasPreviousPage()) {
      selectAllLabel.setText(messages.listSelectAllMessage(dataProvider.getRowCount()));
      selectAllCheckBox.setValue(false);
      selectAllPanel.setVisible(true);
    }
  }

  public void hideSelectAllPanel() {
    selectAllCheckBox.setValue(false);
    selectAllPanel.setVisible(false);
  }

  public Boolean isAllSelected() {
    return selectAllCheckBox.getValue();
  }

  public O getObject() {
    return object;
  }

  public int getRowCount() {
    return dataProvider.getRowCount();
  }

  public Date getDate() {
    return dataProvider.getDate();
  }

  public List<CheckboxSelectionListener<T>> getListeners() {
    return this.listeners;
  }

  protected void addColumn(Column<T, ?> column, SafeHtml headerHTML, boolean nowrap, boolean alignRight) {
    SafeHtmlHeader header = new SafeHtmlHeader(headerHTML);

    display.addColumn(column, header);

    if (nowrap && alignRight) {
      header.setHeaderStyleNames("nowrap text-align-right");
      column.setCellStyleNames("nowrap text-align-right");
    } else if (nowrap) {
      header.setHeaderStyleNames("cellTableFadeOut");
      column.setCellStyleNames("cellTableFadeOut");
    }
  }

  protected void addColumn(Column<T, ?> column, SafeHtml headerHTML, boolean nowrap, boolean alignRight,
    double fixedSize) {
    addColumn(column, headerHTML, nowrap, alignRight);
    display.setColumnWidth(column, fixedSize, Unit.EM);
  }

  protected void addColumn(Column<T, ?> column, String headerText, boolean nowrap, boolean alignRight) {
    addColumn(column, SafeHtmlUtils.fromString(headerText), nowrap, alignRight);
  }

  protected void addColumn(Column<T, ?> column, String headerText, boolean nowrap, boolean alignRight,
    double fixedSize) {
    addColumn(column, SafeHtmlUtils.fromString(headerText), nowrap, alignRight, fixedSize);
  }

  public IndexResult<T> getResult() {
    return result;
  }

  public void setResult(IndexResult<T> result) {
    this.result = result;
  }

  public String translate(String fieldName, String fieldValue) {
    String translation = null;
    if (this.result != null && this.result.getFacetResults() != null) {
      for (FacetFieldResult ffr : this.result.getFacetResults()) {
        if (ffr.getField().equalsIgnoreCase(fieldName)) {
          if (ffr.getValues() != null) {
            for (FacetValue fv : ffr.getValues()) {
              if (fv.getValue().equalsIgnoreCase(fieldValue)) {
                translation = fv.getLabel();
                break;
              }
            }
          }
        }
        if (translation != null) {
          break;
        }
      }
    }
    if (translation == null) {
      translation = fieldValue;
    }
    return translation;
  }

  public boolean hasElementsSelected() {
    SelectedItems<T> selected = getSelected();

    if (selected instanceof SelectedItemsList) {
      SelectedItemsList<T> list = (SelectedItemsList<T>) selected;
      return !list.getIds().isEmpty();
    }

    return true;
  }

  public Actionable<T> getActionable() {
    return actionable;
  }

  public void setActionable(Actionable<T> actionable) {
    this.actionable = actionable;

    actionsButton.setVisible(actionable != null);
  }

  protected void showActions() {
    if (actionable != null) {
      if (actionsPopup.isShowing()) {
        actionsPopup.hide();
      } else {
        AsyncCallback<Actionable.ActionImpact> callback = new AsyncCallback<Actionable.ActionImpact>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(Actionable.ActionImpact impact) {
            if (!Actionable.ActionImpact.NONE.equals(impact)) {
              update();
            }
            actionsPopup.hide();
          }
        };

        if (isAllSelected()) {
          actionsPopup.setWidget(actionable.createActionsLayout(getSelected(), callback));
        } else if (selected.size() == 1) {
          actionsPopup.setWidget(actionable.createActionsLayout(selected.iterator().next(), callback));
        } else if (selected.size() > 1) {
          // TODO create action layout based on selected set
          actionsPopup.setWidget(actionable.createActionsLayout(getSelected(), callback));
        } else {
          Label emptyHelpText = new Label(messages.tableActionEmptyHelp());
          emptyHelpText.addStyleName("actions-empty-help");
          actionsPopup.setWidget(emptyHelpText);
        }

        actionsPopup.setPopupPositionAndShow(new PositionCallback() {

          @Override
          public void setPosition(int offsetWidth, int offsetHeight) {
            int left = actionsButton.getAbsoluteLeft() + actionsButton.getOffsetWidth() - offsetWidth;
            int top = actionsButton.getAbsoluteTop() - offsetHeight - 4;

            actionsPopup.setPopupPosition(left, top);
          }
        });

      }

    }
  }

}
