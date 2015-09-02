/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.CommonConstants;
import pt.gov.dgarq.roda.core.common.AuthenticationDeniedException;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.UserLoginService;
import pt.gov.dgarq.roda.wui.home.client.Home;

/**
 * @author Luis Faria
 * 
 */
public class Login extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      callback.onSuccess(true);
    }

    @Override
    public String getHistoryToken() {
      return "login";
    }

    @Override
    public String getHistoryPath() {
      return getHistoryToken();
    }
  };

  public static final String getViewItemHistoryToken(String id) {
    return RESOLVER.getHistoryPath() + "." + id;
  }

  interface MyUiBinder extends UiBinder<Widget, Login> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static Login instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Login getInstance() {
    if (instance == null) {
      instance = new Login();
    }
    return instance;
  }

  private static CommonConstants constants = (CommonConstants) GWT.create(CommonConstants.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  Button login;

  private Login() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.length == 0) {
      username.setText("");
      password.setText("");
      callback.onSuccess(this);
    } else {
      History.newItem(RESOLVER.getHistoryPath());
      callback.onSuccess(null);
    }
  }

  @UiHandler("login")
  void handleClick(ClickEvent e) {
    doLogin();
  }

  private void doLogin() {
    String usernameText = username.getText();
    String passwordText = password.getText();

    UserLogin.getInstance().login(usernameText, passwordText, new AsyncCallback<AuthenticatedUser>() {

      @Override
      public void onFailure(Throwable caught) {
        // TODO show message
        if (caught instanceof AuthenticationDeniedException) {
          GWT.log("Bad credentials");
        } else {
          GWT.log("Unexpected exception");
        }
      }

      @Override
      public void onSuccess(AuthenticatedUser user) {
        History.newItem(Home.RESOLVER.getHistoryPath());
      }
    });
  }
}
