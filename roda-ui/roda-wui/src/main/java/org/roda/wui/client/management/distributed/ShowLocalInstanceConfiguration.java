package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstanceIdentifierState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
  HTML centralInstanceURLValue;

  @UiField
  HTML isRegisteredValue;

  @UiField
  HTML lastSyncValue;

  @UiField
  HTML instanceIdStateValue;

  public ShowLocalInstanceConfiguration(LocalInstance localInstance) {
    initWidget(uiBinder.createAndBindUi(this));
    this.localInstance = localInstance;

    initElements(localInstance);
  }

  private void initElements(LocalInstance localInstance) {
    IDValue.setText(localInstance.getId());
    centralInstanceURLValue.setText(localInstance.getCentralInstanceURL());
    isRegisteredValue.setText(localInstance.getIsRegistered().toString());
    if (localInstance.getLastSynchronizationDate() != null) {
      lastSyncValue.setText(Humanize.formatDateTime(localInstance.getLastSynchronizationDate()));
    } else {
      lastSyncValue.setText("Never");
    }
    instanceIdStateValue.setHTML(HtmlSnippetUtils.getInstanceIdStateHtml(localInstance));
  }

  @UiHandler("buttonEdit")
  void buttonApplyHandler(ClickEvent e) {
    HistoryUtils.newHistory(EditLocalInstanceConfiguration.RESOLVER);
  }

  @UiHandler("buttonRegister")
  void buttonRegisterHandler(ClickEvent e) {
    BrowserService.Util.getInstance().registerLocalInstance(localInstance, new NoAsyncCallback<LocalInstance>() {
      @Override
      public void onSuccess(LocalInstance result) {
        localInstance = result;
        initElements(localInstance);
        Toast.showInfo("Register", "Success");
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

  @UiHandler("buttonApplyId")
  void buttonApplyIdHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.applyInstanceIdToRepository(), messages.applyInstanceIdToRepositoryMessage(),
      messages.dialogNo(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            localInstance.setInstanceIdentifierState(LocalInstanceIdentifierState.ACTIVE);
            BrowserService.Util.getInstance().updateLocalInstanceConfiguration(localInstance,
              new NoAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                  BrowserService.Util.getInstance().modifyInstanceIdOnRepository(localInstance,
                    new AsyncCallback<Job>() {
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
                            Toast.showInfo(messages.runningInBackgroundTitle(),
                              messages.runningInBackgroundDescription());
                          }

                          @Override
                          public void onSuccess(final Void nothing) {
                            HistoryUtils.newHistory(InternalProcess.RESOLVER);
                          }
                        });
                      }
                    });
                }
              });
          }
        }
      });
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {

    Dialogs.showConfirmDialog(messages.removeInstanceIdFromRepository(),
      messages.removeInstanceIdFromRepositoryMessage(), messages.dialogNo(), messages.dialogYes(),
      new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().deleteLocalInstanceConfiguration(new NoAsyncCallback<Void>() {
              @Override
              public void onSuccess(Void unused) {
                localInstance.setInstanceIdentifierState(LocalInstanceIdentifierState.INACTIVE);
                localInstance.setId(null);
                BrowserService.Util.getInstance().modifyInstanceIdOnRepository(localInstance, new AsyncCallback<Job>() {
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
            });
          }
        }
      });
  }
}
