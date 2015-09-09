package pt.gov.dgarq.roda.wui.common.client.widgets;

import com.google.gwt.cell.client.SafeHtmlCell;
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
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.StringUtility;
import pt.gov.dgarq.roda.wui.management.user.client.UserManagementService;

public class RodaMemberList extends AsyncTableCell<RODAMember> {

  private static final int PAGE_SIZE = 20;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private final Column<RODAMember, SafeHtml> activeColumn;
  private final Column<RODAMember, SafeHtml> typeColumn;
  private final TextColumn<RODAMember> idColumn;
  private final TextColumn<RODAMember> nameColumn;
  private final TextColumn<RODAMember> groupsColumn;

  public RodaMemberList() {
    this(null, null);
  }

  public RodaMemberList(Filter filter, Facets facets) {
    super(filter, facets, "MEMBERS");

    activeColumn = new Column<RODAMember, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(RODAMember member) {
        return SafeHtmlUtils
          .fromSafeConstant(member != null ? (member.isActive() ? "<i class='fa fa-check-circle'></i>"
            : "<i class='fa fa-ban'></i>") : "");

      }
    };

    typeColumn = new Column<RODAMember, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(RODAMember member) {
        return SafeHtmlUtils.fromSafeConstant(member != null ? (member.isUser() ? "<i class='fa fa-user'></i>"
          : "<i class='fa fa-users'></i>") : "");

      }
    };

    idColumn = new TextColumn<RODAMember>() {

      @Override
      public String getValue(RODAMember member) {
        return member != null ? member.getId() : null;
      }
    };
    nameColumn = new TextColumn<RODAMember>() {

      @Override
      public String getValue(RODAMember member) {
        return member != null ? member.getName() : null;
      }
    };
    groupsColumn = new TextColumn<RODAMember>() {

      @Override
      public String getValue(RODAMember member) {
        return member != null ? StringUtility.prettyPrint(member.getAllGroups()) : null;
      }
    };

    activeColumn.setSortable(true);
    typeColumn.setSortable(true);
    idColumn.setSortable(true);
    nameColumn.setSortable(true);

    // TODO externalize strings into constants
    getDisplay().addColumn(activeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-check-circle'></i>"));
    getDisplay().addColumn(typeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>"));
    getDisplay().addColumn(idColumn, "Identifier");
    getDisplay().addColumn(nameColumn, "Name");
    getDisplay().addColumn(groupsColumn, "Groups");

    // getDisplay().setAutoHeaderRefreshDisabled(true);
    Label emptyInfo = new Label("No items to display");
    getDisplay().setEmptyTableWidget(emptyInfo);
    // getDisplay().setColumnWidth(nameColumn, "100%");

    getDisplay().setColumnWidth(activeColumn, "15px");
    getDisplay().setColumnWidth(typeColumn, "15px");

    addStyleName("my-list-rodamember");
    emptyInfo.addStyleName("my-list-rodamember-empty-info");

  }

  @Override
  protected void getData(int start, int length, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<RODAMember>> callback) {

    Filter filter = getFilter();

    // calculate sorter
    Sorter sorter = new Sorter();
    for (int i = 0; i < columnSortList.size(); i++) {
      ColumnSortInfo columnSortInfo = columnSortList.get(i);
      String sortParameterKey;

      if (columnSortInfo.getColumn().equals(activeColumn)) {
        sortParameterKey = RodaConstants.MEMBERS_IS_ACTIVE;
      } else if (columnSortInfo.getColumn().equals(typeColumn)) {
        sortParameterKey = RodaConstants.MEMBERS_IS_USER;
      } else if (columnSortInfo.getColumn().equals(idColumn)) {
        sortParameterKey = RodaConstants.MEMBERS_ID;
      } else if (columnSortInfo.getColumn().equals(nameColumn)) {
        sortParameterKey = RodaConstants.MEMBERS_NAME;
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

    UserManagementService.Util.getInstance().findMembers(filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

  @Override
  protected ProvidesKey<RODAMember> getKeyProvider() {
    return new ProvidesKey<RODAMember>() {

      @Override
      public Object getKey(RODAMember item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
