/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.actions.RiskActions;
import org.roda.wui.client.common.actions.RiskIncidenceActions;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.RiskList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.user.client.ui.SimplePanel;

public class RiskSearch extends SimplePanel {
  private final SearchWrapper searchWrapper;

  public RiskSearch() {
    ListBuilder<IndexedRisk> riskListBuilder = new ListBuilder<>(() -> new RiskList(),
      new AsyncTableCellOptions<>(IndexedRisk.class, "RiskRegister_risks").withActionable(RiskActions.get())
        .bindOpener());

    ListBuilder<RiskIncidence> riskIncidenceListBuilder = new ListBuilder<>(() -> new RiskIncidenceList(),
      new AsyncTableCellOptions<>(RiskIncidence.class, "RiskRegister_riskIncidences")
        .withActionable(RiskIncidenceActions.getForMultipleEdit()).bindOpener());

    searchWrapper = new SearchWrapper(true)
      .createListAndSearchPanel(riskListBuilder).createListAndSearchPanel(riskIncidenceListBuilder);

    setWidget(searchWrapper);
  }

  public void refresh() {
    searchWrapper.refreshAllLists();
  }
}
