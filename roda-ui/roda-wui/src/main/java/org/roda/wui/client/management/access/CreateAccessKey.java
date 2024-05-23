/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.access;

import java.util.List;

import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.generics.CreateAccessKeyRequest;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.AccessKeyDialogs;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.EditUser;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

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
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateAccessKey extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Get User", "get");
        services.membersResource(s -> s.getUser(historyTokens.get(0))).whenComplete((user, error) -> {
          if (user != null) {
            CreateAccessKey createAccessKey = new CreateAccessKey(user);
            callback.onSuccess(createAccessKey);
          }
        });
      } else {
        CreateAccessKey createAccessKey = new CreateAccessKey(null);
        callback.onSuccess(createAccessKey);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public String getHistoryToken() {
      return "create_access_key";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(MemberManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateAccessKey> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private AccessKey accessKey;
  private User user;

  @UiField
  TitlePanel titlePanel;

  @UiField
  Button buttonSave;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  AccessKeyDataPanel accessKeyDataPanel;

  public CreateAccessKey(User user) {
    this.user = user;
    this.accessKey = new AccessKey();
    this.accessKey.setUserName(user.getName());
    this.accessKeyDataPanel = new AccessKeyDataPanel(accessKey, false);
    this.accessKeyDataPanel.setAccessKey(accessKey);

    initWidget(uiBinder.createAndBindUi(this));
    titlePanel.setText(user.getName());
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (accessKeyDataPanel.isValid()) {
      AccessKey accessKeyUpdated = accessKeyDataPanel.getAccessKey();
      accessKey.setName(accessKeyUpdated.getName());
      accessKey.setExpirationDate(accessKeyUpdated.getExpirationDate());
      Services services = new Services("Create access key", "create");
      CreateAccessKeyRequest createAccessKeyRequest = new CreateAccessKeyRequest(this.accessKey.getName(), this.accessKey.getExpirationDate(), this.accessKey.getUserName());
      services.membersResource(s -> s.createAccessKey(createAccessKeyRequest)).whenComplete((accessKey, error) -> {
        if (accessKey != null) {
          AccessKeyDialogs.showAccessKeyDialog(messages.accessKeyLabel(), accessKey, new NoAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
              HistoryUtils.newHistory(EditUser.RESOLVER, accessKey.getUserName());
            }
          });
        }
      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(EditUser.RESOLVER, accessKey.getUserName());
  }
}
