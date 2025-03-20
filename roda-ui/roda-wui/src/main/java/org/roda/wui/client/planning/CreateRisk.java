/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class CreateRisk extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateRisk createRisk = new CreateRisk();
      callback.onSuccess(createRisk);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
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
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  Button buttonApply;
  @UiField
  Button buttonCancel;
  @UiField(provided = true)
  RiskDataPanel riskDataPanel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public CreateRisk() {
    this.riskDataPanel = new RiskDataPanel(null, false);
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (riskDataPanel.isValid()) {
      Risk risk = riskDataPanel.getRisk();
      Services services = new Services("Create a risk", "create");
      services.riskResource(s -> s.createRisk(risk)).whenComplete((created, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          Toast.showInfo(messages.riskCreatedTitle(), messages.riskCreatedMessage());
          HistoryUtils.newHistory(ShowRisk.RESOLVER, created.getUUID());
        }
      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(RiskRegister.RESOLVER);
  }

  interface MyUiBinder extends UiBinder<Widget, CreateRisk> {
  }

}
