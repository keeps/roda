package org.roda.wui.client.disposal.confirmations.data.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.services.DisposalConfirmationRestService;
import org.roda.wui.client.services.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationDataPanel extends Composite
  implements GenericDataPanel<DisposalConfirmation>, HasValueChangeHandlers<DisposalConfirmation> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final GenericDataForm<DisposalConfirmation> form;
  private final FlowPanel extraFieldsPanel = new FlowPanel();
  private final Button saveButton;
  private final Button cancelButton;
  private Set<MetadataValue> extraMetadataValues;

  public DisposalConfirmationDataPanel() {
    this.form = new GenericDataForm<>();

    form.addTextField(messages.disposalHoldTitle(), DisposalConfirmation::getTitle, DisposalConfirmation::setTitle,
      true);

    form.addCustomWidget(extraFieldsPanel);

    // 1. Initialize Buttons
    saveButton = new Button(messages.saveButton());
    saveButton.addStyleName("btn btn-primary btn-play");

    cancelButton = new Button(messages.cancelButton());
    cancelButton.addStyleName("btn btn-link");

    // 2. Wrap buttons in a FlowPanel for spacing
    FlowPanel actionsPanel = new FlowPanel();
    actionsPanel.addStyleName("alignButtonsPanel"); // Uses your existing CSS spacing
    actionsPanel.add(saveButton);
    actionsPanel.add(cancelButton);

    // 3. Inject the buttons at the bottom of the generic form
    form.addCustomWidget(actionsPanel);

    // Initialize the composite using the generic form as the root widget
    initWidget(form);
  }

  /**
   * Defines what happens when the Save button is clicked. It automatically
   * validates the form before executing the runnable.
   */
  public void setSaveHandler(Runnable onSave) {
    saveButton.addClickHandler(event -> {
      if (isValid()) {
        onSave.run();
      }
    });
  }

  /**
   * Defines what happens when the Cancel button is clicked.
   */
  public void setCancelHandler(Runnable onCancel) {
    cancelButton.addClickHandler(event -> onCancel.run());
  }

  public void setDisposalConfirmation(DisposalConfirmation confirmation) {

    Services services = new Services("Get disposal confirmation configurable form", "get");
    services.disposalConfirmationResource(DisposalConfirmationRestService::retrieveDisposalConfirmationForm)
      .whenComplete((result, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          this.extraMetadataValues = result.getValues();
          createForm(extraMetadataValues);
        }
      });
    form.setModel(confirmation);
  }

  private void createForm(Set<MetadataValue> userExtra) {
    extraFieldsPanel.clear();
    FormUtilities.create(extraFieldsPanel, userExtra, false);
  }

  @Override
  public DisposalConfirmation getValue() {
    return form.getValue();
  }

  public Set<MetadataValue> getDisposalConfirmationExtra() {
    return extraMetadataValues;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DisposalConfirmation> handler) {
    return form.addValueChangeHandler(handler);
  }

  @Override
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();

    // 1. Validate Generic Form
    if (!form.isValid()) {
      errorList.add(form.getErrors().getHTML());
    }

    // 3. Validate Extra Fields
    List<String> extraErrors = FormUtilities.validate(extraMetadataValues, extraFieldsPanel);

    // 4. Render Errors
    if (!errorList.isEmpty()) {
      form.getErrors().setVisible(true);
      StringBuilder errorString = new StringBuilder();
      for (String error : errorList) {
        errorString.append(error);
      }

      for (String extraError : extraErrors) {
        errorString.append("<span class='error'>").append(extraError).append("</span><br/>");
      }
      form.getErrors().setHTML(errorString.toString());
    } else {
      form.getErrors().setVisible(false);
    }

    return errorList.isEmpty();
  }
}
