/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import com.google.gwt.core.client.GWT;
import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Miguel Guimarãese <mguimaraes@keep.pt>
 */
public class SavedSearchUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private SavedSearchUtils() {
  };

  public static void share() {
    Toast.showInfo(messages.copiedToClipboardTitle(), messages.copiedToClipboardMessage());
    JavascriptUtils.copyURLToClipboard();
  }
}
