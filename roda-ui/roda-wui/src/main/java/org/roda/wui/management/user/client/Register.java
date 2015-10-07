/**
 * 
 */
package org.roda.wui.management.user.client;

import java.util.Arrays;
import java.util.List;

import org.roda.core.common.EmailAlreadyExistsException;
import org.roda.core.common.UserAlreadyExistsException;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.data.v2.User;
import org.roda.wui.common.captcha.client.AbstractImageCaptcha;
import org.roda.wui.common.captcha.client.DefaultImageCaptcha;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.UserLogin;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class Register implements HistoryResolver {

  private static Register instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Register getInstance() {
    if (instance == null) {
      instance = new Register();
    }
    return instance;
  }

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private boolean initialized;

  private VerticalPanel layout;

  private Label userdataTitle;

  private UserDataPanel userdata;

  private Label disclaimer;

  private Label captchaTitle;

  private AbstractImageCaptcha captcha;

  private WUIButton submit;

  private Register() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;

      layout = new VerticalPanel();
      userdataTitle = new Label(constants.registerUserDataTitle());
      userdata = new UserDataPanel(false, false);
      disclaimer = new Label(constants.registerDisclaimer());
      captchaTitle = new Label(constants.registerCaptchaTitle());
      captcha = new DefaultImageCaptcha();
      submit = new WUIButton(constants.registerSubmit(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);

      submit.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          if (userdata.isValid()) {
            UserManagementService.Util.getInstance().register(userdata.getValue(), userdata.getPassword(),
              captcha.getResponse(), new AsyncCallback<Boolean>() {

              public void onFailure(Throwable caught) {
                if (caught instanceof UserAlreadyExistsException) {
                  Window.alert(constants.registerUserExists());
                } else if (caught instanceof EmailAlreadyExistsException) {
                  Window.alert(constants.registerEmailAlreadyExists());
                } else {
                  logger.error("Error while registering", caught);
                }
                captcha.refresh();
              }

              public void onSuccess(Boolean passed) {
                if (passed.booleanValue()) {
                  Window.alert(constants.registerSuccess());
                  Tools.newHistory(VerifyEmail.getInstance(), userdata.getValue().getName());
                } else {
                  Window.alert(constants.registerWrongCaptcha());
                  captcha.refresh();
                }

              }

            });
          }
        }

      });

      submit.setEnabled(false);

      userdata.addValueChangeHandler(new ValueChangeHandler<User>() {

        @Override
        public void onValueChange(ValueChangeEvent<User> event) {
          submit.setEnabled(userdata.isValid());
        }
      });

      layout.add(userdataTitle);
      layout.add(userdata);
      layout.add(disclaimer);
      layout.add(captchaTitle);
      layout.add(captcha.getWidget());
      layout.add(submit);

      layout.addStyleName("wui-register");
      userdataTitle.addStyleName("register-title");
      userdata.addStyleName("register-userdata");
      disclaimer.addStyleName("register-disclaimer");
      captchaTitle.addStyleName("register-title");
      captcha.getWidget().addStyleName("register-captcha");
      submit.addStyleName("register-submit");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.roda.office.common.client.HistoryResolver#getHistoryPath()
   */
  public List<String> getHistoryPath() {
    return Arrays.asList(getHistoryToken());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.roda.office.common.client.HistoryResolver#getHistoryToken()
   */
  public String getHistoryToken() {
    return "register";
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      if (initialized) {
        captcha.refresh();
        userdata.clear();
      }
      init();
      callback.onSuccess(layout);
    } else {
      Tools.newHistory(this);
      callback.onSuccess(null);
    }
  }

  public void isCurrentUserPermitted(final AsyncCallback<Boolean> callback) {
    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<RodaUser>() {

      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      public void onSuccess(RodaUser user) {
        callback.onSuccess(new Boolean(user.isGuest()));
      }

    });
  }

}
