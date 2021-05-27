package org.roda.wui.client.common.lists;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.institution.Institution;
import org.roda.core.data.v2.institution.RODAInstitution;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class InstitutionList extends AsyncTableCell<RODAInstitution> {
  @Override
  protected void configureDisplay(CellTable<RODAInstitution> display) {

  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    return null;
  }
}
