package org.roda.wui.client.disposal;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
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

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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

  private DisposalRules disposalRules;
  private DisposalSchedules disposalSchedules;
  private DisposalHolds disposalHolds;

  @UiField
  FlowPanel disposalPolicyDescription;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  @UiField
  FlowPanel sidebarButtonsPanel;

  // Disposal Rules

  @UiField
  FlowPanel disposalRulesDescription;

  @UiField
  ScrollPanel disposalRulesTablePanel;

  @UiField
  FlowPanel disposalRulesButtonsPanel;

  // Disposal Schedules

  @UiField
  FlowPanel disposalSchedulesDescription;

  @UiField
  ScrollPanel disposalSchedulesTablePanel;

  @UiField
  FlowPanel disposalSchedulesButtonsPanel;

  // Disposal Holds

  @UiField
  FlowPanel disposalHoldsDescription;

  @UiField
  ScrollPanel disposalHoldsTablePanel;

  @UiField
  FlowPanel disposalHoldsButtonsPanel;

  private static void loadAll(AsyncCallback<Widget> callback) {
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

              @Override
              public void onSuccess(DisposalRules disposalRules) {
                callback.onSuccess(new DisposalPolicy(disposalSchedules, disposalHolds, disposalRules));
              }
            });
          }
        });
      }
    });
  }

  private static void loadDisposalSchedulesAndHolds(AsyncCallback<Widget> callback) {
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

            callback.onSuccess(new DisposalPolicy(disposalSchedules, disposalHolds));
          }
        });
      }
    });
  }

  private static void loadDisposalSchedulesAndRules(AsyncCallback<Widget> callback) {
    BrowserService.Util.getInstance().listDisposalSchedules(new AsyncCallback<DisposalSchedules>() {
      @Override
      public void onFailure(Throwable throwable) {
      }

      @Override
      public void onSuccess(DisposalSchedules disposalSchedules) {
        BrowserService.Util.getInstance().listDisposalRules(new AsyncCallback<DisposalRules>() {
          @Override
          public void onFailure(Throwable throwable) {
          }

          @Override
          public void onSuccess(DisposalRules disposalRules) {

            callback.onSuccess(new DisposalPolicy(disposalSchedules, disposalRules));
          }
        });
      }
    });
  }

  private static void loadDisposalHoldsAndRules(AsyncCallback<Widget> callback) {
    BrowserService.Util.getInstance().listDisposalRules(new AsyncCallback<DisposalRules>() {
      @Override
      public void onFailure(Throwable throwable) {
      }

      @Override
      public void onSuccess(DisposalRules disposalRules) {
        BrowserService.Util.getInstance().listDisposalHolds(new AsyncCallback<DisposalHolds>() {
          @Override
          public void onFailure(Throwable throwable) {
          }

          @Override
          public void onSuccess(DisposalHolds disposalHolds) {

            callback.onSuccess(new DisposalPolicy(disposalHolds, disposalRules));
          }
        });
      }
    });
  }

  private static void loadDisposalSchedules(AsyncCallback<Widget> callback) {
    BrowserService.Util.getInstance().listDisposalSchedules(new AsyncCallback<DisposalSchedules>() {
      @Override
      public void onFailure(Throwable throwable) {
      }

      @Override
      public void onSuccess(DisposalSchedules disposalSchedules) {
        callback.onSuccess(new DisposalPolicy(disposalSchedules));
      }
    });
  }

  private static void loadDisposalHolds(AsyncCallback<Widget> callback) {
    BrowserService.Util.getInstance().listDisposalHolds(new AsyncCallback<DisposalHolds>() {
      @Override
      public void onFailure(Throwable throwable) {
      }

      @Override
      public void onSuccess(DisposalHolds disposalHolds) {
        callback.onSuccess(new DisposalPolicy(disposalHolds));
      }
    });
  }

  private static void loadDisposalRules(AsyncCallback<Widget> callback) {
    BrowserService.Util.getInstance().listDisposalRules(new AsyncCallback<DisposalRules>() {
      @Override
      public void onFailure(Throwable throwable) {
      }

      @Override
      public void onSuccess(DisposalRules disposalRules) {
        callback.onSuccess(new DisposalPolicy(disposalRules));
      }
    });
  }

  private void createDisposalSchedulesDescription() {
    Label header = new Label(messages.disposalSchedulesTitle());
    header.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");
    info.add(new HTMLWidgetWrapper("DisposalScheduleDescription.html"));
    info.addStyleName("page-description");
    disposalSchedulesDescription.add(header);
    disposalSchedulesDescription.add(info);

  }

  private void createDisposalHoldsDescription() {
    Label header = new Label(messages.disposalHoldsTitle());
    header.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");
    info.add(new HTMLWidgetWrapper("DisposalHoldDescription.html"));
    info.addStyleName("page-description");

    disposalHoldsDescription.add(header);
    disposalHoldsDescription.add(info);

  }

  private void createDisposalRulesDescription() {
    Label header = new Label(messages.disposalRulesTitle());
    header.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");
    info.add(new HTMLWidgetWrapper("DisposalRuleDescription.html"));
    info.addStyleName("page-description");

    disposalRulesDescription.add(header);
    disposalRulesDescription.add(info);

  }

  private void createDisposalSchedulesPanel(DisposalSchedules disposalSchedules) {
    disposalSchedulesTablePanel.addStyleName("basicTable");
    // Disposal schedules table
    if (disposalSchedules.getObjects().isEmpty()) {
      Label label = new HTML(
        SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(messages.disposalSchedulesTitle())));
      label.addStyleName("basicTableEmpty");
      disposalSchedulesTablePanel.add(label);
    } else {
      FlowPanel schedulesPanel = new FlowPanel();
      BasicTablePanel<DisposalSchedule> tableSchedules = getBasicTablePanelForDisposalSchedules(disposalSchedules);
      tableSchedules.getSelectionModel().addSelectionChangeHandler(event -> {
        DisposalSchedule selectedSchedule = tableSchedules.getSelectionModel().getSelectedObject();
        if (selectedSchedule != null) {
          tableSchedules.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowDisposalSchedule.RESOLVER.getHistoryPath(),
            selectedSchedule.getId());
          HistoryUtils.newHistory(path);
        }
      });

      schedulesPanel.add(tableSchedules);
      disposalSchedulesTablePanel.add(schedulesPanel);
      disposalSchedulesTablePanel.addStyleName("disposalPolicyScrollPanel");

    }
  }

  private void createDisposalHoldsPanel(DisposalHolds disposalHolds) {
    disposalHoldsTablePanel.addStyleName("basicTable");
    if (disposalHolds.getObjects().isEmpty()) {
      Label label = new HTML(
        SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(messages.disposalHoldsTitle())));
      label.addStyleName("basicTableEmpty");
      disposalHoldsTablePanel.add(label);
    } else {
      FlowPanel holdsPanel = new FlowPanel();
      BasicTablePanel<DisposalHold> tableHolds = getBasicTablePanelForDisposalHolds(disposalHolds);
      tableHolds.getSelectionModel().addSelectionChangeHandler(event -> {
        DisposalHold selectedHold = tableHolds.getSelectionModel().getSelectedObject();
        if (selectedHold != null) {
          tableHolds.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowDisposalHold.RESOLVER.getHistoryPath(), selectedHold.getId());
          HistoryUtils.newHistory(path);
        }
      });

      holdsPanel.add(tableHolds);
      disposalHoldsTablePanel.add(holdsPanel);
      disposalHoldsTablePanel.addStyleName("disposalPolicyScrollPanel");
    }
  }

  private void createDisposalRulesPanel(DisposalRules disposalRules) {
    disposalRulesTablePanel.addStyleName("basicTable");
    if (disposalRules.getObjects().isEmpty()) {
      Label label = new HTML(
        SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(messages.disposalRulesTitle())));
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

  private boolean initSidebarButtons(FlowPanel panel) {

    boolean hasCreatedRuleBtns = initDisposalRuleButtons(panel, true);
    boolean hasCreatedScheduleBtns = initDisposalScheduleButtons(panel, true);
    boolean hasCreatedHoldBtns = initDisposalHoldButtons(panel, true);

    return hasCreatedRuleBtns || hasCreatedScheduleBtns || hasCreatedHoldBtns;
  }

  private boolean initDisposalScheduleButtons(FlowPanel panel, boolean isGroup) {
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_SCHEDULE)) {
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

  private boolean initDisposalHoldButtons(FlowPanel panel, boolean isGroup) {
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_HOLD)) {
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

  private boolean initDisposalRuleButtons(FlowPanel panel, boolean isGroup) {
    boolean ret = false;

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_RULE)) {
      Button newDisposalRuleBtn = new Button();
      newDisposalRuleBtn.addStyleName("btn btn-plus");
      if (isGroup) {
        newDisposalRuleBtn.addStyleName("btn-block");
      }
      newDisposalRuleBtn.setText(messages.newDisposalRuleTitle());
      newDisposalRuleBtn.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          HistoryUtils.newHistory(DisposalPolicy.RESOLVER, CreateDisposalRule.RESOLVER.getHistoryToken());
        }
      });
      panel.add(newDisposalRuleBtn);

      if (disposalRules != null && disposalRules.getObjects().size() > 0) {
        Button editRulesOrderBtn = new Button();
        editRulesOrderBtn.addStyleName("btn btn-edit");
        if (isGroup) {
          editRulesOrderBtn.addStyleName("btn-block");
        } else {
          editRulesOrderBtn.addStyleName("changeRulesOrder");
        }
        editRulesOrderBtn.setText(messages.changeRulesOrder());
        editRulesOrderBtn.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            HistoryUtils.newHistory(DisposalPolicy.RESOLVER, OrderDisposalRules.RESOLVER.getHistoryToken());
          }
        });
        panel.add(editRulesOrderBtn);
      }
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
    initWidget(uiBinder.createAndBindUi(this));
    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private DisposalPolicy(DisposalSchedules disposalSchedules, DisposalHolds disposalHolds,
    DisposalRules disposalRules) {

    this.disposalRules = disposalRules;
    this.disposalSchedules = disposalSchedules;
    this.disposalHolds = disposalHolds;

    initWidget(uiBinder.createAndBindUi(this));

    initSidebar();

    // Create disposal rules description
    createDisposalRulesDescription();

    // Disposal rules table
    createDisposalRulesPanel(disposalRules);

    // Create disposal schedules description
    createDisposalSchedulesDescription();

    // Disposal schedules table
    createDisposalSchedulesPanel(disposalSchedules);

    // Create disposal holds description
    createDisposalHoldsDescription();

    // Disposal holds table
    createDisposalHoldsPanel(disposalHolds);

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private DisposalPolicy(DisposalSchedules disposalSchedules, DisposalHolds disposalHolds) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalSchedules = disposalSchedules;
    this.disposalHolds = disposalHolds;
    disposalRulesTablePanel.setVisible(false);
    disposalRulesButtonsPanel.setVisible(false);

    initSidebar();

    // Create disposal schedules description
    createDisposalSchedulesDescription();

    // Disposal schedules table
    createDisposalSchedulesPanel(disposalSchedules);

    // Create disposal holds description
    createDisposalHoldsDescription();

    // Disposal holds table
    createDisposalHoldsPanel(disposalHolds);

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private DisposalPolicy(DisposalSchedules disposalSchedules, DisposalRules disposalRules) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalRules = disposalRules;
    this.disposalSchedules = disposalSchedules;
    disposalHoldsTablePanel.setVisible(false);
    disposalHoldsButtonsPanel.setVisible(false);

    initSidebar();

    // Create disposal rules description
    createDisposalRulesDescription();

    // Disposal rules table
    createDisposalRulesPanel(disposalRules);

    // Create disposal schedules description
    createDisposalSchedulesDescription();

    // Disposal schedules table
    createDisposalSchedulesPanel(disposalSchedules);

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private DisposalPolicy(DisposalHolds disposalHolds, DisposalRules disposalRules) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalRules = disposalRules;
    this.disposalHolds = disposalHolds;
    disposalSchedulesTablePanel.setVisible(false);
    disposalSchedulesButtonsPanel.setVisible(false);

    initSidebar();

    // Create disposal rules description
    createDisposalRulesDescription();

    // Disposal rules table
    createDisposalRulesPanel(disposalRules);

    // Create disposal holds description
    createDisposalHoldsDescription();

    // Disposal holds table
    createDisposalHoldsPanel(disposalHolds);

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private DisposalPolicy(DisposalSchedules disposalSchedules) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalSchedules = disposalSchedules;
    disposalHoldsTablePanel.setVisible(false);
    disposalHoldsButtonsPanel.setVisible(false);
    disposalRulesTablePanel.setVisible(false);
    disposalRulesButtonsPanel.setVisible(false);

    initSidebar();

    // Create disposal schedules description
    createDisposalSchedulesDescription();

    // Disposal schedules table
    createDisposalSchedulesPanel(disposalSchedules);

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private DisposalPolicy(DisposalHolds disposalHolds) {
    this.disposalHolds = disposalHolds;
    initWidget(uiBinder.createAndBindUi(this));
    disposalSchedulesTablePanel.setVisible(false);
    disposalSchedulesButtonsPanel.setVisible(false);
    disposalRulesTablePanel.setVisible(false);
    disposalRulesButtonsPanel.setVisible(false);

    initSidebar();

    // Create disposal holds description
    createDisposalHoldsDescription();

    // Disposal holds table
    createDisposalHoldsPanel(disposalHolds);

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private DisposalPolicy(DisposalRules disposalRules) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalRules = disposalRules;
    disposalSchedulesTablePanel.setVisible(false);
    disposalSchedulesButtonsPanel.setVisible(false);
    disposalHoldsTablePanel.setVisible(false);
    disposalHoldsButtonsPanel.setVisible(false);

    initSidebar();

    // Create disposal holds description
    createDisposalRulesDescription();

    // Disposal holds table
    createDisposalRulesPanel(disposalRules);

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private BasicTablePanel<DisposalSchedule> getBasicTablePanelForDisposalSchedules(
    DisposalSchedules disposalSchedules) {
    if (disposalSchedules.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplayPreFilters(messages.disposalSchedulesTitle()));
    } else {
      return new BasicTablePanel<DisposalSchedule>(disposalSchedules.getObjects().iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleTitle(), 15, new TextColumn<DisposalSchedule>() {
          @Override
          public String getValue(DisposalSchedule schedule) {
            return schedule.getTitle();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleMandate(), 0, new TextColumn<DisposalSchedule>() {
          @Override
          public String getValue(DisposalSchedule schedule) {
            return schedule.getMandate();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalSchedulePeriod(), 12, new TextColumn<DisposalSchedule>() {
          @Override
          public String getValue(DisposalSchedule schedule) {
            if (schedule.getRetentionPeriodDuration() == null && schedule.getRetentionPeriodIntervalCode() == null) {
              return "";
            } else if (schedule.getRetentionPeriodIntervalCode() == RetentionPeriodIntervalCode.NO_RETENTION_PERIOD) {
              return schedule.getRetentionPeriodIntervalCode().toString();
            } else {
              String value = schedule.getRetentionPeriodDuration().toString() + " "
                + schedule.getRetentionPeriodIntervalCode().toString().toLowerCase();
              return value;
            }
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleActionCol(), 12, new TextColumn<DisposalSchedule>() {
          @Override
          public String getValue(DisposalSchedule schedule) {
            return messages.disposalScheduleAction(schedule.getActionCode().toString());
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleNumberOfAIPs(), 12,
          new TextColumn<DisposalSchedule>() {

            @Override
            public String getValue(DisposalSchedule schedule) {
              return schedule.getNumberOfAIPUnder().toString();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleStateCol(), 12,
          new Column<DisposalSchedule, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(DisposalSchedule schedule) {
              return HtmlSnippetUtils.getDisposalScheduleStateHtml(schedule);

            }
          }));
    }
  }

  private BasicTablePanel<DisposalHold> getBasicTablePanelForDisposalHolds(DisposalHolds disposalHolds) {
    if (disposalHolds.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplayPreFilters(messages.disposalHoldsTitle()));
    } else {
      return new BasicTablePanel<DisposalHold>(disposalHolds.getObjects().iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldTitle(), 15, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            return hold.getTitle();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleMandate(), 0, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            return hold.getMandate();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldNumberOfAIPs(), 12, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            return String.valueOf(hold.getActiveAIPs().size());
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 12,
          new Column<DisposalHold, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(DisposalHold hold) {
              return HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
            }
          }));
    }
  }

  private BasicTablePanel<DisposalRule> getBasicTablePanelForDisposalRules(DisposalRules disposalRules) {
    if (disposalRules.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplayPreFilters(messages.disposalRulesTitle()));
    } else {
      return new BasicTablePanel<DisposalRule>(disposalRules.getObjects().iterator(),

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

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)
        && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)
        && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_RULES)) {
        loadAll(callback);
      } else if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)
        && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)) {
        loadDisposalSchedulesAndHolds(callback);
      } else if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)
        && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_RULES)) {
        loadDisposalSchedulesAndRules(callback);
      } else if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_RULES)
        && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)) {
        loadDisposalHoldsAndRules(callback);
      } else if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)) {
        loadDisposalSchedules(callback);
      } else if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)) {
        loadDisposalHolds(callback);
      } else if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_RULES)) {
        loadDisposalRules(callback);
      }
      callback.onSuccess(this);
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