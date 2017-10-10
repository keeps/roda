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
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.search.SearchFilters;

public class SelectRepresentationInformationDialog extends DefaultSelectDialog<RepresentationInformation, Void> {

  private static final Filter DEFAULT_FILTER_FORMAT = SearchFilters
    .defaultFilter(RepresentationInformation.class.getName());

  public SelectRepresentationInformationDialog(String title) {
    this(title, DEFAULT_FILTER_FORMAT);
  }

  public SelectRepresentationInformationDialog(String title, Filter filter) {
    this(title, filter, false);
  }

  public SelectRepresentationInformationDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.REPRESENTATION_INFORMATION_SEARCH,
      new RepresentationInformationList(filter, null, title, selectable), false);
  }
}
