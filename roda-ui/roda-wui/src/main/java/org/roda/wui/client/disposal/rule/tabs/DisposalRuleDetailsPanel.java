package org.roda.wui.client.disposal.rule.tabs;

import java.util.List;

import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.common.actions.DisposalRuleAction;
import org.roda.wui.client.common.actions.DisposalRuleToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalRuleDetailsPanel extends GenericMetadataCardPanel<DisposalRule> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DisposalRuleDetailsPanel(DisposalRule disposalRule) {
    setData(disposalRule);
  }

  @Override
  protected FlowPanel createHeaderWidget(DisposalRule rule) {
    if (rule == null) {
      return null;
    }

    return new ActionableWidgetBuilder<DisposalRule>(DisposalRuleToolbarActions.get()).buildGroupedListWithObjects(
      new ActionableObject<>(rule), List.of(DisposalRuleAction.EDIT), List.of(DisposalRuleAction.EDIT));
  }

  @Override
  protected void buildFields(DisposalRule data) {
    buildField(messages.disposalRuleTitle()).withValue(data.getTitle()).build();

    buildField(messages.disposalRuleDescription()).withValue(data.getDescription()).build();

    buildField(messages.disposalRuleScheduleName()).withValue(data.getDisposalScheduleName())
      .onClick(event -> HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, data.getDisposalScheduleId())).build();

    buildField(messages.disposalRuleType()).withHtml(HtmlSnippetUtils.getDisposalRuleTypeHtml(data)).build();

    if (ConditionType.IS_CHILD_OF.equals(data.getType())) {
      String conditionTxt = messages.disposalRuleTypeValue(data.getType().toString()) + " " + data.getConditionValue()
        + " (" + data.getConditionKey() + ")";

      buildField(messages.disposalRuleCondition()).withValue(conditionTxt)
        .onClick(event -> HistoryUtils.newHistory(BrowseTop.RESOLVER, data.getConditionKey())).build();

    } else if (ConditionType.METADATA_FIELD.equals(data.getType())) {
      String conditionTxt = data.getConditionKey() + " " + messages.disposalRuleConditionOperator() + " "
        + data.getConditionValue();

      buildField(messages.disposalRuleCondition()).withValue(conditionTxt).build();
    }
  }
}