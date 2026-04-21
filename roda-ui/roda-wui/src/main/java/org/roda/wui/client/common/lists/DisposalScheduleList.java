package org.roda.wui.client.common.lists;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalScheduleList extends AsyncTableCell<DisposalSchedule> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.DISPOSAL_SCHEDULE_TITLE, RodaConstants.DISPOSAL_SCHEDULE_MANDATE,
    RodaConstants.DISPOSAL_SCHEDULE_SCOPE_NOTES, RodaConstants.DISPOSAL_SCHEDULE_STATE,
    RodaConstants.DISPOSAL_SCHEDULE_ACTION, RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_DURATION,
    RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_INTERVAL_CODE);

  private TextColumn<DisposalSchedule> titleColumn;
  private TextColumn<DisposalSchedule> mandateColumn;
  private TextColumn<DisposalSchedule> periodColumn;
  private TextColumn<DisposalSchedule> actionColumn;
  private Column<DisposalSchedule, SafeHtml> stateColumn;

  @Override
  protected void adjustOptions(AsyncTableCellOptions<DisposalSchedule> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<DisposalSchedule> display) {
    titleColumn = new TextColumn<DisposalSchedule>() {
      @Override
      public String getValue(DisposalSchedule schedule) {
        return schedule != null ? schedule.getTitle() : null;
      }
    };

    mandateColumn = new TextColumn<DisposalSchedule>() {
      @Override
      public String getValue(DisposalSchedule schedule) {
        return schedule != null ? schedule.getMandate() : null;
      }
    };

    periodColumn = new TextColumn<DisposalSchedule>() {
      @Override
      public String getValue(DisposalSchedule schedule) {
        if (schedule.getRetentionPeriodDuration() == null && schedule.getRetentionPeriodIntervalCode() == null) {
          return "";
        } else if (schedule.getRetentionPeriodDuration() == null) {
          return messages.retentionPeriod(0, schedule.getRetentionPeriodIntervalCode().toString());
        } else {
          return messages.retentionPeriod(schedule.getRetentionPeriodDuration(),
            schedule.getRetentionPeriodIntervalCode().toString());
        }
      }
    };

    actionColumn = new TextColumn<DisposalSchedule>() {
      @Override
      public String getValue(DisposalSchedule schedule) {
        return messages.disposalScheduleAction(schedule.getActionCode().toString());
      }
    };

    stateColumn = new Column<DisposalSchedule, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(DisposalSchedule schedule) {
        SafeHtml ret = null;
        if (schedule != null) {
          ret = HtmlSnippetUtils.getDisposalScheduleStateHtml(schedule);
        }

        return ret;
      }
    };

    titleColumn.setSortable(true);
    mandateColumn.setSortable(true);
    periodColumn.setSortable(true);
    actionColumn.setSortable(true);
    stateColumn.setSortable(true);

    addColumn(titleColumn, messages.disposalScheduleTitle(), false, false);
    addColumn(mandateColumn, messages.disposalScheduleMandate(), false, false, 20);
    addColumn(periodColumn, messages.disposalSchedulePeriod(), false, false, 15);
    addColumn(actionColumn, messages.disposalScheduleActionCol(), false, false, 15);
    addColumn(stateColumn, messages.disposalScheduleStateCol(), false, false, 9);

    // default sorting
    display.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(titleColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<DisposalSchedule, ?>, List<String>> columnSortingKeyMap = new HashMap<>();

    columnSortingKeyMap.put(titleColumn, Collections.singletonList(RodaConstants.DISPOSAL_SCHEDULE_TITLE));
    columnSortingKeyMap.put(mandateColumn, Collections.singletonList(RodaConstants.DISPOSAL_SCHEDULE_MANDATE));
    columnSortingKeyMap.put(periodColumn,
      Collections.singletonList(RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_DURATION));
    columnSortingKeyMap.put(actionColumn, Collections.singletonList(RodaConstants.DISPOSAL_SCHEDULE_ACTION));
    columnSortingKeyMap.put(stateColumn, Collections.singletonList(RodaConstants.DISPOSAL_SCHEDULE_STATE));
    return createSorter(columnSortList, columnSortingKeyMap);
  }
}
