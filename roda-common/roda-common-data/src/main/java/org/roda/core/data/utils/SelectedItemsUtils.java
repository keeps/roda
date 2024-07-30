package org.roda.core.data.utils;

import org.roda.core.data.v2.generics.select.SelectedItemsAllRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsFilterRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsNoneRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SelectedItemsUtils {

  private SelectedItemsUtils() {
  }

  public static SelectedItemsRequest convertToRESTRequest(SelectedItems<?> items) {
    if (items instanceof SelectedItemsList<?>) {
      SelectedItemsListRequest request = new SelectedItemsListRequest();
      request.setIds(((SelectedItemsList<?>) items).getIds());
      return request;
    } else if (items instanceof SelectedItemsFilter<?>) {
      SelectedItemsFilterRequest request = new SelectedItemsFilterRequest();
      request.setFilter(((SelectedItemsFilter<?>) items).getFilter());
      request.setJustActive(((SelectedItemsFilter<?>) items).justActive());
      return request;
    } else if (items instanceof SelectedItemsNone<?>) {
      return new SelectedItemsNoneRequest();
    } else if (items instanceof SelectedItemsAll<?>) {
      return new SelectedItemsAllRequest();
    }

    return new SelectedItemsListRequest();
  }
}
