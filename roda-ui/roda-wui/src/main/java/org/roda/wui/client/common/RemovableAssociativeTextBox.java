/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class RemovableAssociativeTextBox extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, RemovableAssociativeTextBox> {
  }

  @UiField
  TextBox itemName;

  @UiField
  TextBox hiddenItemId;

  @UiField
  Button removeDynamicTextBoxButton;

  @UiField
  Button dialogButton;

  public RemovableAssociativeTextBox() {
    initWidget(uiBinder.createAndBindUi(this));
    hiddenItemId.setVisible(false);
  }

  public RemovableAssociativeTextBox(String id, String name) {
    initWidget(uiBinder.createAndBindUi(this));
    hiddenItemId.setVisible(false);
    if (id != null) {
      hiddenItemId.setText(id);
      itemName.setText(name);
    }
  }

  public String getHiddenTextBoxValue() {
    return hiddenItemId.getText();
  }

  public void setHiddenTextBoxValue(String value) {
    hiddenItemId.setText(value);
  }

  public void setNameTextBoxValue(String value) {
    itemName.setText(value);
  }

  public void addRemoveClickHandler(ClickHandler clickHandler) {
    removeDynamicTextBoxButton.addClickHandler(clickHandler);
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  public void addSearchClickHandler(ClickHandler clickHandler) {
    dialogButton.addClickHandler(clickHandler);
  }

  public void setEnabled(boolean enabled) {
    itemName.setEnabled(enabled);
  }
}