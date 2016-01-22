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
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
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
public class Preferences implements HistoryResolver {
  private static Preferences instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Preferences getInstance() {
    if (instance == null) {
      instance = new Preferences();
    }
    return instance;
  }

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean initialized;

  private VerticalPanel layout;

  private Label userdataTitle;

  private UserDataPanel userdata;

  private WUIButton submit;

  private Preferences() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;

      layout = new VerticalPanel();
      userdataTitle = new Label(constants.preferencesUserDataTitle());
      userdata = new UserDataPanel(true, false);
      submit = new WUIButton(constants.preferencesSubmit(), WUIButton.Left.ROUND, WUIButton.Right.REC);
      submit.setEnabled(false);

      userdata.addValueChangeHandler(new ValueChangeHandler<User>() {

        @Override
        public void onValueChange(ValueChangeEvent<User> event) {
          submit.setEnabled(userdata.isValid());
        }
      });

      submit.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          UserManagementService.Util.getInstance().editMyUser(userdata.getValue(), userdata.getPassword(),
            new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {
              if (caught instanceof EmailAlreadyExistsException) {
                Window.alert(constants.preferencesEmailAlreadyExists());
              } else {
                logger.error("Error saving preferences", caught);
              }

            }

            public void onSuccess(Void result) {
              Window.alert(constants.preferencesSubmitSuccess());
              // UserLogin.getInstance()
              // .checkForLoginReset();
            }

          });
        }

      });

      layout.add(userdataTitle);
      layout.add(userdata);
      layout.add(submit);

      update();

      layout.addStyleName("wui-preferences");
      userdataTitle.addStyleName("preferences-title");
      userdata.addStyleName("preferences-userdata");
      submit.addStyleName("preferences-submit");
    } else {
      update();
    }
  }

  private void update() {
    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<RodaUser>() {

      public void onFailure(Throwable caught) {
        logger.error("Error getting authenticated user", caught);
      }

      public void onSuccess(RodaUser user) {
        // FIXME
        // userdata.setUser(user);
      }

    });
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
    return "preferences";
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

  public void isCurrentUserPermitted(final AsyncCallback<Boolean> callback) {
    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<RodaUser>() {

      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      public void onSuccess(RodaUser user) {
        callback.onSuccess(new Boolean(!user.isGuest()));
      }

    });
  }
}
