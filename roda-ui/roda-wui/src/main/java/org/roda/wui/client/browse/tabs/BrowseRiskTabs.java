package org.roda.wui.client.browse.tabs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.RiskMitigationTerms;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.planning.RiskDetailsPanel;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class BrowseRiskTabs extends Tabs {

  public void init(IndexedRisk risk, RiskMitigationTerms terms, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new RiskDetailsPanel(risk, terms, actionCallback);
      }
    });

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.riskIncidences()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return buildIncidencesSearchWrapper(risk);
      }
    });
  }

  private Widget buildIncidencesSearchWrapper(IndexedRisk risk) {
    String RISK_INCIDENCE_LIST_ID = "RiskShowPanel_riskIncidences";
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISK_ID, risk.getId()));

    ListBuilder<RiskIncidence> listBuilder = new ListBuilder<>(() -> new RiskIncidenceList(),
      new AsyncTableCellOptions<>(RiskIncidence.class, RISK_INCIDENCE_LIST_ID).withSummary(messages.riskIncidences())
        .withFilter(filter).bindOpener().withSearchPlaceholder(messages.riskIncidenceRegisterSearchPlaceHolder()));

    return new SearchWrapper(false).createListAndSearchPanel(listBuilder);
  }

}
