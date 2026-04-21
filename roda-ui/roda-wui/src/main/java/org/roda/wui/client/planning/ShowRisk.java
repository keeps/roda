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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskMitigationTerms;
import org.roda.wui.client.browse.tabs.BrowseRiskTabs;
import org.roda.wui.client.common.*;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
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
  NavigationToolbar<IndexedRisk> navigationToolbar;
  @UiField
  TitlePanel title;
  @UiField(provided = true)
  BrowseRiskTabs riskTabs;
  @UiField
  BrowseRiskActionsToolbar actionsToolbar;
  private IndexedRisk risk;
  private RiskMitigationTerms riskMitigationTerms;

  public ShowRisk(IndexedRisk risk, RiskMitigationTerms terms) {
    this.risk = risk;
    this.riskMitigationTerms = terms;

    riskTabs = new BrowseRiskTabs();

    initWidget(uiBinder.createAndBindUi(this));

    initHandlers();
    navigationToolbar.withObject(risk).build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getRiskBreadCrumbs(risk));

    actionsToolbar.setLabel(messages.showRiskTitle());
    actionsToolbar.setObjectAndBuild(risk, null, handler);

    title.setText(StringUtils.isNotBlank(risk.getName()) ? risk.getName() : risk.getId());

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    riskTabs.init(risk, terms, handler);
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
      .thenCompose(indexedRisk -> services.riskResource(s -> s.retrieveRiskVersions(riskUUID)).thenCompose(versions -> {
        indexedRisk.setHasVersions(!versions.getVersions().isEmpty());

        return services.riskResource(s -> s.retrieveRiskMitigationTerms(riskUUID)).whenComplete((terms, throwable) -> {
          if (throwable == null) {
            callback.onSuccess(new ShowRisk(indexedRisk, terms));
          } else {
            callback.onFailure(throwable);
          }
        });
      })).exceptionally(throwable -> {
        callback.onFailure(throwable);
        return null;
      });
  }

  private void initHandlers() {
    handlers.put(Actionable.ActionImpact.DESTROYED, () -> HistoryUtils.newHistory(RiskRegister.RESOLVER));

    handlers.put(Actionable.ActionImpact.UPDATED, () -> {
      refreshRisk();
      Toast.showInfo(messages.riskUpdatedTitle(), messages.riskUpdatedMessage());
    });
  }

  private void refreshRisk() {
    Services services = new Services("Retrieve indexed risk", "get");
    String riskUuid = risk.getUUID();

    services
      .rodaEntityRestService(s -> s.findByUuid(riskUuid, LocaleInfo.getCurrentLocale().getLocaleName()),
        IndexedRisk.class)
      .thenCompose(indexedRisk -> services.riskResource(s -> s.retrieveRiskVersions(riskUuid)).thenCompose(versions -> {
        indexedRisk.setHasVersions(!versions.getVersions().isEmpty());

        return services.riskResource(s -> s.retrieveRiskMitigationTerms(riskUuid)).whenComplete((terms, throwable) -> {
          if (throwable == null) {
            updateRiskUI(indexedRisk, terms);
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          }
        });
      })).exceptionally(throwable -> {
        AsyncCallbackUtils.defaultFailureTreatment(throwable);
        return null;
      });
  }

  private void updateRiskUI(IndexedRisk updatedRisk, RiskMitigationTerms updatedTerms) {
    this.risk = updatedRisk;
    this.riskMitigationTerms = updatedTerms;
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getRiskBreadCrumbs(updatedRisk));
    actionsToolbar.setObjectAndBuild(updatedRisk, null, handler);
    title.setText(StringUtils.isNotBlank(updatedRisk.getName()) ? updatedRisk.getName() : updatedRisk.getId());
    riskTabs.clear();
    riskTabs.init(updatedRisk, updatedTerms, handler);
  }

  interface MyUiBinder extends UiBinder<Widget, ShowRisk> {
  }

}
