package org.roda.wui.client.common.lists.utils;

import com.google.gwt.user.cellview.client.Column;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class RadioButtonColumn<T> extends Column<T, Boolean> {

  public RadioButtonColumn() {
    super(new RadioButtonCell());
  }
}
