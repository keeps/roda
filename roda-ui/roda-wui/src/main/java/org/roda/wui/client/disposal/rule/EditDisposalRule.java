package org.roda.wui.client.disposal.rule;

import java.util.List;

import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
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
        BrowserService.Util.getInstance().retrieveDisposalRule(historyTokens.get(0), new NoAsyncCallback<DisposalRule>() {
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
    this.disposalRuleDataPanel = new DisposalRuleDataPanel(disposalRule, disposalSchedules,true);

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
      DisposalRule disposalRuleUpdated = disposalRuleDataPanel.getDisposalRule();
      disposalRule.setTitle(disposalRuleUpdated.getTitle());
      disposalRule.setDescription(disposalRuleUpdated.getDescription());
      disposalRule.setDisposalScheduleId(disposalRuleUpdated.getDisposalScheduleId());
      disposalRule.setDisposalScheduleName(disposalRuleUpdated.getDisposalScheduleName());
      BrowserServiceImpl.Util.getInstance().updateDisposalRule(disposalRule, new NoAsyncCallback<DisposalRule>() {
        @Override
        public void onSuccess(DisposalRule disposalRule) {
          HistoryUtils.newHistory(ShowDisposalRule.RESOLVER, disposalRule.getId());
        }
      });
    } else {
      HistoryUtils.newHistory(ShowDisposalRule.RESOLVER, disposalRule.getId());
    }

  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(ShowDisposalRule.RESOLVER, disposalRule.getId());
  }

}
