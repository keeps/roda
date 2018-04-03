/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
