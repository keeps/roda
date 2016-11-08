/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
