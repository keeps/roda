/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SelectedItemsUtils {
  public static <T extends IsIndexed> boolean isEmpty(SelectedItems<T> selected) {
    return selected instanceof SelectedItemsList && ((SelectedItemsList<?>) selected).getIds().isEmpty();
  }

  public static <T extends IsIndexed> void size(Class<T> classToReturn, SelectedItems<T> selected,
    final AsyncCallback<Long> callback) {

    if (selected instanceof SelectedItemsList) {
      Long size = (long) ((SelectedItemsList<?>) selected).getIds().size();
      callback.onSuccess(size);
    } else if (selected instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
      BrowserService.Util.getInstance().count(classToReturn.getName(), filter, new AsyncCallback<Long>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Long result) {
          callback.onSuccess(result);
        }
      });
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported type: " + selected.getClass().getName()));
    }

  }
}
