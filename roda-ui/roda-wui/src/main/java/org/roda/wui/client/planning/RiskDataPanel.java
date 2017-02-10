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

import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.search.SearchSuggestBox;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
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

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskDataPanel extends Composite implements HasValueChangeHandlers<Risk> {

  interface MyUiBinder extends UiBinder<Widget, RiskDataPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = GWT.create(ClientMessages.class);

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
  SearchSuggestBox<IndexedRisk> identifiedBy;

  @UiField(provided = true)
  SearchSuggestBox<IndexedRisk> category;

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
  SearchSuggestBox<IndexedRisk> mitigationOwner;

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
  private int probabilitiesSize;
  private int impactsSize;
  private RiskIncidenceList incidenceList;

  private Date createdOn;
  private String createdBy;

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

    category = new SearchSuggestBox<IndexedRisk>(IndexedRisk.class, categoryField, false);
    identifiedBy = new SearchSuggestBox<IndexedRisk>(IndexedRisk.class, identifiedByField, false);
    mitigationOwner = new SearchSuggestBox<IndexedRisk>(IndexedRisk.class, ownerField, false);

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
      posMitigationProbability.addItem(messages.riskMitigationProbability(probabilities.get(i)));
      preMitigationProbability.addItem(messages.riskMitigationProbability(probabilities.get(i)));
    }

    List<String> impacts = mitigationProperties.getImpacts();
    impactsSize = impacts.size();
    for (int i = impactsSize - 1; i >= 0; i--) {
      posMitigationImpact.addItem(messages.riskMitigationImpact(impacts.get(i)));
      preMitigationImpact.addItem(messages.riskMitigationImpact(impacts.get(i)));
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
        preMitigationSeverityValue
          .setHTML(HtmlSnippetUtils.getSeverityDefinition(severity, severityLowLimit, severityHighLimit));

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
        posMitigationSeverityValue
          .setHTML(HtmlSnippetUtils.getSeverityDefinition(severity, severityLowLimit, severityHighLimit));

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
      preMitigationSeverityValue
        .setHTML(HtmlSnippetUtils.getSeverityDefinition(0, severityLowLimit, severityHighLimit));
      this.id.setVisible(false);
    } else {
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
    this.preMitigationSeverityValue
      .setHTML(HtmlSnippetUtils.getSeverityDefinition(preSeverity, severityLowLimit, severityHighLimit));

    int probability = getIndex(risk.getPostMitigationProbability(), probabilitiesSize);
    int impact = getIndex(risk.getPostMitigationImpact(), impactsSize);

    this.posMitigationProbability.setSelectedIndex(probability);
    this.posMitigationImpact.setSelectedIndex(impact);
    this.posMitigationNotes.setText(risk.getPostMitigationNotes());

    if (probability != 0 || impact != 0) {
      this.posMitigationSeverityKey.setVisible(true);
      this.posMitigationSeverityValue.setVisible(true);
      int posSeverity = risk.getPostMitigationSeverity();
      this.posMitigationSeverityValue
        .setHTML(HtmlSnippetUtils.getSeverityDefinition(posSeverity, severityLowLimit, severityHighLimit));
    } else {
      this.posMitigationSeverityKey.setVisible(false);
      this.posMitigationSeverityValue.setVisible(false);
    }

    this.mitigationStrategy.setText(risk.getMitigationStrategy());
    this.mitigationOwnerType.setText(risk.getMitigationOwnerType());
    this.mitigationOwner.setValue(risk.getMitigationOwner());
    this.mitigationRelatedEventIdentifierType.setText(risk.getMitigationRelatedEventIdentifierType());
    this.mitigationRelatedEventIdentifierValue.setText(risk.getMitigationRelatedEventIdentifierValue());

    this.createdOn = risk.getCreatedOn();
    this.createdBy = risk.getCreatedBy();
  }

  public Risk getRisk() {
    Risk risk = new Risk();
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
    risk.setPreMitigationSeverityLevel(
      HtmlSnippetUtils.getSeverityLevel(preSeverity, severityLowLimit, severityHighLimit));
    risk.setPreMitigationNotes(preMitigationNotes.getText());

    int posProbability = getIndex(posMitigationProbability.getSelectedIndex(), probabilitiesSize);
    int posImpact = getIndex(posMitigationImpact.getSelectedIndex(), impactsSize);

    risk.setPostMitigationProbability(posProbability);
    risk.setPostMitigationImpact(posImpact);
    if (posProbability == 0 && posImpact == 0) {
      risk.setPostMitigationSeverity(preSeverity);
      risk.setPostMitigationSeverityLevel(
        HtmlSnippetUtils.getSeverityLevel(preSeverity, severityLowLimit, severityHighLimit));
    } else {
      int posSeverity = posProbability * posImpact;
      risk.setPostMitigationSeverity(posSeverity);
      risk.setPostMitigationSeverityLevel(
        HtmlSnippetUtils.getSeverityLevel(posSeverity, severityLowLimit, severityHighLimit));
    }
    risk.setPostMitigationNotes(posMitigationNotes.getText());

    risk.setMitigationStrategy(mitigationStrategy.getText());
    risk.setMitigationOwnerType(mitigationOwnerType.getText());
    risk.setMitigationOwner(mitigationOwner.getValue());
    risk.setMitigationRelatedEventIdentifierType(mitigationRelatedEventIdentifierType.getText());
    risk.setMitigationRelatedEventIdentifierValue(mitigationRelatedEventIdentifierValue.getText());

    if (editmode) {
      risk.setCreatedOn(createdOn);
      risk.setCreatedBy(createdBy);
    }

    // risk.setObjectsSize(this.riskCounter);
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
