package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.common.lists.utils.TooltipTextColumn;
import org.roda.wui.client.disposal.schedule.CreateDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.MyCellTableResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void showDisposalScheduleSelection(String title, DisposalSchedules schedules,
    final AsyncCallback<DisposalSchedule> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    Button cancelButton = new Button(messages.cancelButton());
    Button changeScheduleButton = new Button(messages.applyDisposalScheduleButton());
    Button noScheduleButton = new Button(messages.removeDisposalScheduleButton());
    Button newScheduleButton = new Button(messages.createDisposalScheduleButton());
    FlowPanel footer = new FlowPanel();

    footer.add(cancelButton);
    changeScheduleButton.setEnabled(false);
    changeScheduleButton.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
    newScheduleButton.getElement().getStyle().setFloat(Style.Float.LEFT);
    dialogBox.setWidget(layout);

    CellTable<DisposalSchedule> table = new CellTable<>(Integer.MAX_VALUE,
      (MyCellTableResources) GWT.create(MyCellTableResources.class));

    table.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);

    final ScrollPanel displayScroll = new ScrollPanel(table);
    displayScroll.setSize("100%", "80vh");
    final SimplePanel displayScrollWrapper = new SimplePanel(displayScroll);
    displayScrollWrapper.addStyleName("disposal-schedules-modal-table");

    final SingleSelectionModel<DisposalSchedule> singleSelectionModel = new SingleSelectionModel<>();
    singleSelectionModel.addSelectionChangeHandler(event -> {
      singleSelectionModel.setSelected(singleSelectionModel.getSelectedObject(), true);
      changeScheduleButton.setEnabled(true);
    });

    table.setSelectionModel(singleSelectionModel);

    table.addColumn(new TooltipTextColumn<DisposalSchedule>() {
      @Override
      public String getValue(DisposalSchedule disposalSchedule) {
        return disposalSchedule != null && disposalSchedule.getTitle() != null ? disposalSchedule.getTitle() : "";
      }
    }, messages.disposalScheduleTitle());

    table.addColumn(new TooltipTextColumn<DisposalSchedule>() {
      @Override
      public String getValue(DisposalSchedule disposalSchedule) {
        return disposalSchedule != null && disposalSchedule.getMandate() != null ? disposalSchedule.getMandate() : "";
      }
    }, messages.disposalScheduleMandate());

    table.addColumn(new TextColumn<DisposalSchedule>() {
      @Override
      public String getValue(DisposalSchedule disposalSchedule) {
        return disposalSchedule != null && disposalSchedule.getRetentionPeriodDuration() != null
          ? Integer.toString(disposalSchedule.getRetentionPeriodDuration())
          : "";
      }
    }, messages.disposalSchedulePeriod());

    table.addColumn(new TextColumn<DisposalSchedule>() {
      @Override
      public String getValue(DisposalSchedule disposalSchedule) {
        return disposalSchedule != null && disposalSchedule.getActionCode() != null
          ? messages.disposalScheduleAction(disposalSchedule.getActionCode().name())
          : "";
      }
    }, messages.disposalScheduleActionCol());

    // Create a list data provider.
    final ListDataProvider<DisposalSchedule> dataProvider = new ListDataProvider<>(schedules.getObjects());

    // Add the table to the dataProvider.
    dataProvider.addDataDisplay(table);

    layout.add(displayScrollWrapper);
    layout.add(footer);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onFailure(null);
    });

    noScheduleButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      callback.onSuccess(null);
    });

    changeScheduleButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      callback.onSuccess(singleSelectionModel.getSelectedObject());
    });

    newScheduleButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      callback.onFailure(null);
      HistoryUtils.newHistory(CreateDisposalSchedule.RESOLVER);
    });

    cancelButton.addStyleName("btn btn-link");
    noScheduleButton.addStyleName("btn btn-danger btn-ban");
    changeScheduleButton.addStyleName("btn btn-play");
    newScheduleButton.addStyleName("btn btn-plus");
    table.addStyleName("my-asyncdatagrid-display");

    layout.addStyleName("wui-dialog-layout");
    footer.addStyleName("wui-dialog-layout-footer");
    footer.add(newScheduleButton);
    footer.add(changeScheduleButton);
    footer.add(noScheduleButton);
    footer.add(cancelButton);

    dialogBox.setWidget(layout);
    dialogBox.center();
    dialogBox.show();
  }
}
