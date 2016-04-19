/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.search.SearchSuggestBox;
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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.RiskMessages;
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

  private static RiskMessages messages = GWT.create(RiskMessages.class);

  @UiField
  TextBox name;

  @UiField
  TextArea description;

  @UiField
  DateBox identifiedOn;

  @UiField
  TextBox identifiedBy;

  @UiField(provided = true)
  SearchSuggestBox<Risk> category;

  @UiField
  TextArea notes;

  @UiField
  ListBox preMitigationProbability;

  @UiField
  ListBox preMitigationImpact;

  @UiField
  Label preMitigationSeverityKey;

  @UiField
  HTML preMitigationSeverityValue;

  @UiField
  TextArea preMitigationNotes;

  @UiField
  ListBox posMitigationProbability;

  @UiField
  ListBox posMitigationImpact;

  @UiField
  Label posMitigationSeverityKey;

  @UiField
  HTML posMitigationSeverityValue;

  @UiField
  TextArea posMitigationNotes;

  @UiField
  TextArea mitigationStrategy;

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

  private int severityLowLimit;
  private int severityHighLimit;

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
  public RiskDataPanel(final boolean editmode, final Risk risk) {
    this(true, editmode, risk);
  }

  /**
   * Create a new user data panel
   *
   * @param visible
   * @param editmode
   * @param enableGroupSelect
   * @param enablePermissions
   */

  public RiskDataPanel(final boolean visible, final boolean editmode, final Risk risk) {
    category = new SearchSuggestBox<Risk>(Risk.class, "category");
    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrieveAllMitigationProperties(new AsyncCallback<List<List<String>>>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(List<List<String>> terms) {
        init(visible, editmode, terms, risk);
      }
    });
  }

  public void init(boolean visible, boolean editmode, List<List<String>> mitigationProperties, Risk risk) {

    severityLowLimit = Integer.parseInt(mitigationProperties.get(0).get(0));
    severityHighLimit = Integer.parseInt(mitigationProperties.get(0).get(1));

    // add probability items to list box
    for (int i = 0; i < 6; i++) {
      posMitigationProbability.addItem(mitigationProperties.get(1).get(i));
      if (i != 0) {
        preMitigationProbability.addItem(mitigationProperties.get(1).get(i));
      }
    }

    // add impact items to list box
    for (int i = 0; i < 6; i++) {
      posMitigationImpact.addItem(mitigationProperties.get(2).get(i));
      if (i != 0) {
        preMitigationImpact.addItem(mitigationProperties.get(2).get(i));
      }
    }

    preMitigationProbability.setSelectedIndex(0);
    preMitigationImpact.setSelectedIndex(0);
    posMitigationProbability.setSelectedIndex(0);
    posMitigationImpact.setSelectedIndex(0);

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

    ChangeHandler changePreMitigationHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {

        int probability = preMitigationProbability.getSelectedIndex() + 1;
        int impact = preMitigationImpact.getSelectedIndex() + 1;

        preMitigationSeverityKey.setVisible(true);
        preMitigationSeverityValue.setVisible(true);
        int severity = probability * impact;
        preMitigationSeverityValue.setHTML(getSeverityDefinition(severity, severityLowLimit, severityHighLimit));

        RiskDataPanel.this.onChange();
      }
    };

    ChangeHandler changePosMitigationHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {

        int probability = posMitigationProbability.getSelectedIndex();
        int impact = posMitigationImpact.getSelectedIndex();

        posMitigationSeverityKey.setVisible(true);
        posMitigationSeverityValue.setVisible(true);
        int severity = probability * impact;
        posMitigationSeverityValue.setHTML(getSeverityDefinition(severity, severityLowLimit, severityHighLimit));

        RiskDataPanel.this.onChange();
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
    notes.addChangeHandler(changeHandler);

    preMitigationProbability.addChangeHandler(changePreMitigationHandler);
    preMitigationProbability.addKeyUpHandler(keyUpHandler);
    preMitigationImpact.addChangeHandler(changePreMitigationHandler);
    preMitigationImpact.addKeyUpHandler(keyUpHandler);
    preMitigationNotes.addChangeHandler(changeHandler);

    posMitigationProbability.addChangeHandler(changePosMitigationHandler);
    posMitigationProbability.addKeyUpHandler(keyUpHandler);
    posMitigationImpact.addChangeHandler(changePosMitigationHandler);
    posMitigationImpact.addKeyUpHandler(keyUpHandler);
    posMitigationNotes.addChangeHandler(changeHandler);

    mitigationStrategy.addChangeHandler(changeHandler);
    mitigationOwnerType.addChangeHandler(changeHandler);
    mitigationOwner.addChangeHandler(changeHandler);
    mitigationRelatedEventIdentifierType.addChangeHandler(changeHandler);
    mitigationRelatedEventIdentifierValue.addChangeHandler(changeHandler);

    if (!editmode) {
      posMitigationSeverityKey.setVisible(false);
      posMitigationSeverityValue.setVisible(false);
      this.preMitigationSeverityValue.setHTML(getSeverityDefinition(0, severityLowLimit, severityHighLimit));
    } else {
      setRisk(risk);
    }

    // FIXME it must be visible later
    mitigationRelatedEventIdentifierType.setVisible(false);
    mitigationRelatedEventIdentifierValue.setVisible(false);
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

    if (category.getValue().length() == 0) {
      valid = false;
      category.addStyleName("isWrong");
    } else {
      category.removeStyleName("isWrong");
    }

    checked = true;
    return valid;
  }

  public void setRisk(Risk risk) {
    this.name.setText(risk.getName());
    this.description.setText(risk.getDescription());
    this.identifiedOn.setValue(risk.getIdentifiedOn());
    this.identifiedBy.setText(risk.getIdentifiedBy());
    this.category.setValue(risk.getCategory());
    this.notes.setText(risk.getNotes());

    int preProbability = Math.abs(risk.getPreMitigationProbability() - 1);
    int preImpact = Math.abs(risk.getPreMitigationImpact() - 1);

    this.preMitigationProbability.setSelectedIndex(preProbability);
    this.preMitigationImpact.setSelectedIndex(preImpact);
    this.preMitigationNotes.setText(risk.getPreMitigationNotes());

    this.preMitigationSeverityKey.setVisible(true);
    this.preMitigationSeverityValue.setVisible(true);
    int preSeverity = risk.getPreMitigationSeverity();
    this.preMitigationSeverityValue.setHTML(getSeverityDefinition(preSeverity, severityLowLimit, severityHighLimit));

    this.posMitigationProbability.setSelectedIndex(risk.getPosMitigationProbability());
    this.posMitigationImpact.setSelectedIndex(risk.getPosMitigationImpact());
    this.posMitigationNotes.setText(risk.getPosMitigationNotes());

    int probability = posMitigationProbability.getSelectedIndex();
    int impact = posMitigationImpact.getSelectedIndex();

    if (isElementValid(probability) || isElementValid(impact)) {
      this.posMitigationSeverityKey.setVisible(true);
      this.posMitigationSeverityValue.setVisible(true);
      int posSeverity = risk.getPosMitigationSeverity();
      this.posMitigationSeverityValue.setHTML(getSeverityDefinition(posSeverity, severityLowLimit, severityHighLimit));
    } else {
      this.posMitigationSeverityKey.setVisible(false);
      this.posMitigationSeverityValue.setVisible(false);
    }

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
    risk.setCategory(category.getValue());
    risk.setNotes(notes.getText());

    int preProbability = preMitigationProbability.getSelectedIndex() + 1;
    int preImpact = preMitigationImpact.getSelectedIndex() + 1;

    int preSeverity = preProbability * preImpact;
    risk.setPreMitigationProbability(preProbability);
    risk.setPreMitigationImpact(preImpact);
    risk.setPreMitigationSeverity(preSeverity);
    risk.setPreMitigationSeverityLevel(getSeverityLevel(preSeverity, severityLowLimit, severityHighLimit));
    risk.setPreMitigationNotes(preMitigationNotes.getText());

    int posProbability = posMitigationProbability.getSelectedIndex();
    int posImpact = posMitigationImpact.getSelectedIndex();

    risk.setPosMitigationProbability(posProbability);
    risk.setPosMitigationImpact(posImpact);
    if (posProbability == 0 && posImpact == 0) {
      risk.setPosMitigationSeverity(preSeverity);
      risk.setPosMitigationSeverityLevel(getSeverityLevel(preSeverity, severityLowLimit, severityHighLimit));
    } else {
      int posSeverity = posProbability * posImpact;
      risk.setPosMitigationSeverity(posSeverity);
      risk.setPosMitigationSeverityLevel(getSeverityLevel(posSeverity, severityLowLimit, severityHighLimit));
    }
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
    category.setValue("");
    notes.setText("");

    preMitigationProbability.setSelectedIndex(0);
    preMitigationImpact.setSelectedIndex(0);
    preMitigationNotes.setText("");

    posMitigationProbability.setSelectedIndex(0);
    posMitigationImpact.setSelectedIndex(0);
    posMitigationNotes.setText("");

    mitigationStrategy.setText("");
    mitigationOwnerType.setText("");
    mitigationOwner.setText("");
    mitigationRelatedEventIdentifierType.setText("");
    mitigationRelatedEventIdentifierValue.setText("");
  }

  private boolean isElementValid(Integer value) {
    return value != null && value > 0 && value < 6;
  }

  private String getSeverityLevel(int severity, int lowLimit, int highLimit) {
    if (severity < lowLimit) {
      return messages.showLowSeverity();
    } else if (severity < highLimit) {
      return messages.showModerateSeverity();
    } else {
      return messages.showHighSeverity();
    }
  }

  private SafeHtml getSeverityDefinition(int severity, int lowLimit, int highLimit) {
    if (severity < lowLimit) {
      return SafeHtmlUtils.fromSafeConstant("<span class='label-success'>" + messages.showLowSeverity() + "</span>");
    } else if (severity < highLimit) {
      return SafeHtmlUtils
        .fromSafeConstant("<span class='label-warning'>" + messages.showModerateSeverity() + "</span>");
    } else {
      return SafeHtmlUtils.fromSafeConstant("<span class='label-danger'>" + messages.showHighSeverity() + "</span>");
    }
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
