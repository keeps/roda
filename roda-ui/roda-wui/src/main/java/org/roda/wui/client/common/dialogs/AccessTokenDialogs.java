package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.AccessToken.AccessToken;
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
public class AccessTokenDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void showAccessTokenDialog(String title, AccessToken accessToken,
    final AsyncCallback<Boolean> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    final Button closeButton = new Button(messages.closeButton());
    final FlowPanel layout = new FlowPanel();
    final FlowPanel header = new FlowPanel();
    final FlowPanel footer = new FlowPanel();
    final Label warningMessage = new Label(messages.accessTokenWarningLabel());
    final Label tokenMessage = new Label(accessToken.getAccessKey());
    tokenMessage.getElement().setId("token_message");

    dialogBox.setText(title);
    layout.add(header);
    layout.add(footer);
    header.add(warningMessage);
    header.add(tokenMessage);
    footer.add(closeButton);

    tokenMessage.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        JavascriptUtils.copyToClipboard("token_message");
        Toast.showInfo(messages.copiedToClipboardTitle(), messages.copiedToClipboardMessage());
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

    warningMessage.addStyleName("label-warning");
    closeButton.addStyleName("btn btn-times-circle");

    dialogBox.center();
    dialogBox.show();
  }
}
