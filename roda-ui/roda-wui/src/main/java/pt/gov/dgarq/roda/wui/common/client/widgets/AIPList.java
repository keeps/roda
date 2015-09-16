package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;

public class AIPList extends AsyncTableCell<SimpleDescriptionObject> {

  private static final int PAGE_SIZE = 20;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private final Column<SimpleDescriptionObject, SafeHtml> levelColumn;
  // private final TextColumn<SimpleDescriptionObject> idColumn;
  private final TextColumn<SimpleDescriptionObject> titleColumn;
  private final Column<SimpleDescriptionObject, Date> dateInitialColumn;
  private final Column<SimpleDescriptionObject, Date> dateFinalColumn;

  public AIPList() {
    this(null, null);
  }

  public AIPList(Filter filter, Facets facets) {
    super(filter, facets, "AIPS");

    levelColumn = new Column<SimpleDescriptionObject, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(SimpleDescriptionObject sdo) {
        SafeHtml ret;
        if (sdo == null) {
          logger.error("Trying to display a NULL item");
          ret = null;
        } else {
          ret = DescriptionLevelUtils.getElementLevelIconSafeHtml(sdo.getLevel());
        }
        return ret;
      }
    };

    // idColumn = new TextColumn<SimpleDescriptionObject>() {
    //
    // @Override
    // public String getValue(SimpleDescriptionObject sdo) {
    // return sdo != null ? sdo.getId() : null;
    // }
    // };

    titleColumn = new TextColumn<SimpleDescriptionObject>() {

      @Override
      public String getValue(SimpleDescriptionObject sdo) {
        return sdo != null ? sdo.getTitle() : null;
      }
    };

    dateInitialColumn = new Column<SimpleDescriptionObject, Date>(
      new DateCell(DateTimeFormat.getFormat("yyyy-MM-dd"))) {
      @Override
      public Date getValue(SimpleDescriptionObject sdo) {
        return sdo != null ? sdo.getDateInitial() : null;
      }
    };

    dateFinalColumn = new Column<SimpleDescriptionObject, Date>(new DateCell(DateTimeFormat.getFormat("yyyy-MM-dd"))) {
      @Override
      public Date getValue(SimpleDescriptionObject sdo) {
        return sdo != null ? sdo.getDateFinal() : null;
      }
    };

    levelColumn.setSortable(true);
    // idColumn.setSortable(true);
    titleColumn.setSortable(true);
    dateFinalColumn.setSortable(true);
    dateInitialColumn.setSortable(true);

    // TODO externalize strings into constants
    getDisplay().addColumn(levelColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-tag'></i>"));
    // getDisplay().addColumn(idColumn, "Id");
    getDisplay().addColumn(titleColumn, "Title");
    getDisplay().addColumn(dateInitialColumn, "Date initial");
    getDisplay().addColumn(dateFinalColumn, "Date final");
    getDisplay().setColumnWidth(levelColumn, "35px");
    // getDisplay().setAutoHeaderRefreshDisabled(true);
    Label emptyInfo = new Label("No items to display");
    getDisplay().setEmptyTableWidget(emptyInfo);
    getDisplay().setColumnWidth(titleColumn, "100%");

    dateInitialColumn.setCellStyleNames("nowrap");
    dateFinalColumn.setCellStyleNames("nowrap");

    addStyleName("my-collections-table");
    emptyInfo.addStyleName("my-collections-empty-info");

  }

  @Override
  protected void getData(int start, int length, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<SimpleDescriptionObject>> callback) {

    Filter filter = getFilter();
    if (filter == null) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {
      // calculate sorter
      Sorter sorter = new Sorter();
      for (int i = 0; i < columnSortList.size(); i++) {
        ColumnSortInfo columnSortInfo = columnSortList.get(i);
        String sortParameterKey;
        if (columnSortInfo.getColumn().equals(levelColumn)) {
          sortParameterKey = RodaConstants.SDO_LEVEL;
          // } else if (columnSortInfo.getColumn().equals(idColumn)) {
          // sortParameterKey = RodaConstants.AIP_ID;
        } else if (columnSortInfo.getColumn().equals(titleColumn)) {
          sortParameterKey = RodaConstants.SDO_TITLE;
        } else if (columnSortInfo.getColumn().equals(dateInitialColumn)) {
          sortParameterKey = RodaConstants.SDO_DATE_INITIAL;
        } else if (columnSortInfo.getColumn().equals(dateFinalColumn)) {
          sortParameterKey = RodaConstants.SDO_DATE_FINAL;
        } else {
          sortParameterKey = null;
        }

        if (sortParameterKey != null) {
          sorter.add(new SortParameter(sortParameterKey, !columnSortInfo.isAscending()));
        } else {
          logger.warn("Selecting a sorter that is not mapped");
        }
      }

      // define sublist
      Sublist sublist = new Sublist(start, length);

      BrowserService.Util.getInstance().findDescriptiveMetadata(filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), callback);
    }

  }

  @Override
  protected ProvidesKey<SimpleDescriptionObject> getKeyProvider() {
    return new ProvidesKey<SimpleDescriptionObject>() {

      @Override
      public Object getKey(SimpleDescriptionObject item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
