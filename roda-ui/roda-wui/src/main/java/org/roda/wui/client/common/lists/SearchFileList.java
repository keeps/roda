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
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class SearchFileList extends BasicAsyncTableCell<IndexedFile> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<IndexedFile, SafeHtml> iconColumn;
  private TextColumn<IndexedFile> pathColumn;
  private TextColumn<IndexedFile> filenameColumn;
  private TextColumn<IndexedFile> formatColumn;
  private TextColumn<IndexedFile> lengthColumn;

  public SearchFileList() {
    this(null, true, null, null, false);
  }

  public SearchFileList(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable) {
    super(filter, justActive, facets, summary, selectable);
    super.setSelectedClass(IndexedFile.class);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedFile> display) {
    iconColumn = new Column<IndexedFile, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(IndexedFile file) {
        if (file != null) {
          if (file.isDirectory()) {
            return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-open'></i>");
          } else {
            return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>");
          }
        } else {
          logger.error("Trying to display a NULL item");
        }
        return null;
      }
    };

    pathColumn = new TextColumn<IndexedFile>() {

      @Override
      public String getValue(IndexedFile file) {
        String path = null;
        if (file != null) {
          if (file.getPath() != null) {
            path = Tools.join(file.getPath(), "/");
          } else {
            path = "";
          }
        }

        return path;
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

    formatColumn = new TextColumn<IndexedFile>() {

      @Override
      public String getValue(IndexedFile file) {
        if (file != null && file.getFileFormat() != null) {
          FileFormat format = file.getFileFormat();
          String ret;
          if (StringUtils.isNotBlank(format.getFormatDesignationName())) {
            ret = format.getFormatDesignationName();
            if (StringUtils.isNotBlank(format.getFormatDesignationVersion())) {
              ret = ret + " " + format.getFormatDesignationVersion();
            }
          } else if (StringUtils.isNotBlank(format.getPronom())) {
            ret = format.getPronom();
          } else if (StringUtils.isNotBlank(format.getMimeType())) {
            ret = format.getMimeType();
          } else {
            ret = null;
          }
          return ret;

        } else {
          return null;
        }
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
    formatColumn.setSortable(true);
    lengthColumn.setSortable(true);

    // TODO externalize strings into constants

    addColumn(iconColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-files-o'></i>"), false, false, 3);
    addColumn(filenameColumn, messages.fileName(), true, false);
    addColumn(pathColumn, messages.filePath(), true, false);
    addColumn(formatColumn, messages.fileFormat(), true, false);
    addColumn(lengthColumn, messages.fileLength(), true, false, 7);

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(filenameColumn, false));

    addStyleName("my-files-table");

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
      columnSortingKeyMap.put(lengthColumn, Arrays.asList(RodaConstants.FILE_SIZE));
      columnSortingKeyMap.put(formatColumn, Arrays.asList(RodaConstants.FILE_FORMAT_MIMETYPE));

      Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

      BrowserService.Util.getInstance().find(IndexedFile.class.getName(), filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), getJustActive(), callback);
    }
  }
}
