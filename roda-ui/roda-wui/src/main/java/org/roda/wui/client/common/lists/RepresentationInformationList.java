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
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.utils.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

public class RepresentationInformationList extends BasicAsyncTableCell<RepresentationInformation> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<RepresentationInformation> nameColumn;
  private TextColumn<RepresentationInformation> categoryColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.REPRESENTATION_INFORMATION_ID, RodaConstants.REPRESENTATION_INFORMATION_NAME,
    RodaConstants.REPRESENTATION_INFORMATION_CATEGORIES);

  public RepresentationInformationList() {
    this(null, null, null, false);
  }

  public RepresentationInformationList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(RepresentationInformation.class, filter, facets, summary, selectable, fieldsToReturn);
  }

  public RepresentationInformationList(Filter filter, Facets facets, String summary, boolean selectable, int pageSize,
    int incrementPage) {
    super(RepresentationInformation.class, filter, facets, summary, selectable, pageSize, incrementPage,
      fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<RepresentationInformation> display) {

    nameColumn = new TextColumn<RepresentationInformation>() {

      @Override
      public String getValue(RepresentationInformation ri) {
        return ri != null ? ri.getName() : null;
      }
    };

    categoryColumn = new TextColumn<RepresentationInformation>() {

      @Override
      public String getValue(RepresentationInformation ri) {
        return StringUtils.prettyPrint(ri.getCategories());
      }
    };

    nameColumn.setSortable(true);
    addColumn(categoryColumn, messages.representationInformationCategories(), false, false, 8);
    addColumn(nameColumn, messages.representationInformationName(), false, false);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(nameColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<RepresentationInformation, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.REPRESENTATION_INFORMATION_NAME_SORT));
    columnSortingKeyMap.put(categoryColumn, Arrays.asList(RodaConstants.REPRESENTATION_INFORMATION_CATEGORIES));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

  @Override
  protected ProvidesKey<RepresentationInformation> getKeyProvider() {
    return new ProvidesKey<RepresentationInformation>() {

      @Override
      public Object getKey(RepresentationInformation item) {
        return item.getId();
      }
    };
  }

}
