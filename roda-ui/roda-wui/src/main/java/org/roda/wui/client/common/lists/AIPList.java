/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexedAIP;
import org.roda.core.data.v2.IndexResult;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
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
import com.google.gwt.view.client.ProvidesKey;

public class AIPList extends AsyncTableCell<IndexedAIP> {

  private static final int PAGE_SIZE = 20;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<IndexedAIP, SafeHtml> levelColumn;
  // private TextColumn<SimpleDescriptionObject> idColumn;
  private TextColumn<IndexedAIP> titleColumn;
  private Column<IndexedAIP, Date> dateInitialColumn;
  private Column<IndexedAIP, Date> dateFinalColumn;

  public AIPList() {
    this(null, null, null);
  }

  public AIPList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
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
          ret = DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel());
        }
        return ret;
      }
    };

    // idColumn = new TextColumn<IndexAIP>() {
    //
    // @Override
    // public String getValue(IndexAIP aip) {
    // return aip != null ? aip.getId() : null;
    // }
    // };

    titleColumn = new TextColumn<IndexedAIP>() {

      @Override
      public String getValue(IndexedAIP aip) {
        return aip != null ? aip.getTitle() : null;
      }
    };

    dateInitialColumn = new Column<IndexedAIP, Date>(new DateCell(DateTimeFormat.getFormat("yyyy-MM-dd"))) {
      @Override
      public Date getValue(IndexedAIP aip) {
        return aip != null ? aip.getDateInitial() : null;
      }
    };

    dateFinalColumn = new Column<IndexedAIP, Date>(new DateCell(DateTimeFormat.getFormat("yyyy-MM-dd"))) {
      @Override
      public Date getValue(IndexedAIP aip) {
        return aip != null ? aip.getDateFinal() : null;
      }
    };

    levelColumn.setSortable(true);
    // idColumn.setSortable(true);
    titleColumn.setSortable(true);
    dateFinalColumn.setSortable(true);
    dateInitialColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(levelColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-tag'></i>"));
    // display.addColumn(idColumn, "Id");
    display.addColumn(titleColumn, "Title");
    display.addColumn(dateInitialColumn, "Date initial");
    display.addColumn(dateFinalColumn, "Date final");
    display.setColumnWidth(levelColumn, "35px");
    // display.setAutoHeaderRefreshDisabled(true);
    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(titleColumn, "100%");

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(dateInitialColumn, false));

    dateInitialColumn.setCellStyleNames("nowrap");
    dateFinalColumn.setCellStyleNames("nowrap");

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

      Map<Column<IndexedAIP, ?>, String> columnSortingKeyMap = new HashMap<Column<IndexedAIP, ?>, String>();
      columnSortingKeyMap.put(levelColumn, RodaConstants.AIP_LEVEL);
      columnSortingKeyMap.put(titleColumn, RodaConstants.AIP_TITLE_SORT);
      columnSortingKeyMap.put(dateInitialColumn, RodaConstants.AIP_DATE_INITIAL);
      columnSortingKeyMap.put(dateFinalColumn, RodaConstants.AIP_DATE_FINAL);

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      BrowserService.Util.getInstance().findDescriptiveMetadata(filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), callback);
    }

  }

  @Override
  protected ProvidesKey<IndexedAIP> getKeyProvider() {
    return new ProvidesKey<IndexedAIP>() {

      @Override
      public Object getKey(IndexedAIP item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
