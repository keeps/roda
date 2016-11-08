/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class RemovableAssociativeTextBox extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, RemovableAssociativeTextBox> {
  }

  @UiField
  Label itemName;

  @UiField
  TextBox hiddenItemId;

  @UiField(provided = true)
  Anchor removeDynamicTextBoxButton;

  @UiField
  Button dialogButton;

  public RemovableAssociativeTextBox() {
    removeDynamicTextBoxButton = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));
    initWidget(uiBinder.createAndBindUi(this));
    hiddenItemId.setVisible(false);
    dialogButton.setVisible(false);
  }

  public RemovableAssociativeTextBox(String id, String name) {
    removeDynamicTextBoxButton = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));
    initWidget(uiBinder.createAndBindUi(this));
    hiddenItemId.setVisible(false);
    dialogButton.setVisible(false);
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
    // fires an event to automatically click on the button
    DomEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false), dialogButton);
  }

}