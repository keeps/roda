/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class Footer extends Composite {

  private FlowPanel panel;
  private HTMLWidgetWrapper layout;

  public Footer() {
    super();

    panel = new FlowPanel();
    layout = new HTMLWidgetWrapper("Footer.html");

    panel.add(layout);
    initWidget(layout);
  }
}
