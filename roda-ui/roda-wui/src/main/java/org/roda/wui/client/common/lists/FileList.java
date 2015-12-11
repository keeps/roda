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
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SimpleFile;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
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

public class FileList extends AsyncTableCell<SimpleFile> {

  private static final int PAGE_SIZE = 20;

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<SimpleFile, SafeHtml> iconColumn;
  private TextColumn<SimpleFile> filenameColumn;
  private TextColumn<SimpleFile> mimetypeColumn;
  private TextColumn<SimpleFile> lengthColumn;

  public FileList() {
    this(null, null, null);
  }

  public FileList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
  }

  @Override
  protected void configureDisplay(CellTable<SimpleFile> display) {
    iconColumn = new Column<SimpleFile, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(SimpleFile file) {
        if (file.isFile()) {
          return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>");
        } else {
          return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");
        }
      }
    };

    filenameColumn = new TextColumn<SimpleFile>() {

      @Override
      public String getValue(SimpleFile file) {
        return file.getOriginalName();
      }
    };

    mimetypeColumn = new TextColumn<SimpleFile>() {

      @Override
      public String getValue(SimpleFile file) {
        return (file.getFileFormat() != null && file.getFileFormat().getMimeType() != null
          && !file.getFileFormat().getMimeType().isEmpty()) ? file.getFileFormat().getMimeType() : "";
      }
    };

    lengthColumn = new TextColumn<SimpleFile>() {

      @Override
      public String getValue(SimpleFile file) {
        return Humanize.readableFileSize(file.getSize());
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
    GWT.log("Defining default sorting");
    display.getColumnSortList().push(new ColumnSortInfo(filenameColumn, false));

    addStyleName("my-files-table");
    emptyInfo.addStyleName("my-files-empty-info");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<SimpleFile>> callback) {
    GWT.log("Getting data");
    Filter filter = getFilter();
    if (filter == null) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {

      Map<Column<SimpleFile, ?>, String> columnSortingKeyMap = new HashMap<Column<SimpleFile, ?>, String>();
      columnSortingKeyMap.put(filenameColumn, RodaConstants.FILE_ORIGINALNAME);
      columnSortingKeyMap.put(lengthColumn, RodaConstants.FILE_SIZE);
      columnSortingKeyMap.put(mimetypeColumn, RodaConstants.FILE_FORMAT_MIMETYPE);

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      BrowserService.Util.getInstance().getRepresentationFiles(filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), callback);
    }
  }

  @Override
  protected ProvidesKey<SimpleFile> getKeyProvider() {
    return new ProvidesKey<SimpleFile>() {

      @Override
      public Object getKey(SimpleFile item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
