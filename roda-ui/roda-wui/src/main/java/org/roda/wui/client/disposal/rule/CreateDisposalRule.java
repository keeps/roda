/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.rule;

import java.util.List;

import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.services.DisposalRuleRestService;
import org.roda.wui.client.services.DisposalScheduleRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

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

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class CreateDisposalRule extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      Services services = new Services("List disposal schedules", "get");
      services.disposalScheduleResource(DisposalScheduleRestService::listDisposalSchedules)
        .thenCompose(disposalSchedules -> services.disposalRuleResource(DisposalRuleRestService::listDisposalRules)
          .whenComplete((disposalRulesResult, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              CreateDisposalRule createDisposalRule = new CreateDisposalRule(new DisposalRule(), disposalSchedules,
                disposalRulesResult);
              callback.onSuccess(createDisposalRule);
            }
          }));
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
      return "create_disposal_rule";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static CreateDisposalRule.MyUiBinder uiBinder = GWT.create(CreateDisposalRule.MyUiBinder.class);
  @UiField
  Button buttonSave;
  @UiField
  Button buttonCancel;
  @UiField(provided = true)
  DisposalRuleDataPanel disposalRuleDataPanel;
  private DisposalRule disposalRule;
  private DisposalRules disposalRules;

  public CreateDisposalRule(DisposalRule disposalRule, DisposalSchedules disposalSchedules,
    DisposalRules disposalRules) {
    this.disposalRule = disposalRule;
    this.disposalRules = disposalRules;

    this.disposalRuleDataPanel = new DisposalRuleDataPanel(disposalRule, disposalSchedules, false);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (disposalRuleDataPanel.isValid()) {
      disposalRule = disposalRuleDataPanel.getDisposalRule();
      disposalRule.setOrder(disposalRules.getObjects().size());
      Services services = new Services("Create disposal rule", "create");
      services.disposalRuleResource(s -> s.createDisposalRule(disposalRule)).whenComplete((result, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
        }
      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
  }

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalRule> {
  }
}
