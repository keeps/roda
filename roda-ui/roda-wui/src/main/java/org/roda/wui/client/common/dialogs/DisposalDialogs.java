package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult.ActionType;
import org.roda.wui.client.common.dialogs.utils.DisposalScheduleDialogResult;
import org.roda.wui.client.common.lists.utils.TooltipTextColumn;
import org.roda.wui.client.disposal.hold.CreateDisposalHold;
import org.roda.wui.client.disposal.schedule.CreateDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.MyCellTableResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

import config.i18n.client.ClientMessages;

;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Boolean applyToHierarchy = true;
  private static Boolean overwriteAll = false;

  public static void showDisposalHoldSelection(String title, DisposalHolds holds,
    final AsyncCallback<DisposalHoldDialogResult> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    Button clearHoldsButton = new Button(messages.clearDisposalHoldButton());
    Button overrideDisposalHoldButton = new Button(messages.overrideDisposalHoldButton());
    Button cancelButton = new Button(messages.cancelButton());
    Button selectHoldButton = new Button(messages.applyDisposalHoldButton());
    Button newHoldButton = new Button(messages.createDisposalHoldButton());
    FlowPanel footer = new FlowPanel();

    overrideDisposalHoldButton.setEnabled(false);
    selectHoldButton.setEnabled(false);
    selectHoldButton.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
    clearHoldsButton.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
    overrideDisposalHoldButton.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
    newHoldButton.getElement().getStyle().setFloat(Style.Float.LEFT);
    dialogBox.setWidget(layout);

    CellTable<DisposalHold> table = new CellTable<>(Integer.MAX_VALUE,
      (MyCellTableResources) GWT.create(MyCellTableResources.class));

    table.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);

    final ScrollPanel displayScroll = new ScrollPanel(table);
    displayScroll.setSize("100%", "80vh");
    final SimplePanel displayScrollWrapper = new SimplePanel(displayScroll);
    displayScrollWrapper.addStyleName("disposal-schedules-modal-table");

    final SingleSelectionModel<DisposalHold> singleSelectionModel = new SingleSelectionModel<>();
    singleSelectionModel.addSelectionChangeHandler(event -> {
      singleSelectionModel.setSelected(singleSelectionModel.getSelectedObject(), true);
      selectHoldButton.setEnabled(true);
      overrideDisposalHoldButton.setEnabled(true);
    });

    table.setSelectionModel(singleSelectionModel);

    table.addColumn(new TooltipTextColumn<DisposalHold>() {
      @Override
      public String getValue(DisposalHold disposalHold) {
        return disposalHold != null && disposalHold.getTitle() != null ? disposalHold.getTitle() : "";
      }
    }, messages.disposalScheduleTitle());

    table.addColumn(new TooltipTextColumn<DisposalHold>() {
      @Override
      public String getValue(DisposalHold disposalHold) {
        return disposalHold != null && disposalHold.getMandate() != null ? disposalHold.getMandate() : "";
      }
    }, messages.disposalScheduleMandate());

    // Create a list data provider.
    final ListDataProvider<DisposalHold> dataProvider = new ListDataProvider<>(holds.getObjects());

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

    selectHoldButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      DisposalHoldDialogResult result = new DisposalHoldDialogResult(ActionType.ASSOCIATE,
        singleSelectionModel.getSelectedObject());
      callback.onSuccess(result);
    });

    newHoldButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      callback.onFailure(null);
      HistoryUtils.newHistory(CreateDisposalHold.RESOLVER);
    });

    overrideDisposalHoldButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      DisposalHoldDialogResult result = new DisposalHoldDialogResult(ActionType.OVERRIDE,
        singleSelectionModel.getSelectedObject());
      callback.onSuccess(result);
    });

    clearHoldsButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      DisposalHoldDialogResult result = new DisposalHoldDialogResult(ActionType.CLEAR);
      callback.onSuccess(result);
    });

    cancelButton.addStyleName("btn btn-link");
    selectHoldButton.addStyleName("btn btn-play");
    clearHoldsButton.addStyleName("btn btn-danger btn-ban");
    overrideDisposalHoldButton.addStyleName("btn btn-play");
    newHoldButton.addStyleName("btn btn-plus");
    table.addStyleName("my-asyncdatagrid-display");

    layout.addStyleName("wui-dialog-layout");
    footer.addStyleName("wui-dialog-layout-footer");

    footer.add(newHoldButton);
    footer.add(cancelButton);
    footer.add(clearHoldsButton);
    footer.add(overrideDisposalHoldButton);
    footer.add(selectHoldButton);

    dialogBox.setWidget(layout);
    dialogBox.center();
    dialogBox.show();
  }

  public static void showDisposalScheduleSelection(String title, DisposalSchedules schedules,
    final AsyncCallback<DisposalScheduleDialogResult> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);
    applyToHierarchy = true;
    overwriteAll = false;

    FlowPanel layout = new FlowPanel();
    Button cancelButton = new Button(messages.cancelButton());
    Button changeScheduleButton = new Button(messages.associateDisposalScheduleButton());
    Button noScheduleButton = new Button(messages.disassociateDisposalScheduleButton());
    Button newScheduleButton = new Button(messages.createDisposalScheduleButton());
    CheckBox applyToHierarchyCheckBox = new CheckBox(messages.applyAllButton());
    Label applyToHierarchyDescription = new Label(messages.applyToHierarchyDisposalDialogDescription());
    CheckBox overwriteAllCheckBox = new CheckBox(messages.overwriteAllDisposalDialogCheckBox());
    Label overwriteAllDescription = new Label(messages.overwriteAllDisposalDialogDescription());
    FlowPanel options = new FlowPanel();
    FlowPanel footer = new FlowPanel();

    changeScheduleButton.setEnabled(false);
    noScheduleButton.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
    newScheduleButton.getElement().getStyle().setFloat(Style.Float.LEFT);
    dialogBox.setWidget(layout);

    CellTable<DisposalSchedule> table = new CellTable<>(Integer.MAX_VALUE,
      (MyCellTableResources) GWT.create(MyCellTableResources.class));

    table.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);

    final ScrollPanel displayScroll = new ScrollPanel(table);
    displayScroll.setSize("100%", "70vh");
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
        if (disposalSchedule.getRetentionPeriodIntervalCode() != null && disposalSchedule.getRetentionPeriodDuration() == null) {
          return messages.retentionPeriod(0, disposalSchedule.getRetentionPeriodIntervalCode().name());
        } else if (disposalSchedule.getRetentionPeriodIntervalCode() != null) {
          return messages.retentionPeriod(disposalSchedule.getRetentionPeriodDuration(), disposalSchedule.getRetentionPeriodIntervalCode().name());
        } else {
          return "";
        }
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
    layout.add(options);
    layout.add(footer);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onFailure(null);
    });

    noScheduleButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      DisposalScheduleDialogResult result = new DisposalScheduleDialogResult(
        DisposalScheduleDialogResult.ActionType.CLEAR, singleSelectionModel.getSelectedObject(), applyToHierarchy,
        overwriteAll);
      callback.onSuccess(result);
    });

    changeScheduleButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      GWT.log(applyToHierarchy.toString());
      DisposalScheduleDialogResult result = new DisposalScheduleDialogResult(
        DisposalScheduleDialogResult.ActionType.ASSOCIATE, singleSelectionModel.getSelectedObject(), applyToHierarchy,
        overwriteAll);
      callback.onSuccess(result);
    });

    newScheduleButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      callback.onFailure(null);
      HistoryUtils.newHistory(CreateDisposalSchedule.RESOLVER);
    });

    applyToHierarchyCheckBox.setValue(true);
    applyToHierarchyCheckBox.addStyleName("disposal-dialogs-checkbox form-checkbox");
    applyToHierarchyDescription.addStyleName("form-help");
    applyToHierarchyCheckBox.addValueChangeHandler(valueChangeEvent -> {
      applyToHierarchy = valueChangeEvent.getValue();
      if (applyToHierarchy) {
        overwriteAllCheckBox.setEnabled(true);
      } else {
        overwriteAllCheckBox.setEnabled(false);
        overwriteAllCheckBox.setValue(false);
        overwriteAll = false;
        GWT.log("overwriteAll: " + overwriteAll);
      }
    });

    overwriteAllCheckBox.setValue(false);
    overwriteAllCheckBox.addStyleName("disposal-dialogs-checkbox form-checkbox");
    overwriteAllDescription.addStyleName("form-help");
    overwriteAllCheckBox.addValueChangeHandler(valueChangeEvent -> {
      overwriteAll = valueChangeEvent.getValue();
      GWT.log("onchange overwriteAll: " + overwriteAll);
    });

    cancelButton.addStyleName("btn btn-link");
    noScheduleButton.addStyleName("btn btn-danger btn-ban");
    changeScheduleButton.addStyleName("btn btn-play");
    newScheduleButton.addStyleName("btn btn-plus");
    table.addStyleName("my-asyncdatagrid-display");

    layout.addStyleName("wui-dialog-layout");
    options.add(applyToHierarchyCheckBox);
    options.add(applyToHierarchyDescription);
    options.add(overwriteAllCheckBox);
    options.add(overwriteAllDescription);
    footer.addStyleName("wui-dialog-layout-footer");
    footer.add(newScheduleButton);
    footer.add(cancelButton);
    footer.add(noScheduleButton);
    footer.add(changeScheduleButton);

    dialogBox.setWidget(layout);
    dialogBox.center();
    dialogBox.show();
  }

  public static void showApplyRules(String title, AsyncCallback<Boolean> callback) {
    FlowPanel layout = new FlowPanel();
    FlowPanel footer = new FlowPanel();
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    layout.addStyleName("content");
    layout.addStyleName("wui-dialog-layout");
    footer.addStyleName("wui-dialog-layout-footer");

    Button cancelButton = new Button(messages.cancelButton());
    cancelButton.addStyleName("btn btn-link");
    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onFailure(null);
    });

    Label messageLabel = new Label(messages.applyDisposalRulesDialogExplanation());
    messageLabel.addStyleName("wui-dialog-message");

    Button applyToManualInclusiveButton = new Button();
    applyToManualInclusiveButton.setText(messages.overrideManualAppliedSchedulesButton());
    applyToManualInclusiveButton.addStyleName("btn btn-danger btn-play");
    applyToManualInclusiveButton.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
    applyToManualInclusiveButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      callback.onSuccess(true);
    });

    Button applyToManualExclusiveButton = new Button();
    applyToManualExclusiveButton.setText(messages.applyDisposalRulesButton());
    applyToManualExclusiveButton.addStyleName("btn btn-play");
    applyToManualExclusiveButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onSuccess(false);
    });

    footer.add(cancelButton);
    footer.add(applyToManualInclusiveButton);
    footer.add(applyToManualExclusiveButton);

    layout.add(messageLabel);
    layout.add(footer);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    dialogBox.setWidget(layout);
    dialogBox.center();
    dialogBox.show();
  }

}
