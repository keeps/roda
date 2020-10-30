package org.roda.wui.client.disposal.confirmations;

import java.util.ArrayList;
import java.util.List;

import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.DisposalConfirmationExtraBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

  private DisposalConfirmationExtraBundle disposalConfirmationExtra = null;

  /**
   * Create a new panel to create a disposal confirmation
   */
  public CreateDisposalConfirmationDataPanel() {
    initWidget(uiBinder.createAndBindUi(this));

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

  /**
   * Is user data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    if (title.getText().length() == 0) {
      title.addStyleName("isWrong");
      titleError.setText(messages.mandatoryField());
      titleError.setVisible(true);
      Window.scrollTo(title.getAbsoluteLeft(), title.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.username()));
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
