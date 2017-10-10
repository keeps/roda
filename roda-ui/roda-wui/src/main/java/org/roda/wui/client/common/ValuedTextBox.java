package org.roda.wui.client.common;

import com.google.gwt.user.client.ui.TextBox;

public class ValuedTextBox extends TextBox {
  private String value;

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return this.value;
  }
}
