package org.roda.wui.client.common.lists;

import java.io.Serializable;

import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public abstract class MyAsyncDataProvider<T extends Serializable> extends AsyncDataProvider<T> {

  private final CellTable<T> display;
  private final IndexResultDataProvider<T> dataProvider;

  public MyAsyncDataProvider(CellTable<T> display, IndexResultDataProvider<T> dataProvider) {
    super();
    this.display = display;
    this.dataProvider = dataProvider;
  }

  @Override
  protected void onRangeChanged(HasData<T> display) {
    fetch(display, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        Toast.showError("Error getting data from server: [" + caught.getClass().getName() + "] " + caught.getMessage());
      }

      @Override
      public void onSuccess(Void result) {
        // do nothing
      }
    });
  }

  private void fetch(final HasData<T> display, final AsyncCallback<Void> callback) {
    // Get the new range.
    final Range range = display.getVisibleRange();

    // Get sorting
    ColumnSortList columnSortList = this.display.getColumnSortList();

    // Query the data asynchronously.
    final int start = range.getStart();
    int length = range.getLength();
    dataProvider.getData(new Sublist(start, length), columnSortList, new AsyncCallback<IndexResult<T>>() {

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(IndexResult<T> result) {
        if (result != null) {
          int rowCount = (int) result.getTotalCount();
          updateRowData((int) result.getOffset(), result.getResults());
          updateRowCount(rowCount, true);
          // ValueChangeEvent.fire(AsyncTableCell.this, result);
          fireChangeEvent(result);
        } else {
          // search not yet ready, deliver empty result
        }
        callback.onSuccess(null);
      }
    });
  }

  protected abstract void fireChangeEvent(IndexResult<T> result);

  public void update(final AsyncCallback<Void> callback) {
    fetch(display, callback);
  }

  public void update() {
    update(new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        Toast.showError("Error getting data from server: [" + caught.getClass().getName() + "] " + caught.getMessage());
      }

      @Override
      public void onSuccess(Void result) {
        // do nothing
      }
    });
  }

}
