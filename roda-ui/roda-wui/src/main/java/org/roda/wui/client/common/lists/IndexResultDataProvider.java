package org.roda.wui.client.common.lists;

import java.io.Serializable;

import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IndexResultDataProvider<T extends Serializable> {

  void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<T>> callback);

}
