package org.roda.wui.client.common.dialogs;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ClosableDialog extends DialogBox {

  ClosableDialog() {
    super();
  }

  ClosableDialog(boolean autoHide, boolean modal) {
    super(autoHide, modal);
  }

  @Override
  protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
    super.onPreviewNativeEvent(event);
    if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
      hide();
    }
  }
}
