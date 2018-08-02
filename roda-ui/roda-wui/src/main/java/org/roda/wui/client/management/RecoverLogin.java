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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.main.Login;
import org.roda.wui.client.management.recaptcha.RecaptchaException;
import org.roda.wui.client.management.recaptcha.RecaptchaWidget;
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
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class RecoverLogin extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      RecoverLogin recoverLogin = new RecoverLogin();
      callback.onSuccess(recoverLogin);
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
      return "recover";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, RecoverLogin> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private boolean recaptchaActive = true;

  private RecaptchaWidget recaptchaWidget;

  @UiField
  FlowPanel recoverPanel;

  @UiField
  TextBox usernameOrEmail;

  @UiField
  Button cancel;

  private boolean checked = false;

  private RecoverLogin() {
    initWidget(uiBinder.createAndBindUi(this));
    usernameOrEmail.getElement().setTitle(messages.recoverLoginUsernameOrEmail());

    addAttachHandler(event -> {
      if (event.isAttached()) {
        usernameOrEmail.setFocus(true);
      }
    });

    cancel.addClickHandler(event -> HistoryUtils.newHistory(Login.RESOLVER));

    String recaptchakey = ConfigurationManager.getString(RodaConstants.UI_GOOGLE_RECAPTCHA_CODE_PROPERTY);
    if (StringUtils.isNotBlank(recaptchakey)) {
      recaptchaWidget = new RecaptchaWidget(recaptchakey);
      recoverPanel.add(recaptchaWidget);
    } else {
      recaptchaActive = false;
    }

    usernameOrEmail.addKeyUpHandler(event -> {
      if (checked) {
        isValid();
      }
    });
  }

  /**
   * Is recover login panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    boolean valid = true;

    if (usernameOrEmail.getText().length() == 0) {
      valid = false;
      usernameOrEmail.addStyleName("isWrong");
    } else {
      usernameOrEmail.removeStyleName("isWrong");
    }

    checked = true;

    return valid;
  }

  @UiHandler("recover")
  void handleClick(ClickEvent e) {
    doRecover();
  }

  @UiHandler("usernameOrEmail")
  void handleUsernameKeyPress(KeyPressEvent event) {
    tryToRecoverWhenEnterIsPressed(event);
  }

  private void tryToRecoverWhenEnterIsPressed(KeyPressEvent event) {
    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      doRecover();
    }
  }

  private void doRecover() {
    if (isValid()) {
      String recaptchaResponse = null;
      if (recaptchaActive && recaptchaWidget != null) {
        recaptchaResponse = recaptchaWidget.getResponse();
      }
      UserManagementService.Util.getInstance().requestPasswordReset(usernameOrEmail.getValue(), recaptchaResponse,
        LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            errorMessage(caught);
          }

          @Override
          public void onSuccess(Void result) {
            Dialogs.showInformationDialog(messages.recoverLoginSuccessDialogTitle(),
              messages.recoverLoginSuccessDialogMessage(), messages.recoverLoginSuccessDialogButton(), false,
              new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                  HistoryUtils.newHistory(Login.RESOLVER);
                }

                @Override
                public void onSuccess(Void result) {
                  HistoryUtils.newHistory(Login.RESOLVER);
                }
              });
          }
        });
    }
  }

  private void errorMessage(Throwable caught) {
    if (recaptchaWidget != null) {
      recaptchaWidget.reset();
    }

    if (caught instanceof RecaptchaException) {
      Toast.showError(messages.recoverLoginCaptchaFailed());
    } else if (caught instanceof NotFoundException) {
      Toast.showError(messages.recoverLoginNoSuchUser());
    } else {
      Toast.showError(messages.recoverLoginFailure());
    }
  }
}
