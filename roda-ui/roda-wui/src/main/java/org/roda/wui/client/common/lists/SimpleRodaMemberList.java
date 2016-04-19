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
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

public class SimpleRodaMemberList extends BasicAsyncTableCell<RODAMember> {

  private static final int PAGE_SIZE = 5;

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<RODAMember, SafeHtml> typeColumn;
  private TextColumn<RODAMember> idColumn;
  private TextColumn<RODAMember> nameColumn;

  public SimpleRodaMemberList() {
    this(null, null, null, false);
  }

  public SimpleRodaMemberList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<RODAMember> display) {
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

    typeColumn.setSortable(true);
    idColumn.setSortable(true);
    nameColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(typeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>"));
    display.addColumn(idColumn, "Identifier");
    display.addColumn(nameColumn, "Name");

    // display.setAutoHeaderRefreshDisabled(true);
    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    // display.setColumnWidth(nameColumn, "100%");

    display.setColumnWidth(typeColumn, "15px");

    addStyleName("my-list-rodamember");
    emptyInfo.addStyleName("my-list-rodamember-empty-info");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<RODAMember>> callback) {

    Filter filter = getFilter();

    Map<Column<RODAMember, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<RODAMember, ?>, List<String>>();
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.MEMBERS_IS_USER));
    columnSortingKeyMap.put(idColumn, Arrays.asList(RodaConstants.MEMBERS_ID));
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.MEMBERS_NAME));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

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
