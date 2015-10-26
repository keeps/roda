/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets.wcag;

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class AccessibleTextBox extends TextBox {
  public AccessibleTextBox(Label label) {
    super();
    if (this.getElement().getId() == null) {
      this.getElement().setId("id_" + Random.nextInt(99999));
    }
    if (label == null) {
      label = new Label("label");
      label.setVisible(false);
    }

    label.getElement().setAttribute("for", this.getElement().getId());
    this.getParent().getParent().getElement().appendChild(label.getElement());
  }
}
