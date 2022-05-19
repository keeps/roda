/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 *
 */
public class DisposalConfirmationList extends AsyncTableCell<DisposalConfirmation> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<DisposalConfirmation> titleColumn;
  private Column<DisposalConfirmation, Date> createdOnColumn;
  private TextColumn<DisposalConfirmation> createdByColumn;
  private Column<DisposalConfirmation, SafeHtml> stateColumn;
  private TextColumn<DisposalConfirmation> numberOfAIPsColumn;
  private TextColumn<DisposalConfirmation> sizeColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.DISPOSAL_CONFIRMATION_TITLE, RodaConstants.DISPOSAL_CONFIRMATION_ID,
    RodaConstants.DISPOSAL_CONFIRMATION_CREATED_BY, RodaConstants.DISPOSAL_CONFIRMATION_STATE,
    RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON, RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS,
    RodaConstants.DISPOSAL_CONFIRMATION_STORAGE_SIZE);

  @Override
  protected void adjustOptions(AsyncTableCellOptions<DisposalConfirmation> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<DisposalConfirmation> display) {

    titleColumn = new TextColumn<DisposalConfirmation>() {
      @Override
      public String getValue(DisposalConfirmation confirmation) {
        return confirmation != null ? confirmation.getTitle() : null;
      }
    };

    createdByColumn = new TextColumn<DisposalConfirmation>() {
      @Override
      public String getValue(DisposalConfirmation confirmation) {
        return confirmation != null ? confirmation.getCreatedBy() : null;
      }
    };

    createdOnColumn = new Column<DisposalConfirmation, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(DisposalConfirmation confirmation) {
        return confirmation != null ? confirmation.getCreatedOn() : null;
      }
    };

    stateColumn = new Column<DisposalConfirmation, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(DisposalConfirmation confirmation) {
        SafeHtml ret = null;
        if (confirmation != null) {
          ret = HtmlSnippetUtils.getDisposalConfirmationStateHTML(confirmation.getState());
        }

        return ret;
      }
    };

    numberOfAIPsColumn = new TextColumn<DisposalConfirmation>() {
      @Override
      public String getValue(DisposalConfirmation confirmation) {
        return Long.toString(confirmation.getNumberOfAIPs());
      }
    };

    sizeColumn = new TextColumn<DisposalConfirmation>() {

      @Override
      public String getValue(DisposalConfirmation confirmation) {
        return confirmation != null ? Humanize.readableFileSize(confirmation.getSize()) : "";
      }
    };

    titleColumn.setSortable(true);
    createdOnColumn.setSortable(true);
    createdByColumn.setSortable(true);
    stateColumn.setSortable(true);
    numberOfAIPsColumn.setSortable(true);
    sizeColumn.setSortable(true);

    addColumn(titleColumn, messages.disposalConfirmationTitle(), false, false);
    addColumn(createdOnColumn, messages.disposalConfirmationCreationDate(), false, false, 12);
    addColumn(createdByColumn, messages.disposalConfirmationCreationBy(), false, false, 10);
    addColumn(stateColumn, messages.disposalConfirmationStatus(), false, false, 9);
    addColumn(numberOfAIPsColumn, messages.disposalConfirmationAIPs(), false, false, 5);
    addColumn(sizeColumn, messages.disposalConfirmationSize(), false, false, 8);

    // default sorting
    display.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(stateColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<DisposalConfirmation, ?>, List<String>> columnSortingKeyMap = new HashMap<>();

    columnSortingKeyMap.put(titleColumn, Collections.singletonList(RodaConstants.DISPOSAL_CONFIRMATION_TITLE));
    columnSortingKeyMap.put(createdOnColumn, Collections.singletonList(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON));
    columnSortingKeyMap.put(createdByColumn, Collections.singletonList(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_BY));
    columnSortingKeyMap.put(stateColumn,
      Arrays.asList(RodaConstants.DISPOSAL_CONFIRMATION_STATE, RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON));
    columnSortingKeyMap.put(sizeColumn, Collections.singletonList(RodaConstants.DISPOSAL_CONFIRMATION_STORAGE_SIZE));
    columnSortingKeyMap.put(numberOfAIPsColumn,
      Collections.singletonList(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

  @Override
  protected ProvidesKey<DisposalConfirmation> getKeyProvider() {
    return DisposalConfirmation::getId;
  }
}
