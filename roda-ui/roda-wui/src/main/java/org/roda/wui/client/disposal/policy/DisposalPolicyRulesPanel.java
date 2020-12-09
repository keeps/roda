package org.roda.wui.client.disposal.policy;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.rule.ShowDisposalRule;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class DisposalPolicyRulesPanel extends Composite {

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyRulesPanel> {
  }

  private static DisposalPolicyRulesPanel.MyUiBinder uiBinder = GWT.create(DisposalPolicyRulesPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // Disposal Rules
  @UiField
  FlowPanel disposalRulesDescription;

  @UiField
  ScrollPanel disposalRulesTablePanel;

  private void createDisposalRulesDescription(FlowPanel disposalRulesDescription) {
    Label header = new Label(messages.disposalRulesTitle());
    header.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");
    info.add(new HTMLWidgetWrapper("DisposalRuleDescription.html"));
    info.addStyleName("page-description");

    disposalRulesDescription.add(header);
    disposalRulesDescription.add(info);

  }

  private void createDisposalRulesPanel(ScrollPanel disposalRulesTablePanel,DisposalRules disposalRules) {
    disposalRulesTablePanel.addStyleName("basicTable");
    disposalRulesTablePanel.addStyleName("basicTable-border");
    if (disposalRules.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(disposalRules.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      disposalRulesTablePanel.add(label);
    } else {
      FlowPanel rulesPanel = new FlowPanel();
      BasicTablePanel<DisposalRule> tableRules = getBasicTablePanelForDisposalRules(disposalRules);
      tableRules.getSelectionModel().addSelectionChangeHandler(event -> {
        DisposalRule selectedRule = tableRules.getSelectionModel().getSelectedObject();
        if (selectedRule != null) {
          tableRules.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowDisposalRule.RESOLVER.getHistoryPath(), selectedRule.getId());
          HistoryUtils.newHistory(path);
        }
      });

      rulesPanel.add(tableRules);
      disposalRulesTablePanel.add(rulesPanel);
      disposalRulesTablePanel.addStyleName("disposalPolicyScrollPanel");
    }
  }

  public DisposalPolicyRulesPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_RULES)) {
      BrowserService.Util.getInstance().listDisposalRules(new NoAsyncCallback<DisposalRules>() {
        @Override
        public void onSuccess(DisposalRules disposalRules) {
          init(disposalRulesDescription, disposalRulesTablePanel, disposalRules);
        }
      });
    }
  }

  private void init(FlowPanel disposalRulesDescription, ScrollPanel disposalRulesTablePanel, DisposalRules disposalRules) {
    // Create disposal rules description
    createDisposalRulesDescription(disposalRulesDescription);

    // Disposal rules table
    createDisposalRulesPanel(disposalRulesTablePanel, disposalRules);

  }

  private BasicTablePanel<DisposalRule> getBasicTablePanelForDisposalRules(DisposalRules disposalRules) {
    if (disposalRules.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplayPreFilters(messages.disposalRulesTitle()));
    } else {
      return new BasicTablePanel<DisposalRule>(disposalRules.getObjects().iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.disposalRuleOrder(), 4, new TextColumn<DisposalRule>() {
          @Override
          public String getValue(DisposalRule rule) {
            Integer showOrder = rule.getOrder() + 1;
            return showOrder.toString();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalRuleTitle(), 0, new TextColumn<DisposalRule>() {
          @Override
          public String getValue(DisposalRule rule) {
            return rule.getTitle();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalRuleType(), 12, new TextColumn<DisposalRule>() {
          @Override
          public String getValue(DisposalRule rule) {
            return messages.disposalRuleTypeValue(rule.getType().toString());
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalRuleCondition(), 24, new TextColumn<DisposalRule>() {
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
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalRuleScheduleName(), 12, new TextColumn<DisposalRule>() {
          @Override
          public String getValue(DisposalRule rule) {
            return rule.getDisposalScheduleName();
          }
        }));
    }
  }
}