package org.roda.wui.client.common.lists.utils;

import com.google.gwt.user.cellview.client.Column;

public abstract class TooltipTextColumn<T> extends Column<T, String> {

  /**
   * Construct a new TextColumn.
   */
  public TooltipTextColumn() {
    super(new TooltipTextCell());
  }
}
