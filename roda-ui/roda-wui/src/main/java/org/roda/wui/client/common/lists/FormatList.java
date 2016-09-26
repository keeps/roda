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
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class FormatList extends BasicAsyncTableCell<Format> {

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<Format> nameColumn;
  private TextColumn<Format> categoryColumn;

  public FormatList() {
    this(null, null, null, false);
  }

  public FormatList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(Format.class, filter, facets, summary, selectable);
  }

  public FormatList(Filter filter, Facets facets, String summary, boolean selectable, int pageSize, int incrementPage) {
    super(Format.class, filter, facets, summary, selectable, pageSize, incrementPage);
  }

  @Override
  protected void configureDisplay(CellTable<Format> display) {

    nameColumn = new TextColumn<Format>() {

      @Override
      public String getValue(Format format) {
        return format != null ? format.getName() : null;
      }
    };

    categoryColumn = new TextColumn<Format>() {

      @Override
      public String getValue(Format format) {
        return (format != null && format.getCategories() != null && format.getCategories().size() > 0)
          ? format.getCategories().get(0) : null;
      }
    };

    nameColumn.setSortable(true);
    categoryColumn.setSortable(true);

    // TODO externalize strings into constants
    addColumn(nameColumn, messages.formatName(), false, false);
    addColumn(categoryColumn, messages.formatCategory(), true, false);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(nameColumn, false));

  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<Format, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Format, ?>, List<String>>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.FORMAT_NAME_SORT));
    columnSortingKeyMap.put(categoryColumn, Arrays.asList(RodaConstants.FORMAT_CATEGORY_SORT));

    return createSorter(columnSortList, columnSortingKeyMap);
  }

  @Override
  protected ProvidesKey<Format> getKeyProvider() {
    return new ProvidesKey<Format>() {

      @Override
      public Object getKey(Format item) {
        return item.getId();
      }
    };
  }

}
