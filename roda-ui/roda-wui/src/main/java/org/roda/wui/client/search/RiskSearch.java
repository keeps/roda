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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class RiskSearch extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, RiskSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public RiskSearch() {
    ListBuilder<IndexedRisk> riskListBuilder = new ListBuilder<>(() -> new RiskList(),
      new AsyncTableCellOptions<>(IndexedRisk.class, "RiskRegister_risks").bindOpener());

    ListBuilder<RiskIncidence> riskIncidenceListBuilder = new ListBuilder<>(() -> new RiskIncidenceList(),
      new AsyncTableCellOptions<>(RiskIncidence.class, "RiskRegister_riskIncidences").bindOpener());

    searchWrapper = new SearchWrapper(true)
      .createListAndSearchPanel(riskListBuilder, RiskActions.get(), messages.searchPlaceHolder())
      .createListAndSearchPanel(riskIncidenceListBuilder, RiskIncidenceActions.getForMultipleEdit(),
        messages.searchPlaceHolder());

    initWidget(uiBinder.createAndBindUi(this));

    // TODO tmp
    // searchWrapper.setDropdownLabel(messages.searchListBoxRisks());
    // searchWrapper.addDropdownItem(messages.searchListBoxRisks(),
    // RodaConstants.SEARCH_RISKS);
    // searchWrapper.addDropdownItem(messages.searchListBoxIncidences(),
    // RodaConstants.SEARCH_INCIDENCES);
  }

  public void refresh() {
    searchWrapper.refreshAllLists();
  }
}
