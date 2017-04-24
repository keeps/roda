/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.popup;

import org.roda.core.data.v2.common.Pair;

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
    Pair<Integer, Integer> positionPair = Pair.of(0, 0);
    if (CalloutPosition.BOTTOM_RIGHT.equals(position)) {
      positionPair = getBottomRight(target, offsetWidth, offsetHeight);
    } else if (CalloutPosition.TOP_RIGHT.equals(position)) {
      positionPair = getTopRight(target, offsetWidth, offsetHeight);
    }

    setPopupPosition(positionPair.getFirst(), positionPair.getSecond());
  }

  private Pair<Integer, Integer> getBottomRight(UIObject target, int offsetWidth, int offsetHeight) {
    int left = target.getAbsoluteLeft() + target.getOffsetWidth() / 2 - offsetWidth + ARROW_OFFSET_PX;
    int top = target.getAbsoluteTop() - offsetHeight - MARGIN_FROM_TARGET_PX;

    // change top value if popup top disappears of the page (goes to bottom)
    if (top < 0) {
      top = target.getAbsoluteTop() + target.getOffsetHeight() + MARGIN_FROM_TARGET_PX;
      addStyleDependentName(CalloutPosition.TOP_RIGHT.name().toLowerCase());
    } else {
      addStyleDependentName(CalloutPosition.BOTTOM_RIGHT.name().toLowerCase());
    }

    return Pair.of(left, top);
  }

  private Pair<Integer, Integer> getTopRight(UIObject target, int offsetWidth, int offsetHeight) {
    int left = target.getAbsoluteLeft() + target.getOffsetWidth() / 2 - offsetWidth + ARROW_OFFSET_PX;
    int top = target.getAbsoluteTop() + target.getOffsetHeight() + MARGIN_FROM_TARGET_PX;
    addStyleDependentName(CalloutPosition.TOP_RIGHT.name().toLowerCase());
    return Pair.of(left, top);
  }
}
