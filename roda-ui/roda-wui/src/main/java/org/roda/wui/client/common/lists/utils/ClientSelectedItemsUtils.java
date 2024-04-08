/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.roda.wui.client.services.Services;

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
      Services services = new Services("Count selected items", "post");
      CountRequest countRequest = new CountRequest(classToReturn.getName(), filter, justActive);
      services.index(s -> s.count(countRequest))
        .whenComplete((size, error) -> {
          if (size != null) {
            callback.onSuccess(size);
          } else if (error != null) {
            callback.onFailure(error);
          }
        });
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported type: " + selected.getClass().getName()));
    }

  }
}
