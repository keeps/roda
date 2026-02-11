/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.management.members;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class UserDataPanel extends Composite implements HasValueChangeHandlers<User> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private final boolean editMode;
  @UiField
  TextBox username;
  @UiField
  Label usernameError;
  @UiField(provided = true)
  PasswordPanel password;
  @UiField
  Label passwordError;
  @UiField
  TextBox fullName;
  @UiField
  Label fullNameError;
  @UiField
  TextBox email;
  @UiField
  Label emailError;
  @UiField
  FlowPanel extra;
  @UiField
  HTML errors;
  // has to be true to detected new field changes
  private boolean changed = false;
  private boolean checked = false;
  private Set<MetadataValue> userExtra;

  /**
   * Create a new user data panel
   *
   * @param visible
   * @param editMode
   */
  public UserDataPanel(boolean visible, boolean editMode) {

    password = new PasswordPanel(editMode);
    initWidget(uiBinder.createAndBindUi(this));

    this.editMode = editMode;
    super.setVisible(visible);

    errors.setVisible(false);

    ValueChangeHandler<String> valueChangedHandler = new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        onChange();
      }
    };

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        UserDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    username.addKeyDownHandler(new UserAndGroupKeyDownHandler());

    username.addChangeHandler(changeHandler);
    username.addKeyUpHandler(keyUpHandler);
    password.addValueChangeHandler(valueChangedHandler);
    fullName.addChangeHandler(changeHandler);
    fullName.addKeyUpHandler(keyUpHandler);

    usernameError.setVisible(false);
    passwordError.setVisible(false);
    fullNameError.setVisible(false);
    emailError.setVisible(false);
  }

  @SuppressWarnings("unused")
  private int setSelected(ListBox listbox, String text) {
    int index = -1;
    if (text != null) {
      for (int i = 0; i < listbox.getItemCount(); i++) {
        if (listbox.getValue(i).equals(text)) {
          index = i;
          break;
        }
      }
      if (index >= 0) {
        listbox.setSelectedIndex(index);
      } else {
        listbox.addItem(text);
        index = listbox.getItemCount() - 1;
        listbox.setSelectedIndex(index);
      }
    } else {
      listbox.setSelectedIndex(-1);
    }
    return index;
  }

  /**
   * Get user defined by this panel. This panel defines: name, fullname, title,
   * organization name, postal address, postal code, locality, country, email,
   * phone number, fax and which groups this user belongs to.
   *
   * @return the user modified by this panel
   */
  public User getUser() {
    User user = new User();
    user.setId(username.getText());
    user.setName(username.getText());
    user.setFullName(fullName.getText());
    user.setEmail(email.getText());

    return user;
  }

  /**
   * Set user information of user
   *
   * @param user
   */
  public void setUser(User user) {
    this.username.setText(user.getName());
    this.fullName.setText(user.getFullName());
    this.email.setText(user.getEmail());
    this.userExtra = user.getExtra();

    this.changed = false;
  }

  public void createForm(Set<MetadataValue> userExtra) {
    extra.clear();
    FormUtilities.create(extra, userExtra, false);
  }

  /**
   * Get the password
   *
   * @return the password if changed, or null if it remains the same
   */
  public String getPassword() {
    return password.getValue();
  }

  /**
   * Check if password changed
   *
   * @return true if password changed, false otherwise
   */
  public boolean isPasswordChanged() {
    return password.isChanged();
  }

  /**
   * Is user data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    if (username.getText().isEmpty()) {
      username.addStyleName("isWrong");
      usernameError.setText(messages.mandatoryField());
      usernameError.setVisible(true);
      Window.scrollTo(username.getAbsoluteLeft(), username.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.username()));
    } else {
      username.removeStyleName("isWrong");
      usernameError.setVisible(false);
    }

    if (!password.matchConfirmation()) {
      if (errorList.isEmpty()) {
        Window.scrollTo(password.getAbsoluteLeft(), password.getAbsoluteTop());
      }
      errorList.add(messages.passwordDoesNotMatchConfirmation());
    } else if (password.isSmall()) {
      if (errorList.isEmpty()) {
        Window.scrollTo(password.getAbsoluteLeft(), password.getAbsoluteTop());
      }
      errorList.add(messages.passwordIsTooSmall());
    }

    if (fullName.getText().isEmpty()) {
      fullName.addStyleName("isWrong");
      fullNameError.setText(messages.mandatoryField());
      fullNameError.setVisible(true);
      if (errorList.isEmpty()) {
        Window.scrollTo(fullName.getAbsoluteLeft(), fullName.getAbsoluteTop());
      }
      errorList.add(messages.isAMandatoryField(messages.fullname()));
    } else {
      fullName.removeStyleName("isWrong");
      fullNameError.setVisible(false);
    }

    if (email.getText() == null || "".equals(email.getText().trim())) {
      email.addStyleName("isWrong");
      emailError.setText(messages.mandatoryField());
      emailError.setVisible(true);
      if (errorList.isEmpty()) {
        Window.scrollTo(email.getAbsoluteLeft(), email.getAbsoluteTop());
      }
      errorList.add(messages.isAMandatoryField(messages.email()));
    } else if (!email.getText()
      .matches("^[_A-Za-z0-9-%+]+(\\.[_A-Za-z0-9-%+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)")) {
      email.addStyleName("isWrong");
      emailError.setText(messages.wrongMailFormat());
      emailError.setVisible(true);
      if (errorList.isEmpty()) {
        Window.scrollTo(email.getAbsoluteLeft(), email.getAbsoluteTop());
      }
      errorList.add(messages.emailNotValid());
    } else {
      email.removeStyleName("isWrong");
      emailError.setVisible(false);
    }

    List<String> extraErrors = FormUtilities.validate(userExtra, extra);
    errorList.addAll(extraErrors);
    checked = true;

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

  /**
   * Is user name read only
   *
   * @return true if read only
   */
  public boolean isUsernameReadOnly() {
    return username.isReadOnly();
  }

  /**
   * Set user name read only
   *
   * @param readonly
   */
  public void setUsernameReadOnly(boolean readonly) {
    username.setReadOnly(readonly);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
  }

  public void clear() {
    username.setText("");
    password.clear();
    fullName.setText("");
    email.setText("");

    this.changed = false;
  }

  /**
   * Is user data panel editable, i.e. on create user mode
   *
   * @return true if editable
   */
  public boolean isEditMode() {
    return editMode;
  }

  /**
   * Is user data panel has been changed
   *
   * @return changed
   */
  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<User> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public User getValue() {
    return getUser();
  }

  public Set<MetadataValue> getUserExtra() {
    return userExtra;
  }

  public void setUserExtra(Set<MetadataValue> extra) {
    UserDataPanel.this.userExtra = extra;
    createForm(extra);
  }

  interface MyUiBinder extends UiBinder<Widget, UserDataPanel> {
  }
}
