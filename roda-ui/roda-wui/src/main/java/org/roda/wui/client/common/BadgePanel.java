/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.common.RodaConstants;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BadgePanel extends FlowPanel {

  private final Label textLabel;
  private String currentIconCss = "";

  public BadgePanel() {
    super();
    this.addStyleName("badge-panel");

    textLabel = new Label();
    textLabel.addStyleName("badge-label");
    add(textLabel);
  }

  public void setIconClass(String classSimpleName) {

    // 1. Clean up previous icon classes if they exist
    if (currentIconCss != null && !currentIconCss.isEmpty()) {
      for (String cssClass : currentIconCss.split("\\s+")) {
        this.removeStyleName(cssClass);
      }
    }

    // 2. Apply the new icon classes directly to this panel
    for (String cssClass : classSimpleName.split("\\s+")) {
      this.addStyleName(cssClass);
    }

    currentIconCss = classSimpleName;
  }

  public void setText(String text) {
    textLabel.setText(text);
  }
}
