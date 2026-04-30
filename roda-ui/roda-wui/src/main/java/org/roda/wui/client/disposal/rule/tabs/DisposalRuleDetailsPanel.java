package org.roda.wui.client.disposal.rule.tabs;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisposalRuleAction;
import org.roda.wui.client.common.actions.DisposalRuleToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.FormUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalRuleDetailsPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  ActionsToolbar actionsToolbar;

  @UiField
  FlowPanel detailsPanel;

  private DisposalRule rule;
  private AsyncCallback<Actionable.ActionImpact> localCallback;

  public DisposalRuleDetailsPanel(DisposalRule disposalRule, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    initWidget(uiBinder.createAndBindUi(this));

    // Promote localCallback to an instance variable so refresh() can use it
    this.localCallback = new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        actionCallback.onFailure(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          actionCallback.onSuccess(result);
        }
      }
    };

    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);

    // Initial load
    refresh(disposalRule);
  }

  // Update the method signature to accept the new rule
  public void refresh(DisposalRule newDisposalRule) {
    this.rule = newDisposalRule;

    // 1. Clear out the old details
    clear();

    // 2. Re-populate text fields with the new data
    init(this.rule);

    // 3. Re-bind the actions toolbar with the new rule object
    actionsToolbar.setActionableMenu(new ActionableWidgetBuilder<DisposalRule>(DisposalRuleToolbarActions.get())
      .withActionCallback(localCallback).buildGroupedListWithObjects(new ActionableObject<>(rule),
        List.of(DisposalRuleAction.EDIT), List.of(DisposalRuleAction.EDIT)),
      true);
  }

  private void init(DisposalRule rule) {
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleTitle(), rule.getTitle());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleDescription(), rule.getDescription());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleScheduleName(), rule.getDisposalScheduleName(),
      "btn-link addCursorPointer", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, rule.getDisposalScheduleId());
        }
      });
    FormUtilities.addIfNotBlank(detailsPanel, messages.showUserStatusLabel(), HtmlSnippetUtils.getDisposalRuleTypeHtml(rule));


    if (rule.getType().equals(ConditionType.IS_CHILD_OF)) {
      String conditionTxt = messages.disposalRuleTypeValue(rule.getType().toString()) + " " + rule.getConditionValue()
        + " (" + rule.getConditionKey() + ")";
      FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleCondition(), conditionTxt,
        "btn-link addCursorPointer", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            HistoryUtils.newHistory(BrowseTop.RESOLVER, rule.getConditionKey());
          }
        });
    } else if (rule.getType().equals(ConditionType.METADATA_FIELD)) {

      String conditionTxt = rule.getConditionKey() + " " + messages.disposalRuleConditionOperator() + " "
        + rule.getConditionValue();
      FormUtilities.addIfNotBlank(detailsPanel, messages.disposalRuleType(), SafeHtmlUtils.fromString(conditionTxt));
    }
  }

  public void clear() {
    detailsPanel.clear();
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalRuleDetailsPanel> {
    Widget createAndBindUi(DisposalRuleDetailsPanel detailsPanel);
  }

}
