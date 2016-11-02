/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import java.io.Serializable;
import java.util.Date;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public abstract class MyAsyncDataProvider<T extends Serializable> extends AsyncDataProvider<T> {

  private final CellTable<T> display;
  private final IndexResultDataProvider<T> dataProvider;
  private int rowCount;
  private Date date;
  private Sublist sublist;
  private Sorter sorter;

  public MyAsyncDataProvider(CellTable<T> display, IndexResultDataProvider<T> dataProvider) {
    super();
    this.display = display;
    this.dataProvider = dataProvider;
  }

  @Override
  protected void onRangeChanged(final HasData<T> display) {
    fetch(display, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
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
    sublist = new Sublist(start, length);
    sorter = dataProvider.getSorter(columnSortList);
    dataProvider.getData(sublist, sorter, new AsyncCallback<IndexResult<T>>() {

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(IndexResult<T> result) {
        if (result != null) {
          rowCount = (int) result.getTotalCount();
          date = result.getDate();
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
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Void result) {
        // do nothing
      }
    });
  }

  public int getRowCount() {
    return rowCount;
  }

  public Date getDate() {
    return date;
  }

  public Sublist getSublist() {
    return sublist;
  }

  public Sorter getSorter() {
    return sorter;
  }

}
