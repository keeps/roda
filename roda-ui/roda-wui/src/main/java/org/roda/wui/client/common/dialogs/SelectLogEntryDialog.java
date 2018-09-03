/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;

public class SelectLogEntryDialog extends DefaultSelectDialog<LogEntry> {
  public SelectLogEntryDialog(String title, Filter filter) {
    super(title,
      new ListBuilder<>(() -> new LogEntryList(),
        new AsyncTableCellOptions<>(LogEntry.class, "SelectLogEntryDialog_logEntries").withFilter(filter)
          .withSummary(title)));
  }
}
