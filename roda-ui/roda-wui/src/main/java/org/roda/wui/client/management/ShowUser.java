package org.roda.wui.client.management;

import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.bundle.UserExtraBundle;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
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
        UserManagementService.Util.getInstance().retrieveUser(username, new AsyncCallback<User>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(User user) {
            ShowUser showUser = new ShowUser(user);
            callback.onSuccess(showUser);
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

  interface MyUiBinder extends UiBinder<Widget, ShowUser> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private ActionableWidgetBuilder<RODAMember> actionableWidgetBuilder;
  private final User user;

  private UserExtraBundle userExtraBundle = null;

  @UiField
  Label userNameValue, fullnameValue, emailValue;

  @UiField
  HTML stateValue;

  @UiField
  FlowPanel extraValue, permissionList, groupList;

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

    // Sidebar
    RODAMemberActions rodaMemberActions = RODAMemberActions.get();
    actionableWidgetBuilder = new ActionableWidgetBuilder<>(rodaMemberActions).withBackButton()
      .withActionCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }
}
