package org.roda.wui.client.disposal.policy;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.disposal.Disposal;
import org.roda.wui.client.disposal.hold.CreateDisposalHold;
import org.roda.wui.client.disposal.hold.EditDisposalHold;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.client.disposal.rule.CreateDisposalRule;
import org.roda.wui.client.disposal.rule.EditDisposalRule;
import org.roda.wui.client.disposal.rule.OrderDisposalRules;
import org.roda.wui.client.disposal.rule.ShowDisposalRule;
import org.roda.wui.client.disposal.schedule.CreateDisposalSchedule;
import org.roda.wui.client.disposal.schedule.EditDisposalSchedule;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class DisposalPolicy extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "policy";
    }
  };

  private static DisposalPolicy instance = null;

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicy> {
  }

  private static DisposalPolicy.MyUiBinder uiBinder = GWT.create(DisposalPolicy.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static DisposalPolicy getInstance() {
    if (instance == null) {
      instance = new DisposalPolicy();
    }
    return instance;
  }

  @UiField
  FlowPanel disposalPolicyDescription;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  @UiField
  FlowPanel sidebarButtonsPanel;

  // Disposal Schedules
  @UiField(provided = true)
  DisposalPolicySchedulesPanel disposalPolicySchedulesPanel;

  // Disposal Holds
  @UiField(provided = true)
  DisposalPolicyHoldsPanel disposalPolicyHoldsPanel;

  // Disposal Rules
  @UiField(provided = true)
  DisposalPolicyRulesPanel disposalPolicyRulesPanel;

  private boolean initSidebarButtons(FlowPanel sidebar) {
    boolean hasCreatedScheduleBtns = initDisposalScheduleButtons(sidebar, true, true);
    boolean hasCreatedHoldBtns = initDisposalHoldButtons(sidebar, true, true);
    boolean hasCreatedRuleBtns = initDisposalRuleButtons(sidebar, true, true);

    return hasCreatedRuleBtns || hasCreatedScheduleBtns || hasCreatedHoldBtns;
  }

  private boolean initDisposalScheduleButtons(FlowPanel panel, boolean isGroup, boolean isSidebar) {
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_SCHEDULE)) {
      if (isSidebar) {
        Label label = new Label();
        label.setText(messages.disposalSchedulesTitle());
        panel.add(label);
      }
      Button newDisposalScheduleBtn = new Button();
      newDisposalScheduleBtn.addStyleName("btn btn-plus");
      if (isGroup) {
        newDisposalScheduleBtn.addStyleName("btn-block");
      }
      newDisposalScheduleBtn.setText(messages.newDisposalScheduleTitle());
      newDisposalScheduleBtn.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          HistoryUtils.newHistory(CreateDisposalSchedule.RESOLVER);
        }
      });
      panel.add(newDisposalScheduleBtn);
      return true;
    }
    return false;
  }

  private boolean initDisposalHoldButtons(FlowPanel panel, boolean isGroup, boolean isSidebar) {
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_HOLD)) {
      if (isSidebar) {
        Label label = new Label();
        label.setText(messages.disposalHoldsTitle());
        label.addStyleName("sidebarDisposalLabels");
        panel.add(label);
      }
      Button newDisposalHoldBtn = new Button();
      newDisposalHoldBtn.addStyleName("btn btn-plus");
      if (isGroup) {
        newDisposalHoldBtn.addStyleName("btn-block");
      }
      newDisposalHoldBtn.setText(messages.newDisposalHoldTitle());
      newDisposalHoldBtn.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          HistoryUtils.newHistory(Disposal.RESOLVER, DisposalPolicy.RESOLVER.getHistoryToken(),
            CreateDisposalHold.RESOLVER.getHistoryToken());
        }
      });
      panel.add(newDisposalHoldBtn);
      return true;
    }
    return false;
  }

  private boolean initDisposalRuleButtons(FlowPanel panel, boolean isGroup, boolean isSidebar) {
    boolean ret = false;

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_RULE)) {
      Button createRuleBtn = new Button();
      if (isSidebar) {
        Label label = new Label();
        label.setText(messages.disposalRulesTitle());
        label.addStyleName("sidebarDisposalLabels");
        panel.add(label);
      }
      createRuleBtn.addStyleName("btn btn-plus");
      if (isGroup) {
        createRuleBtn.addStyleName("btn-block");
      }
      createRuleBtn.setText(messages.newDisposalRuleTitle());
      createRuleBtn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          HistoryUtils.newHistory(DisposalPolicy.RESOLVER, CreateDisposalRule.RESOLVER.getHistoryToken());
        }
      });
      panel.add(createRuleBtn);
      ret = true;
    }

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_RULE)) {
      if (isSidebar && !ret) {
        Label label = new Label();
        label.setText(messages.disposalRulesTitle());
        label.addStyleName("sidebarDisposalLabels");
        panel.add(label);
      }
      Button editRulesOrderBtn = new Button();
      editRulesOrderBtn.addStyleName("btn btn-edit");
      if (isGroup) {
        editRulesOrderBtn.addStyleName("btn-block");
      }
      editRulesOrderBtn.setText(messages.editRules());
      editRulesOrderBtn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          HistoryUtils.newHistory(DisposalPolicy.RESOLVER, OrderDisposalRules.RESOLVER.getHistoryToken());
        }
      });
      panel.add(editRulesOrderBtn);
      ret = true;
    }

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE)) {
      if (!ret && isSidebar) {
        Label label = new Label();
        label.setText(messages.disposalRulesTitle());
        label.addStyleName("sidebarDisposalLabels");
        panel.add(label);
      }
      Button applyRules = new Button();
      applyRules.addStyleName("btn btn-play");
      if (isGroup) {
        applyRules.addStyleName("btn-block");
      }
      applyRules.setText(messages.applyRules());
      applyRules.addClickHandler(event -> {
        // TODO
        DisposalDialogs.showApplyRules(messages.applyRules());

      });
      panel.add(applyRules);
      ret = true;
    }

    return ret;
  }

  private void initSidebar() {
    if (initSidebarButtons(sidebarButtonsPanel)) {
      SidebarUtils.showSidebar(contentFlowPanel, sidebarFlowPanel);
    } else {
      SidebarUtils.hideSidebar(contentFlowPanel, sidebarFlowPanel);
    }
  }

  private DisposalPolicy() {
    this.disposalPolicySchedulesPanel = new DisposalPolicySchedulesPanel();
    this.disposalPolicyHoldsPanel = new DisposalPolicyHoldsPanel();
    this.disposalPolicyRulesPanel = new DisposalPolicyRulesPanel();
    initWidget(uiBinder.createAndBindUi(this));
    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private DisposalPolicy(DisposalSchedules disposalSchedules, DisposalHolds disposalHolds, DisposalRules disposalRules) {
    this.disposalPolicySchedulesPanel = new DisposalPolicySchedulesPanel(disposalSchedules);
    this.disposalPolicyHoldsPanel = new DisposalPolicyHoldsPanel(disposalHolds);
    this.disposalPolicyRulesPanel = new DisposalPolicyRulesPanel(disposalRules);

    initWidget(uiBinder.createAndBindUi(this));
    initSidebar();
    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  //TODO

  private void load(AsyncCallback<Widget> callback) {
    BrowserService.Util.getInstance().listDisposalSchedules(new AsyncCallback<DisposalSchedules>() {
      @Override
      public void onFailure(Throwable throwable) {
      }
      @Override
      public void onSuccess(DisposalSchedules disposalSchedules) {
        BrowserService.Util.getInstance().listDisposalHolds(new AsyncCallback<DisposalHolds>() {
          @Override
          public void onFailure(Throwable throwable) {

          }
          @Override
          public void onSuccess(DisposalHolds disposalHolds) {
            BrowserService.Util.getInstance().listDisposalRules(new AsyncCallback<DisposalRules>() {
              @Override
              public void onFailure(Throwable throwable) {
              }
              // Disposal Schedules, holds and rules
              @Override
              public void onSuccess(DisposalRules disposalRules) {
                DisposalPolicy disposalPolicy = new DisposalPolicy(disposalSchedules, disposalHolds, disposalRules);
                callback.onSuccess(disposalPolicy);
              }
            });
          }
        });
      }
    });
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      load(callback);
    } else if (historyTokens.get(0).equals(CreateDisposalSchedule.RESOLVER.getHistoryToken())) {
      CreateDisposalSchedule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(CreateDisposalHold.RESOLVER.getHistoryToken())) {
      CreateDisposalHold.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowDisposalHold.RESOLVER.getHistoryToken())) {
      ShowDisposalHold.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowDisposalSchedule.RESOLVER.getHistoryToken())) {
      ShowDisposalSchedule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditDisposalHold.RESOLVER.getHistoryToken())) {
      EditDisposalHold.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditDisposalSchedule.RESOLVER.getHistoryToken())) {
      EditDisposalSchedule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(CreateDisposalRule.RESOLVER.getHistoryToken())) {
      CreateDisposalRule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowDisposalRule.RESOLVER.getHistoryToken())) {
      ShowDisposalRule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditDisposalRule.RESOLVER.getHistoryToken())) {
      EditDisposalRule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(OrderDisposalRules.RESOLVER.getHistoryToken())) {
      OrderDisposalRules.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    }
  }
}