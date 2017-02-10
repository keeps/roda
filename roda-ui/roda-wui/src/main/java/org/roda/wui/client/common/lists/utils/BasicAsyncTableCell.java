/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;

public abstract class BasicAsyncTableCell<T extends IsIndexed> extends AsyncTableCell<T, Void> {
  public BasicAsyncTableCell(Class<T> classToReturn, List<String> fieldsToReturn) {
    super(classToReturn, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, Filter filter, Facets facets, String summary, boolean selectable,
    int initialPageSize, int pageSizeIncrement, List<String> fieldsToReturn) {
    super(classToReturn, filter, false, facets, summary, selectable, initialPageSize, pageSizeIncrement, null,
      fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, Filter filter, boolean justActive, Facets facets, String summary,
    boolean selectable, int initialPageSize, int pageSizeIncrement, List<String> fieldsToReturn) {
    super(classToReturn, filter, justActive, facets, summary, selectable, initialPageSize, pageSizeIncrement, null,
      fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, Filter filter, boolean justActive, Facets facets, String summary,
    boolean selectable, List<String> fieldsToReturn) {
    super(classToReturn, filter, justActive, facets, summary, selectable, null, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, Filter filter, Facets facets, String summary, boolean selectable,
    List<String> fieldsToReturn) {
    super(classToReturn, filter, false, facets, summary, selectable, null, fieldsToReturn);
  }

}