package org.roda.wui.client.management;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.main.Login;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class UpdatePasswordPanel extends Composite {
  interface MyUiBinder extends UiBinder<Widget, UpdatePasswordPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TitlePanel titlePanel;

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

  @UiField
  Button reset;

  public boolean checked = false;
  @UiConstructor
  public UpdatePasswordPanel(boolean isReset) {
    initWidget(uiBinder.createAndBindUi(this));
    if (isReset) {
      setResetPasswordText();
    } else {
      setSetPasswordText();
    }

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

  public void setResetPasswordText() {
    titlePanel.setText(messages.resetPasswordTitle());
    reset.setText(messages.resetPasswordSubmit());
  }

  public void setSetPasswordText() {
    titlePanel.setText(messages.setPasswordTitle());
    reset.setText(messages.setPasswordSubmit());
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

    if (!password.getText().equals(passwordRepeat.getText())) {
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
    if (isValid() && isPasswordValid()) {
      try (SecureString securePassword = new SecureString(password.getValue().toCharArray())) {
        Services services = new Services("Reset user password", "reset");
        services.membersResource(s -> s.resetUserPassword(username.getValue(), token.getValue(), securePassword)).whenComplete((res, error) -> {
          if (error == null) {
            Dialogs.showInformationDialog(messages.resetPasswordSuccessDialogTitle(),
              messages.resetPasswordSuccessDialogMessage(), messages.resetPasswordSuccessDialogButton(), false,
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
          } else {
            errorMessage(error);
          }
        });
      }
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
