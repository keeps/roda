/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.BrowseMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class RiskList extends AsyncTableCell<Risk> {

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private TextColumn<Risk> nameColumn;
  private Column<Risk, Date> identifiedOnColumn;
  private TextColumn<Risk> identifiedByColumn;

  public RiskList() {
    this(null, null, null, false);
  }

  public RiskList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<Risk> display) {

    nameColumn = new TextColumn<Risk>() {

      @Override
      public String getValue(Risk Risk) {
        return Risk != null ? Risk.getName() : null;
      }
    };

    identifiedOnColumn = new Column<Risk, Date>(new DateCell(
      DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(Risk Risk) {
        return Risk != null ? Risk.getIdentifiedOn() : null;
      }
    };

    identifiedByColumn = new TextColumn<Risk>() {

      @Override
      public String getValue(Risk Risk) {
        return Risk != null ? Risk.getIdentifiedBy() : null;
      }
    };

    nameColumn.setSortable(true);
    identifiedOnColumn.setSortable(true);
    identifiedByColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(nameColumn, "Name");
    display.addColumn(identifiedOnColumn, "Identified On");
    display.addColumn(identifiedByColumn, "Identified By");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(nameColumn, "100%");

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(identifiedOnColumn, false));

    identifiedOnColumn.setCellStyleNames("nowrap");
    identifiedByColumn.setCellStyleNames("nowrap");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Risk>> callback) {

    Filter filter = getFilter();

    Map<Column<Risk, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Risk, ?>, List<String>>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.RISK_NAME));
    columnSortingKeyMap.put(identifiedOnColumn, Arrays.asList(RodaConstants.RISK_IDENTIFIED_ON));
    columnSortingKeyMap.put(identifiedByColumn, Arrays.asList(RodaConstants.RISK_IDENTIFIED_BY));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().find(Risk.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

  @Override
  protected ProvidesKey<Risk> getKeyProvider() {
    return new ProvidesKey<Risk>() {

      @Override
      public Object getKey(Risk item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
