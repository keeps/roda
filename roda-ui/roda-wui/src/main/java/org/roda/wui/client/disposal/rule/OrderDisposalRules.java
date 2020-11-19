package org.roda.wui.client.disposal.rule;

import java.util.List;

import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalRuleActions;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class OrderDisposalRules extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().listDisposalRules(new NoAsyncCallback<DisposalRules>() {
        @Override
        public void onSuccess(DisposalRules disposalRules) {
          OrderDisposalRules orderDisposalRules = new OrderDisposalRules(disposalRules);
          callback.onSuccess(orderDisposalRules);
        }
      });

    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DisposalPolicy.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalPolicy.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "change_disposal_rule_order";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, OrderDisposalRules> {
  }

  private static OrderDisposalRules.MyUiBinder uiBinder = GWT.create(OrderDisposalRules.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel orderDisposalRulesDescription;

  @UiField
  FlowPanel orderDisposalRulesTablePanel;

  @UiField
  Label orderButtonsLabel;

  @UiField
  FlowPanel orderButtonsPanel;

  @UiField
  Button buttonSave;

  @UiField
  Button buttonCancel;

  private final DisposalRules disposalRules;
  private DisposalRule selectedRule;
  private int selectedIndex;
  private BasicTablePanel<DisposalRule> tableRules;

  public OrderDisposalRules(DisposalRules disposalRules) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalRules = disposalRules;
    createDescription();
    createDisposalRulesPanel();
    if (disposalRules.getObjects().isEmpty()) {
      buttonSave.setVisible(false);
      orderButtonsLabel.setVisible(false);
    } else {
      buttonSave.setVisible(true);
      orderButtonsLabel.setVisible(true);
      createOrderButtons();
    }
  }

  private void createOrderButtons() {
    createTopButton();
    createUpButton();
    createDownButton();
    createBottomButton();

  }

  private void createTopButton() {
    Button buttonTop = new Button();
    buttonTop.setText("top");
    buttonTop.addStyleName("btn btn-block btn-default btn-top");
    buttonTop.addClickHandler(clickEvent -> {
      if (selectedRule != null && selectedIndex != 0) {
        orderDisposalRulesTablePanel.clear();
        disposalRules.moveToTop(selectedRule);
        disposalRules.sortRules();
        createDisposalRulesPanel();
        tableRules.getSelectionModel().setSelected(selectedRule, true);
      }
    });
    orderButtonsPanel.add(buttonTop);
  }

  private void createUpButton() {
    Button buttonUp = new Button();
    buttonUp.setText("up");
    buttonUp.addStyleName("btn btn-block btn-default btn-up");
    buttonUp.addClickHandler(clickEvent -> {
      int previousIndex = selectedIndex - 1;
      if (selectedRule != null && previousIndex >= 0) {
        disposalRules.getObjects().get(previousIndex).setOrder(selectedIndex);
        selectedRule.setOrder(previousIndex);
        orderDisposalRulesTablePanel.clear();
        disposalRules.sortRules();
        createDisposalRulesPanel();
        tableRules.getSelectionModel().setSelected(selectedRule, true);
      }
    });
    orderButtonsPanel.add(buttonUp);
  }

  private void createDownButton() {
    Button buttonDown = new Button();
    buttonDown.setText("down");
    buttonDown.addStyleName("btn btn-block btn-default btn-down");
    buttonDown.addClickHandler(clickEvent -> {
      int nextIndex = selectedIndex + 1;
      if (selectedRule != null && nextIndex < disposalRules.getObjects().size()) {
        disposalRules.getObjects().get(nextIndex).setOrder(selectedIndex);
        selectedRule.setOrder(nextIndex);
        orderDisposalRulesTablePanel.clear();
        disposalRules.sortRules();
        createDisposalRulesPanel();
        tableRules.getSelectionModel().setSelected(selectedRule, true);
      }
    });
    orderButtonsPanel.add(buttonDown);
  }

  private void createBottomButton() {
    Button buttonBottom = new Button();
    buttonBottom.setText("bottom");
    buttonBottom.addStyleName("btn btn-block btn-default btn-bottom");
    buttonBottom.addClickHandler(clickEvent -> {
      int lastIndex = disposalRules.getObjects().size() - 1;
      if (selectedRule != null && selectedIndex != lastIndex) {
        orderDisposalRulesTablePanel.clear();
        disposalRules.moveToBottom(selectedRule, selectedIndex);
        disposalRules.sortRules();
        createDisposalRulesPanel();
        tableRules.getSelectionModel().setSelected(selectedRule, true);
      }
    });
    orderButtonsPanel.add(buttonBottom);
  }

  private void createDescription() {
    orderDisposalRulesDescription.add(new HTMLWidgetWrapper("OrderDisposalRulesDescription.html"));
  }

  private void createDisposalRulesPanel() {
    if (disposalRules.getObjects().isEmpty()) {
      Label label = new HTML(
        SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(messages.disposalRulesTitle())));
      label.addStyleName("basicTableEmpty");
      orderDisposalRulesTablePanel.add(label);
    } else {
      FlowPanel rulesPanel = new FlowPanel();
      tableRules = getBasicTablePanelForDisposalRules(disposalRules);
      tableRules.getSelectionModel().addSelectionChangeHandler(event -> {
        selectedRule = tableRules.getSelectionModel().getSelectedObject();
        if (selectedRule != null) {
          selectedIndex = selectedRule.getOrder();
        }
      });

      rulesPanel.add(tableRules);
      orderDisposalRulesTablePanel.add(rulesPanel);
      orderDisposalRulesTablePanel.addStyleName("disposalPolicyScrollPanel");
    }
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
        }), new BasicTablePanel.ColumnInfo<>(messages.disposalRuleCondition(), 24, new TextColumn<DisposalRule>() {
          @Override
          public String getValue(DisposalRule rule) {
            String condition;
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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonSave")
  void buttonSaveHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.saveButton(), messages.confirmChangeRulesOrder(), messages.dialogNo(),
      messages.dialogYes(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean aBoolean) {
          BrowserServiceImpl.Util.getInstance().updateDisposalRules(disposalRules, new NoAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
              Toast.showInfo(messages.updateDisposalRulesOrderSuccessTitle(),
                      messages.updateDisposalRulesOrderSuccessMessage());
              DisposalRuleActions.applyDisposalRulesAction();
            }
          });
        }
      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
  }
}
