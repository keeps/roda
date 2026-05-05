package org.roda.wui.client.disposal.rule.tabs;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.common.actions.DisposalRuleAction;
import org.roda.wui.client.common.actions.DisposalRuleToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalRuleDetailsPanel extends GenericMetadataCardPanel<DisposalRule> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DisposalRuleDetailsPanel(DisposalRule disposalRule) {
    super(createConfiguredToolbar(disposalRule));
    setData(disposalRule);
  }

  private static FlowPanel createConfiguredToolbar(DisposalRule rule) {
    if (rule == null) {
      return null;
    }

    return new ActionableWidgetBuilder<DisposalRule>(DisposalRuleToolbarActions.get()).buildGroupedListWithObjects(
      new ActionableObject<>(rule), List.of(DisposalRuleAction.EDIT), List.of(DisposalRuleAction.EDIT));
  }

  @Override
  public void setData(DisposalRule data) {
    // 1. Clear any existing fields in case setData is called multiple times
    metadataContainer.clear();

    if (data == null) {
      return;
    }

    addFieldIfNotNull(messages.disposalRuleTitle(), DisposalRule::getTitle, data);
    addFieldIfNotNull(messages.disposalRuleDescription(), DisposalRule::getDescription, data);
    addFieldIfNotNull(messages.disposalRuleScheduleName(), DisposalRule::getDisposalScheduleName, data,
      event -> HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, data.getDisposalScheduleId()));
    addFieldIfNotNull(messages.disposalRuleType(), HtmlSnippetUtils.getDisposalRuleTypeHtml(data));
    if (data.getType().equals(ConditionType.IS_CHILD_OF)) {
      String conditionTxt = messages.disposalRuleTypeValue(data.getType().toString()) + " " + data.getConditionValue()
        + " (" + data.getConditionKey() + ")";
      addFieldIfNotNull(messages.disposalRuleCondition(), conditionTxt,
        event -> HistoryUtils.newHistory(BrowseTop.RESOLVER, data.getConditionKey()));
    } else if (data.getType().equals(ConditionType.METADATA_FIELD)) {

      String conditionTxt = data.getConditionKey() + " " + messages.disposalRuleConditionOperator() + " "
        + data.getConditionValue();
      addFieldIfNotNull(messages.disposalRuleCondition(), SafeHtmlUtils.fromString(conditionTxt));
    }
  }

  // private void init(DisposalRule rule) {
  // FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleTitle(),
  // rule.getTitle());
  // FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleDescription(),
  // rule.getDescription());
  // FormUtilities.addIfNotBlank(detailsPanel,
  // messages.disposalRuleScheduleName(), rule.getDisposalScheduleName(),
  // "btn-link addCursorPointer", new ClickHandler() {
  // @Override
  // public void onClick(ClickEvent event) {
  // HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER,
  // rule.getDisposalScheduleId());
  // }
  // });
  // FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleType(),
  // HtmlSnippetUtils.getDisposalRuleTypeHtml(rule));
  //
  //
  // if (rule.getType().equals(ConditionType.IS_CHILD_OF)) {
  // String conditionTxt =
  // messages.disposalRuleTypeValue(rule.getType().toString()) + " " +
  // rule.getConditionValue()
  // + " (" + rule.getConditionKey() + ")";
  // FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleCondition(),
  // conditionTxt,
  // "btn-link addCursorPointer", new ClickHandler() {
  // @Override
  // public void onClick(ClickEvent event) {
  // HistoryUtils.newHistory(BrowseTop.RESOLVER, rule.getConditionKey());
  // }
  // });
  // } else if (rule.getType().equals(ConditionType.METADATA_FIELD)) {
  //
  // String conditionTxt = rule.getConditionKey() + " " +
  // messages.disposalRuleConditionOperator() + " "
  // + rule.getConditionValue();
  // FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleCondition(),
  // SafeHtmlUtils.fromString(conditionTxt));
  // }
  // }
}
