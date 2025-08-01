/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalRuleActions {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void applyDisposalRulesAction() {
    Dialogs.showConfirmDialog(messages.applyDisposalRulesDialogTitle(), messages.applyDisposalRulesDialogMessage(),
      messages.dialogNo(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
          if (result) {
            DisposalDialogs.showApplyRules(messages.applyDisposalRulesDialogTitle(), new NoAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean overrideManualAssociations) {
                Services services = new Services("Apply disposal rules", "job");
                services.disposalRuleResource(s -> s.applyDisposalRules(overrideManualAssociations))
                  .whenComplete((job, throwable) -> {
                    if (throwable != null) {
                      AsyncCallbackUtils.defaultFailureTreatment(throwable);
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    } else {
                      Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {
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
