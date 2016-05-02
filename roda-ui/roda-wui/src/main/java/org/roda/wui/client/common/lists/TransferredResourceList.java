/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class TransferredResourceList extends BasicAsyncTableCell<TransferredResource> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<TransferredResource, SafeHtml> isFileColumn;
  private TextColumn<TransferredResource> nameColumn;
  private TextColumn<TransferredResource> sizeColumn;
  private Column<TransferredResource, Date> creationDateColumn;

  public TransferredResourceList() {
    this(null, null, null, false);
  }

  public TransferredResourceList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
    super.setSelectedClass(TransferredResource.class);
  }

  public TransferredResourceList(Filter filter, Facets facets, String summary, boolean selectable, int initialPageSize,
    int pageSizeIncrement) {
    super(filter, facets, summary, selectable, initialPageSize, pageSizeIncrement);
  }

  @Override
  protected void configureDisplay(final CellTable<TransferredResource> display) {

    isFileColumn = new Column<TransferredResource, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(TransferredResource r) {
        SafeHtml ret;
        if (r == null) {
          logger.error("Trying to display a NULL item");
          ret = null;
        } else if (r.isFile()) {
          ret = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>");
        } else {
          ret = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");
        }
        return ret;
      }
    };

    nameColumn = new TextColumn<TransferredResource>() {

      @Override
      public String getValue(TransferredResource r) {
        return r != null ? r.getName() : null;
      }
    };

    sizeColumn = new TextColumn<TransferredResource>() {

      @Override
      public String getValue(TransferredResource r) {
        return r != null ? Humanize.readableFileSize(r.getSize()) : "";
      }
    };

    creationDateColumn = new Column<TransferredResource, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(TransferredResource r) {
        return r != null ? r.getCreationDate() : null;
      }
    };

    isFileColumn.setSortable(true);
    nameColumn.setSortable(true);
    sizeColumn.setSortable(true);
    creationDateColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(isFileColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-files-o'></i>"));
    display.addColumn(nameColumn, "Name");

    Header<String> sizeHeader = new TextHeader("Size");

    Header<String> sizeFooter = new Header<String>(new TextCell()) {
      @Override
      public String getValue() {
        List<TransferredResource> items = display.getVisibleItems();
        if (items.size() == 0) {
          return "";
        } else {
          long totalSize = 0;
          for (TransferredResource item : items) {
            if (item != null) {
              totalSize += item.getSize();
            }
          }
          return totalSize > 0 ? Humanize.readableFileSize(totalSize) : "";
        }
      }
    };

    display.addColumn(sizeColumn, sizeHeader, sizeFooter);
    display.addColumn(creationDateColumn, "Date created");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(nameColumn, "100%");

    addStyleName("my-list-transferredResource");
    emptyInfo.addStyleName("my-list-transferredResource-empty-info");

    // idColumn.setCellStyleNames("nowrap");
    sizeHeader.setHeaderStyleNames("text-align-right");
    sizeFooter.setHeaderStyleNames("text-align-right");
    sizeColumn.setCellStyleNames("nowrap my-collections-table-cell-alignright");
    creationDateColumn.setCellStyleNames("nowrap my-collections-table-cell-alignright");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<TransferredResource>> callback) {

    Filter filter = getFilter();

    Map<Column<TransferredResource, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<TransferredResource, ?>, List<String>>();
    columnSortingKeyMap.put(isFileColumn, Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_ISFILE));
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_NAME));
    columnSortingKeyMap.put(sizeColumn, Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_SIZE));
    columnSortingKeyMap.put(creationDateColumn, Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_DATE));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().find(TransferredResource.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

}
