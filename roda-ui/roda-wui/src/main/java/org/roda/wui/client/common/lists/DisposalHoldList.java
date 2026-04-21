package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalHoldList extends AsyncTableCell<DisposalHold> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.DISPOSAL_HOLD_TITLE, RodaConstants.DISPOSAL_HOLD_MANDATE,
    RodaConstants.DISPOSAL_HOLD_SCOPE_NOTES, RodaConstants.DISPOSAL_HOLD_STATE);

  private TextColumn<DisposalHold> titleColumn;
  private TextColumn<DisposalHold> mandateColumn;
  private TextColumn<DisposalHold> scopeNotesColumn;
  private Column<DisposalHold, SafeHtml> stateColumn;

  @Override
  protected void adjustOptions(AsyncTableCellOptions<DisposalHold> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<DisposalHold> display) {
    titleColumn = new TextColumn<DisposalHold>() {
      @Override
      public String getValue(DisposalHold hold) {
        return hold != null ? hold.getTitle() : null;
      }
    };

    mandateColumn = new TextColumn<DisposalHold>() {
      @Override
      public String getValue(DisposalHold hold) {
        return hold != null ? hold.getMandate() : null;
      }
    };

    scopeNotesColumn = new TextColumn<DisposalHold>() {
      @Override
      public String getValue(DisposalHold hold) {
        return hold != null ? hold.getScopeNotes() : null;
      }
    };

    stateColumn = new Column<DisposalHold, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(DisposalHold hold) {
        SafeHtml ret = null;
        if (hold != null) {
          ret = HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
        }

        return ret;
      }
    };

    titleColumn.setSortable(true);
    mandateColumn.setSortable(true);
    scopeNotesColumn.setSortable(true);
    stateColumn.setSortable(true);

    addColumn(titleColumn, messages.disposalHoldTitle(), false, false);
    addColumn(mandateColumn, messages.disposalHoldMandate(), false, false, 20);
    addColumn(scopeNotesColumn, messages.disposalHoldNotes(), false, false, 10);
    addColumn(stateColumn, messages.disposalHoldStateCol(), false, false, 9);

    // default sorting
    display.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(titleColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<DisposalHold, ?>, List<String>> columnSortingKeyMap = new HashMap<>();

    columnSortingKeyMap.put(titleColumn, Collections.singletonList(RodaConstants.DISPOSAL_HOLD_TITLE));
    columnSortingKeyMap.put(mandateColumn, Collections.singletonList(RodaConstants.DISPOSAL_HOLD_MANDATE));
    columnSortingKeyMap.put(scopeNotesColumn, Collections.singletonList(RodaConstants.DISPOSAL_HOLD_SCOPE_NOTES));
    columnSortingKeyMap.put(stateColumn, Collections.singletonList(RodaConstants.DISPOSAL_HOLD_STATE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }
}
