package org.roda.wui.client.common;

import com.google.gwt.user.client.ui.Label;

public class ValuedLabel extends Label {
  private String value;

  public ValuedLabel() {
    super();
  }

  public ValuedLabel(String value) {
    super(value);
    this.value = value;
  }

  public ValuedLabel(String text, String value) {
    super(text);
    this.value = value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
