/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.management;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.Login;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author António Lindo
 *
 */
public class SetPassword extends Composite {
  interface MyUiBinder extends UiBinder<Widget, SetPassword> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        SetPassword setPassword = new SetPassword();
        callback.onSuccess(setPassword);
      } else if (historyTokens.size() == 2) {
        SetPassword setPassword = new SetPassword();
        setPassword.updatePasswordPanel.setValuesAndHide(historyTokens.get(0), historyTokens.get(1));
        callback.onSuccess(setPassword);
      } else {
        HistoryUtils.newHistory(Login.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(final AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {

        @Override
        public void onSuccess(User user) {
          if (user.isGuest()) {
            callback.onSuccess(true);
          } else {
            HistoryUtils.newHistory(Welcome.RESOLVER);
            callback.onSuccess(null);
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }
      });
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "setpassword";
    }
  };
  @UiField
  UpdatePasswordPanel updatePasswordPanel;
  private SetPassword() {
    initWidget(uiBinder.createAndBindUi(this));
    UpdatePasswordPanel updatePasswordPanel = new UpdatePasswordPanel(false);

  }

}
