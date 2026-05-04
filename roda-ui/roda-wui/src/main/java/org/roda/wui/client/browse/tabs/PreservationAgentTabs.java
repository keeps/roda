package org.roda.wui.client.browse.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.planning.agents.tabs.PreservationAgentDetailsPanel;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class PreservationAgentTabs extends Tabs {

  public void init(IndexedPreservationAgent agent) {
    this.clear();

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new PreservationAgentDetailsPanel(agent);
      }
    });
  }
}
