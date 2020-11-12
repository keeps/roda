package org.roda.wui.client.disposal.hold;

import java.util.List;

import org.roda.core.data.exceptions.DisposalHoldAlreadyExistsException;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.DisposalPolicy;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;
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
public class CreateDisposalHold extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateDisposalHold createDisposalHold = new CreateDisposalHold(new DisposalHold());
      callback.onSuccess(createDisposalHold);
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
      return "create_disposal_hold";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalHold> {
  }

  private static CreateDisposalHold.MyUiBinder uiBinder = GWT.create(CreateDisposalHold.MyUiBinder.class);

  private DisposalHold disposalHold;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  DisposalHoldDataPanel disposalHoldDataPanel;

  public CreateDisposalHold(DisposalHold disposalHold) {
    this.disposalHold = disposalHold;
    this.disposalHoldDataPanel = new DisposalHoldDataPanel(disposalHold, false);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (disposalHoldDataPanel.isValid()) {
      disposalHold = disposalHoldDataPanel.getDisposalHold();
      BrowserServiceImpl.Util.getInstance().createDisposalHold(disposalHold, new NoAsyncCallback<DisposalHold>() {
        @Override
        public void onSuccess(DisposalHold disposalHold) {
          HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
        }
      });
    }

  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
  }

}
