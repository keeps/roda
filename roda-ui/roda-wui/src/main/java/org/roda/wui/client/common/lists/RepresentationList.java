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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class RepresentationList extends BasicAsyncTableCell<IndexedRepresentation> {

  private static final ClientLogger LOGGER = new ClientLogger(RepresentationList.class.toString());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<IndexedRepresentation> statesColumn;
  private Column<IndexedRepresentation, SafeHtml> typeColumn;
  private TextColumn<IndexedRepresentation> sizeInBytesColumn;
  private TextColumn<IndexedRepresentation> numberOfDataFilesColumn;
  private Column<IndexedRepresentation, Date> createdOnColumn;
  private Column<IndexedRepresentation, Date> updatedOnColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ORIGINAL,
    RodaConstants.REPRESENTATION_TYPE, RodaConstants.REPRESENTATION_SIZE_IN_BYTES,
    RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES, RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FOLDERS,
    RodaConstants.REPRESENTATION_CREATED_ON, RodaConstants.REPRESENTATION_UPDATED_ON,
    RodaConstants.REPRESENTATION_STATES);

  public RepresentationList() {
    this(null, false, null, null, false);
  }

  public RepresentationList(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable) {
    super(IndexedRepresentation.class, filter, justActive, facets, summary, selectable, fieldsToReturn);
  }

  public RepresentationList(Filter filter, boolean justActive, Facets facets, String summary, boolean selectable,
    int initialPageSize, int pageSizeIncrement) {
    super(IndexedRepresentation.class, filter, justActive, facets, summary, selectable, initialPageSize,
      pageSizeIncrement, fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedRepresentation> display) {

    statesColumn = new TextColumn<IndexedRepresentation>() {
      @Override
      public String getValue(IndexedRepresentation rep) {
        List<String> translatedStates = new ArrayList<>();
        for (String state : rep.getRepresentationStates()) {
          translatedStates.add(messages.statusLabel(state));
        }
        return StringUtils.prettyPrint(translatedStates);
      }
    };

    typeColumn = new Column<IndexedRepresentation, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedRepresentation rep) {
        SafeHtml ret;
        if (rep == null) {
          LOGGER.error("Trying to display a NULL item");
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
        return rep != null ? messages.numberOfFiles(rep.getNumberOfDataFiles(), rep.getNumberOfDataFolders()) : null;
      }
    };

    createdOnColumn = new Column<IndexedRepresentation, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(IndexedRepresentation representation) {
        return representation != null ? representation.getCreatedOn() : null;
      }
    };

    updatedOnColumn = new Column<IndexedRepresentation, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM))) {
      @Override
      public Date getValue(IndexedRepresentation representation) {
        return representation != null ? representation.getUpdatedOn() : null;
      }
    };

    /* add sortable */
    statesColumn.setSortable(true);
    typeColumn.setSortable(true);
    sizeInBytesColumn.setSortable(true);
    numberOfDataFilesColumn.setSortable(true);
    createdOnColumn.setSortable(true);
    updatedOnColumn.setSortable(true);

    display.addColumn(typeColumn, messages.representationType());
    display.addColumn(numberOfDataFilesColumn, messages.representationFiles());
    display.addColumn(sizeInBytesColumn, messages.representationSize());
    display.addColumn(statesColumn, messages.representationStatus());
    display.addColumn(createdOnColumn, messages.objectCreatedDate());
    display.addColumn(updatedOnColumn, messages.objectLastModified());

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    statesColumn.setCellStyleNames("nowrap");
    typeColumn.setCellStyleNames("nowrap");
    sizeInBytesColumn.setCellStyleNames("nowrap");
    numberOfDataFilesColumn.setCellStyleNames("nowrap");
    createdOnColumn.setCellStyleNames("nowrap");
    updatedOnColumn.setCellStyleNames("nowrap");

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(typeColumn, true));

    addStyleName("my-representation-table");
    emptyInfo.addStyleName("my-representation-empty-info");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedRepresentation, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(statesColumn, Arrays.asList(RodaConstants.REPRESENTATION_STATES));
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.REPRESENTATION_TYPE));
    columnSortingKeyMap.put(sizeInBytesColumn, Arrays.asList(RodaConstants.REPRESENTATION_SIZE_IN_BYTES));
    columnSortingKeyMap.put(numberOfDataFilesColumn, Arrays.asList(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES));
    columnSortingKeyMap.put(createdOnColumn, Arrays.asList(RodaConstants.REPRESENTATION_CREATED_ON));
    columnSortingKeyMap.put(updatedOnColumn, Arrays.asList(RodaConstants.REPRESENTATION_UPDATED_ON));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
