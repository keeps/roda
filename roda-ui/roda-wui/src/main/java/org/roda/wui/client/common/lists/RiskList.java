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
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
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

import config.i18n.client.RiskMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class RiskList extends AsyncTableCell<Risk> {

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  // private static final BrowseMessages messages =
  // GWT.create(BrowseMessages.class);
  private static final RiskMessages messages = GWT.create(RiskMessages.class);

  private TextColumn<Risk> nameColumn;
  private Column<Risk, Date> identifiedOnColumn;
  private TextColumn<Risk> categoryColumn;
  private TextColumn<Risk> ownerColumn;
  private Column<Risk, SafeHtml> severityColumn;

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
      public String getValue(Risk risk) {
        return risk != null ? risk.getName() : null;
      }
    };

    identifiedOnColumn = new Column<Risk, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(Risk risk) {
        return risk != null ? risk.getIdentifiedOn() : null;
      }
    };

    categoryColumn = new TextColumn<Risk>() {

      @Override
      public String getValue(Risk risk) {
        return risk != null ? risk.getCategory() : null;
      }
    };

    ownerColumn = new TextColumn<Risk>() {

      @Override
      public String getValue(Risk risk) {
        return risk != null ? risk.getMitigationOwner() : null;
      }
    };

    severityColumn = new Column<Risk, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Risk risk) {
        SafeHtml ret = null;
        if (risk != null) {
          if (risk.getPosMitigationSeverity() < 5) {
            ret = SafeHtmlUtils
              .fromSafeConstant("<span class='label-success'>" + messages.showGoodSeverity() + "</span>");
          } else if (risk.getPosMitigationSeverity() < 15) {
            ret = SafeHtmlUtils
              .fromSafeConstant("<span class='label-warning'>" + messages.showNormalSeverity() + "</span>");
          } else {
            ret = SafeHtmlUtils
              .fromSafeConstant("<span class='label-danger'>" + messages.showBadSeverity() + "</span>");
          }
        }

        return ret;
      }
    };

    nameColumn.setSortable(true);
    identifiedOnColumn.setSortable(true);
    categoryColumn.setSortable(true);
    ownerColumn.setSortable(true);
    severityColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(nameColumn, "Name");
    display.addColumn(identifiedOnColumn, "Identified On");
    display.addColumn(categoryColumn, "Category");
    display.addColumn(ownerColumn, "Owner");
    display.addColumn(severityColumn, "Severity");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(nameColumn, "100%");

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(identifiedOnColumn, false));

    identifiedOnColumn.setCellStyleNames("nowrap");
    categoryColumn.setCellStyleNames("nowrap");
    ownerColumn.setCellStyleNames("nowrap");
    severityColumn.setCellStyleNames("nowrap");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Risk>> callback) {

    Filter filter = getFilter();

    Map<Column<Risk, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Risk, ?>, List<String>>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.RISK_NAME));
    columnSortingKeyMap.put(identifiedOnColumn, Arrays.asList(RodaConstants.RISK_IDENTIFIED_ON));
    columnSortingKeyMap.put(categoryColumn, Arrays.asList(RodaConstants.RISK_CATEGORY));
    columnSortingKeyMap.put(ownerColumn, Arrays.asList(RodaConstants.RISK_MITIGATION_OWNER));
    columnSortingKeyMap.put(severityColumn, Arrays.asList(RodaConstants.RISK_POS_MITIGATION_SEVERITY));

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
