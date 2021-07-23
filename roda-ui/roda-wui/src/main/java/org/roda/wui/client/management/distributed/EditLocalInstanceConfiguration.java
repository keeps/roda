package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.server.browse.BrowserServiceImpl;

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
public class EditLocalInstanceConfiguration extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().retrieveLocalInstance(new NoAsyncCallback<LocalInstance>() {
        @Override
        public void onSuccess(LocalInstance localInstance) {
          EditLocalInstanceConfiguration editLocalInstanceConfiguration = new EditLocalInstanceConfiguration(
            localInstance);
          callback.onSuccess(editLocalInstanceConfiguration);
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {EditLocalInstanceConfiguration.RESOLVER}, false,
        callback);
    }

    @Override
    public String getHistoryToken() {
      return "edit_instance_configuration";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(LocalInstanceManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditLocalInstanceConfiguration> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private LocalInstance localInstance;

  @UiField(provided = true)
  LocalInstanceConfigurationDataPanel localInstanceConfigurationDataPanel;

  public EditLocalInstanceConfiguration(LocalInstance localInstance) {
    this.localInstance = localInstance;
    this.localInstanceConfigurationDataPanel = new LocalInstanceConfigurationDataPanel(this.localInstance, false);
    this.localInstanceConfigurationDataPanel.setLocalInstanceConfiguration(this.localInstance);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (localInstanceConfigurationDataPanel.isValid()) {
      LocalInstance localInstanceReturned = localInstanceConfigurationDataPanel.getLocalInstance();
      BrowserServiceImpl.Util.getInstance().updateLocalInstanceConfiguration(localInstanceReturned,
        new NoAsyncCallback<DistributedInstance>() {
          @Override
          public void onSuccess(DistributedInstance distributedInstance) {
            HistoryUtils.newHistory(LocalInstanceManagement.RESOLVER);
          }
        });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(LocalInstanceManagement.RESOLVER);
  }
}
