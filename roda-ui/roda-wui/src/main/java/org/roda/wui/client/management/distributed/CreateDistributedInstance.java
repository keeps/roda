/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.AccessKeyDialogs;
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
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateDistributedInstance extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      CreateDistributedInstance createDistributedInstance = new CreateDistributedInstance();
      callback.onSuccess(createDistributedInstance);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DistributedInstancesManagement.RESOLVER}, false,
        callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DistributedInstancesManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create_distributed_instance";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  Button buttonSave;
  @UiField
  Button buttonCancel;
  @UiField(provided = true)
  DistributedInstanceDataPanel distributedInstanceDataPanel;
  private DistributedInstance distributedInstance;

  public CreateDistributedInstance() {
    this.distributedInstance = new DistributedInstance();
    this.distributedInstanceDataPanel = new DistributedInstanceDataPanel(distributedInstance, false);
    this.distributedInstanceDataPanel.setDistributedInstance(distributedInstance);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (distributedInstanceDataPanel.isValid()) {
      distributedInstance = distributedInstanceDataPanel.getDistributedInstance();
      BrowserService.Util.getInstance().createDistributedInstance(distributedInstance,
        new NoAsyncCallback<DistributedInstance>() {
          @Override
          public void onSuccess(DistributedInstance distributedInstance) {
            BrowserService.Util.getInstance().retrieveAccessKey(distributedInstance.getAccessKeyId(),
              new NoAsyncCallback<AccessKey>() {
                @Override
                public void onSuccess(AccessKey accessKey) {
                  HistoryUtils.newHistory(ShowDistributedInstance.RESOLVER, distributedInstance.getId());
                  AccessKeyDialogs.showAccessKeyDialog(messages.accessKeyLabel(), accessKey,
                    new NoAsyncCallback<Boolean>() {
                      @Override
                      public void onSuccess(Boolean result) {
                        Toast.showInfo(messages.accessKeyLabel(), messages.accessKeySuccessfullyRegenerated());
                      }
                    });
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
    HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
  }

  interface MyUiBinder extends UiBinder<Widget, CreateDistributedInstance> {
  }
}
