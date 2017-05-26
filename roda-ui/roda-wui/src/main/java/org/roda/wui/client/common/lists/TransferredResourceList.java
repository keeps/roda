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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class TransferredResourceList extends BasicAsyncTableCell<TransferredResource> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private boolean addParentPath = false;

  private Column<TransferredResource, SafeHtml> isFileColumn;
  private Column<TransferredResource, SafeHtml> nameColumn;
  private TextColumn<TransferredResource> sizeColumn;
  private Column<TransferredResource, Date> creationDateColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.TRANSFERRED_RESOURCE_ID, RodaConstants.TRANSFERRED_RESOURCE_ISFILE,
    RodaConstants.TRANSFERRED_RESOURCE_NAME, RodaConstants.TRANSFERRED_RESOURCE_SIZE,
    RodaConstants.TRANSFERRED_RESOURCE_DATE, RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH);

  public TransferredResourceList() {
    this(null, null, null, false);
  }

  public TransferredResourceList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(TransferredResource.class, filter, facets, summary, selectable, fieldsToReturn);
    this.addParentPath = false;
  }

  public TransferredResourceList(Filter filter, Facets facets, String summary, boolean selectable,
    boolean addParentPath) {
    super(TransferredResource.class, filter, facets, summary, selectable, fieldsToReturn);
    this.addParentPath = addParentPath;
  }

  public TransferredResourceList(Filter filter, Facets facets, String summary, boolean selectable, int initialPageSize,
    int pageSizeIncrement) {
    super(TransferredResource.class, filter, facets, summary, selectable, initialPageSize, pageSizeIncrement,
      fieldsToReturn);
    this.addParentPath = false;
  }

  public TransferredResourceList(Filter filter, Facets facets, String summary, boolean selectable, int initialPageSize,
    int pageSizeIncrement, boolean addParentPath) {
    super(TransferredResource.class, filter, facets, summary, selectable, initialPageSize, pageSizeIncrement,
      fieldsToReturn);
    this.addParentPath = addParentPath;
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

    nameColumn = new Column<TransferredResource, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(TransferredResource r) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();

        if (r != null) {
          String relativePath = r.getRelativePath();
          b.append(SafeHtmlUtils.fromSafeConstant("<div title='"));
          b.append(SafeHtmlUtils.fromString(relativePath));
          b.append(SafeHtmlUtils.fromSafeConstant("'>"));
          if (relativePath != null && addParentPath) {
            String pathWithoutName = relativePath.substring(0, relativePath.lastIndexOf('/'));
            b.append(SafeHtmlUtils.fromSafeConstant("<span class='table-file-path'>"));
            b.append(SafeHtmlUtils.fromString(pathWithoutName));
            if (!pathWithoutName.isEmpty()) {
              b.append(SafeHtmlUtils.fromSafeConstant("/"));
            }
            b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
          }
          b.append(SafeHtmlUtils.fromString(r.getName()));
          b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
        }

        return b.toSafeHtml();
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

    addColumn(isFileColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-files-o'></i>"), false, false, 3);
    addColumn(nameColumn, messages.transferredResourceName(), true, false);
    addColumn(sizeColumn, messages.transferredResourceSize(), true, true, 7);
    addColumn(creationDateColumn, messages.transferredResourceDateCreated(), true, true, 11);

    addStyleName("my-list-transferredResource");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<TransferredResource, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(isFileColumn, Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_ISFILE));
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_NAME));
    columnSortingKeyMap.put(sizeColumn, Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_SIZE));
    columnSortingKeyMap.put(creationDateColumn, Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_DATE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
