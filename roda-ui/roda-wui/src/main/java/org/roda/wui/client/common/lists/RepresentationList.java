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
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class RepresentationList extends BasicAsyncTableCell<IndexedRepresentation> {

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<IndexedRepresentation> originalColumn;
  private Column<IndexedRepresentation, SafeHtml> typeColumn;
  private TextColumn<IndexedRepresentation> sizeInBytesColumn;
  private TextColumn<IndexedRepresentation> numberOfDataFilesColumn;

  public RepresentationList() {
    this(null, false, null, null, false);
  }

  public RepresentationList(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable) {
    super(IndexedRepresentation.class, filter, justActive, facets, summary, selectable);
  }

  public RepresentationList(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable,
    int initialPageSize, int pageSizeIncrement) {
    super(IndexedRepresentation.class, filter, justActive, facets, summary, selectable, initialPageSize,
      pageSizeIncrement);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedRepresentation> display) {

    originalColumn = new TextColumn<IndexedRepresentation>() {

      @Override
      public String getValue(IndexedRepresentation rep) {
        return rep != null ? rep.isOriginal() ? messages.originalRepresentation() : messages.alternativeRepresentation()
          : null;
      }
    };

    typeColumn = new Column<IndexedRepresentation, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedRepresentation rep) {
        SafeHtml ret;
        if (rep == null) {
          logger.error("Trying to display a NULL item");
          ret = null;
        } else {
          ret = DescriptionLevelUtils.getRepresentationTypeIcon(rep.getType(), true);
        }
        return ret;
      }
    };

    sizeInBytesColumn = new TextColumn<IndexedRepresentation>() {

      @Override
      public String getValue(IndexedRepresentation rep) {
        return rep != null ? Humanize.readableFileSize(rep.getSizeInBytes()) : null;
      }
    };

    numberOfDataFilesColumn = new TextColumn<IndexedRepresentation>() {

      @Override
      public String getValue(IndexedRepresentation rep) {
        return rep != null ? messages.numberOfFiles(rep.getNumberOfDataFiles()) : null;
      }
    };

    /* add sortable */
    originalColumn.setSortable(true);
    typeColumn.setSortable(true);
    sizeInBytesColumn.setSortable(true);
    numberOfDataFilesColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(typeColumn, messages.representationType());
    display.addColumn(numberOfDataFilesColumn, messages.representationFiles());
    display.addColumn(sizeInBytesColumn, messages.representationSize());
    display.addColumn(originalColumn, messages.representationOriginal());

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    // display.setColumnWidth(idColumn, "100%");

    originalColumn.setCellStyleNames("nowrap");
    typeColumn.setCellStyleNames("nowrap");
    sizeInBytesColumn.setCellStyleNames("nowrap");
    numberOfDataFilesColumn.setCellStyleNames("nowrap");

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(typeColumn, true));

    addStyleName("my-representation-table");
    emptyInfo.addStyleName("my-representation-empty-info");

  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedRepresentation, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<IndexedRepresentation, ?>, List<String>>();
    columnSortingKeyMap.put(originalColumn, Arrays.asList(RodaConstants.REPRESENTATION_ORIGINAL));
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.REPRESENTATION_TYPE));
    columnSortingKeyMap.put(sizeInBytesColumn, Arrays.asList(RodaConstants.REPRESENTATION_SIZE_IN_BYTES));
    columnSortingKeyMap.put(numberOfDataFilesColumn, Arrays.asList(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES));

    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
