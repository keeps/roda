/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.search.SearchSuggestBox;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;

import config.i18n.client.ClientMessages;

public class Dialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Dialogs() {
    // do nothing
  }

  public static void showConfirmDialog(String title, String message, String cancelButtonText, String confirmButtonText,
    final AsyncCallback<Boolean> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    Label messageLabel = new Label(message);
    Button cancelButton = new Button(cancelButtonText);
    Button confirmButton = new Button(confirmButtonText);
    FlowPanel footer = new FlowPanel();

    layout.add(messageLabel);
    layout.add(footer);
    footer.add(cancelButton);
    footer.add(confirmButton);

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
    layout.addStyleName("wui-dialog-layout");
    footer.addStyleName("wui-dialog-layout-footer");
    messageLabel.addStyleName("wui-dialog-message");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showLicenseModal(String title, HTML license) {
    final DialogBox dialogBox = new ClosableDialog(true, true);

    VerticalPanel panel = new VerticalPanel();

    ScrollPanel layout = new ScrollPanel();
    layout.setSize("70vw", "80vh");

    FlowPanel footer = new FlowPanel();

    Button closeButton = new Button(messages.closeButton());
    closeButton.addStyleName("btn btn-link");

    layout.setSize("70vw", "80vh");
    layout.addStyleName("wui-dialog-layout");
    layout.setWidget(license);

    footer.add(closeButton);

    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        dialogBox.hide();
      }
    });

    panel.add(layout);
    panel.add(footer);

    dialogBox.setWidget(panel);
    footer.addStyleName("wui-dialog-license-layout-footer");
    dialogBox.setText(title);
    dialogBox.addStyleName("wui-dialog-information");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);
    dialogBox.center();
    dialogBox.show();
  }

  public static void showInformationDialog(String title, final String message, String continueButtonText,
    boolean canCopyMessage) {
    showInformationDialog(title, message, continueButtonText, canCopyMessage, new NoAsyncCallback<>());
  }

  public static void showTechnicalMetadataInformation(String title, String downloadText, String closeText,
    IndexedFile file, String html) {
    final DialogBox dialogBox = new ClosableDialog(true, true);
    FlowPanel main = new FlowPanel();
    ScrollPanel layout = new ScrollPanel();
    FlowPanel footer = new FlowPanel();
    final Button downloadButton = new Button(downloadText);
    final Button closeButton = new Button(closeText);
    layout.setSize("70vw", "60vh");

    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setWidth("100%");
    if (html == null) {
      html = "<div><p>No technical metadata found. Please contact system administrator</p></div>";
    }
    HTML keyHtml = new HTML(html);
    keyHtml.setStyleName("value-overflow");
    verticalPanel.add(keyHtml);

    layout.add(verticalPanel);
    layout.addStyleName("wui-dialog-message");

    footer.add(closeButton);
    footer.add(downloadButton);
    footer.addStyleName("wui-dialog-layout-footer");

    downloadButton.addStyleName("btn btn-download");
    closeButton.addStyleName("btn btn-link");
    main.addStyleName("wui-dialog-layout");
    main.add(layout);
    main.add(footer);

    closeButton.addClickHandler(event -> {
      dialogBox.hide();
    });

    downloadButton.addClickHandler(event -> {
      SafeUri downloadUri = RestUtils.createTechnicalMetadataHTMLUri(file.getAipId(), file.getUUID(), "bin", null);
      Window.Location.assign(downloadUri.asString());
    });

    dialogBox.setWidget(main);
    dialogBox.setText(title);
    dialogBox.addStyleName("wui-dialog-information");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);
    dialogBox.center();
    dialogBox.show();
  }

  public static void showInformationDialog(String title, final String message, String continueButtonText,
    boolean canCopyMessage, final AsyncCallback<Void> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);
    FlowPanel layout = new FlowPanel();
    HTML messageLabel;

    if (canCopyMessage) {
      messageLabel = new HTML("<pre><code id='command_message'>" + message + "</code></pre>");
      messageLabel.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          JavascriptUtils.copyToClipboard("command_message");
          Toast.showInfo(messages.copiedToClipboardTitle(), messages.copiedToClipboardMessage());
        }
      });
    } else {
      messageLabel = new HTML(message);
    }

    layout.add(messageLabel);

    Button continueButton = new Button(continueButtonText);
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
    layout.addStyleName("wui-dialog-layout");
    messageLabel.addStyleName("wui-dialog-message");
    continueButton.addStyleName("btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showInformationDialog(String title, final SafeHtml message, String continueButtonText,
    final AsyncCallback<Void> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    HTML messageLabel = new HTML(message);

    layout.add(messageLabel);

    Button continueButton = new Button(continueButtonText);
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
    layout.addStyleName("wui-dialog-layout");
    messageLabel.addStyleName("wui-dialog-message");
    continueButton.addStyleName("btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showPromptDialog(String title, String message, String value, String placeHolder,
    final RegExp validator, String cancelButtonText, String confirmButtonText, boolean mandatory, boolean isBigText,
    final AsyncCallback<String> callback) {
    showPromptDialog(title, message, value, placeHolder, validator, "", cancelButtonText, confirmButtonText, mandatory,
      isBigText, callback);
  }

  public static void showPromptDialog(String title, String message, String value, String placeHolder,
    final RegExp validator, String validatorErrorMessage, String cancelButtonText, String confirmButtonText,
    boolean mandatory, boolean isBigText, final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();

    if (message != null) {
      final Label messageLabel = new Label(message);
      layout.add(messageLabel);
      messageLabel.addStyleName("wui-dialog-message");
    }

    TextBoxBase inputBox;
    if (!isBigText) {
      inputBox = new TextBox();
    } else {
      inputBox = new TextArea();
      inputBox.addStyleName("ri-edit-html-area");
    }

    if (value != null) {
      inputBox.setText(value);
    }

    if (placeHolder != null) {
      inputBox.getElement().setPropertyString("placeholder", placeHolder);
    }

    inputBox.setTitle("input box");
    layout.add(inputBox);

    SimplePanel errorPanel = new SimplePanel();
    layout.add(errorPanel);

    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);
    confirmButton.setEnabled(!mandatory);
    layout.add(cancelButton);
    layout.add(confirmButton);

    dialogBox.setWidget(layout);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onFailure(null);
    });

    confirmButton.addClickHandler(event -> {
      if (validator.test(inputBox.getText())) {
        dialogBox.hide();
        callback.onSuccess(inputBox.getText());
      }
    });

    inputBox.addValueChangeHandler(event -> {
      boolean isValid = validator.test(event.getValue());
      confirmButton.setEnabled(isValid);
      validatePromptInput(errorPanel, isValid, event.getValue(), validatorErrorMessage);
    });

    inputBox.addKeyUpHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        if (validator.test(inputBox.getText())) {
          dialogBox.hide();
          callback.onSuccess(inputBox.getText());
        }
      } else {
        TextBox box = (TextBox) event.getSource();
        boolean isValid = validator.test(box.getText());
        confirmButton.setEnabled(isValid);
        validatePromptInput(errorPanel, isValid, box.getText(), validatorErrorMessage);
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    inputBox.addStyleName("form-textbox wui-dialog-message");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
    inputBox.setFocus(true);
  }

  private static void validatePromptInput(SimplePanel errorPanel, boolean isValid, String text, String errorMessage) {
    if (isValid) {
      errorPanel.clear();
    } else {
      HTML htmlMessage;
      errorPanel.clear();
      if (StringUtils.isNotBlank(text)) {
        htmlMessage = new HTML(SafeHtmlUtils.fromSafeConstant(errorMessage));
      } else {
        htmlMessage = new HTML(messages.promptDialogEmptyInputError());
      }
      htmlMessage.addStyleName("form-help");
      htmlMessage.addStyleName("error");
      errorPanel.add(htmlMessage);
    }
  }

  public static void showPromptDialogSuggest(String title, String message, String placeHolder, String cancelButtonText,
    String confirmButtonText, SearchSuggestBox<?> suggestBox, final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();

    if (message != null) {
      final Label messageLabel = new Label(message);
      layout.add(messageLabel);
      messageLabel.addStyleName("wui-dialog-message");
    }

    final SearchSuggestBox<?> inputBox = suggestBox;

    if (placeHolder != null) {
      inputBox.getElement().setPropertyString("placeholder", placeHolder);
    }

    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);

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
        callback.onSuccess(inputBox.getValue());
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    inputBox.addStyleName("form-textbox wui-dialog-message");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
    inputBox.setFocus(true);
  }

  public static DialogBox showLoadingModel() {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText("Loading...");

    FlowPanel layout = new FlowPanel();
    Label messageLabel = new Label(messages.executingTaskMessage());

    layout.add(messageLabel);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    dialogBox.addStyleName("wui-dialog-information");
    layout.addStyleName("wui-dialog-layout");
    messageLabel.addStyleName("wui-dialog-message");

    dialogBox.center();
    dialogBox.show();
    return dialogBox;
  }

  public static DialogBox showJobRedirectDialog(String message, final AsyncCallback<Void> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(messages.jobCreatedRedirectTitle());

    FlowPanel layout = new FlowPanel();
    Label messageLabel = new Label(message);
    Button jobButton = new Button(messages.dialogYes());
    Button redirectButton = new Button(messages.dialogNo());

    redirectButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    jobButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(null);
      }
    });

    layout.add(messageLabel);
    layout.add(jobButton);
    layout.add(redirectButton);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    messageLabel.addStyleName("wui-dialog-message");
    jobButton.addStyleName("pull-right btn btn-play");
    redirectButton.addStyleName("btn btn-ban");

    dialogBox.center();
    dialogBox.show();
    return dialogBox;
  }

}
