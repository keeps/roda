/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.RODAMember;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.StringUtility;
import org.roda.wui.common.client.widgets.AsyncTableCell;
import org.roda.wui.management.user.client.UserManagementService;

import com.google.gwt.cell.client.SafeHtmlCell;
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

public class RodaMemberList extends AsyncTableCell<RODAMember> {

  private static final int PAGE_SIZE = 20;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<RODAMember, SafeHtml> activeColumn;
  private Column<RODAMember, SafeHtml> typeColumn;
  private TextColumn<RODAMember> idColumn;
  private TextColumn<RODAMember> nameColumn;
  private TextColumn<RODAMember> groupsColumn;

  public RodaMemberList() {
    this(null, null, null);
  }

  public RodaMemberList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
  }

  @Override
  protected void configureDisplay(CellTable<RODAMember> display) {
    activeColumn = new Column<RODAMember, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(RODAMember member) {
        return SafeHtmlUtils.fromSafeConstant(member != null
          ? (member.isActive() ? "<i class='fa fa-check-circle'></i>" : "<i class='fa fa-ban'></i>") : "");

      }
    };

    typeColumn = new Column<RODAMember, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(RODAMember member) {
        return SafeHtmlUtils.fromSafeConstant(
          member != null ? (member.isUser() ? "<i class='fa fa-user'></i>" : "<i class='fa fa-users'></i>") : "");

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
    display.addColumn(activeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-check-circle'></i>"));
    display.addColumn(typeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>"));
    display.addColumn(idColumn, "Identifier");
    display.addColumn(nameColumn, "Name");
    display.addColumn(groupsColumn, "Groups");

    // display.setAutoHeaderRefreshDisabled(true);
    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    // display.setColumnWidth(nameColumn, "100%");

    display.setColumnWidth(activeColumn, "15px");
    display.setColumnWidth(typeColumn, "15px");

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
