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

import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.UserExtraBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.Login;
import org.roda.wui.client.management.recaptcha.RecaptchaException;
import org.roda.wui.client.management.recaptcha.RecaptchaWidget;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class Register extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        Register register = new Register();
        callback.onSuccess(register);
      } else {
        HistoryUtils.newHistory(this);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(final AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {

        @Override
        public void onSuccess(User user) {
          if (user != null && user.isGuest()) {
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
      return "register";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, Register> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private boolean recaptchaActive = true;
  private RecaptchaWidget recaptchaWidget;
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  FlowPanel registerPanel;

  @UiField(provided = true)
  UserDataPanel userDataPanel;

  /**
   * Create a new panel to edit a user
   *
   * @param user
   *          the user to edit
   */
  public Register() {
    this.userDataPanel = new UserDataPanel(true, false, false, false);

    UserManagementService.Util.getInstance().retrieveDefaultExtraBundle(new AsyncCallback<UserExtraBundle>() {
      @Override
      public void onFailure(Throwable caught) {
        errorMessage(caught);
      }

      @Override
      public void onSuccess(UserExtraBundle result) {
        setExtra(result);
      }
    });

    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrieveGoogleReCAPTCHAAccount(new AsyncCallback<String>() {

      @Override
      public void onSuccess(String result) {
        if (result != null && !result.isEmpty()) {
          recaptchaWidget = new RecaptchaWidget(result);
          registerPanel.add(recaptchaWidget);
        } else {
          recaptchaActive = false;
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        recaptchaActive = false;
      }
    });
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  void setExtra(UserExtraBundle b) {
    this.userDataPanel.setExtraBundle(b);
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (userDataPanel.isValid()) {
      String recaptchaResponse = null;
      if (recaptchaActive) {
        recaptchaResponse = recaptchaWidget.getResponse();
      }

      User user = userDataPanel.getUser();
      user.setActive(false);

      final String password = userDataPanel.getPassword();
      final String recaptcha = recaptchaResponse;

      UserManagementService.Util.getInstance().registerUser(user, password, recaptcha, userDataPanel.getExtra(),
        LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<User>() {

          @Override
          public void onFailure(Throwable caught) {
            errorMessage(caught);
          }

          @Override
          public void onSuccess(final User registeredUser) {
            if (registeredUser.isActive()) {
              Dialogs.showInformationDialog(messages.registerSuccessDialogTitle(),
                messages.registerSuccessDialogMessageActive(), messages.registerSuccessDialogButton(), false,
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
              Dialogs.showInformationDialog(messages.registerSuccessDialogTitle(),
                messages.registerSuccessDialogMessage(), messages.registerSuccessDialogButton(), false,
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
          }
        });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(Login.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (recaptchaActive) {
      recaptchaWidget.reset();
    }

    if (caught instanceof EmailAlreadyExistsException) {
      Toast.showError(messages.registerEmailAlreadyExists());
    } else if (caught instanceof UserAlreadyExistsException) {
      Toast.showError(messages.registerUserExists());
    } else if (caught instanceof RecaptchaException) {
      Toast.showError(messages.registerWrongCaptcha());
    } else {
      Toast.showError(messages.registerFailure());
    }
  }
}
