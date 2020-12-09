package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalRuleActions {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void applyDisposalRulesAction() {
    Dialogs.showConfirmDialog(messages.applyDisposalRulesDialogTitle(),
        messages.applyDisposalRulesDialogMessage(), messages.dialogNo(), messages.dialogYes(),
        new NoAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
            if (result) {
              DisposalDialogs.showApplyRules(messages.applyDisposalRulesDialogTitle(),
                  new NoAsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean applyToManuallyInclusive) {
                      BrowserService.Util.getInstance().applyDisposalRules(applyToManuallyInclusive,
                          new AsyncCallback<Job>() {
                            @Override
                            public void onFailure(Throwable caught) {
                              AsyncCallbackUtils.defaultFailureTreatment(caught);
                              HistoryUtils.newHistory(InternalProcess.RESOLVER);
                            }

                            @Override
                            public void onSuccess(Job job) {
                              Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(),
                                  new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                      Toast.showInfo(messages.runningInBackgroundTitle(),
                                          messages.runningInBackgroundDescription());
                                    }

                                    @Override
                                    public void onSuccess(final Void nothing) {
                                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
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
