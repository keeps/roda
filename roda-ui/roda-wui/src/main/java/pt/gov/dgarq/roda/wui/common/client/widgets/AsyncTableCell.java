package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.PageSizePager;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;

import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;

public abstract class AsyncTableCell<T extends Serializable> extends FlowPanel
  implements HasValueChangeHandlers<IndexResult<T>> {

  private final AsyncDataProvider<T> dataProvider;
  private final SingleSelectionModel<T> selectionModel;
  private final AsyncHandler columnSortHandler;

  private final SimplePager resultsPager;
  private final PageSizePager pageSizePager;
  private final CellTable<T> display;

  private Filter filter;
  private Facets facets;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  public AsyncTableCell() {
    this(null, null);
  }

  public AsyncTableCell(Filter filter, Facets facets) {
    super();

    this.filter = filter;
    this.facets = facets;

    this.dataProvider = new AsyncDataProvider<T>() {

      @Override
      protected void onRangeChanged(HasData<T> display) {
        // Get the new range.
        final Range range = display.getVisibleRange();

        // Get sorting
        ColumnSortList columnSortList = AsyncTableCell.this.display.getColumnSortList();

        // Query the data asynchronously.
        final int start = range.getStart();
        int length = range.getLength();
        getData(start, length, columnSortList, new AsyncCallback<IndexResult<T>>() {

          @Override
          public void onFailure(Throwable caught) {
            logger.error("Error getting data", caught);
          }

          @Override
          public void onSuccess(IndexResult<T> result) {
            if (result != null) {
              int rowCount = (int) result.getTotalCount();
              updateRowData((int) result.getOffset(), result.getResults());
              updateRowCount(rowCount, true);
              ValueChangeEvent.fire(AsyncTableCell.this, result);
            } else {
              // search not yet ready, deliver empty result
            }
          }
        });
      }
    };

    display = new CellTable<T>(getInitialPageSize(), (MyCellTableResources) GWT.create(MyCellTableResources.class),
      getKeyProvider());

    dataProvider.addDataDisplay(display);

    resultsPager = new SimplePager(TextLocation.RIGHT, false, true);
    resultsPager.setDisplay(display);

    pageSizePager = new PageSizePager(getInitialPageSize());
    pageSizePager.setDisplay(display);

    add(resultsPager);
    add(display);
    add(pageSizePager);

    selectionModel = new SingleSelectionModel<>(getKeyProvider());
    display.setSelectionModel(selectionModel);

    columnSortHandler = new AsyncHandler(display);
    display.addColumnSortHandler(columnSortHandler);

    addStyleName("my-asyncdatagrid");
    resultsPager.addStyleName("my-asyncdatagrid-pager-results");
    pageSizePager.addStyleName("my-asyncdatagrid-pager-pagesize");
    display.addStyleName("my-asyncdatagrid-display");

  }

  protected abstract int getInitialPageSize();

  protected abstract ProvidesKey<T> getKeyProvider();

  protected abstract void getData(int start, int length, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<T>> callback);

  protected CellTable<T> getDisplay() {
    return display;
  }

  public SingleSelectionModel<T> getSelectionModel() {
    return selectionModel;
  }

  public void refresh() {
    getSelectionModel().clear();
    getDisplay().setVisibleRangeAndClearData(new Range(0, getInitialPageSize()), true);
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

}
