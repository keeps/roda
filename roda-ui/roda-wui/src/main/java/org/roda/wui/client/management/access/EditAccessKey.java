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
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class EditAccessKey extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Get access key", "get");
        services.membersResource(s -> s.getAccessKey(historyTokens.get(0))).whenComplete((accessKey, error) -> {
          if (accessKey != null) {
            EditAccessKey editAccessKey = new EditAccessKey(accessKey);
            callback.onSuccess(editAccessKey);
          }
        });
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public String getHistoryToken() {
      return "edit_access_key";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(MemberManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  TitlePanel titlePanel;
  @UiField(provided = true)
  AccessKeyDataPanel accessKeyDataPanel;
  private AccessKey accessKey;

  public EditAccessKey(AccessKey accessKey) {
    this.accessKey = accessKey;
    this.accessKeyDataPanel = new AccessKeyDataPanel(accessKey, true);
    this.accessKeyDataPanel.setAccessKey(accessKey);

    initWidget(uiBinder.createAndBindUi(this));
    titlePanel.setText(accessKey.getUserName());
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonUpdate")
  void buttonUpdateHandler(ClickEvent e) {
    if (accessKeyDataPanel.isValid()) {
      AccessKey accessKeyUpdated = accessKeyDataPanel.getAccessKey();
      accessKey.setName(accessKeyUpdated.getName());
      accessKey.setExpirationDate(accessKeyUpdated.getExpirationDate());
      Services services = new Services("Update access key", "update");
      services.membersResource(s -> s.updateAccessKey(this.accessKey)).whenComplete((accessKey, error) -> {
        if (accessKey != null) {
          HistoryUtils.newHistory(EditUser.RESOLVER, accessKey.getUserName());
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

  interface MyUiBinder extends UiBinder<Widget, EditAccessKey> {
  }
}
