/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;

public class SelectRepresentationInformationDialog extends DefaultSelectDialog<RepresentationInformation> {
  public SelectRepresentationInformationDialog(String title, Filter filter) {
    this(title, filter, true);
  }

  public SelectRepresentationInformationDialog(String title, Filter filter, boolean exportCsvVisible) {
    super(title,
      new ListBuilder<>(RepresentationInformationList::new,
        new AsyncTableCellOptions<>(RepresentationInformation.class, "SelectRepresentationInformationDialog_RI")
          .withFilter(filter).withSummary(title).withCsvDownloadButtonVisibility(exportCsvVisible)));
  }
}
