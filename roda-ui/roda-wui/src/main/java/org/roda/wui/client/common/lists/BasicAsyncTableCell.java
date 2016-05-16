package org.roda.wui.client.common.lists;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

public abstract class BasicAsyncTableCell<T extends IsIndexed> extends AsyncTableCell<T, Void> {
  public BasicAsyncTableCell() {
    super();
  }

  public BasicAsyncTableCell(Filter filter, Facets facets, String summary, boolean selectable, int initialPageSize,
    int pageSizeIncrement) {
    super(filter, false, facets, summary, selectable, initialPageSize, pageSizeIncrement, null);
  }

  public BasicAsyncTableCell(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable,
    int initialPageSize, int pageSizeIncrement) {
    super(filter, justActive, facets, summary, selectable, initialPageSize, pageSizeIncrement, null);
  }

  public BasicAsyncTableCell(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable) {
    super(filter, justActive, facets, summary, selectable, null);
  }

  public BasicAsyncTableCell(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, false, facets, summary, selectable, null);
  }
}