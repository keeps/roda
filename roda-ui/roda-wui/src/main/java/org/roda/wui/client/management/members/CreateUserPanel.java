/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.members;

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
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.services.MembersRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class CreateUserPanel extends Composite implements HasValueChangeHandlers<User> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());
  @UiField
  TextBox username;
  @UiField
  Label usernameError;
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
  private boolean changed = true;
  private Set<MetadataValue> userExtra;
  // Add this field
  private Runnable onFormReadyCallback;

  /**
   * Create a new user data panel
   *
   */
  public CreateUserPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    super.setVisible(true);
    errors.setVisible(false);

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        CreateUserPanel.this.onChange();
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
    fullName.addChangeHandler(changeHandler);
    fullName.addKeyUpHandler(keyUpHandler);

    usernameError.setVisible(false);
    fullNameError.setVisible(false);
    emailError.setVisible(false);
    setUser(new User());
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

  private void setUser(User user) {
    this.username.setText(user.getName());
    this.fullName.setText(user.getFullName());
    this.email.setText(user.getEmail());

    Services services = new Services("Get User extra", "get");
    services.membersResource(MembersRestService::getDefaultUserExtra).whenComplete((extra, error) -> {
      if (extra != null) {
        CreateUserPanel.this.userExtra = extra.getExtraFormFields();
        createForm(userExtra);

        // ADD THIS: Trigger the callback to recenter the dialog
        if (onFormReadyCallback != null) {
          onFormReadyCallback.run();
        }
      } else if (error != null) {
        if (error instanceof AuthorizationDeniedException) {
          GWT.log("No permissions: " + error.getMessage());
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        }

        // ADD THIS: It's good practice to also recenter on error
        // in case error messages alter the panel's dimensions
        if (onFormReadyCallback != null) {
          onFormReadyCallback.run();
        }
      }
    });
  }

  public void createForm(Set<MetadataValue> userExtra) {
    extra.clear();
    FormUtilities.create(extra, userExtra, false);
  }

  /**
   * Is user data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    if (username.getText().length() == 0) {
      username.addStyleName("isWrong");
      usernameError.setText(messages.mandatoryField());
      usernameError.setVisible(true);
      Window.scrollTo(username.getAbsoluteLeft(), username.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.username()));
    } else {
      username.removeStyleName("isWrong");
      usernameError.setVisible(false);
    }

    if (fullName.getText().length() == 0) {
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
    return errorList.isEmpty() ? true : false;
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
  }

  public void clear() {
    username.setText("");
    fullName.setText("");
    email.setText("");
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
    ValueChangeEvent.fire(this, getValue());
  }

  public void setOnFormReadyCallback(Runnable callback) {
    this.onFormReadyCallback = callback;
  }

  public User getValue() {
    return getUser();
  }

  public Set<MetadataValue> getUserExtra() {
    return userExtra;
  }

  interface MyUiBinder extends UiBinder<Widget, CreateUserPanel> {
  }
}
