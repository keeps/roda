/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.wui.client.browse.tabs.DisposalRuleTabs;
import org.roda.wui.client.common.DisposalRuleActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class ShowDisposalRule extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve disposal rule", "get");
        services.disposalRuleResource(s -> s.retrieveDisposalRule(historyTokens.get(0)))
          .whenComplete((rule, throwable) -> {
            if (throwable != null) {
              // TODO: REVIEW defaultFailureTreatment
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
              HistoryUtils.newHistory(DisposalPolicy.RESOLVER.getHistoryPath());
            } else {
              ShowDisposalRule panel = new ShowDisposalRule(rule);
              callback.onSuccess(panel);
            }
          });
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DisposalPolicy.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalPolicy.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "disposal_rule";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  NavigationToolbar<DisposalRule> navigationToolbar;

  @UiField
  DisposalRuleActionsToolbar actionsToolbar;

  @UiField
  TitlePanel title;

  @UiField
  DisposalRuleTabs browseTab;

  private Map<Actionable.ActionImpact, Runnable> handlers = new HashMap<>();
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };

  public ShowDisposalRule(final DisposalRule rule) {
    initWidget(uiBinder.createAndBindUi(this));

    initHandlers();

    navigationToolbar.withObject(rule);
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getDisposalRuleBreadcrumbs(rule));
    navigationToolbar.build();

    actionsToolbar.setLabel(messages.showDisposalRuleTitle());
    actionsToolbar.setObjectAndBuild(rule, null, handler);

    title.setText(rule.getTitle());
    title.setIconClass("DisposalRule");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    browseTab.init(rule, handler);
  }

  private void initHandlers() {
    handlers.put(Actionable.ActionImpact.DESTROYED,
      () -> HistoryUtils.newHistory(DisposalPolicy.RESOLVER.getHistoryPath()));
  }

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalRule> {
  }

}
