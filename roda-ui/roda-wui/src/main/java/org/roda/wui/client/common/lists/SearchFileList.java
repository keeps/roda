/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

public class SearchFileList extends AsyncTableCell<IndexedFile> {

  private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<IndexedFile, SafeHtml> iconColumn;
  private Column<IndexedFile, SafeHtml> pathColumn;
  private TextColumn<IndexedFile> formatColumn;
  private TextColumn<IndexedFile> sizeColumn;
  private boolean showFilePath;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_AIP_ID,
    RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_ISDIRECTORY, RodaConstants.FILE_REPRESENTATION_UUID,
    RodaConstants.FILE_ORIGINALNAME, RodaConstants.FILE_FILE_ID, RodaConstants.FILE_PATH, RodaConstants.FILE_SIZE,
    RodaConstants.FILE_FORMAT_VERSION, RodaConstants.FILE_FILEFORMAT);

  public SearchFileList() {
    this(false);
  }

  public SearchFileList(boolean showFilePath) {
    super();
    this.showFilePath = showFilePath;
  }

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedFile> options) {
    options.withFieldsToReturn(fieldsToReturn);
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

    pathColumn = new Column<IndexedFile, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(IndexedFile file) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        if (file != null) {
          List<String> filePath = file.getPath();
          String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
          List<String> fullpath = new ArrayList<>(filePath);
          fullpath.add(fileName);
          b.append(SafeHtmlUtils.fromSafeConstant("<div title='"));
          b.append(SafeHtmlUtils.fromString(StringUtils.join(fullpath, "/")));
          b.append(SafeHtmlUtils.fromSafeConstant("'>"));
          if (showFilePath && filePath != null && !filePath.isEmpty()) {
            String path = StringUtils.join(filePath, "/");
            b.append(SafeHtmlUtils.fromSafeConstant("<span class='table-file-path'>"));
            b.append(SafeHtmlUtils.fromString(path));
            b.append(SafeHtmlUtils.fromSafeConstant("/"));
            b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
          }

          b.append(SafeHtmlUtils.fromString(fileName));
          b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
        }

        return b.toSafeHtml();
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

    sizeColumn = new TextColumn<IndexedFile>() {

      @Override
      public String getValue(IndexedFile file) {
        return (file != null && file.getSize() > 0) ? Humanize.readableFileSize(file.getSize()) : "";
      }
    };

    /* add sortable */
    iconColumn.setSortable(true);
    pathColumn.setSortable(true);
    formatColumn.setSortable(true);
    sizeColumn.setSortable(true);

    addColumn(iconColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-files-o'></i>"), false, false, 3);
    addColumn(pathColumn, messages.filePath(), true, false);
    addColumn(formatColumn, messages.fileFormat(), true, false);
    addColumn(sizeColumn, messages.fileSize(), true, false, 7);

    // define column width priority
    display.setColumnWidth(iconColumn, 3.0, Unit.EM);
    display.setColumnWidth(sizeColumn, 6.0, Unit.EM);

    pathColumn.setCellStyleNames("text-align-left");
    formatColumn.setCellStyleNames("text-align-left");
    sizeColumn.setCellStyleNames("text-align-right");

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(pathColumn, true));
    addStyleName("my-files-table");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedFile, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(iconColumn, Arrays.asList(RodaConstants.FILE_ISDIRECTORY));
    columnSortingKeyMap.put(pathColumn, Arrays.asList(RodaConstants.FILE_ORIGINALNAME, RodaConstants.FILE_FILE_ID));
    columnSortingKeyMap.put(sizeColumn, Arrays.asList(RodaConstants.FILE_SIZE));
    columnSortingKeyMap.put(formatColumn, Arrays.asList(RodaConstants.FILE_FORMAT_MIMETYPE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
