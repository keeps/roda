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
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class RiskList extends BasicAsyncTableCell<IndexedRisk> {

  private static final int PAGE_SIZE = 20;
  private Filter filter;

  private static ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<IndexedRisk> nameColumn;
  private Column<IndexedRisk, Date> identifiedOnColumn;
  private TextColumn<IndexedRisk> categoryColumn;
  private TextColumn<IndexedRisk> ownerColumn;
  private Column<IndexedRisk, SafeHtml> severityColumn;
  private TextColumn<IndexedRisk> objectCounterColumn;

  public RiskList() {
    this(null, null, null, false);
  }

  public RiskList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(IndexedRisk.class);
    this.filter = filter;
  }

  @Override
  protected void configureDisplay(CellTable<IndexedRisk> display) {

    nameColumn = new TextColumn<IndexedRisk>() {

      @Override
      public String getValue(IndexedRisk risk) {
        return risk != null ? risk.getName() : null;
      }
    };

    identifiedOnColumn = new Column<IndexedRisk, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG))) {
      @Override
      public Date getValue(IndexedRisk risk) {
        return risk != null ? risk.getIdentifiedOn() : null;
      }
    };

    categoryColumn = new TextColumn<IndexedRisk>() {

      @Override
      public String getValue(IndexedRisk risk) {
        return risk != null ? risk.getCategory() : null;
      }
    };

    ownerColumn = new TextColumn<IndexedRisk>() {

      @Override
      public String getValue(IndexedRisk risk) {
        return risk != null ? risk.getMitigationOwner() : null;
      }
    };

    severityColumn = new Column<IndexedRisk, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedRisk risk) {
        SafeHtml ret = null;
        if (risk != null) {
          ret = HtmlSnippetUtils.getSeverityDefinition(risk.getPosMitigationSeverityLevel());
        }

        return ret;
      }
    };

    objectCounterColumn = new TextColumn<IndexedRisk>() {
      @Override
      public String getValue(IndexedRisk risk) {
        return Integer.toString(risk.getObjectsSize());
      }
    };

    nameColumn.setSortable(true);
    identifiedOnColumn.setSortable(true);
    categoryColumn.setSortable(true);
    ownerColumn.setSortable(true);
    severityColumn.setSortable(true);
    objectCounterColumn.setSortable(true);

    // TODO externalize strings into constants
    addColumn(nameColumn, messages.riskName(), false, false);
    addColumn(categoryColumn, messages.riskCategory(), false, false);
    addColumn(ownerColumn, messages.riskMitigationOwner(), false, false);
    addColumn(identifiedOnColumn, messages.riskIdentifiedOn(), false, false, 8);
    addColumn(severityColumn, messages.riskPostMitigationSeverity(), false, false, 7);
    addColumn(objectCounterColumn, messages.riskObjects(), false, false, 6);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(severityColumn, false));

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedRisk>> callback) {

    Filter filter = getFilter();
    if (filter == Filter.NULL) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {

      Map<Column<IndexedRisk, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<IndexedRisk, ?>, List<String>>();
      columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.RISK_NAME));
      columnSortingKeyMap.put(identifiedOnColumn, Arrays.asList(RodaConstants.RISK_IDENTIFIED_ON));
      columnSortingKeyMap.put(categoryColumn, Arrays.asList(RodaConstants.RISK_CATEGORY));
      columnSortingKeyMap.put(ownerColumn, Arrays.asList(RodaConstants.RISK_MITIGATION_OWNER));
      columnSortingKeyMap.put(severityColumn, Arrays.asList(RodaConstants.RISK_POS_MITIGATION_SEVERITY));
      columnSortingKeyMap.put(objectCounterColumn, Arrays.asList(RodaConstants.RISK_OBJECTS_SIZE));

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      BrowserService.Util.getInstance().find(IndexedRisk.class.getName(), filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), getJustActive(), callback);
    }

  }

  @Override
  protected ProvidesKey<IndexedRisk> getKeyProvider() {
    return new ProvidesKey<IndexedRisk>() {

      @Override
      public Object getKey(IndexedRisk item) {
        return item.getId();
      }
    };
  }

  @Override
  public void setFilter(Filter filter) {
    if (filter == null) {
      super.setFilter(this.filter);
    } else {
      super.setFilter(filter);
    }

    refresh();
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
