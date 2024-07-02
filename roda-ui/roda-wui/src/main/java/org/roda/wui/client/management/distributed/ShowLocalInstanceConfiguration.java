/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.DistributedInstancesRestService;
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowLocalInstanceConfiguration extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      Services services = new Services("Get local instance", "get");
      services.distributedInstanceResource(s -> s.getLocalInstance()).whenComplete((localInstance, error) -> {
        if (!localInstance.equals(new LocalInstance())) {
          ShowLocalInstanceConfiguration showLocalInstanceConfiguration = new ShowLocalInstanceConfiguration(
            localInstance);
          callback.onSuccess(showLocalInstanceConfiguration);
        } else {
          HistoryUtils.newHistory(CreateLocalInstanceConfiguration.RESOLVER);
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {ShowLocalInstanceConfiguration.RESOLVER}, false,
        callback);
    }

    @Override
    public String getHistoryToken() {
      return "show_instance_management";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(LocalInstanceManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowLocalInstanceConfiguration> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private LocalInstance localInstance;

  @UiField
  HTML IDValue;

  @UiField
  Button buttonSynchronize;

  @UiField
  Button buttonSubscribe;

  @UiField
  HTML centralInstanceURLValue;

  @UiField
  HTML lastSyncValue;

  @UiField
  HTML synchronizationStatusValue;

  public ShowLocalInstanceConfiguration(LocalInstance localInstance) {
    initWidget(uiBinder.createAndBindUi(this));
    this.localInstance = localInstance;

    initElements(localInstance);
  }

  private void initElements(LocalInstance localInstance) {
    IDValue.setText(localInstance.getId());
    centralInstanceURLValue.setText(localInstance.getCentralInstanceURL());
    lastSyncValue.setHTML(HtmlSnippetUtils.getLastSyncHtml(localInstance, false));
    synchronizationStatusValue.setHTML(HtmlSnippetUtils.getInstanceIdStateHtml(localInstance));
    buttonSynchronize.setEnabled(false);
    if (localInstance.getStatus().equals(SynchronizingStatus.ACTIVE)) {
      buttonSubscribe.setVisible(false);
      buttonSynchronize.setEnabled(true);
    }
  }

  @UiHandler("buttonEdit")
  void buttonApplyHandler(ClickEvent e) {
    HistoryUtils.newHistory(EditLocalInstanceConfiguration.RESOLVER);
  }

  @UiHandler("buttonSubscribe")
  void buttonSubscribeHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.applyInstanceIdToRepository(), messages.applyInstanceIdToRepositoryMessage(),
      messages.dialogNo(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Subscribe local instance", "subscribe");
            services.distributedInstanceResource(s -> s.subscribeLocalInstance(localInstance))
              .whenComplete((subscribedLocalInstance, error) -> {
                if (subscribedLocalInstance != null) {
                  buttonSubscribe.setVisible(false);
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      initElements(subscribedLocalInstance);
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);

                    }
                  });
                }
              });
          }
        }
      });
  }

  @UiHandler("buttonSynchronize")
  void buttonSynchronizeHandler(ClickEvent e) {
    Services services = new Services("Synchronize", "synchronize");
    services.distributedInstanceResource(s -> s.synchronize(localInstance)).whenComplete((job, error) -> {
      if (job != null) {
        Toast.showInfo("Create Job", "Success");
        HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
      }
    });
  }

  @UiHandler("buttonUnsubscribe")
  void buttonUnsubscribeHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.removeLocalConfiguration(), messages.removeLocalConfigurationMessage(),
      messages.dialogNo(), messages.dialogYes(), confirmUnsubscribeCallback());

  }

  private NoAsyncCallback<Boolean> confirmUnsubscribeCallback() {
    return new NoAsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
      }

      @Override
      public void onSuccess(Boolean result) {
        super.onSuccess(result);
        if (result) {
          Services services = new Services("Delete local instance configuration", "delete");
          services.distributedInstanceResource(DistributedInstancesRestService::unsubscribeLocalInstance)
            .whenComplete((res, error) -> {
              if (error == null) {
                super.onSuccess(true);
                Toast.showInfo(messages.successfullyUnsubscribedTitle(), messages.successfullyUnsubscribedMessage());
                HistoryUtils.newHistory(BrowseTop.RESOLVER);
              } else {
                AsyncCallbackUtils.defaultFailureTreatment(error);
              }
            });
        }
      }
    };
  }

  private NoAsyncCallback<Boolean> confirmRemoveInstanceIdentifier() {
    return new NoAsyncCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean result) {
        super.onSuccess(result);
        if (result) {
          Services services = new Services("Delete local configuration", "delete");
          services.distributedInstanceResource(DistributedInstancesRestService::deleteLocalConfiguration)
            .whenComplete((res, error) -> {
              if (error == null) {
                Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {
                  @Override
                  public void onFailure(Throwable caught) {
                    Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
                  }

                  @Override
                  public void onSuccess(final Void nothing) {
                    HistoryUtils.newHistory(InternalProcess.RESOLVER);
                  }
                });
              } else {
                AsyncCallbackUtils.defaultFailureTreatment(error);
                HistoryUtils.newHistory(InternalProcess.RESOLVER);
              }
            });
        }
      }
    };
  }
}
