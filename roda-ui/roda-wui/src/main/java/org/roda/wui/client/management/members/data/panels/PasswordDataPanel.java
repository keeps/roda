package org.roda.wui.client.management.members.data.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.wui.client.common.forms.GenericDataPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class PasswordDataPanel extends Composite implements GenericDataPanel<String>, HasValueChangeHandlers<String> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final boolean mandatory;
  private PasswordTextBox passwordBox;
  private PasswordTextBox confirmBox;
  private HTML errors;
  private boolean changed = false;

  /**
   * @param mandatory
   *          If true, the password cannot be empty. If false (e.g., Edit Mode),
   *          it only validates if the user types something.
   */
  public PasswordDataPanel(boolean mandatory) {
    this.mandatory = mandatory;

    FlowPanel mainContainer = new FlowPanel();
    mainContainer.addStyleName("generic-data-panel-fields");

    // 1. Initialize Inputs
    passwordBox = new PasswordTextBox();
    passwordBox.addStyleName("form-textbox");

    confirmBox = new PasswordTextBox();
    confirmBox.addStyleName("form-textbox");

    // 2. Initialize Labels
    Label passwordLabel = new Label(messages.password());
    if (mandatory) {
      passwordLabel.setText(passwordLabel.getText() + "*");
    }
    passwordLabel.addStyleName("form-label");

    Label confirmLabel = new Label(messages.passwordConfirmation());
    if (mandatory) {
      confirmLabel.setText(confirmLabel.getText() + "*");
    }
    confirmLabel.addStyleName("form-label");

    // 4. Setup Error HTML
    errors = new HTML();
    errors.setVisible(false);
    mainContainer.add(errors);

    // 3. Add to layout using the stacked helper
    mainContainer.add(createStackedFieldRow(passwordLabel, passwordBox));
    mainContainer.add(createStackedFieldRow(confirmLabel, confirmBox));

    // 5. Setup Handlers
    ChangeHandler changeHandler = event -> onChange();
    KeyUpHandler keyUpHandler = event -> onChange();

    passwordBox.addChangeHandler(changeHandler);
    passwordBox.addKeyUpHandler(keyUpHandler);
    confirmBox.addChangeHandler(changeHandler);
    confirmBox.addKeyUpHandler(keyUpHandler);

    initWidget(mainContainer);
  }

  /**
   * Helper method to create a stacked layout: <Label> <Password>
   */
  private FlowPanel createStackedFieldRow(Widget labelWidget, Widget inputWidget) {
    FlowPanel stackedField = new FlowPanel();
    stackedField.addStyleName("generic-stacked-field");

    stackedField.add(labelWidget);
    stackedField.add(inputWidget);

    return stackedField;
  }

  @Override
  public boolean isValid() {
    List<String> errorMessages = new ArrayList<>();
    boolean valid = true;

    String pass = passwordBox.getText();
    String conf = confirmBox.getText();

    boolean isPassEmpty = (pass == null || pass.trim().isEmpty());

    // Validation 1: Mandatory check
    if (mandatory && isPassEmpty) {
      valid = false;
      passwordBox.addStyleName("isWrong");
      errorMessages.add(messages.isAMandatoryField(messages.password()));
    }
    // If it has text (or is mandatory), run deeper checks
    else if (!isPassEmpty || mandatory) {

      // Validation 2: Length check (Must be >= 6)
      if (pass.length() < 6) {
        valid = false;
        passwordBox.addStyleName("isWrong");
        errorMessages.add(messages.passwordIsTooSmall());
      } else {
        passwordBox.removeStyleName("isWrong");
      }

      // Validation 3: Match check
      if (!pass.equals(conf)) {
        valid = false;
        confirmBox.addStyleName("isWrong");
        // Only mark password wrong for mismatch if it didn't already fail the length
        // test
        passwordBox.addStyleName("isWrong");
        errorMessages.add(messages.passwordDoesNotMatchConfirmation());
      } else {
        confirmBox.removeStyleName("isWrong");
      }

    } else {
      // It's not mandatory and it's empty (Valid state for Edit modes where you don't
      // change password)
      passwordBox.removeStyleName("isWrong");
      confirmBox.removeStyleName("isWrong");
    }

    // Render Errors
    if (!valid) {
      StringBuilder sb = new StringBuilder();
      for (String err : errorMessages) {
        sb.append("<span class='error'>").append(err).append("</span><br/>");
      }
      errors.setHTML(sb.toString());
      errors.setVisible(true);
    } else {
      errors.setVisible(false);
    }

    return valid;
  }

  @Override
  public String getValue() {
    return passwordBox.getText();
  }

  public boolean isChanged() {
    return changed;
  }

  public void clear() {
    passwordBox.setText("");
    confirmBox.setText("");
    passwordBox.removeStyleName("isWrong");
    confirmBox.removeStyleName("isWrong");
    errors.setVisible(false);
    changed = false;
  }

  protected void onChange() {
    changed = true;
    ValueChangeEvent.fire(this, getValue());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }
}
