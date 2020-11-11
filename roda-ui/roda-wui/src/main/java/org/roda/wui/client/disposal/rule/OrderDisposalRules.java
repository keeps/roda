package org.roda.wui.client.disposal.rule;

import java.util.List;

import org.roda.core.data.exceptions.DisposalRuleAlreadyExistsException;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.DisposalPolicy;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.ui.HTMLPanel;
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
      BrowserService.Util.getInstance().listDisposalRules(new AsyncCallback<DisposalRules>() {
        @Override
        public void onFailure(Throwable throwable) {
        }

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

  private DisposalRule disposalRule;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel orderDisposalRulesDescription;

  @UiField
  FlowPanel orderDisposalRulesTablePanel;

  @UiField
  FlowPanel orderButtonsPanel;

  @UiField
  Button buttonSave;

  @UiField
  Button buttonCancel;

  private DisposalRules disposalRules;
  private DisposalRule selectedRule;
  private int selectedIndex;
  private BasicTablePanel<DisposalRule> tableRules;

  public OrderDisposalRules(DisposalRules disposalRules) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalRules = disposalRules;
    createDescription();
    createDisposalRulesPanel();
    createOrderButtons();
  }

  private void createOrderButtons() {
    createTopButton();
    createUpButton();
    createDownButton();
    createBottomButton();

  }

  private void createTopButton() {
    Button topbtn = new Button();
    topbtn.setText("top");
    topbtn.addStyleName("btn btn-block btn-default btn-top");
    topbtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        if (selectedRule != null && selectedIndex != 0) {
          orderDisposalRulesTablePanel.clear();
          disposalRules.moveToTop(selectedRule);
          disposalRules.sortRules();
          createDisposalRulesPanel();
          tableRules.getSelectionModel().setSelected(selectedRule, true);
        }
      }
    });
    orderButtonsPanel.add(topbtn);
  }

  private void createUpButton() {
    Button upbtn = new Button();
    upbtn.setText("up");
    upbtn.addStyleName("btn btn-block btn-default btn-up");
    upbtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        int previousIndex = selectedIndex - 1;
        if (selectedRule != null && previousIndex >= 0) {
          disposalRules.getObjects().get(previousIndex).setOrder(selectedIndex);
          selectedRule.setOrder(previousIndex);
          orderDisposalRulesTablePanel.clear();
          disposalRules.sortRules();
          createDisposalRulesPanel();
          tableRules.getSelectionModel().setSelected(selectedRule, true);
        }
      }
    });
    orderButtonsPanel.add(upbtn);
  }

  private void createDownButton() {
    Button downbtn = new Button();
    downbtn.setText("down");
    downbtn.addStyleName("btn btn-block btn-default btn-down");
    downbtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        int nextIndex = selectedIndex + 1;
        if (selectedRule != null && nextIndex < disposalRules.getObjects().size()) {
          disposalRules.getObjects().get(nextIndex).setOrder(selectedIndex);
          selectedRule.setOrder(nextIndex);
          orderDisposalRulesTablePanel.clear();
          disposalRules.sortRules();
          createDisposalRulesPanel();
          tableRules.getSelectionModel().setSelected(selectedRule, true);
        }
      }
    });
    orderButtonsPanel.add(downbtn);
  }

  private void createBottomButton() {
    Button bottombtn = new Button();
    bottombtn.setText("bottom");
    bottombtn.addStyleName("btn btn-block btn-default btn-bottom");
    bottombtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        int lastIndex = disposalRules.getObjects().size() - 1;
        if (selectedRule != null && selectedIndex != lastIndex) {
          orderDisposalRulesTablePanel.clear();
          disposalRules.moveToBottom(selectedRule, selectedIndex);
          disposalRules.sortRules();
          createDisposalRulesPanel();
          tableRules.getSelectionModel().setSelected(selectedRule, true);
        }
      }
    });
    orderButtonsPanel.add(bottombtn);
  }

  private void createDescription() {
    HTMLPanel info = new HTMLPanel("");
    info.add(new HTMLWidgetWrapper("OrderDisposalRulesDescription.html"));

    orderDisposalRulesDescription.add(info);

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
    // TODO
  }

  @UiHandler("buttonCreate")
  void buttonCreateHandler(ClickEvent e) {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER, CreateDisposalRule.RESOLVER.getHistoryToken());
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof DisposalRuleAlreadyExistsException) {
      Toast.showError(messages.createDisposalRuleAlreadyExists(disposalRule.getTitle()));
    } else {
      Toast.showError(messages.createDisposalRuleFailure(caught.getMessage()));
    }
  }
}
