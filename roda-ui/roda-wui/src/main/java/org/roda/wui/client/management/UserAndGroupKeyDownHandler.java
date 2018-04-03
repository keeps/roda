/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.TextBox;

public class UserAndGroupKeyDownHandler implements KeyDownHandler {
  private static final int KEY_UNDERSCORE_AND_HYPHEN = 189;
  private static final int KEY_DOT = 190;

  @Override
  public void onKeyDown(KeyDownEvent event) {
    int keyCode = event.getNativeKeyCode();
    if (!(keyCode >= '0' && keyCode <= '9') && !(keyCode >= 'A' && keyCode <= 'Z')
      && !(keyCode >= 'a' && keyCode <= 'z') && keyCode != KEY_DOT && keyCode != KEY_UNDERSCORE_AND_HYPHEN
      && (keyCode != KeyCodes.KEY_TAB) && (keyCode != KeyCodes.KEY_DELETE) && (keyCode != KeyCodes.KEY_ENTER)
      && (keyCode != KeyCodes.KEY_HOME) && (keyCode != KeyCodes.KEY_END) && (keyCode != KeyCodes.KEY_LEFT)
      && (keyCode != KeyCodes.KEY_UP) && (keyCode != KeyCodes.KEY_RIGHT) && (keyCode != KeyCodes.KEY_DOWN)
      && (keyCode != KeyCodes.KEY_BACKSPACE)) {
      ((TextBox) event.getSource()).cancelKey();
    }
  }

}
