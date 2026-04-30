package org.roda.wui.client.management.members.data.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.user.Group;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class GroupDataPanel extends Composite implements GenericDataPanel<Group>, HasValueChangeHandlers<Group> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final GenericDataForm<Group> groupForm;
  private final boolean editMode;

  private final Button saveButton;
  private final Button cancelButton;

  public GroupDataPanel(boolean editMode) {
    this.editMode = editMode;
    this.groupForm = new GenericDataForm<>();

    if (editMode) {
      groupForm.addReadOnlyField(messages.groupName(), Group::getName, true);
      groupForm.addTextField(messages.groupFullname(), Group::getFullName, Group::setFullName, true);
    } else {
      groupForm.addTextField(messages.groupName(), Group::getName, Group::setName, true);
      groupForm.addTextField(messages.groupFullname(), Group::getFullName, Group::setFullName, true);
    }

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
    groupForm.addCustomWidget(actionsPanel);

    // Initialize the composite using the generic form as the root widget
    initWidget(groupForm);
  }

  /**
   * Defines what happens when the Save button is clicked.
   * It automatically validates the form before executing the runnable.
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

  public void setGroup(Group group) {
    groupForm.setModel(group);
  }

  @Override
  public Group getValue() {
    return groupForm.getValue();
  }

  @Override
  public boolean isValid() {
    return groupForm.isValid();
  }

  public boolean isChanged() {
    return groupForm.isChanged();
  }

  public boolean isEditMode() {
    return editMode;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Group> handler) {
    return groupForm.addValueChangeHandler(handler);
  }

  public void clear() {
    setGroup(new Group());
  }
}