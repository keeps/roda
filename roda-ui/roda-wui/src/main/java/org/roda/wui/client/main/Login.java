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
package org.roda.wui.client.main;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.EmailUnverifiedException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.v2.notifications.NotificationState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UpSalePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.RecoverLogin;
import org.roda.wui.client.management.Register;
import org.roda.wui.client.services.Services;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class Login extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      new Login().resolve(historyTokens, callback);
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
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }
  };

  public static final String getViewItemHistoryToken(String id) {
    return RESOLVER.getHistoryPath() + "." + id;
  }

  public static final String cardIdentifier = "collapsable-login-card";

  interface MyUiBinder extends UiBinder<Widget, Login> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private boolean registerDisabled = ConfigurationManager.getBoolean(false, RodaConstants.USER_REGISTRATION_DISABLED);
  private Boolean casActive = ConfigurationManager.getBoolean(false, RodaConstants.UI_SERVICE_CAS_ACTIVE);
  private Boolean multiMethodAuthenticationActive = ConfigurationManager.getBoolean(false,
    RodaConstants.UI_SERVICE_MULTI_METHOD_AUTHENTICATION_ACTIVE);
  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  Label error;

  @UiField
  Button resendEmail;

  @UiField
  Button recover;

  @UiField
  Button register;

  @UiField
  FlowPanel loginPanel;

  @UiField
  FlowPanel loggedInPanel;

  @UiField(provided = true)
  UpSalePanel casMessagePanel;

  @UiField
  FlowPanel mmaPanel;

  @UiField
  InlineHTML loggedInMessage;
  @UiField
  Button logout;

  @UiField
  Button homepage;

  private List<String> serviceTokens = null;

  private Login() {
    casMessagePanel = new UpSalePanel(messages.casTitleText(), messages.casInformationText(), messages.learnMore(),
      ConfigurationManager.getString(RodaConstants.UI_SERVICE_CAS_URL), cardIdentifier);
    initWidget(uiBinder.createAndBindUi(this));

    if (casActive) {
      casMessagePanel.setVisible(false);
    }

    if (multiMethodAuthenticationActive) {
      mmaPanel.setVisible(true);
      List<String> methods = ConfigurationManager
        .getStringList(RodaConstants.UI_SERVICE_MULTI_METHOD_AUTHENTICATION_LIST);
      for (String method : methods) {
        String label = ConfigurationManager.getString(RodaConstants.UI_SERVICE_MULTI_METHOD_AUTHENTICATION_LIST, method,
          "label");
        String path = ConfigurationManager.getString(RodaConstants.UI_SERVICE_MULTI_METHOD_AUTHENTICATION_LIST, method,
          "path");
        Button authMethodBtn = new Button(label);
        authMethodBtn.addStyleName("btn btn-block btn-play");
        authMethodBtn.addClickHandler(clickEvent -> Window.Location.assign(path));
        mmaPanel.add(authMethodBtn);
      }
    }

    addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached()) {
          username.setFocus(true);
        }
      }
    });
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    username.setText("");
    password.setText("");
    error.setText("");

    if (!casActive) {
      casMessagePanel.setVisible(JavascriptUtils.accessLocalStorage(cardIdentifier));
    }

    resendEmail.setVisible(false);
    serviceTokens = historyTokens;

    if (this.registerDisabled) {
      recover.setVisible(false);
      register.setVisible(false);
    }
    UserLogin.getInstance().getAuthenticatedUser(new NoAsyncCallback<User>() {
      @Override
      public void onSuccess(User user) {
        loggedInMessage.setHTML(messages.loggedIn(user.getName()));
        loginPanel.setVisible(user.isGuest());
        loggedInPanel.setVisible(!user.isGuest());
        callback.onSuccess(Login.this);
      }

      @Override
      public void onFailure(Throwable caught) {
        callback.onSuccess(Login.this);
      }
    });
  }

  @UiHandler("homepage")
  void handleHomepage(ClickEvent e) {
    HistoryUtils.newHistory(Welcome.RESOLVER);
  }

  @UiHandler("logout")
  void handleLogout(ClickEvent e) {
    UserLogin.getInstance().logout(RESOLVER.getHistoryToken());
  }

  @UiHandler("login")
  void handleLogin(ClickEvent e) {
    doLogin();
  }

  @UiHandler("register")
  void handleRegister(ClickEvent e) {
    HistoryUtils.newHistory(Register.RESOLVER);
  }

  @UiHandler("recover")
  void handleRecover(ClickEvent e) {
    HistoryUtils.newHistory(RecoverLogin.RESOLVER);
  }

  @UiHandler("username")
  void handleUsernameKeyPress(KeyPressEvent event) {
    tryToLoginWhenEnterIsPressed(event);
  }

  @UiHandler("password")
  void handlePasswordKeyPress(KeyPressEvent event) {
    tryToLoginWhenEnterIsPressed(event);
  }

  @UiHandler("resendEmail")
  void handleResendEmail(final ClickEvent e) {
    Services services = new Services("Resend email", "resend");
    services
      .membersResource(
        s -> s.sendEmailVerification(username.getText(), LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((result, error) -> {
        if (result != null) {
          if (result.getState() == NotificationState.COMPLETED) {
            Dialogs.showInformationDialog(messages.loginResendEmailSuccessDialogTitle(),
              messages.loginResendEmailSuccessDialogMessage(), messages.loginResendEmailSuccessDialogButton(), false,
              new AsyncCallback<Void>() {

                @Override
                public void onSuccess(final Void result) {
                  HistoryUtils.newHistory(Login.RESOLVER);
                }

                @Override
                public void onFailure(final Throwable caught) {
                  HistoryUtils.newHistory(Login.RESOLVER);
                }
              });
          } else {
            Dialogs.showInformationDialog(messages.loginResendEmailFailureDialogTitle(),
              messages.loginResendEmailFailureDialogMessage(), messages.loginResendEmailFailureDialogButton(), false,
              new AsyncCallback<Void>() {

                @Override
                public void onSuccess(final Void result) {
                  HistoryUtils.newHistory(Login.RESOLVER);
                }

                @Override
                public void onFailure(final Throwable caught) {
                  HistoryUtils.newHistory(Login.RESOLVER);
                }
              });
          }
        } else if (error != null) {
          Toast.showError(messages.loginResendEmailVerificationFailure());
        }
      });
  }

  private void tryToLoginWhenEnterIsPressed(KeyPressEvent event) {
    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      doLogin();
    }
  }

  private void doLogin() {
    String usernameText = username.getText();
    String passwordText = password.getText();

    error.setText("");

    if (usernameText.trim().length() == 0 || passwordText.trim().length() == 0) {
      error.setText(messages.fillUsernameAndPasswordMessage());
    } else {
      UserLogin.getInstance().login(usernameText, passwordText, new AsyncCallback<User>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof EmailUnverifiedException) {
            error.setText(messages.emailUnverifiedMessage());
            resendEmail.setVisible(true);
          } else if (caught instanceof InactiveUserException) {
            error.setText(messages.inactiveUserMessage());
          } else if (caught instanceof AuthenticationDeniedException) {
            error.setText(messages.wrongUsernameAndPasswordMessage());
          } else {
            error.setText(messages.systemCurrentlyUnavailableMessage());
          }
        }

        @Override
        public void onSuccess(User user) {
          if (serviceTokens != null && !serviceTokens.isEmpty()) {
            HistoryUtils.newHistory(serviceTokens);
          } else {
            HistoryUtils.newHistory(Welcome.RESOLVER);
          }
        }
      });
    }
  }
}
