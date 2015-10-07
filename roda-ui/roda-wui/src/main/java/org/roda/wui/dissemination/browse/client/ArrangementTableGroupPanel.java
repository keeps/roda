/**
 * 
 */
package org.roda.wui.dissemination.browse.client;

import org.roda.core.data.eadc.ArrangementTableGroup;
import org.roda.core.data.eadc.ArrangementTableRow;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Luis Faria
 * 
 */
public class ArrangementTableGroupPanel extends FlexTable {

  // private final ArrangementTableGroup table;

  private int rows;

  public ArrangementTableGroupPanel(ArrangementTableGroup table) {
    super();
    // this.table = table;

    rows = 0;
    insertRows(table.getHead().getRows(), true);
    insertRows(table.getBody().getRows(), false);
    this.addStyleName("wui-browse-arrangementTable");

  }

  public void insertRows(ArrangementTableRow[] tableRows, boolean header) {
    for (int i = 0; i < tableRows.length; i++) {
      String[] entries = tableRows[i].getEntries();
      for (int j = 0; j < entries.length; j++) {
        Label entryLabel = new Label(entries[j]);
        this.setWidget(rows, j, entryLabel);
        entryLabel.addStyleName(header ? "header-widget" : "body-widget");
        this.getCellFormatter().addStyleName(rows, j, header ? "header-cell" : "body-cell");
      }
      if (!header) {
        this.getRowFormatter().addStyleName(rows, "body-row");
      }
      rows++;
    }

  }
}
