/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management;

import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.management.access.AccessKeyTablePanel;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowUser extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String username = historyTokens.get(0);
        Services services = new Services("Get User", "get");
        services.membersResource(s -> s.getUser(username)).whenComplete((user, error) -> {
          if (user != null) {
            ShowUser showUser = new ShowUser(user);
            callback.onSuccess(showUser);
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
      return "show_user";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final User user;
  @UiField
  Label userNameValue;
  @UiField
  Label fullnameValue;
  @UiField
  Label emailValue;
  @UiField
  HTML stateValue;
  @UiField
  FlowPanel extraValue;
  @UiField
  FlowPanel permissionList;
  @UiField
  FlowPanel groupList;
  @UiField
  FlowPanel accessKeyTablePanel;
  @UiField
  SimplePanel actionsSidebar;
  @UiField
  FlowPanel contentFlowPanel;
  @UiField
  FlowPanel sidebarFlowPanel;

  public ShowUser(User user) {
    this.user = user;
    initWidget(uiBinder.createAndBindUi(this));
    initElements();
  }

  private void initElements() {
    userNameValue.setText(user.getName());
    fullnameValue.setText(user.getFullName());
    emailValue.setText(user.getEmail());
    stateValue.setHTML(HtmlSnippetUtils.getUserStateHtml(user));

    if (!user.getExtra().isEmpty()) {
      HtmlSnippetUtils.createExtraShow(extraValue, user.getExtra(), false);
    }

    // Groups
    if (user.getGroups().isEmpty()) {
      groupList.add(new Label(messages.showUserEmptyGroupList()));
    } else {
      for (String group : user.getGroups()) {
        groupList.add(createListItem(group));
      }
    }

    // Permissions
    buildPermissionList();

    accessKeyTablePanel.add(new AccessKeyTablePanel(user.getId()));

    // Sidebar
    RODAMemberActions rodaMemberActions = RODAMemberActions.get();
    ActionableWidgetBuilder<RODAMember> actionableWidgetBuilder = new ActionableWidgetBuilder<>(rodaMemberActions)
      .withBackButton().withActionCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
        @Override
        public void onSuccess(Actionable.ActionImpact result) {
          if (result.equals(Actionable.ActionImpact.DESTROYED)) {
            HistoryUtils.newHistory(MemberManagement.RESOLVER);
          }
        }
      });

    SidebarUtils.toggleSidebar(contentFlowPanel, sidebarFlowPanel, rodaMemberActions.hasAnyRoles());
    actionsSidebar.setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(this.user)));
  }

  private void buildPermissionList() {
    Set<String> allUserRoles = user.getAllRoles();

    if (allUserRoles.isEmpty()) {
      permissionList.add(new Label(messages.showUserEmptyPermissions()));
    } else {
      List<String> roles = ConfigurationManager.getStringList("ui.role");
      for (String role : roles) {
        String description;
        try {
          description = messages.role(role);
        } catch (MissingResourceException e) {
          description = role + " (needs translation)";
        }
        if (allUserRoles.contains(role)) {
          permissionList.add(createListItem(description));
        }
      }
    }
  }

  private FlowPanel createListItem(String item) {
    FlowPanel panel = new FlowPanel();
    InlineHTML bullet = new InlineHTML("&#8226;");
    InlineHTML value = new InlineHTML(item);

    bullet.addStyleName("bullet");
    panel.add(bullet);
    panel.add(value);

    return panel;
  }

  interface MyUiBinder extends UiBinder<Widget, ShowUser> {
  }
}
