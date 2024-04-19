/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortList;
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
  private List<String> fieldsToReturn = new ArrayList<>();

  public MyAsyncDataProvider(CellTable<T> display, List<String> fieldsToReturn,
    IndexResultDataProvider<T> dataProvider) {
    super();
    this.display = display;
    this.dataProvider = dataProvider;
    this.fieldsToReturn = fieldsToReturn;
  }

  @Override
  protected void onRangeChanged(final HasData<T> display) {
    fetch(display, fieldsToReturn);
  }

  private CompletableFuture<Void> fetch(final HasData<T> display, final List<String> fieldsToReturn) {
    // Get the new range.
    final Range range = display.getVisibleRange();

    // Get sorting
    ColumnSortList columnSortList = this.display.getColumnSortList();

    // Query the data asynchronously.
    final int start = range.getStart();
    int length = range.getLength();
    sublist = new Sublist(start, length);
    sorter = dataProvider.getSorter(columnSortList);
    CompletableFuture<Void> ret = new CompletableFuture<>();
    dataProvider.getData(sublist, sorter, fieldsToReturn).whenComplete((result, throwable) -> {
      if (result != null) {
        rowCount = (int) result.getTotalCount();
        date = result.getDate();
        updateRowData((int) result.getOffset(), result.getResults());
        updateRowCount(rowCount, true);
        // ValueChangeEvent.fire(AsyncTableCell.this, result);
        fireChangeEvent(result);
      } else if (throwable != null) {
        // to do send error
        ret.completeExceptionally(throwable);
      } else {
        // search not yet ready, deliver empty result
        ret.complete(null);
      }
    });
    return ret;
  }

  protected abstract void fireChangeEvent(IndexResult<T> result);

  public CompletableFuture<Void> update(List<String> fieldsToReturn) {
    return fetch(display, fieldsToReturn);
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
