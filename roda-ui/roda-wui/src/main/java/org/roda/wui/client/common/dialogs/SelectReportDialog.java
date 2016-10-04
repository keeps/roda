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
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.search.SearchFilters;

public class SelectReportDialog extends DefaultSelectDialog<Report, Void> {

  private static final Filter DEFAULT_FILTER_REPORT = SearchFilters.defaultFilter(Report.class.getName());

  public SelectReportDialog(String title) {
    this(title, DEFAULT_FILTER_REPORT);
  }

  public SelectReportDialog(String title, Filter filter) {
    this(title, filter, false);
  }

  public SelectReportDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.JOB_REPORT_SEARCH, new SimpleJobReportList(filter, null, title, selectable),
      false);
  }

}
