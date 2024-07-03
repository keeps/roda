/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.services.DistributedInstancesRestService;
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
public class EditLocalInstanceConfiguration extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      Services services = new Services("Get local instance", "get");
      services.distributedInstanceResource(DistributedInstancesRestService::getLocalInstance)
        .whenComplete((localInstance, error) -> {
          if (!localInstance.equals(new LocalInstance())) {
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
      Services services = new Services("Update local instance", "update");
      services.distributedInstanceResource(s -> s.updateLocalInstanceConfiguration(localInstanceReturned))
        .whenComplete((updateLocalInstance, error) -> {
          if (updateLocalInstance != null) {
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
