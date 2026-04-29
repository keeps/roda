package org.roda.wui.client.management.members;

import java.util.List;

import org.roda.core.data.v2.user.Group;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.management.members.data.panels.GroupDataPanel;
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
public class EditGroup extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String groupId = historyTokens.get(0);
        Services services = new Services("Get Group", "get");
        services.membersResource(s -> s.getGroup(groupId)).whenComplete((group, error) -> {
          if (group != null) {
            EditGroup editGroup = new EditGroup(group);
            callback.onSuccess(editGroup);
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
      return "edit_group";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<Group> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel groupDataPanel;

  public EditGroup(Group group) {
    initWidget(uiBinder.createAndBindUi(this));

    GroupDataPanel groupDataForm = new GroupDataPanel(true);
    groupDataForm.setGroup(group);

    // Bind the Save Action logic
    groupDataForm.setSaveHandler(() -> {
      Group updatedGroup = groupDataForm.getValue();
      Services services = new Services("Update group", "update");
      services.membersResource(s -> s.updateGroup(updatedGroup)).whenComplete((updated, error) -> {
        if (error == null) {
          Toast.showInfo(messages.groups(), messages.groupSuccessfullyUpdated());
          HistoryUtils.newHistory(ShowMember.RESOLVER, updated.getUUID());
        } else {
          Toast.showError(messages.groups(), messages.failedToUpdateGroup());
          HistoryUtils.newHistory(ShowMember.RESOLVER, group.getUUID());
        }
      });
    });

    // Bind the Cancel Action logic
    groupDataForm.setCancelHandler(() -> HistoryUtils.newHistory(ShowMember.RESOLVER, group.getUUID()));

    groupDataPanel.add(groupDataForm);

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getEditMemberBreadcrumbs(group));

    actionsToolbar.setLabel(messages.showGroupTitle());
    actionsToolbar.build();

    title.setText(group.getFullName());
    title.setIconClass("Group");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, EditGroup> {
  }
}