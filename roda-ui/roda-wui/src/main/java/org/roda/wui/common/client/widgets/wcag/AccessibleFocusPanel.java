/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets.wcag;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

public class AccessibleFocusPanel extends FocusPanel {

  public AccessibleFocusPanel() {
    super();
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleFocusPanel(Widget w) {
    super(w);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }
}
