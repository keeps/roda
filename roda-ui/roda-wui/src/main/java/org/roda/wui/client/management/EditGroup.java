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

import java.util.List;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.user.Group;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class EditGroup extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String groupname = historyTokens.get(0);
        Services services = new Services("Get Group", "get");
        services.membersResource(s -> s.getGroup(groupname)).whenComplete((group, error) -> {
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

  interface MyUiBinder extends UiBinder<Widget, EditGroup> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private Group group;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  GroupDataPanel groupDataPanel;

  /**
   * Create a new panel to edit a group
   *
   * @param group
   *          the group to edit
   */
  public EditGroup(Group group) {
    this.group = group;

    this.groupDataPanel = new GroupDataPanel(true, true);
    this.groupDataPanel.setGroup(group);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (groupDataPanel.isChanged()) {
      if (groupDataPanel.isValid()) {
        group = groupDataPanel.getGroup();
        Services services = new Services("Update group", "update");
        services.membersResource(s -> s.updateGroup(group)).whenComplete((res, error) -> {
          if (error == null) {
            HistoryUtils.newHistory(MemberManagement.RESOLVER);
          } else {
            errorMessage(error);
          }
        });
      }
    } else {
      HistoryUtils.newHistory(MemberManagement.RESOLVER);
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    Services services = new Services("Delete group", "delete");
    services.membersResource(s -> s.deleteGroup(group.getId())).whenComplete((res, error) -> {
      if (error == null) {
        HistoryUtils.newHistory(MemberManagement.RESOLVER);
      } else {
        errorMessage(error);
      }
    });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowGroup.RESOLVER, group.getId());
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editGroupNotFound(group.getName()));
      cancel();
    } else {
      Toast.showError(messages.editGroupFailure(EditGroup.this.group.getName(), caught.getMessage()));
    }
  }
}
