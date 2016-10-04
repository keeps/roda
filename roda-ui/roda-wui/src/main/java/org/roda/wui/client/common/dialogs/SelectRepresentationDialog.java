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
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.search.SearchFilters;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class SelectRepresentationDialog extends DefaultSelectDialog<IndexedRepresentation, Void> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Filter DEFAULT_FILTER_REPRESENTATION = SearchFilters
    .defaultFilter(IndexedRepresentation.class.getName());

  private static final Boolean DEFAULT_JUST_ACTIVE = Boolean.TRUE;
  private static final Facets DEFAULT_FACETS = null;
  private static final Boolean SELECTABLE = Boolean.FALSE;

  public SelectRepresentationDialog(String title, boolean hidePreFilters) {
    this(title, DEFAULT_FILTER_REPRESENTATION, DEFAULT_JUST_ACTIVE, hidePreFilters);
  }

  public SelectRepresentationDialog(String title, Filter filter, boolean justActive, boolean hidePreFilters) {
    this(title, filter, justActive, hidePreFilters, SELECTABLE);
  }

  public SelectRepresentationDialog(String title, Filter filter, boolean justActive, boolean hidePreFilters,
    boolean selectable) {
    super(title, filter, RodaConstants.REPRESENTATION_SEARCH, new RepresentationList(filter, justActive, DEFAULT_FACETS,
      messages.selectRepresentationSearchResults(), selectable), hidePreFilters);
  }
}
