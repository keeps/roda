package org.roda.wui.client.common.lists;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SelectedItemsUtils {
  public static <T extends IsIndexed> boolean isEmpty(SelectedItems<T> selected) {
    return selected instanceof SelectedItemsSet && ((SelectedItemsSet<?>) selected).getSet().isEmpty();
  }

  public static <T extends IsIndexed> void size(Class<T> classToReturn, SelectedItems<T> selected,
    final AsyncCallback<Long> callback) {

    if (selected instanceof SelectedItemsSet) {
      Long size = (long) ((SelectedItemsSet<?>) selected).getSet().size();
      callback.onSuccess(size);
    } else if (selected instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
      BrowserService.Util.getInstance().count(TransferredResource.class.getName(), filter, new AsyncCallback<Long>() {

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
