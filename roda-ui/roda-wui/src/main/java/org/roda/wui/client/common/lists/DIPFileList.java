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

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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

  private Column<DIPFile, SafeHtml> iconColumn;
  private TextColumn<DIPFile> idColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.DIPFILE_IS_DIRECTORY, RodaConstants.DIPFILE_ID, RodaConstants.DIPFILE_DIP_ID);

  public DIPFileList() {
    this(null, null, null, false);
  }

  public DIPFileList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(DIPFile.class, filter, facets, summary, selectable, fieldsToReturn);
  }

  public DIPFileList(Filter filter, Facets facets, String summary, boolean selectable, int initialPageSize,
    int pageSizeIncrement) {
    super(DIPFile.class, filter, facets, summary, selectable, initialPageSize, pageSizeIncrement, fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<DIPFile> display) {

    iconColumn = new Column<DIPFile, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(DIPFile file) {
        if (file != null) {
          if (file.isDirectory()) {
            return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");
          } else {
            return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>");
          }
        } else {
          logger.error("Trying to display a NULL item");
        }
        return null;
      }
    };

    idColumn = new TextColumn<DIPFile>() {

      @Override
      public String getValue(DIPFile file) {
        return file != null ? file.getId() : null;
      }
    };

    iconColumn.setSortable(true);
    idColumn.setSortable(true);

    addColumn(iconColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-files-o'></i>"), false, false, 2);
    display.addColumn(idColumn, messages.fileName());

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(idColumn, true));
    display.setColumnWidth(iconColumn, 2.5, Unit.EM);

    addStyleName("my-collections-table");
    emptyInfo.addStyleName("my-collections-empty-info");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<DIPFile, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    // setting secondary sorter to title
    columnSortingKeyMap.put(iconColumn, Arrays.asList(RodaConstants.DIPFILE_IS_DIRECTORY));
    columnSortingKeyMap.put(idColumn, Arrays.asList(RodaConstants.DIPFILE_ID));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
