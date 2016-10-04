/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.search.SearchFilters;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class SelectTransferResourceDialog extends DefaultSelectDialog<TransferredResource, Void> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(TransferredResource.class.getName());

  private static final Facets DEFAULT_FACETS = null;
  private static final Boolean SELECTABLE = Boolean.FALSE;

  public SelectTransferResourceDialog(String title) {
    this(title, DEFAULT_FILTER);
  }

  public SelectTransferResourceDialog(String title, Filter filter) {
    this(title, filter, SELECTABLE);
  }

  public SelectTransferResourceDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.TRANSFERRED_RESOURCE_SEARCH, new TransferredResourceList(filter, DEFAULT_FACETS,
      messages.selectTransferredResourcesSearchResults(), selectable), false);
  }
}
