/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.wui.client.common.utils.JavascriptUtils;
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
}
