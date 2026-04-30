package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.disposal.schedule.CreateDisposalSchedule;
import org.roda.wui.client.disposal.schedule.EditDisposalSchedule;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalScheduleActions {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalScheduleActions() {
    // Private constructor to prevent instantiation
  }

  public static void newDisposalSchedule(AsyncCallback<Actionable.ActionImpact> callback) {
    HistoryUtils.newHistory(CreateDisposalSchedule.RESOLVER.getHistoryPath());
    callback.onSuccess(Actionable.ActionImpact.UPDATED);
  }

  public static void editDisposalSchedule(DisposalSchedule schedule, AsyncCallback<Actionable.ActionImpact> callback) {
    HistoryUtils.newHistory(EditDisposalSchedule.RESOLVER, schedule.getId());
    callback.onSuccess(Actionable.ActionImpact.UPDATED);
  }

  public static void removeDisposalSchedule(DisposalSchedule schedule,
    AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.disposalScheduleRemoveConfirmDialogTitle(),
      messages.disposalScheduleRemoveConfirmDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Delete disposal schedule", "delete");
            services.disposalScheduleResource(s -> s.deleteDisposalSchedule(schedule.getId()))
              .whenComplete((res, error) -> {
                if (error == null) {
                  Timer timer = new Timer() {
                    @Override
                    public void run() {
                      Toast.showInfo(messages.disposalSchedulesTitle(), messages.disposalScheduleRemovedWithSuccess());
                      doActionCallbackDestroyed();
                    }
                  };

                  timer.schedule(RodaConstants.ACTION_TIMEOUT);
                } else {
                  Toast.showError(error.getMessage());
                  callback.onSuccess(Actionable.ActionImpact.NONE);
                }
              });
          } else {
            doActionCallbackNone();
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }
      });
  }
}
