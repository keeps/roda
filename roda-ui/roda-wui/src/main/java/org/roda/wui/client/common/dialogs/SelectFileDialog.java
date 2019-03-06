/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class SelectFileDialog extends DefaultSelectDialog<IndexedFile> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public SelectFileDialog(String title, Filter filter, boolean justActive) {
    super(title,
      new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedFile.class, "SelectFileDialog_simpleFiles").withFilter(filter)
          .withJustActive(justActive).withSummary(messages.selectFileSearchResults())));
  }
}
