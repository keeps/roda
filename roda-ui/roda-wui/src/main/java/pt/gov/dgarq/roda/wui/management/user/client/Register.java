/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

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
import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.captcha.client.AbstractImageCaptcha;
import pt.gov.dgarq.roda.wui.common.captcha.client.DefaultImageCaptcha;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;

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
                  History.newItem(VerifyEmail.getInstance().getHistoryPath() + "." + userdata.getValue().getName());
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
   * pt.gov.dgarq.roda.office.common.client.HistoryResolver#getHistoryPath()
   */
  public String getHistoryPath() {
    return getHistoryToken();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * pt.gov.dgarq.roda.office.common.client.HistoryResolver#getHistoryToken()
   */
  public String getHistoryToken() {
    return "register";
  }

  public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.length == 0) {
      if (initialized) {
        captcha.refresh();
        userdata.clear();
      }
      init();
      callback.onSuccess(layout);
    } else {
      History.newItem(getHistoryPath());
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
