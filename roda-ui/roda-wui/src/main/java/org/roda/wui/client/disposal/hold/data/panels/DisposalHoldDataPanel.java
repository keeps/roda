package org.roda.wui.client.disposal.hold.data.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldDataPanel extends Composite
  implements GenericDataPanel<DisposalHold>, HasValueChangeHandlers<DisposalHold> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final GenericDataForm<DisposalHold> disposalHoldForm;

  private final Button saveButton;
  private final Button cancelButton;

  public DisposalHoldDataPanel() {
    this.disposalHoldForm = new GenericDataForm<>();

    disposalHoldForm.addTextField(messages.disposalHoldTitle(), DisposalHold::getTitle, DisposalHold::setTitle, true);
    disposalHoldForm.addTextField(messages.disposalHoldDescription(), DisposalHold::getDescription,
      DisposalHold::setDescription, false);
    disposalHoldForm.addTextField(messages.disposalHoldMandate(), DisposalHold::getMandate, DisposalHold::setMandate,
      false);
    disposalHoldForm.addTextArea(messages.disposalHoldNotes(), DisposalHold::getScopeNotes,
      DisposalHold::setScopeNotes, false);

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
    disposalHoldForm.addCustomWidget(actionsPanel);

    // Initialize the composite using the generic form as the root widget
    initWidget(disposalHoldForm);
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

  public void setDisposalHold(DisposalHold hold) {
    disposalHoldForm.setModel(hold);
  }

  @Override
  public DisposalHold getValue() {
    return disposalHoldForm.getValue();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DisposalHold> handler) {
    return disposalHoldForm.addValueChangeHandler(handler);
  }

  @Override
  public boolean isValid() {
    return disposalHoldForm.isValid();
  }
}
