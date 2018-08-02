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

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class Profile extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(User user) {
          Profile preferences = new Profile(new User(user));
          callback.onSuccess(preferences);
        }
      });

    }

    @Override
    public void isCurrentUserPermitted(final AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(User user) {
          callback.onSuccess(!user.isGuest());
        }
      });
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "profile";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, Profile> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final User user;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  UserDataPanel userDataPanel;

  /**
   * Create a new panel to edit a user
   *
   * @param user
   *          the user to edit
   */
  public Profile(User user) {
    this.user = user;

    this.userDataPanel = new UserDataPanel(true, true, false, false);
    this.userDataPanel.setUser(user);

    initWidget(uiBinder.createAndBindUi(this));

    userDataPanel.setUsernameReadOnly(true);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (userDataPanel.isChanged()) {
      if (userDataPanel.isValid()) {
        final User user = userDataPanel.getUser();
        final String password = userDataPanel.getPassword();

        UserManagementService.Util.getInstance().updateMyUser(user, password, userDataPanel.getExtra(),
          new AsyncCallback<User>() {

            @Override
            public void onFailure(Throwable caught) {
              errorMessage(caught);
            }

            @Override
            public void onSuccess(User updatedUser) {
              UserLogin.getInstance().updateLoggedUser(updatedUser);
              HistoryUtils.newHistory(Welcome.RESOLVER);
            }
          });
      }
    } else {
      HistoryUtils.newHistory(Welcome.RESOLVER);
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(Welcome.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editUserNotFound(user.getName()));
      cancel();
    } else if (caught instanceof AlreadyExistsException) {
      Toast.showError(messages.editUserEmailAlreadyExists(user.getEmail()));
    } else {
      Toast.showError(messages.editUserFailure(Profile.this.user.getName(), caught.getMessage()));
    }
  }
}
