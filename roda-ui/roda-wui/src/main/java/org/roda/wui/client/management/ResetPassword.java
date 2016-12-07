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

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.main.Login;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class ResetPassword extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 0) {
        ResetPassword recoverLogin = new ResetPassword();
        callback.onSuccess(recoverLogin);
      } else if (historyTokens.size() == 2) {
        ResetPassword recoverLogin = new ResetPassword();
        recoverLogin.setValuesAndHide(historyTokens.get(0), historyTokens.get(1));
        callback.onSuccess(recoverLogin);
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

    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    public String getHistoryToken() {
      return "resetpassword";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ResetPassword> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField
  FlowPanel recoverPanel;

  @UiField
  Label usernameLabel;

  @UiField
  TextBox username;

  @UiField
  Label tokenLabel;

  @UiField
  TextBox token;

  @UiField
  TextBox password;

  @UiField
  TextBox passwordRepeat;

  @UiField
  Button cancel;

  private boolean checked = false;

  private ResetPassword() {

    initWidget(uiBinder.createAndBindUi(this));
    addAttachHandler(new AttachEvent.Handler() {

      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached()) {
          username.setFocus(true);
        }
      }
    });

    cancel.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        HistoryUtils.newHistory(Login.RESOLVER);
      }
    });

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (checked)
          isValid();
      }
    };

    KeyUpHandler keyUpHandlerPassword = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        isPasswordValid();
      }

    };

    username.addKeyUpHandler(keyUpHandler);
    token.addKeyUpHandler(keyUpHandler);

    password.addKeyUpHandler(keyUpHandlerPassword);
    passwordRepeat.addKeyUpHandler(keyUpHandlerPassword);
  }

  public void setValuesAndHide(String initialUsername, String initialToken) {
    username.setText(initialUsername);
    token.setText(initialToken);

    usernameLabel.setVisible(false);
    username.setVisible(false);
    tokenLabel.setVisible(false);
    token.setVisible(false);
  }

  /**
   * Is recover login panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    boolean valid = true;

    if (username.getText().length() == 0) {
      valid = false;
      username.addStyleName("isWrong");
    } else {
      username.removeStyleName("isWrong");
    }

    if (token.getText().length() == 0) {
      valid = false;
      token.addStyleName("isWrong");
    } else {
      token.removeStyleName("isWrong");
    }

    checked = true;

    return valid;
  }

  private boolean isPasswordValid() {
    boolean valid = true;

    if (password.getText().length() == 0) {
      valid = false;
      password.addStyleName("invalid");
    } else {
      password.removeStyleName("invalid");
    }

    if (passwordRepeat.getText().length() == 0) {
      valid = false;
      passwordRepeat.addStyleName("invalid");
    } else {
      passwordRepeat.removeStyleName("invalid");
    }

    if (!passwordRepeat.getText().equals(passwordRepeat.getText())) {
      valid = false;
      passwordRepeat.addStyleName("invalid");
    } else {
      passwordRepeat.removeStyleName("invalid");
    }

    return valid;
  }

  @UiHandler("reset")
  void handleClick(ClickEvent e) {
    doReset();
  }

  @UiHandler("username")
  void handleUsernameKeyPress(KeyPressEvent event) {
    tryToResetWhenEnterIsPressed(event);
  }

  private void tryToResetWhenEnterIsPressed(KeyPressEvent event) {
    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      doReset();
    }
  }

  private void doReset() {
    if (isValid()) {
      UserManagementService.Util.getInstance().resetUserPassword(username.getValue(), password.getValue(),
        token.getValue(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            errorMessage(caught);
          }

          @Override
          public void onSuccess(Void result) {
            Dialogs.showInformationDialog(messages.resetPasswordSuccessDialogTitle(),
              messages.resetPasswordSuccessDialogMessage(), messages.resetPasswordSuccessDialogButton(),
              new AsyncCallback<Void>() {

                @Override
                public void onSuccess(Void result) {
                  HistoryUtils.newHistory(Login.RESOLVER);
                }

                @Override
                public void onFailure(Throwable caught) {
                  HistoryUtils.newHistory(Login.RESOLVER);
                }
              });
          }
        });
    }
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.resetPasswordNoSuchUser());
    } else if (caught instanceof InvalidTokenException) {
      Toast.showError(messages.resetPasswordInvalidToken());
    } else {
      Toast.showError(messages.resetPasswordFailure());
    }
  }
}
