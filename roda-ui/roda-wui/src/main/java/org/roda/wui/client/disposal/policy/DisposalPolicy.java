/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.policy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.roda.wui.client.browse.tabs.DisposalPolicyTabs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.disposal.Disposal;
import org.roda.wui.client.disposal.hold.CreateDisposalHold;
import org.roda.wui.client.disposal.hold.EditDisposalHold;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.client.disposal.rule.CreateDisposalRule;
import org.roda.wui.client.disposal.rule.EditDisposalRule;
import org.roda.wui.client.disposal.rule.ShowDisposalRule;
import org.roda.wui.client.disposal.schedule.CreateDisposalSchedule;
import org.roda.wui.client.disposal.schedule.EditDisposalSchedule;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import java.util.List;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class DisposalPolicy extends Composite {
  private static DisposalPolicy instance = null;
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "policy";
    }
  };
  private static DisposalPolicy.MyUiBinder uiBinder = GWT.create(DisposalPolicy.MyUiBinder.class);
  @UiField
  FlowPanel disposalPolicyDescription;
  @UiField
  DisposalPolicyTabs browseTab;

  private DisposalPolicy() {
    initWidget(uiBinder.createAndBindUi(this));
    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
    browseTab.init();
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static DisposalPolicy getInstance() {
    if (instance == null) {
      instance = new DisposalPolicy();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      DisposalPolicy disposalPolicy = new DisposalPolicy();
      callback.onSuccess(disposalPolicy);
    } else if (historyTokens.get(0).equals(CreateDisposalSchedule.RESOLVER.getHistoryToken())) {
      CreateDisposalSchedule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(CreateDisposalHold.RESOLVER.getHistoryToken())) {
      CreateDisposalHold.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowDisposalHold.RESOLVER.getHistoryToken())) {
      ShowDisposalHold.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowDisposalSchedule.RESOLVER.getHistoryToken())) {
      ShowDisposalSchedule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditDisposalHold.RESOLVER.getHistoryToken())) {
      EditDisposalHold.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditDisposalSchedule.RESOLVER.getHistoryToken())) {
      EditDisposalSchedule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(CreateDisposalRule.RESOLVER.getHistoryToken())) {
      CreateDisposalRule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowDisposalRule.RESOLVER.getHistoryToken())) {
      ShowDisposalRule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditDisposalRule.RESOLVER.getHistoryToken())) {
      EditDisposalRule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicy> {
  }
}