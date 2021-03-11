/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.confirmations;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.DisposalConfirmationExtraBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateDisposalConfirmationDataPanel extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      CreateDisposalConfirmationDataPanel createDisposalConfirmation = new CreateDisposalConfirmationDataPanel();
      callback.onSuccess(createDisposalConfirmation);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(CreateDisposalConfirmation.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "insert_information";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalConfirmationDataPanel> {
  }

  private static CreateDisposalConfirmationDataPanel.MyUiBinder uiBinder = GWT
    .create(CreateDisposalConfirmationDataPanel.MyUiBinder.class);

  @UiField
  FlowPanel createDisposalConfirmationFormDescription;

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel extra;

  @UiField
  HTML errors;

  @UiField
  TextBox title;

  @UiField
  Label titleError;

  @UiField
  FlowPanel buttonsPanel;

  private DisposalConfirmationExtraBundle disposalConfirmationExtra = null;

  /**
   * Create a new panel to create a disposal confirmation
   */
  public CreateDisposalConfirmationDataPanel() {
    initWidget(uiBinder.createAndBindUi(this));

    initActions();
    createDisposalConfirmationFormDescription.add(new HTMLWidgetWrapper("CreateDisposalConfirmationExtraDescription.html"));

    errors.setVisible(false);

    BrowserService.Util.getInstance()
      .retrieveDisposalConfirmationExtraBundle(new AsyncCallback<DisposalConfirmationExtraBundle>() {

        @Override
        public void onFailure(Throwable throwable) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        }

        @Override
        public void onSuccess(DisposalConfirmationExtraBundle result) {
          disposalConfirmationExtra = result;
          FormUtilities.create(extra, disposalConfirmationExtra.getValues(), true);
        }
      });

  }

  private void initActions() {
    Button btnCancel = new Button();
    btnCancel.setText(messages.cancelButton());
    btnCancel.addStyleName("btn btn-block btn-default btn-times-circle");
    btnCancel.addClickHandler(e -> {
      CreateDisposalConfirmation.getInstance().clear();
      HistoryUtils.newHistory(ShowDisposalConfirmation.RESOLVER);
    });

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION)) {
      Button btnCreate = new Button();
      btnCreate.setText(messages.confirmButton());
      btnCreate.addStyleName("btn btn-block btn-play");
      btnCreate.addClickHandler(e -> {
        boolean valid = isValid();
        if (valid) {
          createDisposalConfirmationReport();
        }
      });

      buttonsPanel.add(btnCreate);
    }

    buttonsPanel.add(btnCancel);
  }

  private void createDisposalConfirmationReport() {
    SelectedItems<IndexedAIP> selectedItemsList = CreateDisposalConfirmation.getInstance().getSelected();
    if (selectedItemsList == null) {
      Toast.showInfo("Error", "Error");
      HistoryUtils.newHistory(ShowDisposalConfirmation.RESOLVER);
    } else {
      BrowserService.Util.getInstance().createDisposalConfirmationReport(selectedItemsList, title.getText(), disposalConfirmationExtra,
        new AsyncCallback<Job>() {
          @Override
          public void onFailure(Throwable throwable) {
            HistoryUtils.newHistory(InternalProcess.RESOLVER);
          }

          @Override
          public void onSuccess(Job job) {
            Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

              @Override
              public void onFailure(Throwable caught) {
                Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
                CreateDisposalConfirmation.getInstance().clear();
                HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
              }

              @Override
              public void onSuccess(final Void nothing) {
                CreateDisposalConfirmation.getInstance().clear();
                HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
              }
            });
          }
        });
    }
  }

  /**
   * Is data panel valid
   *
   * @return true if valid
   */
  private boolean isValid() {
    List<String> errorList = new ArrayList<>();
    if (title.getText().length() == 0) {
      title.addStyleName("isWrong");
      titleError.setText(messages.mandatoryField());
      titleError.setVisible(true);
      Window.scrollTo(title.getAbsoluteLeft(), title.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.disposalConfirmationDataPanelTitle()));
    } else {
      title.removeStyleName("isWrong");
      titleError.setVisible(false);
    }

    List<String> extraErrors = FormUtilities.validate(disposalConfirmationExtra.getValues(), extra);
    errorList.addAll(extraErrors);

    if (!errorList.isEmpty()) {
      errors.setVisible(true);
      StringBuilder errorString = new StringBuilder();
      for (String error : errorList) {
        errorString.append("<span class='error'>").append(error).append("</span>");
        errorString.append("<br/>");
      }
      errors.setHTML(errorString.toString());
    } else {
      errors.setVisible(false);
    }
    return errorList.isEmpty();
  }
}
