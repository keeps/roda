/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
