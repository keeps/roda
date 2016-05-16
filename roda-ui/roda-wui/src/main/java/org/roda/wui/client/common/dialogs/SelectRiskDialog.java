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
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.common.lists.RiskList;

public class SelectRiskDialog extends DefaultSelectDialog<IndexedRisk, Void> {

  private static final Filter DEFAULT_FILTER_RISK = new Filter(
    new BasicSearchFilterParameter(RodaConstants.RISK_SEARCH, "*"));

  public SelectRiskDialog(String title) {
    this(title, DEFAULT_FILTER_RISK);
  }

  public SelectRiskDialog(String title, Filter filter) {
    super(title, filter, RodaConstants.RISK_SEARCH, new RiskList(filter, null, title, false));
  }

}
