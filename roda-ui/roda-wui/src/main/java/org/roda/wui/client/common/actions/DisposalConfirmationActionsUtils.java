package org.roda.wui.client.common.actions;

import java.util.Collections;

import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class DisposalConfirmationActionsUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalConfirmationActionsUtils() {
  }

  public static void permanentlyDeleteDisposalConfirmationReport(DisposalConfirmation confirmation,
    AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.permanentlyDeleteConfirmDialogTitle(),
      messages.permanentlyDeleteConfirmDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Permanently deletes records in disposal confirmation", "delete");
            services
              .disposalConfirmationResource(s -> s.permanentlyDeleteRecordsInDisposalConfirmation(
                new SelectedItemsListRequest(Collections.singletonList(confirmation.getUUID()))))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.permanentlyDeleteRecordsSuccessTitle(),
                        messages.permanentlyDeleteRecordsSuccessMessage());
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  public static void restoreDestroyedRecord(DisposalConfirmation confirmation,
    AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.restoreDestroyedRecordsConfirmDialogTitle(),
      messages.restoreDestroyedRecordsConfirmDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Recover disposal confirmation", "recover");
            services
              .disposalConfirmationResource(s -> s.restoreDisposalConfirmation(
                new SelectedItemsListRequest(Collections.singletonList(confirmation.getUUID()))))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.restoreDestroyedRecordsSuccessTitle(),
                        messages.restoreDestroyedRecordsSuccessMessage());
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  public static void destroyDisposalConfirmationContent(DisposalConfirmation report,
    AsyncCallback<Actionable.ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.destroyDisposalConfirmationContentDialogTitle(),
      messages.destroyDisposalConfirmationContentDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Destroy records in disposal confirmation", "destroy");
            services
              .disposalConfirmationResource(s -> s.destroyRecordsInDisposalConfirmation(
                new SelectedItemsListRequest(Collections.singletonList(report.getUUID()))))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.deleteConfirmationReportSuccessTitle(),
                        messages.deleteConfirmationReportSuccessMessage());
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackUpdated();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }
}
