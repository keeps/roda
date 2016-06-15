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
import org.roda.wui.common.client.tools.StringUtility;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class RodaMemberList extends BasicAsyncTableCell<RODAMember> {

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<RODAMember, SafeHtml> activeColumn;
  private Column<RODAMember, SafeHtml> typeColumn;
  private TextColumn<RODAMember> idColumn;
  private TextColumn<RODAMember> nameColumn;
  private TextColumn<RODAMember> groupsColumn;

  public RodaMemberList() {
    this(null, null, null, false);
  }

  public RodaMemberList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(RODAMember.class);
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

    groupsColumn = new TextColumn<RODAMember>() {

      @Override
      public String getValue(RODAMember member) {
        return member != null ? StringUtility.prettyPrint(member.getAllGroups()) : null;
      }
    };

    activeColumn = new Column<RODAMember, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(RODAMember member) {
        return SafeHtmlUtils.fromSafeConstant(member != null
          ? (member.isActive() ? "<i class='fa fa-check-circle'></i>" : "<i class='fa fa-ban'></i>") : "");

      }
    };

    typeColumn.setSortable(true);
    idColumn.setSortable(true);
    nameColumn.setSortable(true);
    activeColumn.setSortable(true);

    // TODO externalize strings into constants
    addColumn(typeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>"), false, false, 3);
    addColumn(idColumn, messages.userIdentifier(), true, false);
    addColumn(nameColumn, messages.userName(), true, false);
    addColumn(groupsColumn, messages.userGroups(), true, false);
    addColumn(activeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-check-circle'></i>"), false, false, 2);

    addStyleName("my-list-rodamember");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<RODAMember>> callback) {

    Filter filter = getFilter();

    Map<Column<RODAMember, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<RODAMember, ?>, List<String>>();
    columnSortingKeyMap.put(activeColumn, Arrays.asList(RodaConstants.MEMBERS_IS_ACTIVE));
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.MEMBERS_IS_USER));
    columnSortingKeyMap.put(idColumn, Arrays.asList(RodaConstants.MEMBERS_ID));
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.MEMBERS_NAME));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    UserManagementService.Util.getInstance().findMembers(filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }
}
