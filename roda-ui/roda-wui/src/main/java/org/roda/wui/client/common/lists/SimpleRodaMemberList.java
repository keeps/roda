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
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

public class SimpleRodaMemberList extends BasicAsyncTableCell<RODAMember> {

  private static final int PAGE_SIZE = 5;

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<RODAMember, SafeHtml> typeColumn;
  private TextColumn<RODAMember> idColumn;
  private TextColumn<RODAMember> nameColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.MEMBERS_ID,
    RodaConstants.MEMBERS_IS_USER, RodaConstants.MEMBERS_NAME, RodaConstants.MEMBERS_ID);

  public SimpleRodaMemberList(String listId) {
    this(listId, null, null, false);
  }

  public SimpleRodaMemberList(String listId, Filter filter, String summary, boolean selectable) {
    super(RODAMember.class, listId, filter, summary, selectable, fieldsToReturn);
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
        return member != null ? member.getFullName() : null;
      }
    };

    typeColumn.setSortable(true);
    idColumn.setSortable(true);
    nameColumn.setSortable(true);

    addColumn(typeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>"), false, false, 2);
    addColumn(idColumn, messages.userIdentifier(), true, false);
    addColumn(nameColumn, messages.userFullName(), true, false);

    addStyleName("my-list-rodamember");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<RODAMember, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.MEMBERS_IS_USER));
    columnSortingKeyMap.put(idColumn, Arrays.asList(RodaConstants.MEMBERS_ID));
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.MEMBERS_NAME));

    return createSorter(columnSortList, columnSortingKeyMap);
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
