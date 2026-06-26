/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import java.util.List;

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.rule.ChangeOrderRequest;
import org.roda.core.data.v2.disposal.rule.OrderPositions;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult.ActionType;
import org.roda.wui.client.common.dialogs.utils.DisposalScheduleDialogResult;
import org.roda.wui.client.common.lists.DisposalHoldList;
import org.roda.wui.client.common.lists.DisposalScheduleList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.RadioButton;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void reOrderDisposalRules(String title, String message, AsyncCallback<ChangeOrderRequest> callback) {
    IntegerBox positionInput;
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setHTML(title);
    dialogBox.addStyleName("create-group-dialog");

    final FlowPanel layout = new FlowPanel();

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(messages.cancelButton());
    final Button saveButton = new Button(messages.saveButton());
    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    final FlowPanel content = new FlowPanel();
    content.addStyleName("wui-dialog-content");

    HTML messageLabel = new HTML(message);
    messageLabel.addStyleName("wui-dialog-message");
    layout.add(messageLabel);

    // Create Radio Buttons in the same group
    RadioButton moveTopRadio = new RadioButton("destinationGroup", messages.moveToTop());
    RadioButton moveBottomRadio = new RadioButton("destinationGroup", messages.moveToBottom());
    RadioButton movePosRadio = new RadioButton("destinationGroup", messages.moveToPositionNumber());

    moveTopRadio.setValue(true); // Default selection

    // Input box for specific position
    positionInput = new IntegerBox();
    positionInput.setWidth("50px");
    positionInput.setEnabled(false); // Disabled by default until radio is clicked

    // Handler to toggle the input box based on radio selection
    ClickHandler radioClickHandler = event -> positionInput.setEnabled(movePosRadio.getValue());
    moveTopRadio.addClickHandler(radioClickHandler);
    moveBottomRadio.addClickHandler(radioClickHandler);
    movePosRadio.addClickHandler(radioClickHandler);

    HorizontalPanel posPanel = new HorizontalPanel();
    posPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    posPanel.add(movePosRadio);
    posPanel.add(positionInput);

    content.add(moveTopRadio);
    content.add(moveBottomRadio);
    content.add(posPanel);

    layout.add(content);
    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ChangeOrderRequest changeOrderRequest = new ChangeOrderRequest();
        dialogBox.hide();
        if (moveTopRadio.getValue()) {
          changeOrderRequest.setPosition(OrderPositions.TOP);
        } else if (moveBottomRadio.getValue()) {
          changeOrderRequest.setPosition(OrderPositions.BOTTOM);
        } else {
          changeOrderRequest.setPosition(OrderPositions.POSITION);
          changeOrderRequest.setNewOrder(positionInput.getValue() != null ? positionInput.getValue() : 1);
        }
        callback.onSuccess(changeOrderRequest);
      }
    });

    cancelButton.addStyleName("btn btn-link");
    saveButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showDisposalHoldSelection(String title, Filter filter,
    final AsyncCallback<DisposalHoldDialogResult> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    FlowPanel footer = new FlowPanel();
    Button cancelButton = new Button(messages.cancelButton());
    Button associateButton = new Button(messages.associateDisposalHoldButton());
    associateButton.setEnabled(false);

    ListBuilder<DisposalHold> listBuilder = new ListBuilder<>(() -> new DisposalHoldList(false),
      new AsyncTableCellOptions<>(DisposalHold.class, "DisposalHoldSelection_holds").withFilter(filter)
        .withCsvDownloadButtonVisibility(false).withSummary(title).withRecenteringOfParentDialog(dialogBox)
        .withForceSelectable(true).addCheckboxSelectionListener(selected -> associateButton.setEnabled(
          selected instanceof SelectedItemsList<?> && !((SelectedItemsList<?>) selected).getIds().isEmpty())));

    SearchWrapper searchWrapper = new SearchWrapper(false).withListsInsideScrollPanel("selectAipResultsPanel")
      .createListAndSearchPanel(listBuilder);

    layout.add(searchWrapper);
    layout.add(footer);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onFailure(null);
    });

    associateButton.addClickHandler(event -> {
      SelectedItems<DisposalHold> selectedItems = searchWrapper.getSelectedItems(DisposalHold.class);
      if (selectedItems instanceof SelectedItemsList<?>) {
        List<String> ids = ((SelectedItemsList<DisposalHold>) selectedItems).getIds();
        if (!ids.isEmpty()) {
          dialogBox.hide();
          DisposalHoldDialogResult result = new DisposalHoldDialogResult(ActionType.ASSOCIATE, ids);
          callback.onSuccess(result);
        }
      }
    });

    cancelButton.addStyleName("btn btn-link");
    associateButton.addStyleName("btn btn-play");

    layout.addStyleName("wui-dialog-layout");
    footer.addStyleName("wui-dialog-layout-footer");
    footer.add(cancelButton);
    footer.add(associateButton);

    dialogBox.addStyleName("wui-dialog-prompt");
    dialogBox.setWidget(layout);
    dialogBox.center();
    dialogBox.show();
  }

  public static void showDisposalScheduleSelection(String title, Filter filter,
    final AsyncCallback<DisposalScheduleDialogResult> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    FlowPanel footer = new FlowPanel();
    Button cancelButton = new Button(messages.cancelButton());
    Button associateButton = new Button(messages.associateDisposalScheduleButton());
    associateButton.setEnabled(false);
    final DisposalScheduleList scheduleList = new DisposalScheduleList(true);
    final String[] selectedScheduleId = new String[1];

    ListBuilder<DisposalSchedule> listBuilder = new ListBuilder<>(() -> scheduleList,
      new AsyncTableCellOptions<>(DisposalSchedule.class, "DisposalScheduleSelection_schedules").withFilter(filter)
        .withCsvDownloadButtonVisibility(false).withSummary(title).withRecenteringOfParentDialog(dialogBox)
        .addSelectionChangeHandler(event -> {
          DisposalSchedule selected = scheduleList.getSelectionModel().getSelectedObject();
          selectedScheduleId[0] = selected != null ? selected.getUUID() : null;
          associateButton.setEnabled(selectedScheduleId[0] != null);
        }));

    SearchWrapper searchWrapper = new SearchWrapper(false).withListsInsideScrollPanel("selectAipResultsPanel")
      .createListAndSearchPanel(listBuilder);

    layout.add(searchWrapper);
    layout.add(footer);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onFailure(null);
    });

    associateButton.addClickHandler(event -> {
      if (selectedScheduleId[0] != null) {
        dialogBox.hide();
        DisposalScheduleDialogResult result = new DisposalScheduleDialogResult(
          DisposalScheduleDialogResult.ActionType.ASSOCIATE, selectedScheduleId[0]);
        callback.onSuccess(result);
      }
    });

    cancelButton.addStyleName("btn btn-link");
    associateButton.addStyleName("btn btn-play");

    layout.addStyleName("wui-dialog-layout");
    footer.addStyleName("wui-dialog-layout-footer");
    footer.add(cancelButton);
    footer.add(associateButton);

    dialogBox.addStyleName("wui-dialog-prompt");
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

    HTML messageLabel = new HTML(messages.applyDisposalRulesDialogExplanation());
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
