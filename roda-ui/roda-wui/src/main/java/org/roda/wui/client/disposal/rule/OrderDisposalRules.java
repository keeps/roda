/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalRuleActions;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.services.DisposalRuleRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

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
      Services services = new Services("List disposal rules", "get");
      services.disposalRuleResource(DisposalRuleRestService::listDisposalRules).whenComplete((result, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          OrderDisposalRules orderDisposalRules = new OrderDisposalRules(result);
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static OrderDisposalRules.MyUiBinder uiBinder = GWT.create(OrderDisposalRules.MyUiBinder.class);
  private final DisposalRules disposalRules;
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
    buttonTop.setText(messages.editRulesOrderTop());
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
    buttonUp.setText(messages.editRulesOrderUp());
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
    buttonDown.setText(messages.editRulesOrderDown());
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
    buttonBottom.setText(messages.editRulesOrderBottom());
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

  @UiHandler("buttonSave")
  void buttonSaveHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.saveButton(), messages.confirmChangeRulesOrder(), messages.dialogNo(),
      messages.dialogYes(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            Services services = new Services("Update multiple disposal rules", "update");
            List<CompletableFuture<DisposalRule>> futures = new ArrayList<>();
            for (DisposalRule rule : disposalRules.getObjects()) {
              futures.add(services.disposalRuleResource(s -> s.updateDisposalRule(rule)).toCompletableFuture());
            }
            CompletableFuture<?>[] futuresArray = futures.toArray(new CompletableFuture<?>[0]);
            CompletableFuture<List<DisposalRule>> listFuture = CompletableFuture.allOf(futuresArray)
              .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            listFuture.join();

            Toast.showInfo(messages.updateDisposalRulesOrderSuccessTitle(),
              messages.updateDisposalRulesOrderSuccessMessage());
            DisposalRuleActions.applyDisposalRulesAction();
          }
        }
      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
  }

  interface MyUiBinder extends UiBinder<Widget, OrderDisposalRules> {
  }
}
