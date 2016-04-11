/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import java.util.Date;

import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 *
 */
public class RiskDataPanel extends Composite implements HasValueChangeHandlers<Risk> {

  interface MyUiBinder extends UiBinder<Widget, RiskDataPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  @UiField
  TextBox name;

  @UiField
  TextArea description;

  @UiField
  DateBox identifiedOn;

  @UiField
  TextBox identifiedBy;

  @UiField
  TextBox category;

  @UiField
  TextArea notes;

  @UiField
  IntegerBox preMitigationProbability;

  @UiField
  IntegerBox preMitigationImpact;

  @UiField
  IntegerBox preMitigationSeverity;

  @UiField
  TextArea preMitigationNotes;

  @UiField
  IntegerBox posMitigationProbability;

  @UiField
  IntegerBox posMitigationImpact;

  @UiField
  IntegerBox posMitigationSeverity;

  @UiField
  TextArea posMitigationNotes;

  @UiField
  TextBox mitigationStrategy;

  @UiField
  TextBox mitigationOwnerType;

  @UiField
  TextBox mitigationOwner;

  @UiField
  TextBox mitigationRelatedEventIdentifierType;

  @UiField
  TextBox mitigationRelatedEventIdentifierValue;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean editmode;

  private boolean changed = false;
  private boolean checked = false;

  /**
   * Create a new user data panel
   *
   * @param editmode
   *          if user name should be editable
   * @param enableGroupSelect
   *          if the list of groups to which the user belong to should be
   *          editable
   *
   */
  public RiskDataPanel(boolean editmode) {
    this(true, editmode);
  }

