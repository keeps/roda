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
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class SimpleFileList extends AsyncTableCell<IndexedFile, Boolean> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<IndexedFile, SafeHtml> iconColumn;
  private TextColumn<IndexedFile> filenameColumn;

  public SimpleFileList() {
    super(null, null, null, false, false);
  }

  public SimpleFileList(Filter filter, Facets facets, String summary, boolean selectable, boolean showInactive) {
    super(filter, facets, summary, selectable, showInactive);
    super.setSelectedClass(IndexedFile.class);
  }

  public SimpleFileList(Filter filter, Facets facets, String summary, boolean selectable, boolean showInactive,
    int initialPageSize, int pageSizeIncrement) {
    super(filter, facets, summary, selectable, initialPageSize, pageSizeIncrement, showInactive);
    super.setSelectedClass(IndexedFile.class);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedFile> display) {
    iconColumn = new Column<IndexedFile, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(IndexedFile file) {
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

    filenameColumn = new TextColumn<IndexedFile>() {

      @Override
      public String getValue(IndexedFile file) {
        String fileName = null;
        if (file != null) {
          fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
        }

        return fileName;
      }
    };

    /* add sortable */
    filenameColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(iconColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-files-o'></i>"));
    display.addColumn(filenameColumn, "Name");
    // display.addColumn(mimetypeColumn, "Mimetype");
    // display.addColumn(lengthColumn, "Length");
    display.setColumnWidth(iconColumn, "35px");
    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(filenameColumn, "100%");

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(filenameColumn, false));

    addStyleName("my-files-table");
    emptyInfo.addStyleName("my-files-empty-info");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<IndexedFile>> callback) {
    Filter filter = getFilter();
    if (filter == null) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {

      Map<Column<IndexedFile, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<IndexedFile, ?>, List<String>>();
      columnSortingKeyMap.put(filenameColumn, Arrays.asList(RodaConstants.FILE_ORIGINALNAME));

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      boolean showInactive = getObject();
      BrowserService.Util.getInstance().find(IndexedFile.class.getName(), filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), showInactive, callback);
    }
  }

}
