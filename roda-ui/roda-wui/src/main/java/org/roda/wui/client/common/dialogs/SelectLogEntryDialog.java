/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.search.SearchFilters;

public class SelectLogEntryDialog extends DefaultSelectDialog<LogEntry, Void> {

  private static final Filter DEFAULT_FILTER_LOG = SearchFilters.defaultFilter(LogEntry.class.getName());

  public SelectLogEntryDialog(String title) {
    this(title, DEFAULT_FILTER_LOG);
  }

  public SelectLogEntryDialog(String title, Filter filter) {
    this(title, filter, false);
  }

  public SelectLogEntryDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.LOG_SEARCH, new LogEntryList(filter, null, title, selectable), false);
  }

}
