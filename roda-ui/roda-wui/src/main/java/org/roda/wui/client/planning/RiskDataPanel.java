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

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.lists.RiskIncidenceList;
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
import com.google.gwt.user.client.ui.FlowPanel;
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
  Label id;

  @UiField
  TextBox idBox;

  @UiField
  TextBox name;

  @UiField
  TextArea description;

  @UiField
  DateBox identifiedOn;

  @UiField(provided = true)
  SearchSuggestBox<Risk> identifiedBy;

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
  Label mitigationOwnerTypeKey;

  @UiField
  TextBox mitigationOwnerType;

  @UiField(provided = true)
  SearchSuggestBox<Risk> mitigationOwner;

  @UiField
  TextBox mitigationRelatedEventIdentifierType;

  @UiField
  TextBox mitigationRelatedEventIdentifierValue;

  @UiField
  FlowPanel incidenceListPanel;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean editmode;

  private boolean changed = false;
  private boolean checked = false;

  private int severityLowLimit;
  private int severityHighLimit;
  private int probabilitiesSize;
  private int impactsSize;
  private int riskCounter;
  private RiskIncidenceList incidenceList;

  /**
   * Create a new user data panel
   *
   * @param editmode
   *          if user name should be editable
   * @param risk
   *          the risk to use
   *
   */
  public RiskDataPanel(final boolean editmode, final IndexedRisk risk, final String categoryField,
    final String identifiedByField, final String ownerField) {

    category = new SearchSuggestBox<Risk>(Risk.class, categoryField);
    identifiedBy = new SearchSuggestBox<Risk>(Risk.class, identifiedByField);
    mitigationOwner = new SearchSuggestBox<Risk>(Risk.class, ownerField);

    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrieveAllMitigationProperties(new AsyncCallback<MitigationPropertiesBundle>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(MitigationPropertiesBundle terms) {
        init(editmode, terms, risk);
      }
    });
  }

  public void init(boolean editmode, MitigationPropertiesBundle mitigationProperties, IndexedRisk risk) {
    severityLowLimit = mitigationProperties.getSeverityLowLimit();
    severityHighLimit = mitigationProperties.getSeverityHighLimit();

    List<String> probabilities = mitigationProperties.getProbabilities();
    probabilitiesSize = probabilities.size();
    for (int i = probabilitiesSize - 1; i >= 0; i--) {
      posMitigationProbability.addItem(probabilities.get(i));
      preMitigationProbability.addItem(probabilities.get(i));
    }

    List<String> impacts = mitigationProperties.getImpacts();
    impactsSize = impacts.size();
    for (int i = impactsSize - 1; i >= 0; i--) {
      posMitigationImpact.addItem(impacts.get(i));
      preMitigationImpact.addItem(impacts.get(i));
    }

    preMitigationProbability.setSelectedIndex(probabilitiesSize - 1);
    preMitigationImpact.setSelectedIndex(impactsSize - 1);
    posMitigationProbability.setSelectedIndex(probabilitiesSize - 1);
    posMitigationImpact.setSelectedIndex(impactsSize - 1);

    this.editmode = editmode;
    super.setVisible(true);

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    identifiedOn.setFormat(dateFormat);
    identifiedOn.getDatePicker().setYearArrowsVisible(true);
    identifiedOn.setFireNullValues(true);
    identifiedOn.setValue(new Date());

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

        int probability = getIndex(preMitigationProbability.getSelectedIndex(), probabilitiesSize);
        int impact = getIndex(preMitigationImpact.getSelectedIndex(), impactsSize);

        preMitigationSeverityKey.setVisible(true);
        preMitigationSeverityValue.setVisible(true);
        int severity = probability * impact;
        preMitigationSeverityValue.setHTML(getSeverityDefinition(severity, severityLowLimit, severityHighLimit));

        // if (posMitigationProbability.getSelectedIndex() == probabilitiesSize
        // - 1
        // && preMitigationProbability.getSelectedIndex() != probabilitiesSize -
        // 1) {
        // posMitigationProbability.setSelectedIndex(probability - 1);
        // }
        //
        // if (posMitigationImpact.getSelectedIndex() == impactsSize - 1
        // && preMitigationImpact.getSelectedIndex() != impactsSize - 1) {
        // posMitigationImpact.setSelectedIndex(probability - 1);
        // }

        RiskDataPanel.this.onChange();
      }
    };

    ChangeHandler changePosMitigationHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {

        int probability = getIndex(posMitigationProbability.getSelectedIndex(), probabilitiesSize);
        int impact = getIndex(posMitigationImpact.getSelectedIndex(), impactsSize);

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
    description.setVisibleLines(6);
    identifiedOn.addValueChangeHandler(new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        onChange();
      }
    });

    ValueChangeHandler<String> valueChangeHandler = new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        RiskDataPanel.this.onChange();
      }
    };

    category.addValueChangeHandler(valueChangeHandler);
    identifiedBy.addValueChangeHandler(valueChangeHandler);
    notes.addChangeHandler(changeHandler);
    notes.setVisibleLines(6);

    preMitigationProbability.addChangeHandler(changePreMitigationHandler);
    preMitigationProbability.addKeyUpHandler(keyUpHandler);
    preMitigationImpact.addChangeHandler(changePreMitigationHandler);
    preMitigationImpact.addKeyUpHandler(keyUpHandler);
    preMitigationNotes.addChangeHandler(changeHandler);
    preMitigationNotes.setVisibleLines(6);

    posMitigationProbability.addChangeHandler(changePosMitigationHandler);
    posMitigationProbability.addKeyUpHandler(keyUpHandler);
    posMitigationImpact.addChangeHandler(changePosMitigationHandler);
    posMitigationImpact.addKeyUpHandler(keyUpHandler);
    posMitigationNotes.addChangeHandler(changeHandler);
    posMitigationNotes.setVisibleLines(6);

    mitigationStrategy.addChangeHandler(changeHandler);
    mitigationStrategy.setVisibleLines(6);
    mitigationOwnerType.addChangeHandler(changeHandler);
    mitigationOwner.addValueChangeHandler(valueChangeHandler);
    mitigationRelatedEventIdentifierType.addChangeHandler(changeHandler);
    mitigationRelatedEventIdentifierValue.addChangeHandler(changeHandler);

    if (!editmode) {
      posMitigationSeverityKey.setVisible(false);
      posMitigationSeverityValue.setVisible(false);
      preMitigationSeverityValue.setHTML(getSeverityDefinition(0, severityLowLimit, severityHighLimit));
      this.id.setVisible(false);
    } else {
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISKS, risk.getId()));
      incidenceList = new RiskIncidenceList(filter, null, "Incidences", true);
      incidenceListPanel.add(incidenceList);
      this.idBox.setVisible(false);
      setRisk(risk);
    }

    // FIXME it must be visible later
    mitigationOwnerTypeKey.setVisible(false);
    mitigationOwnerType.setVisible(false);
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

    if (identifiedBy.getValue().length() == 0) {
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

  public void setRisk(IndexedRisk risk) {
    this.id.setText(risk.getId());
    this.name.setText(risk.getName());
    this.description.setText(risk.getDescription());
    this.identifiedOn.setValue(risk.getIdentifiedOn());
    this.identifiedBy.setValue(risk.getIdentifiedBy());
    this.category.setValue(risk.getCategory());
    this.notes.setText(risk.getNotes());

    int preProbability = getIndex(risk.getPreMitigationProbability(), probabilitiesSize);
    int preImpact = getIndex(risk.getPreMitigationImpact(), impactsSize);

    this.preMitigationProbability.setSelectedIndex(preProbability);
    this.preMitigationImpact.setSelectedIndex(preImpact);
    this.preMitigationNotes.setText(risk.getPreMitigationNotes());

    this.preMitigationSeverityKey.setVisible(true);
    this.preMitigationSeverityValue.setVisible(true);
    int preSeverity = risk.getPreMitigationSeverity();
    this.preMitigationSeverityValue.setHTML(getSeverityDefinition(preSeverity, severityLowLimit, severityHighLimit));

    int probability = getIndex(risk.getPosMitigationProbability(), probabilitiesSize);
    int impact = getIndex(risk.getPosMitigationImpact(), impactsSize);

    this.posMitigationProbability.setSelectedIndex(probability);
    this.posMitigationImpact.setSelectedIndex(impact);
    this.posMitigationNotes.setText(risk.getPosMitigationNotes());

    if (probability != 0 || impact != 0) {
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
    this.mitigationOwner.setValue(risk.getMitigationOwner());
    this.mitigationRelatedEventIdentifierType.setText(risk.getMitigationRelatedEventIdentifierType());
    this.mitigationRelatedEventIdentifierValue.setText(risk.getMitigationRelatedEventIdentifierValue());

    this.riskCounter = risk.getObjectsSize();
  }

  public IndexedRisk getRisk() {
    IndexedRisk risk = new IndexedRisk();
    if (idBox.isVisible() && idBox.getText() != null && !idBox.getText().equals("")) {
      risk.setId(idBox.getText());
    }
    risk.setName(name.getText());
    risk.setDescription(description.getText());
    risk.setIdentifiedOn(identifiedOn.getValue());
    risk.setIdentifiedBy(identifiedBy.getValue());
    risk.setCategory(category.getValue());
    risk.setNotes(notes.getText());

    int preProbability = getIndex(preMitigationProbability.getSelectedIndex(), probabilitiesSize);
    int preImpact = getIndex(preMitigationImpact.getSelectedIndex(), impactsSize);

    int preSeverity = preProbability * preImpact;
    risk.setPreMitigationProbability(preProbability);
    risk.setPreMitigationImpact(preImpact);
    risk.setPreMitigationSeverity(preSeverity);
    risk.setPreMitigationSeverityLevel(getSeverityLevel(preSeverity, severityLowLimit, severityHighLimit));
    risk.setPreMitigationNotes(preMitigationNotes.getText());

    int posProbability = getIndex(posMitigationProbability.getSelectedIndex(), probabilitiesSize);
    int posImpact = getIndex(posMitigationImpact.getSelectedIndex(), impactsSize);

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
    risk.setMitigationOwner(mitigationOwner.getValue());
    risk.setMitigationRelatedEventIdentifierType(mitigationRelatedEventIdentifierType.getText());
    risk.setMitigationRelatedEventIdentifierValue(mitigationRelatedEventIdentifierValue.getText());

    risk.setObjectsSize(this.riskCounter);

    return risk;
  }

  public void clear() {
    name.setText("");
    description.setText("");
    identifiedBy.setValue("");
    category.setValue("");
    notes.setText("");

    preMitigationProbability.setSelectedIndex(probabilitiesSize - 1);
    preMitigationImpact.setSelectedIndex(impactsSize - 1);
    preMitigationNotes.setText("");

    posMitigationProbability.setSelectedIndex(probabilitiesSize - 1);
    posMitigationImpact.setSelectedIndex(impactsSize - 1);
    posMitigationNotes.setText("");

    mitigationStrategy.setText("");
    mitigationOwnerType.setText("");
    mitigationOwner.setValue("");
    mitigationRelatedEventIdentifierType.setText("");
    mitigationRelatedEventIdentifierValue.setText("");
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

  private int getIndex(int mitigationField, int fieldsSize) {
    return fieldsSize - mitigationField - 1;
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

  public SelectedItems<RiskIncidence> getSelectedIncidences() {
    return incidenceList.getSelected();
  }
}
