package org.roda.wui.client.common.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.wui.client.common.utils.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

public class RepresentationDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final String ADD_REPRESENTATION_TYPE = "#__ADDNEW__#";

  private RepresentationDialogs() {
    // do nothing
  }

  public static void showPromptDialogRepresentationTypes(String title, String message, String cancelButtonText,
    String confirmButtonText, List<String> types, boolean isControlledVocabulary,
    final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();

    if (message != null) {
      final Label messageLabel = new Label(message);
      layout.add(messageLabel);
      messageLabel.addStyleName("wui-dialog-message");
    }

    final ListBox select = new ListBox();

    for (String type : types) {
      select.addItem(type);
    }

    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);

    layout.add(select);

    final TextBox newTypeBox = new TextBox();
    final Label newTypeLabel = new Label(messages.entityTypeNewLabel());
    newTypeBox.setVisible(false);
    newTypeLabel.setVisible(false);

    if (!isControlledVocabulary) {
      select.addItem(messages.entityTypeAddNew(), ADD_REPRESENTATION_TYPE);

      newTypeBox.getElement().setPropertyString("placeholder", messages.entityTypeNewLabel());
      layout.add(newTypeLabel);
      layout.add(newTypeBox);
    }

    select.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        String selectedValue = select.getSelectedValue();
        newTypeLabel.setVisible(selectedValue.equals(ADD_REPRESENTATION_TYPE));
        newTypeBox.setVisible(selectedValue.equals(ADD_REPRESENTATION_TYPE));
      }
    });

    layout.add(cancelButton);
    layout.add(confirmButton);

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

    confirmButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();

        if (StringUtils.isNotBlank(newTypeBox.getText())) {
          callback.onSuccess(newTypeBox.getText());
        } else {
          callback.onSuccess(select.getSelectedValue());
        }
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    select.addStyleName("form-textbox wui-dialog-message");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");
    newTypeBox.addStyleName("form-textbox wui-dialog-message");

    dialogBox.center();
    dialogBox.show();
    select.setFocus(true);
  }

  private static CheckBox getRepresentationStateCheckBox(String state, boolean value) {
    final CheckBox stateBox = new CheckBox();
    stateBox.setText(messages.statusLabel(state));
    stateBox.setFormValue(state);
    stateBox.setValue(value);
    stateBox.addStyleName("form-checkbox");
    return stateBox;
  }

  public static void showPromptDialogRepresentationStates(String title, String cancelButtonText,
    String confirmButtonText, List<String> states, final AsyncCallback<List<String>> callback) {
    final DialogBox dialogBox = new DialogBox(true, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();
    final List<CheckBox> checkBoxes = new ArrayList<>();

    final Label label = new Label(messages.otherStatusLabel());
    final TextBox otherBox = new TextBox();
    otherBox.getElement().setPropertyString("placeholder", messages.otherStatusPlaceholder());
    otherBox.addStyleName("form-textbox wui-dialog-message");

    for (String state : states) {
      CheckBox stateBox = getRepresentationStateCheckBox(state, true);
      layout.add(stateBox);
      checkBoxes.add(stateBox);
    }

    for (String state : RepresentationState.values()) {
      if (!states.contains(state)) {
        CheckBox stateBox = getRepresentationStateCheckBox(state, false);
        layout.add(stateBox);
        checkBoxes.add(stateBox);
      }
    }

    layout.add(label);
    layout.add(otherBox);

    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);

    layout.add(cancelButton);
    layout.add(confirmButton);
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

    confirmButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        List<String> newStates = new ArrayList<>();

        for (CheckBox checkBox : checkBoxes) {
          if (checkBox.getValue()) {
            newStates.add(checkBox.getFormValue());
          }
        }

        if (StringUtils.isNotBlank(otherBox.getValue())) {
          newStates.add(otherBox.getValue());
        }

        callback.onSuccess(newStates);
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }
}
