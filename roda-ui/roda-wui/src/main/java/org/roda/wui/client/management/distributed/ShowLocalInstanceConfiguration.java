package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
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
      BrowserService.Util.getInstance().retrieveLocalInstance(new NoAsyncCallback<LocalInstance>() {
        @Override
        public void onSuccess(LocalInstance localInstance) {
          if (localInstance != null) {
            ShowLocalInstanceConfiguration showLocalInstanceConfiguration = new ShowLocalInstanceConfiguration(
              localInstance);
            callback.onSuccess(showLocalInstanceConfiguration);
          } else {
            HistoryUtils.newHistory(CreateLocalInstanceConfiguration.RESOLVER);
          }
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
  HTML isSubscribedValue;

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
    isSubscribedValue.setText(localInstance.getIsSubscribed().toString());
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
          if (result == true) {
            BrowserService.Util.getInstance().subscribeLocalInstance(localInstance,
              new NoAsyncCallback<LocalInstance>() {
                @Override
                public void onSuccess(LocalInstance result) {
                  buttonSubscribe.setVisible(false);
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      initElements(result);
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
    BrowserService.Util.getInstance().synchronizeBundle(localInstance, new NoAsyncCallback<Job>() {
      @Override
      public void onSuccess(Job job) {
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
          BrowserService.Util.getInstance().deleteLocalInstanceConfiguration(new NoAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
              super.onSuccess(unused);
              Dialogs.showConfirmDialog(messages.removeInstanceIdFromRepository(),
                messages.removeInstanceIdFromRepositoryMessage(), messages.dialogNo(), messages.dialogYes(),
                confirmRemoveInstanceIdentifier());
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
          BrowserService.Util.getInstance().removeLocalConfiguration(new LocalInstance(), new AsyncCallback<Job>() {
            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
              HistoryUtils.newHistory(InternalProcess.RESOLVER);
            }

            @Override
            public void onSuccess(Job job) {
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
            }
          });
        }
      }
    };
  }
}
