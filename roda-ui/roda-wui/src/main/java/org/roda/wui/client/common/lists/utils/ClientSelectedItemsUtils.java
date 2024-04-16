/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ClientSelectedItemsUtils {

  private ClientSelectedItemsUtils() {
    // do nothing
  }

  public static boolean isEmpty(SelectedItems<?> selected) {
    return selected instanceof SelectedItemsList && ((SelectedItemsList<?>) selected).getIds().isEmpty();
  }

  public static <T extends IsIndexed> void size(Class<T> classToReturn, SelectedItems<T> selected,
    final AsyncCallback<Long> callback) {

    if (selected instanceof SelectedItemsList) {
      Long size = (long) ((SelectedItemsList<T>) selected).getIds().size();
      callback.onSuccess(size);
    } else if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<T> selectedItemsFilter = (SelectedItemsFilter<T>) selected;
      Filter filter = selectedItemsFilter.getFilter();
      boolean justActive = selectedItemsFilter.justActive();
      BrowserService.Util.getInstance().count(classToReturn.getName(), filter, justActive, new AsyncCallback<Long>() {

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
