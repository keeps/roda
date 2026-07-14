package org.roda.wui.client.browse.tabs;

import org.roda.core.data.v2.risks.RiskIncidence;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import org.roda.wui.client.planning.risks.tabs.RiskIncidenceDetailsPanel;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RiskIncidenceTabs extends Tabs {

  public void init(RiskIncidence incidence) {
    this.clear();

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new RiskIncidenceDetailsPanel(incidence);
      }
    });
  }
}
