package org.roda.wui.client.common;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class Dialogs {

  public static void showConfirmDialog(String title, String message, String cancelButtonText, String confirmButtonText,
    final AsyncCallback<Boolean> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    Label messageLabel = new Label(message);
    Button cancelButton = new Button(cancelButtonText);
    Button confirmButton = new Button(confirmButtonText);

    layout.add(messageLabel);
    layout.add(cancelButton);
    layout.add(confirmButton);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(false);
      }
    });

    confirmButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(true);
      }
    });

    dialogBox.addStyleName("wui-dialog-confirm");
    layout.addStyleName("wui-dialog-confirm-layout");
    messageLabel.addStyleName("wui-dialog-confirm-message");
    cancelButton.addStyleName("wui-dialog-confirm-cancel btn btn-ban");
    confirmButton.addStyleName("wui-dialog-confirm-ok btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showInformationDialog(String title, String message, String continueButtonText,
    final AsyncCallback<Void> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    Label messageLabel = new Label(message);
    Button continueButton = new Button(continueButtonText);

    layout.add(messageLabel);
    layout.add(continueButton);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    continueButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(null);
      }
    });

    dialogBox.addStyleName("wui-dialog-information");
    layout.addStyleName("wui-dialog-information-layout");
    messageLabel.addStyleName("wui-dialog-information-message");
    continueButton.addStyleName("wui-dialog-information-ok btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showPromptDialog(String title, String message, final RegExp validator, String cancelButtonText,
    String confirmButtonText, final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();
    final Label messageLabel = new Label(message);
    final TextBox inputBox = new TextBox();
    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);

    layout.add(messageLabel);
    layout.add(inputBox);
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
        callback.onSuccess(inputBox.getText());
      }
    });
    
    inputBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        boolean isValid = validator.test(inputBox.getText());
        confirmButton.setEnabled(isValid);
        if(isValid) {
          inputBox.addStyleName("error");
        } else {
          inputBox.removeStyleName("error");
        }
      }
    });
    
    confirmButton.setEnabled(validator.test(inputBox.getText()));

    dialogBox.addStyleName("wui-dialog-confirm");
    layout.addStyleName("wui-dialog-confirm-layout");
    messageLabel.addStyleName("wui-dialog-confirm-message");
    cancelButton.addStyleName("wui-dialog-confirm-cancel btn btn-ban");
    confirmButton.addStyleName("wui-dialog-confirm-ok btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

}
