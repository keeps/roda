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
package org.roda.wui.management.user.client;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.management.user.client.recaptcha.RecaptchaWidget;

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
import config.i18n.client.UserManagementMessages;

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
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      callback.onSuccess(true);
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

  private final User user;
  
  private boolean recaptchaActive = true;
  
  private RecaptchaWidget recaptchaWidget;

  private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);
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
      
      final User user = userDataPanel.getUser();
      final String password = userDataPanel.getPassword();
      final String recaptcha = recaptchaResponse;
      
      UserManagementService.Util.getInstance().register(user, password, recaptcha, new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean result) {
          // TODO Auto-generated method stub
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
    Tools.newHistory(Welcome.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof EmailAlreadyExistsException) {
      Toast.showError(constants.registerEmailAlreadyExists());
    } else {
      Toast.showError(messages.editUserFailure(Register.this.user.getName(), caught.getMessage()));
    }
  }
}
