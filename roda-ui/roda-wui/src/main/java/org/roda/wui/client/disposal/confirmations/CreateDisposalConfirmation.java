package org.roda.wui.client.disposal.confirmations;

import java.util.List;

import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationCreateRequest;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationForm;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.disposal.confirmations.data.panels.DisposalConfirmationDataPanel;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class CreateDisposalConfirmation extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  // --- IN-MEMORY TRANSFER VARIABLE ---
  private static SelectedItems<IndexedAIP> pendingSelection;
  // -----------------------------------
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      // Safety check: if pendingSelection is null, it means the user hit F5 on this
      // page.
      // We bounce them back to the Overdue Records page because the context is lost.
      if (pendingSelection == null) {
        HistoryUtils.newHistory(OverdueRecords.RESOLVER);
        callback.onSuccess(null);
        return;
      }

      CreateDisposalConfirmation confirmation = new CreateDisposalConfirmation(pendingSelection);

      // Clear the static variable immediately so it doesn't leak or accidentally get
      // reused
      pendingSelection = null;

      callback.onSuccess(confirmation);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DisposalConfirmations.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalConfirmations.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create";
    }
  };
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<DisposalConfirmation> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel dataPanel;
  public CreateDisposalConfirmation(final SelectedItems<IndexedAIP> selectedItems) {
    initWidget(uiBinder.createAndBindUi(this));

    DisposalConfirmationDataPanel confirmationDataPanel = new DisposalConfirmationDataPanel();
    confirmationDataPanel.setDisposalConfirmation(new DisposalConfirmation());
    dataPanel.add(confirmationDataPanel);

    confirmationDataPanel.setSaveHandler(() -> {
      DisposalConfirmation value = confirmationDataPanel.getValue();
      DisposalConfirmationCreateRequest request = new DisposalConfirmationCreateRequest(value.getTitle(), selectedItems,
        new DisposalConfirmationForm(confirmationDataPanel.getDisposalConfirmationExtra()));
      Services services = new Services("Create disposal confirmation", "create");
      services.disposalConfirmationResource(s -> s.createDisposalConfirmation(request))
        .whenComplete((job, throwable) -> {
          if (throwable != null) {
            HistoryUtils.newHistory(InternalProcess.RESOLVER);
          } else {
            Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

              @Override
              public void onFailure(Throwable caught) {
                Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
                HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
              }

              @Override
              public void onSuccess(final Void nothing) {
                HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
              }
            });
          }
        });
    });

    confirmationDataPanel.setCancelHandler(() -> HistoryUtils.newHistory(OverdueRecords.RESOLVER));

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateDisposalConfirmationBreadcrumbs());

    actionsToolbar.setLabel(messages.showDisposalConfirmationTitle());
    actionsToolbar.build();

    title.setText(messages.createDisposalConfirmationTitle());
    title.setIconClass("DisposalConfirmations");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  public static void setPendingSelection(SelectedItems<IndexedAIP> selection) {
    pendingSelection = selection;
  }

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalConfirmation> {
  }
}