/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.HashMap;
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
import org.roda.wui.common.client.tools.Humanize;

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
import com.google.gwt.view.client.ProvidesKey;

public class FileList extends AsyncTableCell<IndexedFile> {

  private static final int PAGE_SIZE = 20;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<IndexedFile, SafeHtml> iconColumn;
  private TextColumn<IndexedFile> filenameColumn;
  private TextColumn<IndexedFile> mimetypeColumn;
  private TextColumn<IndexedFile> lengthColumn;

  public FileList() {
    this(null, null, null);
  }

  public FileList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
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
        return (file != null) ? file.getOriginalName() : null;
      }
    };

    mimetypeColumn = new TextColumn<IndexedFile>() {

      @Override
      public String getValue(IndexedFile file) {
        return (file != null && file.getFileFormat() != null && file.getFileFormat().getMimeType() != null
          && !file.getFileFormat().getMimeType().isEmpty()) ? file.getFileFormat().getMimeType() : "";
      }
    };

    lengthColumn = new TextColumn<IndexedFile>() {

      @Override
      public String getValue(IndexedFile file) {
        return (file != null) ? Humanize.readableFileSize(file.getSize()) : null;
      }
    };

    /* add sortable */
    filenameColumn.setSortable(true);
    mimetypeColumn.setSortable(true);
    lengthColumn.setSortable(true);

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

      Map<Column<IndexedFile, ?>, String> columnSortingKeyMap = new HashMap<Column<IndexedFile, ?>, String>();
      columnSortingKeyMap.put(filenameColumn, RodaConstants.FILE_ORIGINALNAME);
      columnSortingKeyMap.put(lengthColumn, RodaConstants.FILE_SIZE);
      columnSortingKeyMap.put(mimetypeColumn, RodaConstants.FILE_FORMAT_MIMETYPE);

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      BrowserService.Util.getInstance().getRepresentationFiles(filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), callback);
    }
  }

  @Override
  protected ProvidesKey<IndexedFile> getKeyProvider() {
    return new ProvidesKey<IndexedFile>() {

      @Override
      public Object getKey(IndexedFile item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
