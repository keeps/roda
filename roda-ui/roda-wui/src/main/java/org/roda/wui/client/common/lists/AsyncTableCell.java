/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.MyCellTableResources;
import org.roda.wui.common.client.widgets.wcag.AccessibleCellTable;
import org.roda.wui.common.client.widgets.wcag.AccessibleSimplePager;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.PageSizePager;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;

public abstract class AsyncTableCell<T extends Serializable> extends FlowPanel
  implements HasValueChangeHandlers<IndexResult<T>> {

  private final MyAsyncDataProvider<T> dataProvider;
  private final SingleSelectionModel<T> selectionModel;
  private final AsyncHandler columnSortHandler;

  private final AccessibleSimplePager resultsPager;
  private final PageSizePager pageSizePager;
  private final CellTable<T> display;

  private Filter filter;

  private Facets facets;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  public AsyncTableCell() {
    this(null, null, null);
  }

  public AsyncTableCell(Filter filter, Facets facets, String summary) {
    super();

    if (summary == null) {
      summary = "summary" + Random.nextInt(1000);
    }

    this.filter = filter;
    this.facets = facets;

    display = new AccessibleCellTable<T>(getInitialPageSize(),
      (MyCellTableResources) GWT.create(MyCellTableResources.class), getKeyProvider(), summary);
    display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
    display.setLoadingIndicator(new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>")));

    configureDisplay(display);

    this.dataProvider = new MyAsyncDataProvider<T>(display, new IndexResultDataProvider<T>() {

      @Override
      public void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<T>> callback) {
        AsyncTableCell.this.getData(sublist, columnSortList, callback);
      }
    }) {

      @Override
      protected void fireChangeEvent(IndexResult<T> result) {
        ValueChangeEvent.fire(AsyncTableCell.this, result);
      }
    };

    dataProvider.addDataDisplay(display);

    resultsPager = new AccessibleSimplePager(AccessibleSimplePager.TextLocation.RIGHT, false, true);
    resultsPager.setDisplay(display);

    pageSizePager = new PageSizePager(getInitialPageSize());
    pageSizePager.setDisplay(display);

    add(resultsPager);
    add(display);
    add(pageSizePager);

    selectionModel = new SingleSelectionModel<>(getKeyProvider());

    Handler<T> selectionEventManager = getSelectionEventManager();
    if (selectionEventManager != null)

    {
      display.setSelectionModel(selectionModel, selectionEventManager);
    } else

    {
      display.setSelectionModel(selectionModel);
    }

    columnSortHandler = new AsyncHandler(display);
    display.addColumnSortHandler(columnSortHandler);

    addStyleName("my-asyncdatagrid");
    resultsPager.addStyleName("my-asyncdatagrid-pager-results");
    pageSizePager.addStyleName("my-asyncdatagrid-pager-pagesize");
    display.addStyleName("my-asyncdatagrid-display");

  }

  protected abstract void configureDisplay(CellTable<T> display);

  protected abstract int getInitialPageSize();

  protected abstract ProvidesKey<T> getKeyProvider();

  protected abstract void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<T>> callback);

  protected CellPreviewEvent.Handler<T> getSelectionEventManager() {
    // none by default
    return null;
  }

  public SingleSelectionModel<T> getSelectionModel() {
    return selectionModel;
  }

  public void refresh() {
    display.setVisibleRangeAndClearData(new Range(0, getInitialPageSize()), true);
    getSelectionModel().clear();
  }

  public void update() {
    dataProvider.update();
  }

  private Timer autoUpdateTimer = null;
  private int autoUpdateTimerMillis = 0;

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

  public void setFilter(Filter filter) {
    this.filter = filter;
    refresh();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<IndexResult<T>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public Facets getFacets() {
    return facets;
  }

  public void setFacets(Facets facets) {
    this.facets = facets;
    refresh();
  }

  public List<T> getVisibleItems() {
    return display.getVisibleItems();
  }

  protected Sorter createSorter(ColumnSortList columnSortList, Map<Column<T, ?>, String> columnSortingKeyMap) {
    Sorter sorter = new Sorter();
    for (int i = 0; i < columnSortList.size(); i++) {
      ColumnSortInfo columnSortInfo = columnSortList.get(i);

      String sortParameterKey = columnSortingKeyMap.get(columnSortInfo.getColumn());
      if (sortParameterKey != null) {
        sorter.add(new SortParameter(sortParameterKey, !columnSortInfo.isAscending()));
      } else {
        logger.warn("Selecting a sorter that is not mapped");
      }
    }
    return sorter;
  }

  public void nextItemSelection() {
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      int selectedIndex = getVisibleItems().indexOf(selectedItem);

      if (selectedIndex == -1) {
        getSelectionModel().setSelected(getVisibleItems().get(0), true);
      } else {
        getSelectionModel().setSelected(getVisibleItems().get(selectedIndex + 1), true);
      }
    } else {
      getSelectionModel().setSelected(getVisibleItems().get(0), true);
    }
  }

  public void previousItemSelection() {
    if (getSelectionModel().getSelectedObject() != null) {
      T selectedItem = getSelectionModel().getSelectedObject();
      int selectedIndex = getVisibleItems().indexOf(selectedItem);

      if (selectedIndex == -1) {
        getSelectionModel().setSelected(getVisibleItems().get(getVisibleItems().size() - 1), true);
      } else {
        getSelectionModel().setSelected(getVisibleItems().get(selectedIndex - 1), true);
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

  public void prevousPage() {
    resultsPager.previousPage();
  }
}
