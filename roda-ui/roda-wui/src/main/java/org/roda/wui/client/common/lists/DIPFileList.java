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
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class DIPFileList extends BasicAsyncTableCell<DIPFile> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<DIPFile> idColumn;
  private TextColumn<DIPFile> sizeColumn;

  public DIPFileList() {
    this(null, null, null, false);
  }

  public DIPFileList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(DIPFile.class, filter, facets, summary, selectable);
  }

  public DIPFileList(Filter filter, Facets facets, String summary, boolean selectable, int initialPageSize,
    int pageSizeIncrement) {
    super(DIPFile.class, filter, facets, summary, selectable, initialPageSize, pageSizeIncrement);
  }

  @Override
  protected void configureDisplay(CellTable<DIPFile> display) {

    idColumn = new TextColumn<DIPFile>() {

      @Override
      public String getValue(DIPFile file) {
        return file != null ? file.getId() : null;
      }
    };

    sizeColumn = new TextColumn<DIPFile>() {

      @Override
      public String getValue(DIPFile file) {
        return file != null ? Long.toString(file.getSize()) : null;
      }
    };

    idColumn.setSortable(true);
    sizeColumn.setSortable(true);

    display.addColumn(idColumn, messages.fileId());
    display.addColumn(sizeColumn, messages.fileSize());

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(idColumn, true));

    // display.setColumnWidth(titleColumn, 7.0, Unit.EM);
    display.setColumnWidth(sizeColumn, 13.0, Unit.EM);

    sizeColumn.setCellStyleNames("nowrap");

    addStyleName("my-collections-table");
    emptyInfo.addStyleName("my-collections-empty-info");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<DIPFile, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<DIPFile, ?>, List<String>>();
    // setting secondary sorter to title
    columnSortingKeyMap.put(idColumn, Arrays.asList(RodaConstants.DIP_FILE_ID));
    columnSortingKeyMap.put(sizeColumn, Arrays.asList(RodaConstants.DIP_FILE_SIZE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
