package org.roda.wui.client.disposal.confirmations;

import java.util.List;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ShowDisposalConfirmation extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalConfirmations.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "confirmation";
    }
  };

  private static ShowDisposalConfirmation instance = null;
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalConfirmation> {
  }

  private static ShowDisposalConfirmation.MyUiBinder uiBinder = GWT.create(ShowDisposalConfirmation.MyUiBinder.class);

  public static ShowDisposalConfirmation getInstance() {
    if (instance == null) {
      instance = new ShowDisposalConfirmation();
    }
    return instance;
  }

  private ShowDisposalConfirmation() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      ShowDisposalConfirmation panel = new ShowDisposalConfirmation();
      callback.onSuccess(panel);
    } else {
      HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
      callback.onSuccess(null);
    }
  }
}
