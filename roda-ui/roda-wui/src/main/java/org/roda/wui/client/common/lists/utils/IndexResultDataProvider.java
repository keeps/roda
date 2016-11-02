/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import java.io.Serializable;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IndexResultDataProvider<T extends Serializable> {

  void getData(Sublist sublist, Sorter sorter, AsyncCallback<IndexResult<T>> callback);

  Sorter getSorter(ColumnSortList columnSortList);

}
