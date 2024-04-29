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

import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
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
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowGroup extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String groupname = historyTokens.get(0);
        Services services = new Services("Get Group", "get");
        services.membersResource(s -> s.getGroup(groupname)).whenComplete((group, error) -> {
          if (group != null) {
            ShowGroup showGroup = new ShowGroup(group);
            callback.onSuccess(showGroup);
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
      return "show_group";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowGroup> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private ActionableWidgetBuilder<RODAMember> actionableWidgetBuilder;
  private Group group;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Label fullnameValue, groupNameValue;

  @UiField
  FlowPanel userList, permissionList;
  @UiField
  SimplePanel actionsSidebar;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  public ShowGroup(Group group) {
    this.group = group;
    initWidget(uiBinder.createAndBindUi(this));
    initElements();
  }

  private void initElements() {
    groupNameValue.setText(group.getName());
    fullnameValue.setText(group.getFullName());

    if (group.getUsers().isEmpty()) {
      userList.add(new Label(messages.showGroupEmptyUserList()));
    } else {
      for (String user : group.getUsers()) {
        userList.add(createListItem(user));
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
    actionsSidebar.setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(this.group)));
  }

  private void buildPermissionList() {
    Set<String> allGroupRoles = group.getAllRoles();

    if (allGroupRoles.isEmpty()) {
      permissionList.add(new Label(messages.showGroupEmptyPermissions()));
    } else {
      List<String> roles = ConfigurationManager.getStringList("ui.role");
      for (String role : roles) {
        String description;
        try {
          description = messages.role(role);
        } catch (MissingResourceException e) {
          description = role + " (needs translation)";
        }
        if (allGroupRoles.contains(role)) {
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
