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

import org.roda.core.data.common.EmailAlreadyExistsException;
import org.roda.core.data.common.NoSuchRODAObjectException;
import org.roda.core.data.common.NoSuchUserException;
import org.roda.wui.client.about.About;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
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
public class VerifyEmail implements HistoryResolver {

  private static VerifyEmail instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static VerifyEmail getInstance() {
    if (instance == null) {
      instance = new VerifyEmail();
    }
    return instance;
  }

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean initialized;

  private VerticalPanel layout;

  private Grid inputvalues;

  private Label userInputLabel;

  private Label tokenInputLabel;

  private TextBox userInputBox;

  private TextBox tokenInputBox;

  private HorizontalPanel bottomButtons;

  private WUIButton verifyEmail;

  private WUIButton resendEmail;

  private WUIButton changeEmail;

  private VerifyEmail() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;

      layout = new VerticalPanel();
      inputvalues = new Grid(2, 2);
      userInputLabel = new Label(constants.verifyEmailUsername());
      tokenInputLabel = new Label(constants.verifyEmailToken());
      userInputBox = new TextBox();
      tokenInputBox = new TextBox();
      bottomButtons = new HorizontalPanel();
      verifyEmail = new WUIButton(constants.verifyEmailVerify(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);
      resendEmail = new WUIButton(constants.verifyEmailResend(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_UP);

      changeEmail = new WUIButton(constants.verifyEmailChange(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_UP);

      verifyEmail.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          UserManagementService.Util.getInstance().verifyemail(userInputBox.getText(), tokenInputBox.getText(),
            new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {
              if (caught instanceof NoSuchRODAObjectException) {
                Window.alert(constants.verifyEmailNoSuchUser());
                userInputBox.setFocus(true);
              } else {
                logger.error("Error verifying token", caught);
              }
            }

            public void onSuccess(Boolean verified) {
              if (verified.booleanValue()) {
                Window.alert(constants.verifyEmailSuccess());
                Tools.newHistory(About.RESOLVER);
              } else {
                Window.alert(constants.verifyEmailWrongToken());
                tokenInputBox.setFocus(true);
              }

            }

          });
        }

      });

      resendEmail.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          UserManagementService.Util.getInstance().resendEmailVerification(userInputBox.getText(),
            new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {
              logger.error("Error resending " + "verification email", caught);
            }

            public void onSuccess(Boolean successful) {
              if (successful.booleanValue()) {
                Window.alert(constants.verifyEmailResendSuccess());
              } else {
                Window.alert(constants.verifyEmailResendFailure());
              }

            }

          });
        }

      });

      changeEmail.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          String email = "";
          do {
            email = Window.prompt(constants.verifyEmailChangePrompt(), email);
          } while (email != null && !email
            .matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)"));

          if (email != null) {
            UserManagementService.Util.getInstance().changeUnverifiedEmail(userInputBox.getText(), email,
              new AsyncCallback<Boolean>() {

              public void onFailure(Throwable caught) {
                if (caught instanceof NoSuchUserException) {
                  Window.alert(constants.verifyEmailNoSuchUser());
                } else if (caught instanceof EmailAlreadyExistsException) {
                  Window.alert(constants.verifyEmailAlreadyExists());
                } else {
                  logger.error("Error changing unverified email", caught);
                }
              }

              public void onSuccess(Boolean success) {
                if (success.booleanValue()) {
                  Window.alert(constants.verifyEmailChangeSuccess());
                  tokenInputBox.setText("");
                  tokenInputBox.setFocus(true);
                } else {
                  Window.alert(constants.verifyEmailChangeFailure());
                  userInputBox.setFocus(true);
                }

              }

            });
          }

        }

      });

      updateVisibles();
      KeyboardListener updateListener = new KeyboardListener() {

        public void onKeyDown(Widget sender, char keyCode, int modifiers) {

        }

        public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        }

        public void onKeyUp(Widget sender, char keyCode, int modifiers) {
          updateVisibles();
        }

      };

      userInputBox.addKeyboardListener(updateListener);
      tokenInputBox.addKeyboardListener(updateListener);

      layout.add(inputvalues);
      layout.add(bottomButtons);

      inputvalues.setWidget(0, 0, userInputLabel);
      inputvalues.setWidget(0, 1, userInputBox);
      inputvalues.setWidget(1, 0, tokenInputLabel);
      inputvalues.setWidget(1, 1, tokenInputBox);

      bottomButtons.add(verifyEmail);
      bottomButtons.add(resendEmail);
      bottomButtons.add(changeEmail);

      bottomButtons.setCellWidth(verifyEmail, "100%");

      layout.addStyleName("wui-verifyemail");
      inputvalues.addStyleName("verifyemail-input");
      userInputLabel.addStyleName("verifyemail-input-user-label");
      userInputBox.addStyleName("verifyemail-input-user-box");
      tokenInputLabel.addStyleName("verifyemail-input-token-label");
      tokenInputBox.addStyleName("verifyemail-input-token-box");

      bottomButtons.addStyleName("verifyemail-buttons");
      verifyEmail.addStyleName("verifyemail-button-verify");
      resendEmail.addStyleName("verifyemail-button-resend");
      changeEmail.addStyleName("verifyemail-button-change");
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
    return "verifyemail";
  }

  public void isCurrentUserPermitted(final AsyncCallback<Boolean> callback) {
    callback.onSuccess(Boolean.TRUE);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      init();
      callback.onSuccess(layout);
    } else if (historyTokens.size() == 1) {
      init();
      userInputBox.setText(historyTokens.get(0));
      updateVisibles();
      callback.onSuccess(layout);
    } else if (historyTokens.size() == 2) {
      init();
      userInputBox.setText(historyTokens.get(0));
      tokenInputBox.setText(historyTokens.get(1));
      updateVisibles();
      callback.onSuccess(layout);
    } else {
      Tools.newHistory(this, historyTokens.get(0), historyTokens.get(1));
      callback.onSuccess(null);
    }
  }

  /**
   * Update the visible and enabled state of buttons and other components
   * 
   */
  private void updateVisibles() {
    boolean userEmpty = userInputBox.getText().length() == 0;
    boolean tokenEmpty = tokenInputBox.getText().length() == 0;

    verifyEmail.setEnabled(!userEmpty && !tokenEmpty);
    resendEmail.setEnabled(!userEmpty);
    changeEmail.setEnabled(!userEmpty);
  }

}
