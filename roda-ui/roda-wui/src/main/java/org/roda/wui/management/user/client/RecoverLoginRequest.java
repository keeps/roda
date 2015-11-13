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

import org.roda.core.common.NoSuchUserException;
import org.roda.core.data.v2.RodaUser;
import org.roda.wui.client.about.About;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.captcha.client.AbstractImageCaptcha;
import org.roda.wui.common.captcha.client.DefaultImageCaptcha;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class RecoverLoginRequest implements HistoryResolver {

  private static RecoverLoginRequest instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static RecoverLoginRequest getInstance() {
    if (instance == null) {
      instance = new RecoverLoginRequest();
    }
    return instance;
  }

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean initialized;

  private VerticalPanel layout;

  private HorizontalPanel usernameOrEmailLayout;

  private Label usernameOrEmailLabel;

  private TextBox usernameOrEmailBox;

  private Label captchaLabel;

  private AbstractImageCaptcha captcha;

  private WUIButton submit;

  private RecoverLoginRequest() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      layout = new VerticalPanel();
      usernameOrEmailLayout = new HorizontalPanel();
      usernameOrEmailLabel = new Label(constants.recoverLoginUsernameOrEmail());
      usernameOrEmailBox = new TextBox();
      usernameOrEmailLayout.add(usernameOrEmailLabel);
      usernameOrEmailLayout.add(usernameOrEmailBox);
      captchaLabel = new Label(constants.recoverLoginCaptchaTitle());
      captcha = new DefaultImageCaptcha();
      submit = new WUIButton(constants.recoverLoginSubmit(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);

      submit.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          UserManagementService.Util.getInstance().requestPassordReset(usernameOrEmailBox.getText(),
            captcha.getResponse(), new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {
              if (caught instanceof NoSuchUserException) {
                Window.alert(constants.recoverLoginNoSuchUser());
              } else {
                logger.error("Error requesting password reset", caught);
              }
            }

            public void onSuccess(Boolean captchaSuccess) {
              if (captchaSuccess.booleanValue()) {
                Window.alert(constants.recoverLoginSuccess());
                Tools.newHistory(About.RESOLVER);
              } else {
                Window.alert(constants.recoverLoginCaptchaFailed());
                captcha.refresh();
              }
            }

          });
        }

      });

      submit.setEnabled(false);

      usernameOrEmailBox.addKeyboardListener(new KeyboardListener() {

        public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        }

        public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        }

        public void onKeyUp(Widget sender, char keyCode, int modifiers) {
          submit.setEnabled(usernameOrEmailBox.getText().length() > 0);

        }

      });

      layout.add(usernameOrEmailLayout);
      layout.add(captchaLabel);
      layout.add(captcha.getWidget());
      layout.add(submit);

      layout.addStyleName("wui-recoverLogin");
      usernameOrEmailLayout.addStyleName("usernameOrEmail-layout");
      usernameOrEmailLabel.addStyleName("usernameOrEmail-label");
      usernameOrEmailBox.addStyleName("usernameOrEmail-box");
      captchaLabel.addStyleName("captcha-title");
      submit.addStyleName("submit");
    } else {
      captcha.refresh();
    }
  }

  public List<String> getHistoryPath() {
    return Arrays.asList(getHistoryToken());
  }

  public String getHistoryToken() {
    return "recoverLogin";
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

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      init();
      callback.onSuccess(layout);
    } else {
      Tools.newHistory(this);
      callback.onSuccess(null);
    }
  }

}
