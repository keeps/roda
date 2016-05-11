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
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class AIPList extends AsyncTableCell<IndexedAIP, Boolean> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<IndexedAIP, SafeHtml> levelColumn;
  private TextColumn<IndexedAIP> titleColumn;
  private TextColumn<IndexedAIP> datesColumn;

  public AIPList() {
    super(null, null, null, false, Boolean.FALSE);
  }

  public AIPList(Filter filter, Facets facets, String summary, boolean selectable, boolean showInactive) {
    super(filter, facets, summary, selectable, showInactive);
    super.setSelectedClass(IndexedAIP.class);
  }

  public AIPList(Filter filter, Facets facets, String summary, boolean selectable, boolean showInactive,
    int initialPageSize, int pageSizeIncrement) {
    super(filter, facets, summary, selectable, initialPageSize, pageSizeIncrement, showInactive);
    super.setSelectedClass(IndexedAIP.class);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedAIP> display) {
    levelColumn = new Column<IndexedAIP, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedAIP aip) {
        SafeHtml ret;
        if (aip == null) {
          logger.error("Trying to display a NULL item");
          ret = null;
        } else {
          ret = DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), true);
        }
        return ret;
      }
    };

    titleColumn = new TextColumn<IndexedAIP>() {

      @Override
      public String getValue(IndexedAIP aip) {
        return aip != null ? aip.getTitle() : null;
      }
    };

    datesColumn = new TextColumn<IndexedAIP>() {

      @Override
      public String getValue(IndexedAIP aip) {
        return Humanize.getDatesText(aip.getDateInitial(), aip.getDateFinal(), false);
      }
    };

    levelColumn.setSortable(true);
    titleColumn.setSortable(true);
    datesColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(levelColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-tag'></i>&nbsp;Level"));
    display.addColumn(titleColumn, "Title");
    display.addColumn(datesColumn, "Dates");
    display.setColumnWidth(levelColumn, "35px");
    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(titleColumn, "100%");

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(datesColumn, false));

    levelColumn.setCellStyleNames("nowrap");
    datesColumn.setCellStyleNames("nowrap");

    addStyleName("my-collections-table");
    emptyInfo.addStyleName("my-collections-empty-info");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedAIP>> callback) {

    Filter filter = getFilter();
    if (filter == null) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {

      Map<Column<IndexedAIP, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<IndexedAIP, ?>, List<String>>();
      columnSortingKeyMap.put(levelColumn, Arrays.asList(RodaConstants.AIP_LEVEL));
      columnSortingKeyMap.put(titleColumn, Arrays.asList(RodaConstants.AIP_TITLE_SORT));
      columnSortingKeyMap.put(datesColumn, Arrays.asList(RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL));

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      Boolean showInactive = getObject();
      BrowserService.Util.getInstance().find(IndexedAIP.class.getName(), filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), showInactive, callback);
    }
  }

}
