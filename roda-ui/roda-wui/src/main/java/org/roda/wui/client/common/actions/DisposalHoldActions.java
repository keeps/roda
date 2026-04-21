package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalHoldActions {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalHoldActions() {
    // private constructor to prevent instantiation
  }

  public static void lift(DisposalHold hold, AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.liftDisposalHoldDialogTitle(), messages.liftDisposalHoldDialogMessage(1),
      messages.cancelButton(), messages.confirmButton(), new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onFailure(Throwable throwable) {
          callback.onSuccess(Actionable.ActionImpact.NONE);
        }

        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
              new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                  // do nothing
                }

                @Override
                public void onSuccess(String details) {
                  Services services = new Services("Lift disposal hold", "job");

                  services.disposalHoldResource(s -> s.liftDisposalHold(hold.getUUID(), details))
                    .whenComplete((job, throwable) -> {
                      if (throwable != null) {
                        AsyncCallbackUtils.defaultFailureTreatment(throwable);
                      } else {
                        Toast.showInfo(messages.runningInBackgroundTitle(), messages.updateDisposalHoldMessage());
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
              });
          }
        }
      });
  }
}
