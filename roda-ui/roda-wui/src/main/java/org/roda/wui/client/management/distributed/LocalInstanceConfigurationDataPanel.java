/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.distributed;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.tools.ValidationUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LocalInstanceConfigurationDataPanel extends Composite implements HasValueChangeHandlers<LocalInstance> {
  public static final String IS_WRONG = "isWrong";

  interface MyUiBinder extends UiBinder<Widget, LocalInstanceConfigurationDataPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox IDValue;

  @UiField
  Label IDError;

  @UiField
  TextBox secretValue;

  @UiField
  Label secretError;

  @UiField
  TextBox centralInstanceURLValue;

  @UiField
  Label centralInstanceURError;

  @UiField
  HTML errors;

  private boolean checked = false;

  public LocalInstanceConfigurationDataPanel(LocalInstance localInstance, boolean editMode) {
    initWidget(uiBinder.createAndBindUi(this));

    setInitialState(localInstance);
    initHandlers();

    if (editMode) {
      setLocalInstanceConfiguration(localInstance);
    }
  }

  public void setLocalInstanceConfiguration(LocalInstance localInstance) {
    this.IDValue.setText(localInstance.getId());
    this.secretValue.setText(localInstance.getAccessKey());
    this.centralInstanceURLValue.setText(localInstance.getCentralInstanceURL());
  }

  private void initHandlers() {
    ChangeHandler changeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        LocalInstanceConfigurationDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        LocalInstanceConfigurationDataPanel.this.onChange();
      }
    };

    IDValue.addChangeHandler(changeHandler);
    IDValue.addKeyUpHandler(keyUpHandler);

    secretValue.addChangeHandler(changeHandler);
    secretValue.addKeyUpHandler(keyUpHandler);

    centralInstanceURLValue.addChangeHandler(changeHandler);
    centralInstanceURLValue.addKeyUpHandler(keyUpHandler);
  }

  private void setInitialState(LocalInstance localInstance) {
    errors.setVisible(false);
    centralInstanceURLValue.getElement().setPropertyString("placeholder",
      messages.localInstanceConfigurationCentralInstanceURLPlaceholder());
  }

  public LocalInstance getLocalInstance() {
    LocalInstance localInstance = new LocalInstance();
    localInstance.setId(IDValue.getText());
    localInstance.setAccessKey(secretValue.getText());
    localInstance.setCentralInstanceURL(centralInstanceURLValue.getText());
    return localInstance;
  }

  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    // ID
    if (StringUtils.isBlank(IDValue.getText())) {
      IDValue.addStyleName(IS_WRONG);
      IDError.setText(messages.mandatoryField());
      IDError.setVisible(true);
      Window.scrollTo(IDValue.getAbsoluteLeft(), IDValue.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.localInstanceConfigurationIDLabel()));
    } else {
      IDValue.removeStyleName(IS_WRONG);
      IDError.setVisible(false);
    }

    // SECRET
    if (StringUtils.isBlank(secretValue.getText())) {
      secretValue.addStyleName(IS_WRONG);
      secretError.setText(messages.mandatoryField());
      secretError.setVisible(true);
      Window.scrollTo(secretValue.getAbsoluteLeft(), secretValue.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.localInstanceConfigurationSecretLabel()));
    } else {
      secretValue.removeStyleName(IS_WRONG);
      secretError.setVisible(false);
    }

    // CENTRAL URL
    if (StringUtils.isBlank(centralInstanceURLValue.getText())) {
      centralInstanceURLValue.addStyleName(IS_WRONG);
      centralInstanceURError.setText(messages.mandatoryField());
      centralInstanceURError.setVisible(true);
      Window.scrollTo(centralInstanceURLValue.getAbsoluteLeft(), centralInstanceURLValue.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.localInstanceConfigurationCentralInstanceURLLabel()));
    } else if (!ValidationUtils.isValidURL(centralInstanceURLValue.getText(), false)) {
      centralInstanceURLValue.addStyleName(IS_WRONG);
      centralInstanceURError.setText(messages.localInstanceConfigurationInvalidURL());
      centralInstanceURError.setVisible(true);
      Window.scrollTo(centralInstanceURLValue.getAbsoluteLeft(), centralInstanceURLValue.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.localInstanceConfigurationCentralInstanceURLLabel()));
    } else {
      centralInstanceURLValue.removeStyleName(IS_WRONG);
      centralInstanceURError.setVisible(false);
    }

    checked = true;

    if (!errorList.isEmpty()) {
      errors.setVisible(true);
      StringBuilder errorString = new StringBuilder();
      for (String error : errorList) {
        errorString.append("<span class='error'>").append(error).append("</span>");
        errorString.append("<br/>");
      }
      errors.setHTML(errorString.toString());
    } else {
      errors.setVisible(false);
    }

    return errorList.isEmpty();
  }

  public void clear() {
    IDValue.setText("");
    secretValue.setText("");
    centralInstanceURLValue.setText("");
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<LocalInstance> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public LocalInstance getValue() {
    return getLocalInstance();
  }

  @UiHandler("buttonTest")
  void buttonTestHandler(ClickEvent e) {
    if (isValid()) {
      Services services = new Services("Test local instance configuration", "test");
      services.distributedInstanceResource(s -> s.testLocalInstanceConfiguration(getLocalInstance()))
        .whenComplete((result, error) -> {
          if (result != null) {
            if (result.isEmpty()) {
              Dialogs.showInformationDialog(messages.testLocalInstanceConfigurationDialogTitle(),
                messages.testLocalInstanceConfigurationDialogMessage(centralInstanceURLValue.getText()),
                messages.closeButton(), false);
            } else {
              StringBuilder errorMessage = new StringBuilder();
              result.forEach(i -> {
                errorMessage.append("\n");
                errorMessage.append(i);
              });
              Dialogs.showInformationDialog(
                messages.testLocalInstanceConfigurationDialogTitle(), messages
                  .testLocalInstanceConfigurationDialogMessageError(centralInstanceURLValue.getText() + errorMessage),
                messages.closeButton(), false);
            }
          }
        });
    }
  }
}
