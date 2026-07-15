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
package org.roda.wui.client.management.members;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.management.members.tabs.RODAMemberDetailsPanel;
import org.roda.wui.common.client.HistoryResolver;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
      return List.of(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "profile";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<User> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel content;

  public Profile(User user) {
    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withObject(user);
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getProfileBreadcrumbs(user));
    navigationToolbar.build();

    actionsToolbar.setLabel(messages.preferencesUserDataTitle());
    actionsToolbar.build();

    title.setText(messages.preferencesUserDataTitle());
    title.setIconClass("User");
    title.addStyleName("mb-16");

    content.add(new RODAMemberDetailsPanel(user));

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, Profile> {
  }
}
