/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class RemovableTextBox extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, RemovableTextBox> {
  }

  @UiField
  TextBox item;

  @UiField
  Anchor removeDynamicTextBoxButton;

  // the first removable text box is not removable and has an "add" button
  private final boolean isAddTextBox;

  public RemovableTextBox() {
    this(false);
  }

  public RemovableTextBox(boolean first) {
    this.isAddTextBox = first;
    initWidget(uiBinder.createAndBindUi(this));
    if (first) {
      removeDynamicTextBoxButton.setHTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>"));
    } else {
      removeDynamicTextBoxButton.setHTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));
    }
  }

  public RemovableTextBox(boolean first, String content) {
    this(first);
    if (content != null) {
      item.setText(content);
    }
  }

  public RemovableTextBox(String content) {
    this(false, content);
  }

  public String getTextBoxValue() {
    return item.getText();
  }

  public HandlerRegistration addRemoveClickHandler(ClickHandler clickHandler) {
    return removeDynamicTextBoxButton.addClickHandler(clickHandler);
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return item.addChangeHandler(handler);
  }

  public boolean isAddTextBox() {
    return isAddTextBox;
  }
}
