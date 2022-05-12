package org.roda.wui.client.management.distributed;

import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
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
        BrowserService.Util.getInstance().retrieveDistributedInstance(historyTokens.get(0), new NoAsyncCallback<DistributedInstance>() {
          @Override
          public void onSuccess(DistributedInstance result) {
            EditDistributedInstance editDistributedInstance = new EditDistributedInstance(result);
            callback.onSuccess(editDistributedInstance);
          }
        });
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DistributedInstancesManagement.RESOLVER}, false, callback);
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

  interface MyUiBinder extends UiBinder<Widget, EditDistributedInstance> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DistributedInstance distributedInstance;

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
            distributedInstance.setStatus(SynchronizingStatus.INACTIVE);
            BrowserServiceImpl.Util.getInstance().updateDistributedInstance(distributedInstance, new NoAsyncCallback<DistributedInstance>() {
              @Override
              public void onSuccess(DistributedInstance distributedInstance) {
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
            distributedInstance.setStatus(SynchronizingStatus.ACTIVE);
            BrowserServiceImpl.Util.getInstance().updateDistributedInstance(distributedInstance, new NoAsyncCallback<DistributedInstance>() {
              @Override
              public void onSuccess(DistributedInstance distributedInstance) {
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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (distributedInstanceDataPanel.isValid()) {
      DistributedInstance distributedInstanceUpdated = distributedInstanceDataPanel.getDistributedInstance();
      distributedInstance.setName(distributedInstanceUpdated.getName());
      distributedInstance.setDescription(distributedInstanceUpdated.getDescription());
      //distributedInstance.setNameIdentifier(distributedInstanceUpdated.getNameIdentifier());
      BrowserServiceImpl.Util.getInstance().updateDistributedInstance(this.distributedInstance, new NoAsyncCallback<DistributedInstance>() {
        @Override
        public void onSuccess(DistributedInstance distributedInstance) {
          HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
        }
      });
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserServiceImpl.Util.getInstance().deleteDistributedInstance(distributedInstance.getId(), new NoAsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
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
}
