package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalRuleList extends AsyncTableCell<DisposalRule> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.DISPOSAL_RULE_ORDER, RodaConstants.DISPOSAL_RULE_TITLE, RodaConstants.DISPOSAL_RULE_SELECTION_METHOD,
    RodaConstants.DISPOSAL_RULE_CONDITION_KEY, RodaConstants.DISPOSAL_RULE_CONDITION_VALUE);

  private TextColumn<DisposalRule> orderColumn;
  private TextColumn<DisposalRule> titleColumn;
  private TextColumn<DisposalRule> selectionMethodColumn;
  private TextColumn<DisposalRule> conditionColumn;

  @Override
  protected void adjustOptions(AsyncTableCellOptions<DisposalRule> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<DisposalRule> display) {
    orderColumn = new TextColumn<DisposalRule>() {
      @Override
      public String getValue(DisposalRule rule) {
        int showOrder = rule.getOrder() + 1;
        return "" + showOrder;
      }
    };

    titleColumn = new TextColumn<DisposalRule>() {
      @Override
      public String getValue(DisposalRule rule) {
        return rule != null ? rule.getTitle() : null;
      }
    };

    selectionMethodColumn = new TextColumn<DisposalRule>() {
      @Override
      public String getValue(DisposalRule rule) {
        return rule != null ? messages.disposalRuleTypeValue(rule.getType().toString()) : null;
      }
    };

    conditionColumn = new TextColumn<DisposalRule>() {
      @Override
      public String getValue(DisposalRule rule) {
        String condition = "";
        if (rule.getType().equals(ConditionType.METADATA_FIELD)) {
          condition = rule.getConditionKey() + " " + messages.disposalRuleConditionOperator() + " "
            + rule.getConditionValue();
        } else {
          condition = rule.getConditionValue();
        }
        return messages.disposalRuleTypeValue(condition);
      }
    };

    orderColumn.setSortable(true);
    titleColumn.setSortable(true);
    selectionMethodColumn.setSortable(true);
    conditionColumn.setSortable(false);

    addColumn(orderColumn, messages.disposalRuleOrder(), false, false, 4);
    addColumn(titleColumn, messages.disposalRuleTitle(), false, false);
    addColumn(selectionMethodColumn, messages.disposalRuleType(), false, false, 20);
    addColumn(conditionColumn, messages.disposalRuleCondition(), false, false, 25);

    // default sorting
    display.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(orderColumn, true));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<DisposalRule, ?>, List<String>> columnSortingKeyMap = new HashMap<>();

    columnSortingKeyMap.put(orderColumn, Collections.singletonList(RodaConstants.DISPOSAL_RULE_ORDER));
    columnSortingKeyMap.put(titleColumn, Collections.singletonList(RodaConstants.DISPOSAL_RULE_TITLE));
    columnSortingKeyMap.put(selectionMethodColumn,
      Collections.singletonList(RodaConstants.DISPOSAL_RULE_SELECTION_METHOD));
    return createSorter(columnSortList, columnSortingKeyMap);
  }
}
