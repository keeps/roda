/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.lists.utils.ListBuilder;

/**
 * Package-private class to store search options
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
class SearchOptionsBundle<T extends IsIndexed> {
  private final ListBuilder<T> tableCell;
  private final String label;
  private final Filter defaultFilter;
  private final String allFilter;
  private final boolean incremental;
  private final boolean showAdvancedSearch;
  private final boolean hidePrefilters;

  SearchOptionsBundle(String label, ListBuilder<T> tableCell, Filter defaultFilter, String allFilter,
    boolean incremental, boolean showAdvancedSearch, boolean hidePrefilters) {
    this.tableCell = tableCell;
    this.label = label;
    this.defaultFilter = defaultFilter;
    this.allFilter = allFilter;
    this.incremental = incremental;
    this.showAdvancedSearch = showAdvancedSearch;
    this.hidePrefilters = hidePrefilters;
  }

  SearchOptionsBundle(ListBuilder<T> tableCell, Filter defaultFilter, String allFilter, boolean incremental,
    boolean showAdvancedSearch, boolean hidePrefilters) {
    this.tableCell = tableCell;
    this.defaultFilter = defaultFilter;
    this.allFilter = allFilter;
    this.incremental = incremental;
    this.showAdvancedSearch = showAdvancedSearch;
    this.hidePrefilters = hidePrefilters;
    this.label = null;
  }

  public ListBuilder<T> getTableCell() {
    return tableCell;
  }

  public String getLabel() {
    return label;
  }

  public Filter getDefaultFilter() {
    return defaultFilter;
  }

  public String getAllFilter() {
    return allFilter;
  }

  public boolean getIncremental() {
    return incremental;
  }

  public boolean getShowAdvancedSearch() {
    return showAdvancedSearch;
  }

  public boolean getHidePrefilters() {
    return hidePrefilters;
  }
}
