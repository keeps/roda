package org.roda.wui.client.management.members;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.CreateUserRequest;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.management.members.data.panels.UserDataPanel;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.HashSet;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<User> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel userDataPanel;

  public CreateUser(User user) {
    initWidget(uiBinder.createAndBindUi(this));

    // 1. Create the panel and keep a reference
    UserDataPanel dataPanel = new UserDataPanel(false);
    dataPanel.setUser(user);
    dataPanel.setSaveHandler(() -> {

      CreateUserRequest request = new CreateUserRequest(user.getEmail(), user.getName(), user.getFullName(),
        new HashSet<>(), new HashSet<>(), user.isGuest(), null, dataPanel.getUserExtra());

      Services services = new Services("Create user", "create");
      services.membersResource(s -> s.createUser(request, LocaleInfo.getCurrentLocale().getLocaleName()))
        .whenComplete((created, error) -> {
          if (error == null) {
            Toast.showInfo(messages.groups(), messages.groupSuccessfullyCreated());
            HistoryUtils.newHistory(ShowMember.RESOLVER, created.getUUID());
          } else {
            Toast.showError(messages.groups(), error.getMessage());
            HistoryUtils.newHistory(MemberManagement.RESOLVER);
          }
        });
    });

    // Bind the Cancel Action logic
    dataPanel.setCancelHandler(() -> HistoryUtils.newHistory(MemberManagement.RESOLVER));

    userDataPanel.add(dataPanel);

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateUserBreadcrumbs());

    actionsToolbar.setLabel(messages.showUserTitle());

    // 3. Pass the shared object
    actionsToolbar.setObjectAndBuild(user, null, new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        // Redirect user or show success message if result == ActionImpact.UPDATED
      }
    });

    title.setText(messages.createUserTitle());
    title.setIconClass("User");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, CreateUser> {
  }
}
