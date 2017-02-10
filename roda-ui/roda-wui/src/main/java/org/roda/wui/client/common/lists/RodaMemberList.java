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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

public class RodaMemberList extends BasicAsyncTableCell<RODAMember> {

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<RODAMember, SafeHtml> activeColumn;
  private Column<RODAMember, SafeHtml> typeColumn;
  private TextColumn<RODAMember> nameColumn;
  private TextColumn<RODAMember> fullNameColumn;
  private TextColumn<RODAMember> groupsColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.MEMBERS_ID,
    RodaConstants.MEMBERS_IS_USER, RodaConstants.MEMBERS_NAME, RodaConstants.MEMBERS_FULLNAME,
    RodaConstants.MEMBERS_GROUPS, RodaConstants.MEMBERS_IS_ACTIVE);

  public RodaMemberList() {
    this(null, null, null, false);
  }

  public RodaMemberList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(RODAMember.class, filter, facets, summary, selectable, fieldsToReturn);
  }

  public RodaMemberList(Filter filter, Facets facets, String summary, boolean selectable, int pageSize,
    int incrementPage) {
    super(RODAMember.class, filter, facets, summary, selectable, pageSize, incrementPage, fieldsToReturn);
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

    nameColumn = new TextColumn<RODAMember>() {

      @Override
      public String getValue(RODAMember member) {
        return member != null ? member.getName() : null;
      }
    };

    fullNameColumn = new TextColumn<RODAMember>() {

      @Override
      public String getValue(RODAMember member) {
        return member != null ? member.getFullName() : null;
      }
    };

    groupsColumn = new TextColumn<RODAMember>() {

      @Override
      public String getValue(RODAMember member) {
        if (member instanceof User) {
          User user = (User) member;
          return StringUtils.prettyPrint(user.getGroups());
        } else {
          return null;
        }
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
    nameColumn.setSortable(true);
    nameColumn.setSortable(true);
    activeColumn.setSortable(true);

    addColumn(typeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>"), false, false, 3);
    addColumn(nameColumn, messages.userIdentifier(), true, false);
    addColumn(fullNameColumn, messages.userFullName(), true, false);
    addColumn(groupsColumn, messages.userGroups(), true, false);
    addColumn(activeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-check-circle'></i>"), false, false, 2);

    addStyleName("my-list-rodamember");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<RODAMember, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<RODAMember, ?>, List<String>>();
    columnSortingKeyMap.put(activeColumn, Arrays.asList(RodaConstants.MEMBERS_IS_ACTIVE));
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.MEMBERS_IS_USER));
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.MEMBERS_NAME));
    columnSortingKeyMap.put(fullNameColumn, Arrays.asList(RodaConstants.MEMBERS_FULLNAME));

    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
