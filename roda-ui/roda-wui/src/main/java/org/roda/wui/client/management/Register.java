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
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.Login;
import org.roda.wui.client.management.recaptcha.RecaptchaException;
import org.roda.wui.client.management.recaptcha.RecaptchaWidget;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 *
 */
public class Register extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 0) {
        User user = new User();
        Register register = new Register(user);
        callback.onSuccess(register);
      } else {
        Tools.newHistory(this);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(final AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<RodaUser>() {

        @Override
        public void onSuccess(RodaUser user) {
          if (user != null && user.isGuest()) {
            callback.onSuccess(true);
          } else {
            Tools.newHistory(Welcome.RESOLVER);
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
      return "register";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, Register> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private User user;

  private boolean recaptchaActive = true;

  private RecaptchaWidget recaptchaWidget;

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

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
  public Register(User user) {
    this.user = user;

    this.userDataPanel = new UserDataPanel(true, false, false, false);
    this.userDataPanel.setUser(user);

    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().getGoogleReCAPTCHAAccount(new AsyncCallback<String>() {

      @Override
      public void onSuccess(String result) {
        if (result != null) {
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

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (userDataPanel.isValid()) {
      String recaptchaResponse = null;
      if (recaptchaActive) {
        recaptchaResponse = recaptchaWidget.getResponse();
      }

      user = userDataPanel.getUser();
      user.setActive(false);

      final String password = userDataPanel.getPassword();
      final String recaptcha = recaptchaResponse;

      BrowserService.Util.getInstance().isRegisterActive(new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean result) {
          final boolean registerActive = result;
          user.setActive(result);

          UserManagementService.Util.getInstance().registerUser(user, password, recaptcha, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
              errorMessage(caught);
            }

            @Override
            public void onSuccess(Void result) {
              if (registerActive) {
                Dialogs.showInformationDialog(constants.registerSuccessDialogTitle(),
                  constants.registerSuccessDialogMessageActive(), constants.registerSuccessDialogButton(),
                  new AsyncCallback<Void>() {

                  @Override
                  public void onSuccess(Void result) {
                    Tools.newHistory(Login.RESOLVER);
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                    Tools.newHistory(Login.RESOLVER);
                  }
                });
              } else {
                UserManagementService.Util.getInstance().sendEmailVerification(user.getId(), new AsyncCallback<Void>() {

                  @Override
                  public void onSuccess(Void result) {
                    Dialogs.showInformationDialog(constants.registerSuccessDialogTitle(),
                      constants.registerSuccessDialogMessage(), constants.registerSuccessDialogButton(),
                      new AsyncCallback<Void>() {

                      @Override
                      public void onSuccess(Void result) {
                        Tools.newHistory(Login.RESOLVER);
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        Tools.newHistory(Login.RESOLVER);
                      }
                    });
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                    sendEmailVerificationFailure(caught);
                  }
                });
              }
            }
          });
        }

        @Override
        public void onFailure(Throwable caught) {
          errorMessage(caught);
        }
      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(Login.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    recaptchaWidget.reset();
    if (caught instanceof EmailAlreadyExistsException) {
      Toast.showError(constants.registerEmailAlreadyExists());
    } else if (caught instanceof UserAlreadyExistsException) {
      Toast.showError(constants.registerUserExists());
    } else if (caught instanceof RecaptchaException) {
      Toast.showError(constants.registerWrongCaptcha());
    } else {
      Toast.showError(constants.registerFailure());
    }
  }

  private void sendEmailVerificationFailure(Throwable caught) {
    Toast.showError(constants.registerSendEmailVerificationFailure());
  }
}
