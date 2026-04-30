package org.roda.wui.client.management.members.data.panels;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.services.MembersRestService;
import org.roda.wui.client.services.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class UserDataPanel extends Composite implements GenericDataPanel<User>, HasValueChangeHandlers<User> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final GenericDataForm<User> userForm;
  private final FlowPanel extraFieldsPanel = new FlowPanel();
  private final boolean editMode;
  private final Button saveButton;
  private final Button cancelButton;
  private Set<MetadataValue> userExtra;

  public UserDataPanel(boolean editMode) {
    this.editMode = editMode;
    this.userForm = new GenericDataForm<>();

    if (editMode) {
      userForm.addReadOnlyField(messages.groupName(), User::getName, true);
      userForm.addTextField(messages.groupFullname(), User::getFullName, User::setFullName, true);
      userForm.addTextField(messages.email(), User::getEmail, User::setEmail, true, false,
              "^[_A-Za-z0-9-%+]+(\\.[_A-Za-z0-9-%+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)",
              messages.emailNotValid());
      userForm.addCustomWidget(extraFieldsPanel);
    } else {
      userForm.addTextField(messages.username(), User::getName, User::setName, true);
      userForm.addTextField(messages.userFullName(), User::getFullName, User::setFullName, true);

      userForm.addTextField(messages.email(), User::getEmail, User::setEmail, true, false,
        "^[_A-Za-z0-9-%+]+(\\.[_A-Za-z0-9-%+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)",
        messages.emailNotValid());

      userForm.addCustomWidget(extraFieldsPanel);
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
    userForm.addCustomWidget(actionsPanel);

    // Initialize the composite using the generic form as the root widget
    initWidget(userForm);
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

  public void setUser(User user) {
    userForm.setModel(user);

    if (!editMode) {

      Services services = new Services("Get User extra", "get");
      services.membersResource(MembersRestService::getDefaultUserExtra).whenComplete((extra, error) -> {
        if (extra != null) {
          this.userExtra = extra.getExtraFormFields();
          createForm(userExtra);
        } else if (error != null) {
          if (error instanceof AuthorizationDeniedException) {
            GWT.log("No permissions: " + error.getMessage());
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(error);
          }
        }
      });
    } else {
      this.userExtra = user.getExtra();
      createForm(userExtra);
    }
  }

  private void createForm(Set<MetadataValue> userExtra) {
    extraFieldsPanel.clear();
    FormUtilities.create(extraFieldsPanel, userExtra, false);
  }

  public Set<MetadataValue> getUserExtra() {
    return userExtra;
  }

  @Override
  public User getValue() {
    return userForm.getValue();
  }

  @Override
  public boolean isValid() {

    List<String> errorList = new ArrayList<>();

    // 1. Validate Generic Form
    if (!userForm.isValid()) {
      errorList.add(userForm.getErrors().getHTML());
    }

    // 3. Validate Extra Fields
    List<String> extraErrors = FormUtilities.validate(userExtra, extraFieldsPanel);

    // 4. Render Errors
    if (!errorList.isEmpty()) {
      userForm.getErrors().setVisible(true);
      StringBuilder errorString = new StringBuilder();
      for (String error : errorList) {
        errorString.append(error);
      }

      for (String extraError : extraErrors) {
        errorString.append("<span class='error'>").append(extraError).append("</span><br/>");
      }
      userForm.getErrors().setHTML(errorString.toString());
    } else {
      userForm.getErrors().setVisible(false);
    }

    return errorList.isEmpty();
  }

  public boolean isChanged() {
    return userForm.isChanged();
  }

  public boolean isEditMode() {
    return editMode;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<User> handler) {
    return userForm.addValueChangeHandler(handler);
  }
}