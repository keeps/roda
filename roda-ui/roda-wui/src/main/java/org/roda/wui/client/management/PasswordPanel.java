/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management;

import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

public class PasswordPanel extends SimplePanel implements HasValueChangeHandlers<String> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private FlowPanel editLayout;

  private Label editPasswordLabel;
  private PasswordTextBox editPassword;
  private Label editPasswordRepeatLabel;
  private PasswordTextBox editPasswordRepeat;

  private Label editPasswordNote;
  private Button editButton;
  private boolean buttonMode;
  private boolean changed;

  public PasswordPanel(boolean editmode) {
    changed = false;

    editLayout = new FlowPanel();
    editPassword = new PasswordTextBox();
    editPasswordRepeat = new PasswordTextBox();
    editPasswordLabel = new Label(messages.password());
    editPasswordLabel.addStyleName("form-label");
    editPasswordRepeatLabel = new Label(messages.passwordConfirmation());
    editPasswordRepeatLabel.addStyleName("form-label");
    editPasswordNote = new Label(messages.passwordNote());

    editLayout.add(editPasswordLabel);
    editLayout.add(editPassword);
    editLayout.add(editPasswordRepeatLabel);
    editLayout.add(editPasswordRepeat);

    editButton = new Button(messages.userDataChangePassword());
    editButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        setWidget(editLayout);
        buttonMode = false;
        onChange();
      }
    });

    if (editmode) {
      FlowPanel editButtonPanel = new FlowPanel();
      Label passwordLabel = new Label(messages.password());
      passwordLabel.addStyleName("form-label");
      editButtonPanel.add(passwordLabel);
      editButtonPanel.add(editButton);
      setWidget(editButtonPanel);
      buttonMode = true;
    } else {
      setWidget(editLayout);
      buttonMode = false;
    }

    KeyUpHandler handler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    editPassword.addKeyUpHandler(handler);
    editPasswordRepeat.addKeyUpHandler(handler);

    this.addStyleName("password");
    editPassword.addStyleName("password-input");
    editPassword.addStyleName("form-textbox");
    editPassword.getElement().setTitle(messages.password());
    // WCAGUtilities.getInstance().makeAccessible(editPassword.getParent().getElement());

    editPasswordRepeat.addStyleName("password-input");
    editPasswordRepeat.addStyleName("form-textbox");
    editPasswordRepeat.getElement().setTitle(messages.password());
    // WCAGUtilities.getInstance().makeAccessible(editPasswordRepeat.getParent().getElement());

    editPasswordNote.addStyleName("password-note");
    // WCAGUtilities.getInstance().makeAccessible(editPasswordNote.getParent().getElement());

    editButton.addStyleName("password-button");
    editButton.addStyleName("btn");
    editButton.addStyleName("btn-play");
  }

  public boolean isChanged() {
    return changed;
  }

  private void updateAfterValidation(boolean valid) {
    if (!valid) {
      editPassword.addStyleName("isWrong");
      editPasswordRepeat.addStyleName("isWrong");
    } else {
      editPassword.removeStyleName("isWrong");
      editPasswordRepeat.removeStyleName("isWrong");
    }
  }

  public boolean matchConfirmation() {
    boolean valid = editPassword.getValue().equals(editPasswordRepeat.getValue());
    updateAfterValidation(valid);
    return valid;
  }

  public boolean isSmall() {
    boolean small;

    if (buttonMode && StringUtils.isBlank(editPassword.getValue())) {
      small = false;
    } else {
      small = editPassword.getValue().length() < 6;
    }

    updateAfterValidation(!small);
    return small;
  }

  public boolean isValid() {
    boolean valid = true;
    if (!buttonMode
      && (!editPassword.getValue().equals(editPasswordRepeat.getValue()) || editPassword.getValue().length() < 6)) {
      valid = false;
    }

    updateAfterValidation(valid);
    return valid;
  }

  protected void onChange() {
    changed = true;
    ValueChangeEvent.fire(this, getValue());
  }

  /**
   * Get the new password
   *
   * @return the new password or null if none set
   *
   */
  public String getValue() {
    return changed && isValid() ? editPassword.getText() : null;
  }

  @Override
  public void clear() {
    editPassword.setText("");
    editPasswordRepeat.setText("");
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

}
