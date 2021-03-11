/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RadioButtonCell extends CheckboxCell {

  private static final SafeHtml INPUT_SELECTED = SafeHtmlUtils
    .fromSafeConstant("<input type=\"radio\" name=\"selected\" tabindex=\"-1\" checked/>");
  private static final SafeHtml INPUT_UNCHECKED = SafeHtmlUtils
    .fromSafeConstant("<input type=\"radio\" name=\"selected\" tabindex=\"-1\"/>");

  @Override
  public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
    // Get the view data.
    Object key = context.getKey();
    Boolean viewData = getViewData(key);

    if (value != null && value) {
      sb.append(INPUT_SELECTED);
    } else {
      sb.append(INPUT_UNCHECKED);
    }
  }
}
