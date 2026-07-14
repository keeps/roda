/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning.risks;

import java.util.List;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.planning.RiskRegister;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class CreateRisk extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateRisk createRisk = new CreateRisk();
      callback.onSuccess(createRisk);
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
      return "create_risk";
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

  public CreateRisk() {
    initWidget(uiBinder.createAndBindUi(this));

    RiskDataPanel dataPanel = new RiskDataPanel();
    riskDataPanel.add(dataPanel);

    dataPanel.setSaveHandler(() -> {
      Risk risk = dataPanel.getValue();
      Services services = new Services("Create a risk", "create");
      services.riskResource(s -> s.createRisk(risk)).whenComplete((created, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          Toast.showInfo(messages.riskCreatedTitle(), messages.riskCreatedMessage());
          HistoryUtils.newHistory(ShowRisk.RESOLVER, created.getUUID());
        }
      });
    });

    dataPanel.setCancelHandler(() -> HistoryUtils.newHistory(RiskRegister.RESOLVER));

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateRiskBreadCrumbs());

    actionsToolbar.setLabel(messages.newRiskTitle());
    actionsToolbar.build();

    title.setText(messages.newRiskTitle());
    title.setIconClass("IndexedRisk");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, CreateRisk> {
  }

}
