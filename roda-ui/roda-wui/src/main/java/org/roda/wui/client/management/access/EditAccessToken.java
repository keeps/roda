package org.roda.wui.client.management.access;

import java.util.List;

import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.EditUser;
import org.roda.wui.client.management.MemberManagement;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class EditAccessToken extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        BrowserService.Util.getInstance().retrieveAccessToken(historyTokens.get(0), new NoAsyncCallback<AccessToken>() {
          @Override
          public void onSuccess(AccessToken result) {
            EditAccessToken editAccessToken = new EditAccessToken(result);
            callback.onSuccess(editAccessToken);
          }
        });
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public String getHistoryToken() {
      return "edit_access_tokens";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(MemberManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditAccessToken> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private AccessToken accessToken;

  @UiField
  TitlePanel titlePanel;

  @UiField(provided = true)
  AccessTokenDataPanel accessTokenDataPanel;

  public EditAccessToken(AccessToken accessToken) {
    this.accessToken = accessToken;
    this.accessTokenDataPanel = new AccessTokenDataPanel(accessToken, true);
    this.accessTokenDataPanel.setAccessToken(accessToken);

    initWidget(uiBinder.createAndBindUi(this));
    titlePanel.setText(accessToken.getUserName());
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonUpdate")
  void buttonUpdateHandler(ClickEvent e) {
    if (accessTokenDataPanel.isValid()) {
      AccessToken accessTokenUpdated = accessTokenDataPanel.getAccessToken();
      accessToken.setName(accessTokenUpdated.getName());
      accessToken.setExpirationDate(accessTokenUpdated.getExpirationDate());
      BrowserServiceImpl.Util.getInstance().updateAccessToken(this.accessToken, new NoAsyncCallback<AccessToken>() {
        @Override
        public void onSuccess(AccessToken accessToken) {
          HistoryUtils.newHistory(EditUser.RESOLVER, accessToken.getUserName());
        }
      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(EditUser.RESOLVER, accessToken.getUserName());
  }
}
