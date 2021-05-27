package org.roda.wui.client.management;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.TextArea;
import org.roda.core.data.v2.institution.Institution;
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
public class InstitutionDataPanel extends Composite implements HasValueChangeHandlers<Institution> {
  public static final String IS_WRONG = "isWrong";

  interface MyUiBinder extends UiBinder<Widget, InstitutionDataPanel> {
  }

  private static InstitutionDataPanel.MyUiBinder uiBinder = GWT.create(InstitutionDataPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox name;

  @UiField
  Label nameError;

  @UiField
  TextBox identifier;

  @UiField
  Label identifierError;

  @UiField
  TextArea description;

  @UiField
  HTML errors;

  private final boolean editMode;

  private boolean changed = false;
  private boolean checked = false;

  public InstitutionDataPanel(Institution institution, boolean editMode) {
    initWidget(uiBinder.createAndBindUi(this));

    this.editMode = editMode;

    setInitialState();
    initHandlers();

    if (editMode) {
      setInstitution(institution);
    }
  }

  private void initHandlers() {
    ChangeHandler changeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        InstitutionDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        InstitutionDataPanel.this.onChange();
      }
    };

    name.addChangeHandler(changeHandler);
    name.addKeyUpHandler(keyUpHandler);

    identifier.addChangeHandler(changeHandler);
    identifier.addKeyUpHandler(keyUpHandler);

    description.addChangeHandler(changeHandler);
    description.addKeyUpHandler(keyUpHandler);
  }

  private void setInitialState() {
    errors.setVisible(false);
  }

  public void setInstitution(Institution institution) {
    this.name.setText(institution.getName());
    this.identifier.setText(institution.getNameIdentifier());
    this.description.setText(institution.getDescription());
  }

  public Institution getInstitution() {
    Institution institution = new Institution();
    institution.setName(name.getText());
    institution.setNameIdentifier(identifier.getText());
    institution.setDescription(description.getText());

    return institution;
  }

  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    //name
    if (StringUtils.isBlank(name.getText())) {
      name.addStyleName(IS_WRONG);
      nameError.setText(messages.mandatoryField());
      nameError.setVisible(true);
      Window.scrollTo(name.getAbsoluteLeft(), name.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.institutionNameLabel()));
    } else {
      name.removeStyleName(IS_WRONG);
      nameError.setVisible(false);
    }

    //Identifier
    if (StringUtils.isBlank(identifier.getText())) {
      identifier.addStyleName(IS_WRONG);
      identifierError.setText(messages.mandatoryField());
      identifierError.setVisible(true);
      Window.scrollTo(identifier.getAbsoluteLeft(), identifier.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.institutionIDLabel()));
    } else if (identifier.getText().length() != 3) {
      identifier.addStyleName(IS_WRONG);
      identifierError.setText(messages.mandatoryField());
      identifierError.setVisible(true);
      Window.scrollTo(identifier.getAbsoluteLeft(), identifier.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.institutionIDLabel()));
    } else {
      identifier.removeStyleName(IS_WRONG);
      identifierError.setVisible(false);
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
    name.setText("");
    identifier.setText("");
    description.setText("");
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Institution> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public Institution getValue() {
    return getInstitution();
  }
}
