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
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.common.lists.RiskList;
import org.roda.wui.client.common.search.SearchFilters;

public class SelectRiskDialog extends DefaultSelectDialog<IndexedRisk, Void> {

  private static final Filter DEFAULT_FILTER_RISK = SearchFilters.defaultFilter(IndexedRisk.class.getName());

  public SelectRiskDialog(String title) {
    this(title, DEFAULT_FILTER_RISK);
  }

  public SelectRiskDialog(String title, Filter filter) {
    this(title, filter, false);
  }

  public SelectRiskDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.RISK_SEARCH, new RiskList("SelectRiskDialog_risks", filter, title, selectable),
      false);
  }

}
