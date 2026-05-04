package org.roda.wui.client.browse.tabs;

import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.disposal.rule.tabs.DisposalRuleDetailsPanel;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DisposalRuleTabs extends Tabs {

  public void init(DisposalRule rule, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    this.clear();

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new DisposalRuleDetailsPanel(rule);
      }
    });
  }
}