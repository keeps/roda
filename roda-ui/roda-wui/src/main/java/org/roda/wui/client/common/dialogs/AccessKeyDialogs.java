/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.CreateAccessKeyRequest;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

import java.util.Date;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

public class AccessKeyDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void showAccessKeyDialog(String title, AccessKey accessKey, final AsyncCallback<Boolean> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    final Button closeButton = new Button(messages.closeButton());
    final Button copyAndCloseButton = new Button(messages.copyAndCloseButton());
    final FlowPanel layout = new FlowPanel();
    final FlowPanel header = new FlowPanel();
    final FlowPanel footer = new FlowPanel();
    final Label infoMessage = new Label(messages.accessKeyInfo());
    final Label warningMessage = new Label(messages.accessKeyWarningLabel());
    final Label tokenMessage = new Label(accessKey.getKey());
    tokenMessage.getElement().setId("token_message");
    closeButton.getElement().setId("closeButton");

    dialogBox.setText(title);
    layout.add(header);
    layout.add(footer);
    header.add(infoMessage);
    header.add(tokenMessage);
    header.add(warningMessage);
    footer.add(closeButton);
    footer.add(copyAndCloseButton);

    tokenMessage.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        JavascriptUtils.copyToClipboard("token_message");
        Toast.showInfo(messages.copiedToClipboardTitle(), messages.copiedToClipboardMessage());
      }
    });

    copyAndCloseButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        JavascriptUtils.copyToClipboard("token_message");
        Toast.showInfo(messages.copiedToClipboardTitle(), messages.copiedToClipboardMessage());
        dialogBox.hide();
        callback.onSuccess(true);
      }
    });

    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        dialogBox.hide();
        callback.onSuccess(true);
      }
    });

    dialogBox.setWidget(layout);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    header.addStyleName("wui-dialog-message");
    footer.addStyleName("wui-dialog-layout-footer");

    infoMessage.addStyleName("token-infoMessage");
    warningMessage.addStyleName("label-tokenInfo");
    copyAndCloseButton.addStyleName("btn btn-times-circle");
    closeButton.addStyleName("btn btn-times-circle");

    dialogBox.center();
    dialogBox.show();
  }

  public static void createAccessKeyDialog(String title, String tokenName, boolean create,
                                           final AsyncCallback<CreateAccessKeyRequest> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    final Button cancelButton = new Button(messages.cancelButton());
    final Button confirmButton = new Button(messages.confirmButton());
    final FlowPanel layout = new FlowPanel();
    final FlowPanel header = new FlowPanel();
    final FlowPanel footer = new FlowPanel();

    final Label tokenNameLabel = new Label(messages.accessKeyNameLabel());
    final TextBox tokenNameTextBox = new TextBox();
    final Label tokenNameTextBoxErrorLabel = new Label(messages.mandatoryField());

    if (!create) {
      tokenNameTextBox.setText(tokenName);
      tokenNameTextBox.setEnabled(false);
    }

    final Label expirationDateLabel = new Label(messages.accessKeyExpirationDateLabel());
    final DateBox expirationDateBox = new DateBox();
    final Label expirationDateErrorLabel = new Label();

    tokenNameTextBoxErrorLabel.addStyleName("form-label-error");
    tokenNameTextBoxErrorLabel.setVisible(false);
    expirationDateErrorLabel.addStyleName("form-label-error");
    expirationDateErrorLabel.setVisible(false);

    // Setting up DateBox format
    DateBox.DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    expirationDateBox.setFormat(dateFormat);
    expirationDateBox.getDatePicker().setYearArrowsVisible(true);
    expirationDateBox.setFireNullValues(true);
    expirationDateBox.setValue(new Date());

    dialogBox.setText(title);
    layout.add(header);
    layout.add(footer);

    header.add(tokenNameLabel);
    header.add(tokenNameTextBox);
    header.add(tokenNameTextBoxErrorLabel);

    header.add(expirationDateLabel);
    header.add(expirationDateBox);
    header.add(expirationDateErrorLabel);

    footer.add(cancelButton);
    footer.add(confirmButton);

    confirmButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        boolean errors = false;
        String name = tokenNameTextBox.getText();
        if (create && (name == null || StringUtils.isBlank(name))) {
          tokenNameTextBoxErrorLabel.setVisible(true);
          errors = true;
        } else {
          tokenNameTextBoxErrorLabel.setVisible(false);
        }

        Date selectedDate = expirationDateBox.getValue();
        if (selectedDate == null) {
          expirationDateErrorLabel.setVisible(true);
          expirationDateErrorLabel.setText(messages.mandatoryField());
          errors = true;
        } else if (selectedDate.before(new Date())) {
          expirationDateErrorLabel.setVisible(true);
          expirationDateErrorLabel.setText(messages.accessKeyExpirationDateInThePast());
          errors = true;
        } else {
          expirationDateErrorLabel.setVisible(false);
        }

        if (!errors) {
          dialogBox.hide();
          CreateAccessKeyRequest request = new CreateAccessKeyRequest();
          if (create) {
            request.setName(tokenNameTextBox.getText());
          }
          request.setExpirationDate(selectedDate);
          callback.onSuccess(request);
        }
      }
    });

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        dialogBox.hide();
        callback.onSuccess(null);
      }
    });

    dialogBox.setWidget(layout);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    header.addStyleName("wui-dialog-message");
    footer.addStyleName("wui-dialog-layout-footer");

    tokenNameLabel.addStyleName("form-label");
    tokenNameTextBox.addStyleName("form-textbox form-textbox-small");
    expirationDateLabel.addStyleName("form-label");
    expirationDateBox.addStyleName("form-textbox form-textbox-small");
    confirmButton.addStyleName("btn btn-play");
    cancelButton.addStyleName("btn btn-link");

    dialogBox.center();
    dialogBox.show();
  }
}
