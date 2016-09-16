/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.common.lists.JobList;

public class SelectJobDialog extends DefaultSelectDialog<Job, Void> {

  private static final Filter DEFAULT_FILTER_JOB = new Filter(
    new BasicSearchFilterParameter(RodaConstants.JOB_SEARCH, "*"));

  public SelectJobDialog(String title) {
    this(title, DEFAULT_FILTER_JOB);
  }

  public SelectJobDialog(String title, Filter filter) {
    this(title, filter, false);
  }

  public SelectJobDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.JOB_SEARCH, new JobList(filter, null, title, selectable), false);
  }

}
