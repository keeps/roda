/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.rule;

import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalRuleActions;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.server.browse.BrowserServiceImpl;

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
public class EditDisposalRule extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        BrowserService.Util.getInstance().retrieveDisposalRule(historyTokens.get(0),
          new NoAsyncCallback<DisposalRule>() {
            @Override
            public void onSuccess(DisposalRule disposalRule) {
              BrowserService.Util.getInstance().listDisposalSchedules(new NoAsyncCallback<DisposalSchedules>() {
                @Override
                public void onSuccess(DisposalSchedules disposalSchedules) {
                  EditDisposalRule panel = new EditDisposalRule(disposalRule, disposalSchedules);
                  callback.onSuccess(panel);
                }
              });
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
      return "edit_disposal_rule";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditDisposalRule> {
  }

  private static EditDisposalRule instance = null;

  private static EditDisposalRule.MyUiBinder uiBinder = GWT.create(EditDisposalRule.MyUiBinder.class);

  private DisposalRule disposalRule;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static EditDisposalRule getInstance() {
    if (instance == null) {
      instance = new EditDisposalRule();
    }
    return instance;
  }

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  DisposalRuleDataPanel disposalRuleDataPanel;

  public EditDisposalRule() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public EditDisposalRule(DisposalRule disposalRule, DisposalSchedules disposalSchedules) {
    this.disposalRule = disposalRule;
    this.disposalRuleDataPanel = new DisposalRuleDataPanel(disposalRule, disposalSchedules, true);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (disposalRuleDataPanel.isChanged() && disposalRuleDataPanel.isValid()) {
      boolean runApplyRulesPlugin = false;
      DisposalRule disposalRuleUpdated = disposalRuleDataPanel.getDisposalRule();
      disposalRule.setTitle(disposalRuleUpdated.getTitle());
      disposalRule.setDescription(disposalRuleUpdated.getDescription());

      if (!disposalRule.getDisposalScheduleId().equals(disposalRuleUpdated.getDisposalScheduleId())
        && !disposalRule.getDisposalScheduleName().equals(disposalRuleUpdated.getDisposalScheduleName())) {
        runApplyRulesPlugin = true;
      }
      disposalRule.setDisposalScheduleId(disposalRuleUpdated.getDisposalScheduleId());
      disposalRule.setDisposalScheduleName(disposalRuleUpdated.getDisposalScheduleName());

      if (!disposalRule.getType().equals(disposalRuleUpdated.getType())) {
        runApplyRulesPlugin = true;
      }
      disposalRule.setType(disposalRuleUpdated.getType());

      if (disposalRuleUpdated.getConditionKey() != null) {
        if (!disposalRule.getConditionKey().equals(disposalRuleUpdated.getConditionKey())) {
          runApplyRulesPlugin = true;
        }
        disposalRule.setConditionKey(disposalRuleUpdated.getConditionKey());
      }

      if (disposalRuleUpdated.getConditionValue() != null) {
        if (!disposalRule.getConditionValue().equals(disposalRuleUpdated.getConditionValue())) {
          runApplyRulesPlugin = true;
        }
        disposalRule.setConditionValue(disposalRuleUpdated.getConditionValue());
      }

      if (!runApplyRulesPlugin) {
        BrowserServiceImpl.Util.getInstance().updateDisposalRule(disposalRule, new NoAsyncCallback<DisposalRule>() {
          @Override
          public void onSuccess(DisposalRule disposalRule) {
            HistoryUtils.newHistory(ShowDisposalRule.RESOLVER, disposalRule.getId());
          }
        });
      } else {
        Dialogs.showConfirmDialog(messages.saveButton(), messages.confirmEditRuleMessage(), messages.dialogNo(),
          messages.dialogYes(), new NoAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean confirm) {
              if (confirm) {
                BrowserServiceImpl.Util.getInstance().updateDisposalRule(disposalRule,
                  new NoAsyncCallback<DisposalRule>() {
                    @Override
                    public void onSuccess(DisposalRule disposalRule) {
                      DisposalRuleActions.applyDisposalRulesAction();
                    }
                  });
              }
            }
          });
      }
    } else {
      HistoryUtils.newHistory(ShowDisposalRule.RESOLVER, disposalRule.getId());
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(ShowDisposalRule.RESOLVER, disposalRule.getId());
  }

}
