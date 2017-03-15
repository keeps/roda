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

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.v2.user.Group;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
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
public class CreateGroup extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateGroup createGroup = new CreateGroup(new Group());
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

  interface MyUiBinder extends UiBinder<Widget, CreateGroup> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private Group group;

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  GroupDataPanel groupDataPanel;

  /**
   * Create a new panel to create a group
   *
   * @param group
   *          the group to create
   */
  public CreateGroup(Group group) {
    this.group = group;

    this.groupDataPanel = new GroupDataPanel(true, false);
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
    if (groupDataPanel.isValid()) {
      group = groupDataPanel.getGroup();

      UserManagementService.Util.getInstance().createGroup(group, new AsyncCallback<Void>() {

        @Override
        public void onSuccess(Void result) {
          HistoryUtils.newHistory(MemberManagement.RESOLVER);
        }

        @Override
        public void onFailure(Throwable caught) {
          errorMessage(caught);
        }
      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(MemberManagement.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof AlreadyExistsException) {
      Toast.showError(messages.createGroupAlreadyExists(group.getName()));
    } else {
      Toast.showError(messages.createGroupFailure(caught.getMessage()));
    }
  }
}
