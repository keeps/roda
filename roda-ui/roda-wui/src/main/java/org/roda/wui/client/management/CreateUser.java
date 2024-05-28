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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.user.CreateUserRequest;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.List;

/**
 * @author Luis Faria
 *
 */
public class CreateUser extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateUser createUser = new CreateUser(new User());
      callback.onSuccess(createUser);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(MemberManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create_user";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateUser> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private User user;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  CreateUserPanel userDataPanel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public CreateUser(User user) {
    this.user = user;

    this.userDataPanel = new CreateUserPanel(true, false, true);
    this.userDataPanel.setUser(user);
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (userDataPanel.isValid()) {
      user = userDataPanel.getUser();
      Services services = new Services("Create RODA user", "create");
      CreateUserRequest userOperations = new CreateUserRequest(user, null, userDataPanel.getUserExtra());
      services.membersResource(s -> s.createUser(userOperations,  LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((createdUser, error) -> {
        if (createdUser != null) {
          HistoryUtils.newHistory(MemberManagement.RESOLVER);
        } else if (error != null) {
          errorMessage(error);
        }
      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(MemberManagement.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof EmailAlreadyExistsException) {
      Toast.showError(messages.createUserEmailAlreadyExists(user.getEmail()));
    } else if (caught instanceof UserAlreadyExistsException) {
      Toast.showError(messages.createUserAlreadyExists(user.getId()));
    } else {
      Toast.showError(messages.createUserFailure(caught.getMessage()));
    }
  }

}
