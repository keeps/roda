/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class RiskIncidenceList extends BasicAsyncTableCell<RiskIncidence> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private TextColumn<RiskIncidence> objectColumn;
  private TextColumn<RiskIncidence> objectTypeColumn;

  public RiskIncidenceList() {
    super(null, null, null, false);
  }

  public RiskIncidenceList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(RiskIncidence.class);
  }

  public RiskIncidenceList(Filter filter, Facets facets, String summary, boolean selectable, int initialPageSize,
    int pageSizeIncrement) {
    super(filter, facets, summary, selectable, initialPageSize, pageSizeIncrement);
    super.setSelectedClass(RiskIncidence.class);
  }

  @Override
  protected void configureDisplay(CellTable<RiskIncidence> display) {

    objectColumn = new TextColumn<RiskIncidence>() {

      @Override
      public String getValue(RiskIncidence incidence) {
        if (incidence != null) {
          if (incidence.getFileId() != null) {
            return incidence.getFileId();
          } else if (incidence.getRepresentationId() != null) {
            return incidence.getRepresentationId();
          } else if (incidence.getAipId() != null) {
            return incidence.getAipId();
          }
        }

        return null;
      }
    };

    objectTypeColumn = new TextColumn<RiskIncidence>() {

      @Override
      public String getValue(RiskIncidence incidence) {
        return incidence != null ? incidence.getObjectClass() : null;
      }
    };

    objectColumn.setSortable(true);
    objectTypeColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(objectColumn, "Object ID");
    display.addColumn(objectTypeColumn, "Object Type");
    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(objectColumn, "100%");
    objectTypeColumn.setCellStyleNames("nowrap");

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(objectTypeColumn, true));
    addStyleName("my-collections-table");
    emptyInfo.addStyleName("my-collections-empty-info");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<RiskIncidence>> callback) {

    Filter filter = getFilter();
    if (filter == null) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {

      Map<Column<RiskIncidence, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<RiskIncidence, ?>, List<String>>();
      columnSortingKeyMap.put(objectColumn, Arrays.asList(RodaConstants.RISK_INCIDENCE_AIP_ID,
        RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, RodaConstants.RISK_INCIDENCE_FILE_ID));
      columnSortingKeyMap.put(objectTypeColumn, Arrays.asList(RodaConstants.RISK_INCIDENCE_OBJECT_CLASS));

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      BrowserService.Util.getInstance().find(RiskIncidence.class.getName(), filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), true, callback);
    }
  }

}
