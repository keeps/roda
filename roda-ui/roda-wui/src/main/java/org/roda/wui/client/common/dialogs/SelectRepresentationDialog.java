/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class SelectRepresentationDialog extends DefaultSelectDialog<IndexedRepresentation> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public SelectRepresentationDialog(String title, Filter filter, boolean justActive) {
    super(title,
      new ListBuilder<>(RepresentationList::new,
        new AsyncTableCellOptions<>(IndexedRepresentation.class, "SelectRepresentationDialog_representations")
          .withFilter(filter).withJustActive(justActive).withSummary(messages.selectRepresentationSearchResults())));
  }
}
