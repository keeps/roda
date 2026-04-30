/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import com.google.gwt.user.client.Timer;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.disposal.rule.ChangeOrderRequest;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.disposal.rule.CreateDisposalRule;
import org.roda.wui.client.disposal.rule.EditDisposalRule;
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

  private DisposalRuleActions() {
  }

  public static void newRule(AsyncCallback<Actionable.ActionImpact> callback) {
    HistoryUtils.newHistory(CreateDisposalRule.RESOLVER.getHistoryPath());
    callback.onSuccess(Actionable.ActionImpact.NONE);
  }

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

  public static void editDisposalRule(DisposalRule rule, AsyncCallback<Actionable.ActionImpact> callback) {
    HistoryUtils.newHistory(EditDisposalRule.RESOLVER, rule.getId());
    callback.onSuccess(Actionable.ActionImpact.UPDATED);
  }

  public static void removeDisposalRule(DisposalRule rule, AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.deleteDisposalRuleDialogTitle(),
      messages.deleteDisposalRuleDialogMessage(rule.getTitle()), messages.dialogNo(), messages.dialogYes(),
      new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            Services services = new Services("Delete disposal rule", "deletion");
            services.disposalRuleResource(s -> s.deleteDisposalRule(rule.getId())).whenComplete((unused, throwable) -> {
              if (throwable != null) {
                AsyncCallbackUtils.defaultFailureTreatment(throwable);
              } else {
                Toast.showInfo(messages.showDisposalRuleTitle(),
                  messages.deleteDisposalRuleSuccessMessage(rule.getTitle()));
                DisposalRuleActions.applyDisposalRulesAction();
              }
            });
          } else {
            callback.onSuccess(Actionable.ActionImpact.NONE);
          }
        }
      });
  }

  public static void removeMultipleDisposalRules(SelectedItems<DisposalRule> items,
    AsyncCallback<Actionable.ActionImpact> callback) {

  }

  public static void changeDisposalRulesOrder(SelectedItems<DisposalRule> items,
    AsyncCallback<Actionable.ActionImpact> callback) {
    DisposalDialogs.reOrderDisposalRules(messages.editRules(), messages.disposalRuleChangeOrderMessage(),
      new AsyncCallback<ChangeOrderRequest>() {
        @Override
        public void onSuccess(ChangeOrderRequest changeOrderRequest) {
          changeOrderRequest.setItems(SelectedItemsUtils.convertToRESTRequest(items));

          Services services = new Services("Update disposal rule order", "update");
          services.disposalRuleResource(s -> s.changeDisposalRuleOrder(changeOrderRequest))
            .whenComplete((unused, throwable) -> {
              if (throwable != null) {
                AsyncCallbackUtils.defaultFailureTreatment(throwable);
                callback.onSuccess(Actionable.ActionImpact.NONE);
              } else {
                Toast.showInfo(messages.showDisposalRuleTitle(), messages.updateDisposalRuleOrderSuccessMessage());
                Timer timer = new Timer() {
                  @Override
                  public void run() {
                    callback.onSuccess(Actionable.ActionImpact.UPDATED);
                  }
                };

                timer.schedule(RodaConstants.ACTION_TIMEOUT);
              }
            });
        }

        @Override
        public void onFailure(Throwable caught) {
          callback.onSuccess(Actionable.ActionImpact.NONE);
        }
      });
  }
}
