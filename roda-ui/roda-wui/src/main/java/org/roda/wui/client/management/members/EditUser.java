package org.roda.wui.client.management.members;

import java.util.List;

import com.google.gwt.user.client.ui.Button;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.UpdateUserRequest;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.members.data.panels.UserDataPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class EditUser extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String username = historyTokens.get(0);
        Services services = new Services("Get User", "get");
        services.membersResource(s -> s.getUser(username)).whenComplete((user, error) -> {
          if (user != null) {
            EditUser editUser = new EditUser(user);
            callback.onSuccess(editUser);
          } else if (error != null) {
            callback.onFailure(error);
          }
        });
      } else {
        HistoryUtils.newHistory(MemberManagement.RESOLVER);
        callback.onSuccess(null);
      }
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
      return "edit_user";
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
  @UiField
  FlowPanel actions;

  public EditUser(User user) {
    initWidget(uiBinder.createAndBindUi(this));

    // 1. Create the panel and keep a reference
    UserDataPanel dataPanel = new UserDataPanel(true);
    dataPanel.setUser(user);
    dataPanel.setSaveHandler(() ->{
      UpdateUserRequest request = new UpdateUserRequest();
      request.setUser(dataPanel.getValue());
      request.setPassword(null);
      request.setValues(dataPanel.getUserExtra());

      Services services = new Services("Update user", "update");
      services.membersResource(s -> s.updateUser(request)).whenComplete((updated, error) -> {
        if (error == null) {
          Toast.showInfo(messages.groups(), messages.userSuccessfullyUpdated());
          HistoryUtils.newHistory(ShowMember.RESOLVER, updated.getUUID());
        } else {
          Toast.showError(messages.groups(), messages.failedToUpdateUser());
          HistoryUtils.newHistory(ShowMember.RESOLVER, updated.getUUID());
        }
      });
    });

    dataPanel.setCancelHandler(() -> HistoryUtils.newHistory(ShowMember.RESOLVER, user.getUUID()));

    userDataPanel.add(dataPanel);

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getEditMemberBreadcrumbs(user));

    actionsToolbar.setLabel(messages.showUserTitle());

    actionsToolbar.build();

    title.setText(user.getFullName());
    title.setIconClass("User");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, EditUser> {
  }
}
