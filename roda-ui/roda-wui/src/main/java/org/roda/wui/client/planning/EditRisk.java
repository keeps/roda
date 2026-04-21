/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class EditRisk extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve indexed risk", "get");

        services
          .rodaEntityRestService(s -> s.findByUuid(historyTokens.get(0), LocaleInfo.getCurrentLocale().getLocaleName()),
            IndexedRisk.class)
          .whenComplete((risk, throwable) -> {
            if (throwable != null) {
              callback.onFailure(throwable);
            } else {
              callback.onSuccess(new EditRisk(risk));
            }
          });
      }
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
      return "edit_risk";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  NavigationToolbar<IndexedRisk> navigationToolbar;

  @UiField
  FlowPanel riskDataPanel;

  @UiField
  NoActionsToolbar actionsToolbar;

  @UiField
  TitlePanel title;

  /**
   * Create a panel to edit Risk
   *
   * @param risk
   *
   */
  public EditRisk(IndexedRisk risk) {
    initWidget(uiBinder.createAndBindUi(this));
    RiskDataPanel dataPanel = new RiskDataPanel(risk, true);
    riskDataPanel.add(dataPanel);

    dataPanel.setSaveHandler(() -> {
      Services services = new Services("Update risk", "update");
      services.riskResource(s -> s.updateRisk(dataPanel.getValue())).whenComplete((updated, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          Toast.showInfo(messages.riskUpdatedTitle(), messages.riskUpdatedMessage());
          HistoryUtils.newHistory(ShowRisk.RESOLVER, updated.getUUID());
        }
      });
    });

    dataPanel.setCancelHandler(() -> HistoryUtils.newHistory(ShowRisk.RESOLVER, risk.getUUID()));

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getEditRiskBreadCrumbs(risk));

    actionsToolbar.setLabel(messages.editRiskTitle());
    actionsToolbar.build();

    title.setText(StringUtils.isNotBlank(risk.getName()) ? risk.getName() : risk.getId());
    title.setIconClass("IndexedRisk");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, EditRisk> {
  }

}
