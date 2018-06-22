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
import org.roda.wui.common.client.tools.ConfigurationManager;

public abstract class BasicAsyncTableCell<T extends IsIndexed> extends AsyncTableCell<T, Void> {
  public BasicAsyncTableCell(Class<T> classToReturn, String listId, List<String> fieldsToReturn) {
    super(classToReturn, listId, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, String listId, Filter filter, Facets facets, String summary,
    boolean selectable, int initialPageSize, int pageSizeIncrement, List<String> fieldsToReturn) {
    super(classToReturn, listId, filter, false, facets, summary, selectable, initialPageSize, pageSizeIncrement, null,
      fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, String listId, Filter filter, boolean justActive, Facets facets,
    String summary, boolean selectable, int initialPageSize, int pageSizeIncrement, List<String> fieldsToReturn) {
    super(classToReturn, listId, filter, justActive, facets, summary, selectable, initialPageSize, pageSizeIncrement,
      null, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, String listId, Filter filter, boolean justActive, Facets facets,
    String summary, boolean selectable, List<String> fieldsToReturn) {
    super(classToReturn, listId, filter, justActive, facets, summary, selectable, null, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, String listId, Filter filter, Facets facets, String summary,
    boolean selectable, List<String> fieldsToReturn) {
    super(classToReturn, listId, filter, false, facets, summary, selectable, null, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, String listId, Filter filter, String summary, boolean selectable,
    int initialPageSize, int pageSizeIncrement, List<String> fieldsToReturn) {
    super(classToReturn, listId, filter, false, ConfigurationManager.FacetFactory.getFacets(listId), summary,
      selectable, initialPageSize, pageSizeIncrement, null, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, String listId, Filter filter, boolean justActive, String summary,
    boolean selectable, int initialPageSize, int pageSizeIncrement, List<String> fieldsToReturn) {
    super(classToReturn, listId, filter, justActive, ConfigurationManager.FacetFactory.getFacets(listId), summary,
      selectable, initialPageSize, pageSizeIncrement, null, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, String listId, Filter filter, boolean justActive, String summary,
    boolean selectable, List<String> fieldsToReturn) {
    super(classToReturn, listId, filter, justActive, ConfigurationManager.FacetFactory.getFacets(listId), summary,
      selectable, null, fieldsToReturn);
  }

  public BasicAsyncTableCell(Class<T> classToReturn, String listId, Filter filter, String summary, boolean selectable,
    List<String> fieldsToReturn) {
    super(classToReturn, listId, filter, false, ConfigurationManager.FacetFactory.getFacets(listId), summary,
      selectable, null, fieldsToReturn);
  }
}
