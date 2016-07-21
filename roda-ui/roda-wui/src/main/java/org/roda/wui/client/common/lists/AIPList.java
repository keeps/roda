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
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
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

import config.i18n.client.ClientMessages;

public class AIPList extends BasicAsyncTableCell<IndexedAIP> {

  private static final SafeHtml HAS_REPRESENTATIONS_ICON = SafeHtmlUtils
    .fromSafeConstant("<i class='fa fa-paperclip' title='Has representations' aria-hidden='true'></i>");
  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<IndexedAIP, SafeHtml> levelColumn;
  private TextColumn<IndexedAIP> titleColumn;
  private TextColumn<IndexedAIP> datesColumn;
  private Column<IndexedAIP, SafeHtml> hasRepresentationsColumn;

  public AIPList() {
    this(null, false, null, null, false);
  }

  public AIPList(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable) {
    super(filter, justActive, facets, summary, selectable);
    super.setSelectedClass(IndexedAIP.class);
  }

  public AIPList(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable,
    int initialPageSize, int pageSizeIncrement) {
    super(filter, justActive, facets, summary, selectable, initialPageSize, pageSizeIncrement);
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

    hasRepresentationsColumn = new Column<IndexedAIP, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedAIP aip) {
        SafeHtml ret;
        if (aip == null) {
          logger.error("Trying to display a NULL item");
          ret = null;
        } else if (aip.getHasRepresentations()) {
          // TODO set title and aria
          ret = HAS_REPRESENTATIONS_ICON;
        } else {
          ret = null;
        }
        return ret;
      }
    };

    levelColumn.setSortable(true);
    titleColumn.setSortable(true);
    datesColumn.setSortable(true);
    hasRepresentationsColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(levelColumn,
      SafeHtmlUtils.fromSafeConstant("<i class='fa fa-tag'></i>&nbsp;" + messages.aipLevel()));
    display.addColumn(titleColumn, messages.aipGenericTitle());
    display.addColumn(datesColumn, messages.aipDates());
    display.addColumn(hasRepresentationsColumn, HAS_REPRESENTATIONS_ICON);

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(datesColumn, false));

    display.setColumnWidth(levelColumn, 5.0, Unit.EM);
    display.setColumnWidth(datesColumn, 13.0, Unit.EM);
    display.setColumnWidth(hasRepresentationsColumn, 3.0, Unit.EM);

    levelColumn.setCellStyleNames("nowrap");
    datesColumn.setCellStyleNames("nowrap");

    addStyleName("my-collections-table");
    emptyInfo.addStyleName("my-collections-empty-info");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedAIP>> callback) {

    Filter filter = getFilter();
    if (filter == Filter.NULL) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {

      Map<Column<IndexedAIP, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<IndexedAIP, ?>, List<String>>();
      columnSortingKeyMap.put(levelColumn, Arrays.asList(RodaConstants.AIP_LEVEL));
      columnSortingKeyMap.put(titleColumn, Arrays.asList(RodaConstants.AIP_TITLE_SORT));
      columnSortingKeyMap.put(datesColumn, Arrays.asList(RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL));
      columnSortingKeyMap.put(hasRepresentationsColumn, Arrays.asList(RodaConstants.AIP_HAS_REPRESENTATIONS));

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      BrowserService.Util.getInstance().find(IndexedAIP.class.getName(), filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), getJustActive(), callback);
    }
  }

}
