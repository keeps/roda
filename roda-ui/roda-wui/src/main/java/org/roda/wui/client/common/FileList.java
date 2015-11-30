/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.File;
import org.roda.core.data.v2.IndexResult;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.AsyncTableCell;

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

public class FileList extends AsyncTableCell<File> {

  private static final int PAGE_SIZE = 20;

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<File, SafeHtml> iconColumn;
  private TextColumn<File> filenameColumn;
  private TextColumn<File> mimetypeColumn;
  private TextColumn<File> lengthColumn;

  public FileList() {
    this(null, null, null);
  }

  public FileList(Filter filter, Facets facets, String summary) {
    super(filter, facets, summary);
  }

  @Override
  protected void configureDisplay(CellTable<File> display) {
    iconColumn = new Column<File, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(File arg0) {
        // TODO Auto-generated method stub
        return null;
      }
    };

    filenameColumn = new TextColumn<File>() {

      @Override
      public String getValue(File file) {
        return file.getFilename();
      }
    };

    mimetypeColumn = new TextColumn<File>() {

      @Override
      public String getValue(File file) {
        return (file.getFileFormat() != null && !file.getFileFormat().getMimeType().isEmpty())
          ? file.getFileFormat().getMimeType() : "";
      }
    };

    lengthColumn = new TextColumn<File>() {

      @Override
      public String getValue(File file) {
        return file.getLength();
      }
    };

    /* add sortable */
    filenameColumn.setSortable(true);
    mimetypeColumn.setSortable(true);
    lengthColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(iconColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-tag'></i>"));
    display.addColumn(filenameColumn, "Filename");
    display.addColumn(mimetypeColumn, "Mimetype");
    display.addColumn(lengthColumn, "Length");
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
  protected void getData(int start, int length, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<File>> callback) {
    GWT.log("Getting data");
    Filter filter = getFilter();
    if (filter == null) {
      // search not yet ready, deliver empty result
      callback.onSuccess(null);
    } else {
      // calculate sorter
      // TODO define sorters
      Sorter sorter = new Sorter();
      for (int i = 0; i < columnSortList.size(); i++) {
        ColumnSortInfo columnSortInfo = columnSortList.get(i);
        String sortParameterKey;
        if (columnSortInfo.getColumn().equals(filenameColumn)) {
          sortParameterKey = RodaConstants.SDO_TITLE;
        } else if (columnSortInfo.getColumn().equals(lengthColumn)) {
          sortParameterKey = RodaConstants.SDO_DATE_INITIAL;
        } else if (columnSortInfo.getColumn().equals(mimetypeColumn)) {
          sortParameterKey = RodaConstants.SDO_DATE_FINAL;
        } else {
          sortParameterKey = null;
        }

        if (sortParameterKey != null) {
          sorter.add(new SortParameter(sortParameterKey, !columnSortInfo.isAscending()));
        } else {
          logger.warn("Selecting a sorter that is not mapped");
        }
      }

      // define sublist
      Sublist sublist = new Sublist(start, length);

      // TODO correct method
      BrowserService.Util.getInstance().getRepresentationFiles(filter, sorter, sublist, getFacets(),
        LocaleInfo.getCurrentLocale().getLocaleName(), callback);
    }
  }

  @Override
  protected ProvidesKey<File> getKeyProvider() {
    return new ProvidesKey<File>() {

      @Override
      public Object getKey(File item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
