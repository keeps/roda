/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.management.members;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class UserDataPanel extends Composite implements GenericDataPanel<User>, HasValueChangeHandlers<User> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private final boolean editMode;
  private boolean changed = false;
  private Set<MetadataValue> userExtra;

  // UI Components
  private FlowPanel mainContainer = new FlowPanel();
  private GenericDataForm<User> userForm = new GenericDataForm<>();
  private PasswordPanel passwordPanel;
  private FlowPanel extraFieldsPanel = new FlowPanel();
  private HTML errors = new HTML();

  public UserDataPanel(boolean visible, boolean editMode) {
    this.editMode = editMode;
    this.passwordPanel = new PasswordPanel(editMode);

    // 1. Setup Generic Fields
    userForm.addReadOnlyField(messages.username(), User::getName, true);
    userForm.addTextField(messages.fullname(), User::getFullName, User::setFullName, true, false);

    // Email with Regex Validation
    userForm.addTextField(messages.email(), User::getEmail, User::setEmail, true, false,
      "^[_A-Za-z0-9-%+]+(\\.[_A-Za-z0-9-%+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)",
      messages.emailNotValid());

    // userForm.addCustomWidget(passwordPanel);
    userForm.addCustomWidget(extraFieldsPanel);

    // 3. Assemble Main Layout
    mainContainer.add(userForm);

    errors.setVisible(false);
    mainContainer.add(errors);

    initWidget(mainContainer);
    super.setVisible(visible);

    // 4. Handlers
    ValueChangeHandler<User> changeHandler = event -> onChange();
    userForm.addValueChangeHandler(changeHandler);
    passwordPanel.addValueChangeHandler(event -> onChange());
  }

  public User getUser() {
    return userForm.getValue();
  }

  public void setUser(User user) {
    userForm.setModel(user);
    this.userExtra = user.getExtra();
    createForm(this.userExtra);
    this.changed = false;
  }

  public void createForm(Set<MetadataValue> userExtra) {
    extraFieldsPanel.clear();
    FormUtilities.create(extraFieldsPanel, userExtra, false);
  }

  public String getPassword() {
    return passwordPanel.getValue();
  }

  public boolean isPasswordChanged() {
    return passwordPanel.isChanged();
  }

  public Set<MetadataValue> getUserExtra() {
    return userExtra;
  }

  @Override
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();

    // 1. Validate Generic Form
    if (!userForm.isValid()) {
      errorList.add(messages.mandatoryField() + " / " + messages.emailNotValid());
    }

    // 2. Validate Password Panel
    if (!passwordPanel.matchConfirmation()) {
      Window.scrollTo(passwordPanel.getAbsoluteLeft(), passwordPanel.getAbsoluteTop());
      errorList.add(messages.passwordDoesNotMatchConfirmation());
    } else if (passwordPanel.isSmall()) {
      Window.scrollTo(passwordPanel.getAbsoluteLeft(), passwordPanel.getAbsoluteTop());
      errorList.add(messages.passwordIsTooSmall());
    }

    // 3. Validate Extra Fields
    List<String> extraErrors = FormUtilities.validate(userExtra, extraFieldsPanel);
    errorList.addAll(extraErrors);

    // 4. Render Errors
    if (!errorList.isEmpty()) {
      errors.setVisible(true);
      StringBuilder errorString = new StringBuilder();
      for (String error : errorList) {
        errorString.append("<span class='error'>").append(error).append("</span><br/>");
      }
      errors.setHTML(errorString.toString());
    } else {
      errors.setVisible(false);
    }

    return errorList.isEmpty();
  }

  public boolean isChanged() {
    return changed || userForm.isChanged();
  }

  protected void onChange() {
    changed = true;
    ValueChangeEvent.fire(this, getUser());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<User> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public User getValue() {
    return getUser();
  }
}
