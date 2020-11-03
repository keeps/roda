package org.roda.wui.client.disposal.rule;

import java.util.List;

import org.roda.core.data.exceptions.DisposalRuleAlreadyExistsException;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.Disposal;
import org.roda.wui.client.disposal.DisposalPolicy;
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
import org.roda.wui.server.browse.BrowserServiceImpl;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class CreateDisposalRule extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().listDisposalSchedules(new AsyncCallback<DisposalSchedules>() {
        @Override
        public void onFailure(Throwable throwable) {
        }

        @Override
        public void onSuccess(DisposalSchedules disposalSchedules) {
          CreateDisposalRule createDisposalRule = new CreateDisposalRule(new DisposalRule(), disposalSchedules);
          callback.onSuccess(createDisposalRule);
        }
      });

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

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalRule> {
  }

  private static CreateDisposalRule.MyUiBinder uiBinder = GWT.create(CreateDisposalRule.MyUiBinder.class);

  private DisposalRule disposalRule;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonNext;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  DisposalRuleDataPanel disposalRuleDataPanel;

  public CreateDisposalRule(DisposalRule disposalRule, DisposalSchedules disposalSchedules) {
    this.disposalRule = disposalRule;

    this.disposalRuleDataPanel = new DisposalRuleDataPanel(disposalRule, disposalSchedules, false);
    this.disposalRuleDataPanel.setDisposalRule(disposalRule);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonNext")
  void buttonApplyHandler(ClickEvent e) {
    if (disposalRuleDataPanel.isValid()) {
      disposalRule = disposalRuleDataPanel.getDisposalRule();
      BrowserServiceImpl.Util.getInstance().createDisposalRule(disposalRule,
        new AsyncCallback<DisposalRule>() {

          @Override
          public void onFailure(Throwable caught) {
            errorMessage(caught);
          }

          @Override
          public void onSuccess(DisposalRule createdDisposalSchedule) {
            HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
          }

        });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof DisposalRuleAlreadyExistsException) {
      Toast.showError(messages.createDisposalRuleAlreadyExists(disposalRule.getTitle()));
    } else {
      Toast.showError(messages.createDisposalRuleFailure(caught.getMessage()));
    }
  }
}
