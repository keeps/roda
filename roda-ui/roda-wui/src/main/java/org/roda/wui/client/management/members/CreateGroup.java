package org.roda.wui.client.management.members;

import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.requests.CreateGroupRequest;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.forms.GenericDataPanel;
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

public class CreateGroup extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateGroup createGroup = new CreateGroup();
      callback.onSuccess(createGroup);
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
      return "create_group";
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

  public CreateGroup() {
    initWidget(uiBinder.createAndBindUi(this));

    // 1. Create the panel and keep a reference
    GroupDataPanel dataPanel = new GroupDataPanel(false);
    dataPanel.setGroup(new Group());
    // Bind the Save Action logic
    dataPanel.setSaveHandler(() -> {
      Group group = dataPanel.getValue();
      CreateGroupRequest request = new CreateGroupRequest();
      request.setName(group.getName());
      request.setFullName(group.getFullName());
      request.setDirectRoles(new HashSet<>());
      Services services = new Services("Create group", "create");
      services.membersResource(s -> s.createGroup(request)).whenComplete((created, error) -> {
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

    groupDataPanel.add(dataPanel);

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateGroupBreadcrumbs());

    actionsToolbar.setLabel(messages.showGroupTitle());

    // 3. Pass the shared object
    actionsToolbar.build();

    title.setText(messages.createGroupTitle());
    title.setIconClass("Group");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, CreateGroup> {
  }
}
