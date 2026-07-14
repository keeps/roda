/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.planning.risks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.tabs.RiskIncidenceTabs;
import org.roda.wui.client.common.BrowseRiskIncidenceActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowRiskIncidence extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      resolveShowRiskIncidence(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RiskIncidenceRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "riskincidence";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final Map<Actionable.ActionImpact, Runnable> handlers = new HashMap<>();
  private final AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<RiskIncidence> navigationToolbar;
  @UiField
  TitlePanel title;
  @UiField
  BrowseRiskIncidenceActionsToolbar actionsToolbar;
  @UiField
  RiskIncidenceTabs riskIncidenceTabs;

  /**
   * Create a new panel to view a risk incidence
   */
  public ShowRiskIncidence(RiskIncidence incidence) {
    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withObject(incidence).build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getRiskIncidenceBreadcrumbs(incidence));

    actionsToolbar.setLabel(messages.showRiskIncidenceTitle());
    actionsToolbar.setObjectAndBuild(incidence, null, null);

    title.setText(messages.showRiskIncidenceTitle());
    title.setIconClass("RiskIncidence");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    riskIncidenceTabs.init(incidence);
  }

  private static void resolveShowRiskIncidence(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() != 1) {
      HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER);
      callback.onSuccess(null);
      return;
    }

    Services services = new Services("Retrieve risk incidence", "get");
    String riskIncidenceId = historyTokens.get(0);

    services.rodaEntityRestService(s -> s.findByUuid(riskIncidenceId, LocaleInfo.getCurrentLocale().getLocaleName()),
      RiskIncidence.class).whenComplete((value, error) -> {
        if (error != null) {
          callback.onFailure(error);
        } else if (value != null) {
          ShowRiskIncidence incidencePanel = new ShowRiskIncidence(value);
          callback.onSuccess(incidencePanel);
        }
      });
  }

  interface MyUiBinder extends UiBinder<Widget, ShowRiskIncidence> {
  }

}
