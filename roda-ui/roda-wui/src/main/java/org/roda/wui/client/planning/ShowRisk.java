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
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.browse.tabs.BrowseRiskTabs;
import org.roda.wui.client.common.*;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowRisk extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      resolveShowRisk(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "risk";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final String riskUuid;

  private final AsyncCallback<Actionable.ActionImpact> actionCallback = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (result.equals(Actionable.ActionImpact.DESTROYED)) {
        HistoryUtils.newHistory(RiskRegister.RESOLVER);
      } else if (Actionable.ActionImpact.UPDATED.equals(result)) {
        refreshRisk();
        Toast.showInfo(messages.riskUpdatedTitle(), messages.riskUpdatedMessage());
      }
    }
  };

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  NavigationToolbar<IndexedRisk> navigationToolbar;

  @UiField
  TitlePanel title;

  @UiField(provided = true)
  BrowseRiskTabs riskTabs;

  @UiField
  BrowseRiskActionsToolbar actionsToolbar;

  public ShowRisk(IndexedRisk risk) {
    riskUuid = risk.getUUID();

    riskTabs = new BrowseRiskTabs();
    riskTabs.init(risk, actionCallback);
    initWidget(uiBinder.createAndBindUi(this));

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse browse-file browse_main_panel");

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getRiskBreadCrumbs(risk));
    actionsToolbar.setLabel(messages.showRiskTitle());
    actionsToolbar.setObjectAndBuild(risk, null, actionCallback);
    title.setText(StringUtils.isNotBlank(risk.getName()) ? risk.getName() : risk.getId());
  }

  private static void resolveShowRisk(List<String> historyTokens, final AsyncCallback<Widget> callback) {

    if (historyTokens.size() != 1) {
      HistoryUtils.newHistory(RiskRegister.RESOLVER);
      callback.onSuccess(null);
      return;
    }

    Services services = new Services("Retrieve indexed risk", "get");
    String riskUUID = historyTokens.get(0);

    services
      .rodaEntityRestService(s -> s.findByUuid(riskUUID, LocaleInfo.getCurrentLocale().getLocaleName()),
        IndexedRisk.class)
      .thenCompose(indexedRisk -> services.riskResource(s -> s.retrieveRiskVersions(riskUUID))
        .whenComplete((result, throwable) -> {
          if (throwable == null) {
            indexedRisk.setHasVersions(!result.getVersions().isEmpty());
            callback.onSuccess(new ShowRisk(indexedRisk));
          } else {
            callback.onFailure(throwable);
          }
        }));
  }

  private void refreshRisk() {
    Services services = new Services("Retrieve indexed risk", "get");
    services
      .rodaEntityRestService(s -> s.findByUuid(riskUuid, LocaleInfo.getCurrentLocale().getLocaleName()),
        IndexedRisk.class)
      .thenCompose(indexedRisk -> services.riskResource(s -> s.retrieveRiskVersions(riskUuid))
        .whenComplete((result, throwable) -> {
          if (throwable == null) {
            indexedRisk.setHasVersions(!result.getVersions().isEmpty());
            updateRiskUI(indexedRisk);
          }
        }));
  }

  private void updateRiskUI(IndexedRisk risk) {
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getRiskBreadCrumbs(risk));
    actionsToolbar.setObjectAndBuild(risk, null, actionCallback);
    title.setText(StringUtils.isNotBlank(risk.getName()) ? risk.getName() : risk.getId());
    riskTabs.clear();
    riskTabs.init(risk, actionCallback);
  }

  interface MyUiBinder extends UiBinder<Widget, ShowRisk> {
  }
}
