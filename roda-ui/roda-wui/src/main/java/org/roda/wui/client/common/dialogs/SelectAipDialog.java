/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class SelectAipDialog extends DefaultSelectDialog<IndexedAIP> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final String listId = "SelectAipDialog_AIPs";

  public SelectAipDialog(String title) {
    super(title, new ListBuilder<>(AIPList::new, new AsyncTableCell.Options<>(IndexedAIP.class, listId)
      .withSummary(messages.selectAipSearchResults()).withJustActive(true)));
  }

  public SelectAipDialog(String title, Filter filter, boolean justActive) {
    this(title, filter, justActive, true);
  }

  public SelectAipDialog(String title, Filter filter, boolean justActive, boolean exportCsvVisible) {
    super(title, new ListBuilder<>(AIPList::new, new AsyncTableCell.Options<>(IndexedAIP.class, listId)
      .withSummary(messages.selectAipSearchResults()).withJustActive(justActive).withFilter(filter)));
  }
}
