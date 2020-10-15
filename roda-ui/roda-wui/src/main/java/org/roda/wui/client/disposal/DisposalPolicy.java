package org.roda.wui.client.disposal;

import java.util.List;

import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

public class DisposalPolicy extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
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

  @UiField
  FlowPanel disposalPolicyDescription;

  @UiField
  FlowPanel contentDisposalSchedulesTable;

  @UiField
  Button newDisposalSchedule;

  @UiField
  FlowPanel contentDisposalHoldsTable;

  @UiField
  Button newDisposalHold;

  private DisposalSchedules disposalSchedules;
  private DisposalHolds disposalHolds;

  /**
   * Create a representation information page
   */
  private DisposalPolicy(DisposalSchedules disposalSchedules, DisposalHolds disposalHolds) {
    initWidget(uiBinder.createAndBindUi(this));
    this.disposalSchedules = disposalSchedules;
    this.disposalHolds = disposalHolds;

    // Disposal schedules table
    if (disposalSchedules.getObjects().isEmpty()) {
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters("disposal schedules")));
      label.addStyleName("basicTableEmpty");
      contentDisposalSchedulesTable.add(label);
    } else {
      FlowPanel schedulesPanel = new FlowPanel();
      BasicTablePanel<DisposalSchedule> table = getBasicTablePanelForDisposalSchedules(disposalSchedules);
      table.getSelectionModel().addSelectionChangeHandler(event -> {
        DisposalSchedule selectedObject = table.getSelectionModel().getSelectedObject();
        if (selectedObject != null) {
          table.getSelectionModel().clear();
        }
      });

      schedulesPanel.add(table);
      contentDisposalSchedulesTable.add(schedulesPanel);

      // if(PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_SCHEDULE)){
      //Button buttonNewHold = new Button("New disposal schedule");
      //buttonNewHold.addStyleName("rodaButton");
      //contentNewDisposalScheduleBtn.add(buttonNewHold);
      // }
    }

    // Disposal holds table
    if (disposalHolds.getObjects().isEmpty()) {
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters("disposal holds")));
      label.addStyleName("basicTableEmpty");
      contentDisposalHoldsTable.add(label);
    } else {
      FlowPanel holdsPanel = new FlowPanel();
      BasicTablePanel<DisposalHold> tableHolds = getBasicTablePanelForDisposalHolds(disposalHolds);
      tableHolds.getSelectionModel().addSelectionChangeHandler(event -> {
        DisposalHold selectedHold = tableHolds.getSelectionModel().getSelectedObject();
        if (selectedHold != null) {
          tableHolds.getSelectionModel().clear();
        }
      });

      holdsPanel.add(tableHolds);
      contentDisposalHoldsTable.add(holdsPanel);

      // if(PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_SCHEDULE)){
      //Button buttonNewHold = new Button("New disposal hold");
      //buttonNewHold.addStyleName("rodaButton");
      //buttonNewHold.click();
      //contentNewDisposalHoldBtn.add(buttonNewHold);

      // }
    }

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  private BasicTablePanel<DisposalSchedule> getBasicTablePanelForDisposalSchedules(
    DisposalSchedules disposalSchedules) {
    Label header = new Label("Disposal Schedule");
    header.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");

    if (disposalSchedules.getObjects().isEmpty()) {
      return new BasicTablePanel<>(header, messages.noItemsToDisplayPreFilters("disposal schedules"));
    } else {
      return new BasicTablePanel<DisposalSchedule>(header, info, disposalSchedules.getObjects().iterator(),

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

        new BasicTablePanel.ColumnInfo<>(messages.disposalSchedulePeriod(), 8, new TextColumn<DisposalSchedule>() {
          @Override
          public String getValue(DisposalSchedule schedule) {
            String value = schedule.getRetentionPeriodDuration().toString() + " "
              + schedule.getRetentionPeriodIntervalCode().toString().toLowerCase();
            return value;
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleActionCol(), 8, new TextColumn<DisposalSchedule>() {
          @Override
          public String getValue(DisposalSchedule schedule) {
            return messages.disposalScheduleAction(schedule.getActionCode().toString());
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleNumberOfAIPs(), 8,
          new TextColumn<DisposalSchedule>() {

            @Override
            public String getValue(DisposalSchedule schedule) {
              return schedule.getNumberOfAIPUnder().toString();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleStateCol(), 8,
          new Column<DisposalSchedule, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(DisposalSchedule schedule) {
              return HtmlSnippetUtils.getDisposalScheduleStateHtml(schedule);

            }
          }));
    }
  }

  private BasicTablePanel<DisposalHold> getBasicTablePanelForDisposalHolds(DisposalHolds disposalHolds) {
    Label headerHolds = new Label("Disposal Hold");
    headerHolds.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");

    if (disposalHolds.getObjects().isEmpty()) {
      return new BasicTablePanel<>(headerHolds, messages.noItemsToDisplayPreFilters("disposal holds"));
    } else {
      return new BasicTablePanel<DisposalHold>(headerHolds, info, disposalHolds.getObjects().iterator(),

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

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldNumberOfAIPs(), 8, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            return String.valueOf(hold.getActiveAIPs().size());
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 8,
          new Column<DisposalHold, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(DisposalHold hold) {
              return HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
            }
          }));
    }
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } /*
       * else { String basePage = historyTokens.remove(0); if
       * (ShowRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
       * ShowRepresentationInformation.RESOLVER.resolve(historyTokens, callback); }
       * else if
       * (CreateRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage))
       * { CreateRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
       * } else if
       * (EditRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
       * EditRepresentationInformation.RESOLVER.resolve(historyTokens, callback); }
       * else if
       * (RepresentationInformationAssociations.RESOLVER.getHistoryToken().equals(
       * basePage)) {
       * RepresentationInformationAssociations.RESOLVER.resolve(historyTokens,
       * callback); } else if (Search.RESOLVER.getHistoryToken().equals(basePage)) {
       * searchPanel.setFilter(RepresentationInformation.class,
       * SearchFilters.createFilterFromHistoryTokens(historyTokens));
       * callback.onSuccess(this); } else { HistoryUtils.newHistory(RESOLVER);
       * callback.onSuccess(null); } }
       */
  }
}