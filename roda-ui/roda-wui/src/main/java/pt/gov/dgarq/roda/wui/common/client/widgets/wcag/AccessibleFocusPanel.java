package pt.gov.dgarq.roda.wui.common.client.widgets.wcag;

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
