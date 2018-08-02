/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class SelectTransferResourceDialog extends DefaultSelectDialog<TransferredResource> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public SelectTransferResourceDialog(String title, Filter filter) {
    super(title,
      new ListBuilder<>(() -> new TransferredResourceList(true),
        new AsyncTableCell.Options<>(TransferredResource.class, "SelectTransferResourceDialog_transferredResources")
          .withFilter(filter).withSummary(messages.selectTransferredResourcesSearchResults())));
  }
}
