/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets;

import com.google.gwt.user.cellview.client.CellTable;

public interface MyCellTableResources extends CellTable.Resources {
  /**
   * The styles applied to the table.
   */
  interface TableStyle extends CellTable.Style {
  }

  @Override
  @Source({CellTable.Style.DEFAULT_CSS, "MyCellTable.css"})
  TableStyle cellTableStyle();

}
