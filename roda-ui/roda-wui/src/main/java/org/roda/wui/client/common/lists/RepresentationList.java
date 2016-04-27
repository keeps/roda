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
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class RepresentationList extends BasicAsyncTableCell<IndexedRepresentation> {

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private TextColumn<IndexedRepresentation> idColumn;
  private TextColumn<IndexedRepresentation> originalColumn;
  private TextColumn<IndexedRepresentation> typeColumn;
  private TextColumn<IndexedRepresentation> sizeInBytesColumn;
  private TextColumn<IndexedRepresentation> totalNumberOfFilesColumn;

  public RepresentationList() {
    this(null, null, null, false);
  }

  public RepresentationList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedRepresentation> display) {

    idColumn = new TextColumn<IndexedRepresentation>() {

      @Override
      public String getValue(IndexedRepresentation rep) {
        return rep != null ? rep.getId() : null;
      }
    };

    originalColumn = new TextColumn<IndexedRepresentation>() {

      @Override
      public String getValue(IndexedRepresentation rep) {
        return rep != null ? rep.isOriginal() ? "original" : "alternative" : null;
      }
    };

    typeColumn = new TextColumn<IndexedRepresentation>() {

      @Override
      public String getValue(IndexedRepresentation rep) {
        if (rep != null && StringUtils.isNotBlank(rep.getType())) {
          return rep.getType();
        } else {
          return null;
        }
      }
    };

    sizeInBytesColumn = new TextColumn<IndexedRepresentation>() {

      @Override
      public String getValue(IndexedRepresentation rep) {
        return rep != null ? Humanize.readableFileSize(rep.getSizeInBytes()) : null;
      }
    };

    totalNumberOfFilesColumn = new TextColumn<IndexedRepresentation>() {

      @Override
      public String getValue(IndexedRepresentation rep) {
        return rep != null ? rep.getTotalNumberOfFiles() + " files" : null;
      }
    };

    /* add sortable */
    idColumn.setSortable(true);
    originalColumn.setSortable(true);
    typeColumn.setSortable(true);
    sizeInBytesColumn.setSortable(true);
    totalNumberOfFilesColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(idColumn, "Id");
    display.addColumn(originalColumn, "Original");
    display.addColumn(typeColumn, "Type");
    display.addColumn(sizeInBytesColumn, "Size");
    display.addColumn(totalNumberOfFilesColumn, "Number of files");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(idColumn, "100%");

    originalColumn.setCellStyleNames("nowrap");
    typeColumn.setCellStyleNames("nowrap");
    sizeInBytesColumn.setCellStyleNames("nowrap");
    totalNumberOfFilesColumn.setCellStyleNames("nowrap");

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(idColumn, false));

    addStyleName("my-representation-table");
    emptyInfo.addStyleName("my-representation-empty-info");

  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedRepresentation>> callback) {
    Filter filter = getFilter();
    if (filter == null) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {

      Map<Column<IndexedRepresentation, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<IndexedRepresentation, ?>, List<String>>();
      columnSortingKeyMap.put(idColumn, Arrays.asList(RodaConstants.REPRESENTATION_ID));
      columnSortingKeyMap.put(originalColumn, Arrays.asList(RodaConstants.REPRESENTATION_ORIGINAL));
      columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.REPRESENTATION_TYPE));
      columnSortingKeyMap.put(sizeInBytesColumn, Arrays.asList(RodaConstants.REPRESENTATION_SIZE_IN_BYTES));
      columnSortingKeyMap.put(totalNumberOfFilesColumn,
        Arrays.asList(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES));

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      BrowserService.Util.getInstance().find(IndexedRepresentation.class.getName(), filter, sorter, sublist,
        getFacets(), LocaleInfo.getCurrentLocale().getLocaleName(), callback);
    }
  }

}
