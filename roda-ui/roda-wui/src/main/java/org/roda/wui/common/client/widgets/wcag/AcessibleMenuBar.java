package org.roda.wui.common.client.widgets.wcag;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.MenuBar;

public class AcessibleMenuBar extends MenuBar {
  public AcessibleMenuBar() {
    super();
    makeAccessible();
  }

  public AcessibleMenuBar(boolean orientation) {
    super(orientation);
    makeAccessible();
  }

  private void makeAccessible() {
    Element firstChildElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstChildElement.getTagName())) {
      firstChildElement.setTitle("input_title");
    }
  }
}
