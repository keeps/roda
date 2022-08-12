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

import com.google.gwt.user.client.ui.TextArea;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
public class DistributedInstanceDataPanel extends Composite implements HasValueChangeHandlers<DistributedInstance> {
  public static final String IS_WRONG = "isWrong";

  interface MyUiBinder extends UiBinder<Widget, DistributedInstanceDataPanel> {
  }

  private static DistributedInstanceDataPanel.MyUiBinder uiBinder = GWT.create(DistributedInstanceDataPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox name;

  @UiField
  Label nameError;

//  @UiField
//  TextBox identifier;
//
//  @UiField
//  Label identifierError;

  @UiField
  TextArea description;

  @UiField
  HTML errors;

  private final boolean editMode;

  private boolean changed = false;
  private boolean checked = false;

  public DistributedInstanceDataPanel(DistributedInstance distributedInstance, boolean editMode) {
    initWidget(uiBinder.createAndBindUi(this));

    this.editMode = editMode;

    setInitialState();
    initHandlers();

    if (editMode) {
      setDistributedInstance(distributedInstance);
    }
  }

  private void initHandlers() {
    ChangeHandler changeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        DistributedInstanceDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        DistributedInstanceDataPanel.this.onChange();
      }
    };

    name.addChangeHandler(changeHandler);
    name.addKeyUpHandler(keyUpHandler);

//    identifier.addChangeHandler(changeHandler);
//    identifier.addKeyUpHandler(keyUpHandler);

    description.addChangeHandler(changeHandler);
    description.addKeyUpHandler(keyUpHandler);
  }

  private void setInitialState() {
    errors.setVisible(false);
  }

  public void setDistributedInstance(DistributedInstance distributedInstance) {
    this.name.setText(distributedInstance.getName());
    //this.identifier.setText(distributedInstance.getNameIdentifier());
    this.description.setText(distributedInstance.getDescription());
  }

  public DistributedInstance getDistributedInstance() {
    DistributedInstance distributedInstance = new DistributedInstance();
    distributedInstance.setName(name.getText());
    //distributedInstance.setNameIdentifier(identifier.getText());
    distributedInstance.setDescription(description.getText());

    return distributedInstance;
  }

  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    //name
    if (StringUtils.isBlank(name.getText())) {
      name.addStyleName(IS_WRONG);
      nameError.setText(messages.mandatoryField());
      nameError.setVisible(true);
      Window.scrollTo(name.getAbsoluteLeft(), name.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.distributedInstanceNameLabel()));
    } else {
      name.removeStyleName(IS_WRONG);
      nameError.setVisible(false);
    }

    //Identifier
//    if (StringUtils.isBlank(identifier.getText())) {
//      identifier.addStyleName(IS_WRONG);
//      identifierError.setText(messages.mandatoryField());
//      identifierError.setVisible(true);
//      Window.scrollTo(identifier.getAbsoluteLeft(), identifier.getAbsoluteTop());
//      errorList.add(messages.isAMandatoryField(messages.distributedInstanceIDLabel()));
//    } else if (identifier.getText().length() != 3) {
//      identifier.addStyleName(IS_WRONG);
//      identifierError.setText(messages.mandatoryField());
//      identifierError.setVisible(true);
//      Window.scrollTo(identifier.getAbsoluteLeft(), identifier.getAbsoluteTop());
//      errorList.add(messages.isAMandatoryField(messages.distributedInstanceIDLabel()));
//    } else {
//      identifier.removeStyleName(IS_WRONG);
//      identifierError.setVisible(false);
//    }

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
    name.setText("");
    //identifier.setText("");
    description.setText("");
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DistributedInstance> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public DistributedInstance getValue() {
    return getDistributedInstance();
  }
}