  /**
   * Create a new user data panel
   *
   * @param visible
   * @param editmode
   * @param enableGroupSelect
   * @param enablePermissions
   */
  public RiskDataPanel(boolean visible, boolean editmode) {
    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    super.setVisible(visible);

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    identifiedOn.setFormat(dateFormat);
    identifiedOn.getDatePicker().setYearArrowsVisible(true);
    identifiedOn.setFireNullValues(true);

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        RiskDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    name.addChangeHandler(changeHandler);
    name.addKeyUpHandler(keyUpHandler);
    description.addChangeHandler(changeHandler);
    description.addKeyUpHandler(keyUpHandler);
    identifiedOn.addValueChangeHandler(new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        onChange();
      }
    });

    identifiedBy.addChangeHandler(changeHandler);
    identifiedBy.addKeyUpHandler(keyUpHandler);
    category.addChangeHandler(changeHandler);
    category.addKeyUpHandler(keyUpHandler);
    notes.addChangeHandler(changeHandler);

    preMitigationProbability.addChangeHandler(changeHandler);
    preMitigationProbability.addKeyUpHandler(keyUpHandler);
    preMitigationImpact.addChangeHandler(changeHandler);
    preMitigationImpact.addKeyUpHandler(keyUpHandler);
    preMitigationSeverity.addChangeHandler(changeHandler);
    preMitigationSeverity.addKeyUpHandler(keyUpHandler);
    preMitigationNotes.addChangeHandler(changeHandler);

    posMitigationProbability.addChangeHandler(changeHandler);
    posMitigationProbability.addKeyUpHandler(keyUpHandler);
    posMitigationImpact.addChangeHandler(changeHandler);
    posMitigationImpact.addKeyUpHandler(keyUpHandler);
    posMitigationSeverity.addChangeHandler(changeHandler);
    posMitigationSeverity.addKeyUpHandler(keyUpHandler);
    posMitigationNotes.addChangeHandler(changeHandler);

    mitigationStrategy.addChangeHandler(changeHandler);
    mitigationOwnerType.addChangeHandler(changeHandler);
    mitigationOwner.addChangeHandler(changeHandler);
    mitigationRelatedEventIdentifierType.addChangeHandler(changeHandler);
    mitigationRelatedEventIdentifierValue.addChangeHandler(changeHandler);
  }

  public boolean isValid() {
    boolean valid = true;

    if (name.getText().length() == 0) {
      valid = false;
      name.addStyleName("isWrong");
    } else {
      name.removeStyleName("isWrong");
    }

    if (identifiedOn.getValue() == null || identifiedOn.getValue().after(new Date())) {
      valid = false;
      identifiedOn.addStyleName("isWrong");
    } else {
      identifiedOn.removeStyleName("isWrong");
    }

    if (identifiedBy.getText().length() == 0) {
      valid = false;
      identifiedBy.addStyleName("isWrong");
    } else {
      identifiedBy.removeStyleName("isWrong");
    }

    if (category.getText().length() == 0) {
      valid = false;
      category.addStyleName("isWrong");
    } else {
      category.removeStyleName("isWrong");
    }

    if (preMitigationProbability.getText().length() == 0) {
      valid = false;
      preMitigationProbability.addStyleName("isWrong");
    } else {
      preMitigationProbability.removeStyleName("isWrong");
    }

    if (preMitigationImpact.getText().length() == 0) {
      valid = false;
      preMitigationImpact.addStyleName("isWrong");
    } else {
      preMitigationImpact.removeStyleName("isWrong");
    }

    if (preMitigationSeverity.getText().length() == 0) {
      valid = false;
      preMitigationSeverity.addStyleName("isWrong");
    } else {
      preMitigationSeverity.removeStyleName("isWrong");
    }

    try {
      Integer.parseInt(preMitigationProbability.getText());
      preMitigationProbability.removeStyleName("isWrong");
    } catch (NumberFormatException e) {
      valid = false;
      preMitigationProbability.addStyleName("isWrong");
    }

    try {
      Integer.parseInt(preMitigationImpact.getText());
      preMitigationImpact.removeStyleName("isWrong");
    } catch (NumberFormatException e) {
      valid = false;
      preMitigationImpact.addStyleName("isWrong");
    }

    try {
      Integer.parseInt(preMitigationSeverity.getText());
      preMitigationSeverity.removeStyleName("isWrong");
    } catch (NumberFormatException e) {
      valid = false;
      preMitigationSeverity.addStyleName("isWrong");
    }

    try {
      Integer.parseInt(posMitigationProbability.getText());
      posMitigationProbability.removeStyleName("isWrong");
    } catch (NumberFormatException e) {
      valid = false;
      posMitigationProbability.addStyleName("isWrong");
    }

    try {
      Integer.parseInt(posMitigationImpact.getText());
      posMitigationImpact.removeStyleName("isWrong");
    } catch (NumberFormatException e) {
      valid = false;
      posMitigationImpact.addStyleName("isWrong");
    }

    try {
      Integer.parseInt(posMitigationSeverity.getText());
      posMitigationSeverity.removeStyleName("isWrong");
    } catch (NumberFormatException e) {
      valid = false;
      posMitigationSeverity.addStyleName("isWrong");
    }

    checked = true;
    return valid;
  }

  public void setRisk(Risk risk) {
    this.name.setText(risk.getName());
    this.description.setText(risk.getDescription());
    this.identifiedOn.setValue(risk.getIdentifiedOn());
    this.identifiedBy.setText(risk.getIdentifiedBy());
    this.category.setText(risk.getCategory());
    this.notes.setText(risk.getNotes());

    this.preMitigationProbability.setValue(risk.getPreMitigationProbability());
    this.preMitigationImpact.setValue(risk.getPreMitigationImpact());
    this.preMitigationSeverity.setValue(risk.getPreMitigationSeverity());
    this.preMitigationNotes.setText(risk.getPreMitigationNotes());

    this.posMitigationProbability.setValue(risk.getPosMitigationProbability());
    this.posMitigationImpact.setValue(risk.getPosMitigationImpact());
    this.posMitigationSeverity.setValue(risk.getPosMitigationSeverity());
    this.posMitigationNotes.setText(risk.getPosMitigationNotes());

    this.mitigationStrategy.setText(risk.getMitigationStrategy());
    this.mitigationOwnerType.setText(risk.getMitigationOwnerType());
    this.mitigationOwner.setText(risk.getMitigationOwner());
    this.mitigationRelatedEventIdentifierType.setText(risk.getMitigationRelatedEventIdentifierType());
    this.mitigationRelatedEventIdentifierValue.setText(risk.getMitigationRelatedEventIdentifierValue());
  }

  public Risk getRisk() {

    Risk risk = new Risk();
    risk.setName(name.getText());
    risk.setDescription(description.getText());
    risk.setIdentifiedOn(identifiedOn.getValue());
    risk.setIdentifiedBy(identifiedBy.getText());
    risk.setCategory(category.getText());
    risk.setNotes(notes.getText());

    risk.setPreMitigationProbability(preMitigationProbability.getValue());
    risk.setPreMitigationImpact(preMitigationImpact.getValue());
    risk.setPreMitigationSeverity(preMitigationSeverity.getValue());
    risk.setPreMitigationNotes(preMitigationNotes.getText());

    risk.setPosMitigationProbability(posMitigationProbability.getValue());
    risk.setPosMitigationImpact(posMitigationImpact.getValue());
    risk.setPosMitigationSeverity(posMitigationSeverity.getValue());
    risk.setPosMitigationNotes(posMitigationNotes.getText());

    risk.setMitigationStrategy(mitigationStrategy.getText());
    risk.setMitigationOwnerType(mitigationOwnerType.getText());
    risk.setMitigationOwner(mitigationOwner.getText());
    risk.setMitigationRelatedEventIdentifierType(mitigationRelatedEventIdentifierType.getText());
    risk.setMitigationRelatedEventIdentifierValue(mitigationRelatedEventIdentifierValue.getText());

    return risk;
  }

  public void clear() {
    name.setText("");
    description.setText("");
    identifiedBy.setText("");
    category.setText("");
    notes.setText("");

    preMitigationProbability.setValue(null);
    preMitigationImpact.setValue(null);
    preMitigationSeverity.setValue(null);
    preMitigationNotes.setText("");

    posMitigationProbability.setValue(null);
    posMitigationImpact.setValue(null);
    posMitigationSeverity.setValue(null);
    posMitigationNotes.setText("");

    mitigationStrategy.setText("");
    mitigationOwnerType.setText("");
    mitigationOwner.setText("");
    mitigationRelatedEventIdentifierType.setText("");
    mitigationRelatedEventIdentifierValue.setText("");
  }

  /**
   * Is user data panel editable, i.e. on create user mode
   *
   * @return true if editable
   */
  public boolean isEditmode() {
    return editmode;
  }

  /**
   * Is user data panel has been changed
   *
   * @return changed
   */
  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Risk> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public Risk getValue() {
    return getRisk();
  }
}
