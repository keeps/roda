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
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
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
public class FormatList extends BasicAsyncTableCell<Format> {

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<Format> nameColumn;
  private TextColumn<Format> categoryColumn;

  public FormatList() {
    this(null, null, null, false);
  }

  public FormatList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(Format.class);
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
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Format>> callback) {

    Filter filter = getFilter();
    if (filter == Filter.NULL) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {

      Map<Column<Format, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Format, ?>, List<String>>();
      columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.FORMAT_NAME_SORT));
      columnSortingKeyMap.put(categoryColumn, Arrays.asList(RodaConstants.FORMAT_CATEGORY_SORT));

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      boolean justActive = false;
      BrowserService.Util.getInstance().find(Format.class.getName(), filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), justActive, callback);
    }
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

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
