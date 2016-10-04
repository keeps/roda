/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.lists.FormatList;
import org.roda.wui.client.common.search.SearchFilters;

public class SelectFormatDialog extends DefaultSelectDialog<Format, Void> {

  private static final Filter DEFAULT_FILTER_FORMAT = SearchFilters.defaultFilter(Format.class.getName());

  public SelectFormatDialog(String title) {
    this(title, DEFAULT_FILTER_FORMAT);
  }

  public SelectFormatDialog(String title, Filter filter) {
    this(title, filter, false);
  }

  public SelectFormatDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.FORMAT_SEARCH, new FormatList(filter, null, title, selectable), false);
  }
}
