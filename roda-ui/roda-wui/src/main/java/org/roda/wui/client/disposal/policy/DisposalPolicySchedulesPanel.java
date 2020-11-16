package org.roda.wui.client.disposal.policy;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
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

public class DisposalPolicySchedulesPanel extends Composite {

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicySchedulesPanel> {
  }

  private static DisposalPolicySchedulesPanel.MyUiBinder uiBinder = GWT
    .create(DisposalPolicySchedulesPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // Disposal Schedules
  @UiField
  FlowPanel disposalSchedulesDescription;

  @UiField
  ScrollPanel disposalSchedulesTablePanel;

  private void createDisposalSchedulesDescription(FlowPanel disposalSchedulesDescription) {
    Label header = new Label(messages.disposalSchedulesTitle());
    header.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");
    info.add(new HTMLWidgetWrapper("DisposalScheduleDescription.html"));
    info.addStyleName("page-description");
    disposalSchedulesDescription.add(header);
    disposalSchedulesDescription.add(info);

  }

  private void createDisposalSchedulesPanel(ScrollPanel disposalSchedulesTablePanel,
    DisposalSchedules disposalSchedules) {
    disposalSchedulesTablePanel.addStyleName("basicTable-border");
    disposalSchedulesTablePanel.addStyleName("basicTable");
    if (disposalSchedules.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(disposalSchedules.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
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

  public DisposalPolicySchedulesPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)) {
      BrowserService.Util.getInstance().listDisposalSchedules(new NoAsyncCallback<DisposalSchedules>() {
        @Override
        public void onSuccess(DisposalSchedules disposalSchedules) {
          init(disposalSchedulesDescription, disposalSchedulesTablePanel, disposalSchedules);
        }
      });
    }
  }

  private void init(FlowPanel disposalSchedulesDescription, ScrollPanel disposalSchedulesTablePanel,
    DisposalSchedules disposalSchedules) {
    // Create disposal schedules description
    createDisposalSchedulesDescription(disposalSchedulesDescription);

    // Disposal schedules table
    createDisposalSchedulesPanel(disposalSchedulesTablePanel, disposalSchedules);
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
              return schedule.getRetentionPeriodDuration().toString() + " "
                + schedule.getRetentionPeriodIntervalCode().toString().toLowerCase();
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
              return schedule.getApiCounter().toString();
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

}