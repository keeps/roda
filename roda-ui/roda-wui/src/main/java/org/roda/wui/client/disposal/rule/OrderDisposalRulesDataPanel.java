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
public class OrderDisposalRulesDataPanel extends Composite {

  interface MyUiBinder extends UiBinder<Widget, OrderDisposalRulesDataPanel> {
  }

  private static OrderDisposalRulesDataPanel.MyUiBinder uiBinder = GWT.create(OrderDisposalRulesDataPanel.MyUiBinder.class);

  private DisposalRule disposalRule;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel orderDisposalRulesTablePanel;

  @UiField
  FlowPanel orderButtonsPanel;


  private DisposalRules disposalRules;
  private DisposalRule selectedRule;
  private int selectedIndex;
  private BasicTablePanel<DisposalRule> tableRules;

  public OrderDisposalRulesDataPanel(DisposalRules disposalRules, boolean isCreation) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalRules = disposalRules;
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

  private void createDisposalRulesPanel() {
    orderDisposalRulesTablePanel.addStyleName("basicTable");
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

        new BasicTablePanel.ColumnInfo<>(messages.disposalRuleKey(), 12, new TextColumn<DisposalRule>() {
          @Override
          public String getValue(DisposalRule rule) {
            return rule.getKey();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalRuleValue(), 12, new TextColumn<DisposalRule>() {
          @Override
          public String getValue(DisposalRule rule) {
            return rule.getValue();
          }
        })

      );
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

}
