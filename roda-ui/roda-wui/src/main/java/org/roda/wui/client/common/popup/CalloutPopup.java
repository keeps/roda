/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.popup;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;

public class CalloutPopup extends PopupPanel {

  private static final int MARGIN_FROM_TARGET_PX = 10;
  private static final int ARROW_OFFSET_PX = 16;

  public enum CalloutPosition {
    BOTTOM_RIGHT, TOP_RIGHT;
  }

  public CalloutPopup() {
    super(true, true);
    init();
  }

  public CalloutPopup(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    init();
  }

  public CalloutPopup(boolean autoHide) {
    super(autoHide);
    init();
  }

  private void init() {
    setStyleName("actions-popup");
  }

  public void showRelativeTo(final UIObject target, final CalloutPosition position) {
    addStyleDependentName(position.name().toLowerCase());

    setPopupPositionAndShow(new PositionCallback() {

      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        showRelativeTo(target, position, offsetWidth, offsetHeight);
      }
    });
  }

  private void showRelativeTo(UIObject target, CalloutPosition position, int offsetWidth, int offsetHeight) {
    int left;
    int top;
    if (CalloutPosition.BOTTOM_RIGHT.equals(position)) {
      left = target.getAbsoluteLeft() + target.getOffsetWidth() / 2 - offsetWidth + ARROW_OFFSET_PX;
      top = target.getAbsoluteTop() - offsetHeight - MARGIN_FROM_TARGET_PX;

    } else if (CalloutPosition.TOP_RIGHT.equals(position)) {
      left = target.getAbsoluteLeft() + target.getOffsetWidth() / 2 - offsetWidth + ARROW_OFFSET_PX;
      top = target.getAbsoluteTop() + target.getOffsetHeight() + MARGIN_FROM_TARGET_PX;
    } else {
      left = 0;
      top = 0;
    }

    setPopupPosition(left, top);
  }

}
