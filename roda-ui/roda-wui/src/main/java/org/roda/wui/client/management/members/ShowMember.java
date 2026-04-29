package org.roda.wui.client.management.members;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.browse.tabs.RODAMemberTabs;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.RODAMemberActionsToolbar;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
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
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.tools.StringUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowMember extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String username = historyTokens.get(0);
        Services services = new Services("Get Member", "get");
        services.membersResource(s -> s.getMember(username)).whenComplete((member, error) -> {
          if (member != null) {
            ShowMember showMember = new ShowMember(member);
            callback.onSuccess(showMember);
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
      return "show_member";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  NavigationToolbar<RODAMember> navigationToolbar;

  @UiField
  RODAMemberActionsToolbar actionsToolbar;

  @UiField
  TitlePanel title;

  @UiField
  RODAMemberTabs browseTab;

  private Map<Actionable.ActionImpact, Runnable> handlers = new HashMap<>();
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };

  public ShowMember(RODAMember member) {
    initWidget(uiBinder.createAndBindUi(this));

    initHandlers(member);

    updateView(member);

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  private void updateView(RODAMember member) {
    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getRODAMemberBreadcrumbs(member));

    if (member.isUser()) {
      actionsToolbar.setLabel(messages.showUserTitle());
      title.setIconClass("User");
    } else {
      actionsToolbar.setLabel(messages.showGroupTitle());
      title.setIconClass("Group");
    }

    actionsToolbar.setObjectAndBuild(member, null, handler);

    title.setText(StringUtils.isBlank(member.getFullName()) ? member.getId() : member.getFullName());

    browseTab.init(member, handler);
  }

  private void initHandlers(RODAMember member) {
    handlers.put(Actionable.ActionImpact.DESTROYED,
      () -> HistoryUtils.newHistory(MemberManagement.RESOLVER.getHistoryPath()));

    // Change this to use the DOM Swap refresh method instead of HistoryUtils
    handlers.put(Actionable.ActionImpact.UPDATED, () -> refreshToolbar(member.getId(), member.isUser()));
  }

  private void refreshToolbar(String id, boolean isUser) {
    Services services = new Services("Get Updated Member", "get");

    if (isUser) {
      services.membersResource(s -> s.getUser(id)).whenComplete((updatedUser, error) -> {
        if (updatedUser != null) {
          updateShowUserUI(updatedUser);
        }
      });
    } else {
      services.membersResource(s -> s.getGroup(id)).whenComplete((updatedGroup, error) -> {
        if (updatedGroup != null) {
          updateShowUserUI(updatedGroup);
        }
      });
    }
  }

  private void updateShowUserUI(RODAMember updatedMember) {
    title
      .setText(StringUtils.isBlank(updatedMember.getFullName()) ? updatedMember.getId() : updatedMember.getFullName());

    actionsToolbar.setObjectAndBuild(updatedMember, null, handler);

    browseTab.init(updatedMember, handler);
  }

  interface MyUiBinder extends UiBinder<Widget, ShowMember> {
  }
}