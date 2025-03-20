/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.UpdateDistributedInstanceRequest;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
public class EditDistributedInstance extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Get distributed instance", "get");
        services.distributedInstanceResource(s -> s.getDistributedInstance(historyTokens.get(0)))
          .whenComplete((distributedInstance, error) -> {
            if (distributedInstance != null) {
              EditDistributedInstance editDistributedInstance = new EditDistributedInstance(distributedInstance);
              callback.onSuccess(editDistributedInstance);
            }
          });
      }
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
      return "edit_distributed_instance";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  Button buttonSave;
  @UiField
  Button buttonCancel;
  @UiField
  Button buttonRemove;
  @UiField
  Button buttonChangeStatus;
  @UiField(provided = true)
  DistributedInstanceDataPanel distributedInstanceDataPanel;
  private DistributedInstance distributedInstance;

  public EditDistributedInstance(final DistributedInstance distributedInstance) {
    this.distributedInstance = distributedInstance;
    this.distributedInstanceDataPanel = new DistributedInstanceDataPanel(distributedInstance, true);
    this.distributedInstanceDataPanel.setDistributedInstance(distributedInstance);

    initWidget(uiBinder.createAndBindUi(this));
    initStatusButton(distributedInstance);
  }

  private void initStatusButton(DistributedInstance distributedInstance) {
    switch (distributedInstance.getStatus()) {
      case ACTIVE:
      case CREATED:
        buttonChangeStatus.setText(messages.distributedInstanceStatusButtonDeactivateLabel());
        buttonChangeStatus.addStyleName("btn-default btn-times-circle");
        buttonChangeStatus.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            Services services = new Services("Update distributed instance status", "update");
            services
              .distributedInstanceResource(
                s -> s.updateDistributedInstanceSyncStatus(distributedInstance.getId(), false))
              .whenComplete((updatedDistributedInstance, error) -> {
                if (updatedDistributedInstance != null) {
                  HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
                }
              });
          }
        });
        break;
      case INACTIVE:
        buttonChangeStatus.setText(messages.distributedInstanceStatusButtonActivateLabel());
        buttonChangeStatus.addStyleName("btn-success btn-check");
        buttonChangeStatus.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            Services services = new Services("Update distributed instance status", "update");
            services
              .distributedInstanceResource(
                s -> s.updateDistributedInstanceSyncStatus(distributedInstance.getId(), true))
              .whenComplete((updatedDistributedInstance, error) -> {
                if (updatedDistributedInstance != null) {
                  HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
                }
              });
          }
        });
        break;
      default:
        buttonChangeStatus.setVisible(false);
    }
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (distributedInstanceDataPanel.isValid()) {
      DistributedInstance distributedInstanceUpdated = distributedInstanceDataPanel.getDistributedInstance();
      UpdateDistributedInstanceRequest request = new UpdateDistributedInstanceRequest(
        distributedInstanceUpdated.getId(), distributedInstanceUpdated.getName(),
        distributedInstanceUpdated.getDescription());
      // distributedInstance.setNameIdentifier(distributedInstanceUpdated.getNameIdentifier());
      Services services = new Services("Update distributed instance", "update");
      services.distributedInstanceResource(s -> s.updateDistributedInstance(request))
        .whenComplete((updatedDistributedInstance, error) -> {
          if (updatedDistributedInstance != null) {
            HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
          }
        });
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.removeDistributedInstanceTitle(), messages.removeDistributedInstanceLabel(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            Services services = new Services("Delete distributed instance", "delete");
            services.distributedInstanceResource(s -> s.deleteDistributedInstance(distributedInstance.getId()))
              .whenComplete((result, error) -> {
                HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
              });
          }
        }
      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowDistributedInstance.RESOLVER, distributedInstance.getId());
  }

  interface MyUiBinder extends UiBinder<Widget, EditDistributedInstance> {
  }
}
