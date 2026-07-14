/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning.risks;

import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskMitigationProperties;
import org.roda.wui.client.common.IncrementalList;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;
import org.roda.wui.client.common.forms.TagInputWidget;
import org.roda.wui.client.common.search.SearchSuggestBox;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.RiskRestService;
import org.roda.wui.client.services.Services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskDataPanel extends Composite implements HasValueChangeHandlers<Risk>, GenericDataPanel<Risk> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final GenericDataForm<Risk> form;
  private final boolean editmode;
  private final Button saveButton;
  private final Button cancelButton;
  private final FlowPanel actionsPanel;
  private DateBox identifiedOn;
  private SearchSuggestBox<IndexedRisk> identifiedBy;
  private IncrementalList categories;
  private SearchSuggestBox<IndexedRisk> mitigationOwner;
  private ListBox preMitigationProbability;
  private ListBox preMitigationImpact;
  private HTML preMitigationSeverityValue;
  private ListBox postMitigationProbability;
  private ListBox postMitigationImpact;
  private HTML postMitigationSeverityValue;
  private FlowPanel postMitigationSeverityRow;
  private int severityLowLimit;
  private int severityHighLimit;
  private Date createdOn;
  private String createdBy;

  public RiskDataPanel() {
    this(null, false);
  }

  public RiskDataPanel(IndexedRisk risk, boolean editMode) {
    this.editmode = editMode;
    this.form = new GenericDataForm<>();

    saveButton = new Button(messages.saveButton());
    saveButton.addStyleName("btn btn-primary btn-play");

    cancelButton = new Button(messages.cancelButton());
    cancelButton.addStyleName("btn btn-link");

    actionsPanel = new FlowPanel();
    actionsPanel.addStyleName("alignButtonsPanel");
    actionsPanel.add(saveButton);
    actionsPanel.add(cancelButton);
    actionsPanel.setVisible(false);

    initWidget(form);

    Services services = new Services("Retrieve risk mitigation properties", "get");
    services.riskResource(RiskRestService::retrieveRiskMitigationProperties).whenComplete((properties, throwable) -> {
      if (throwable == null) {
        if (editMode) {
          initEditMode(risk, properties);
        } else {
          initCreateMode(properties);
        }

      }
    });
  }

  private void initCreateMode(RiskMitigationProperties properties) {
    Risk risk = new Risk();

    initMitigationLimits(properties);
    form.addTextField(messages.riskIdentifier(), Risk::getId, Risk::setId, true);
    addCommonEditableFields(properties);
    form.addCustomWidget(actionsPanel);
    form.setModel(risk);

    updatePreMitigationSeverity();
    updatePostMitigationSeverity();
  }

  private void initEditMode(IndexedRisk indexedRisk, RiskMitigationProperties properties) {
    Risk risk = new Risk(indexedRisk);

    createdOn = indexedRisk.getCreatedOn();
    createdBy = indexedRisk.getCreatedBy();

    initMitigationLimits(properties);
    form.addReadOnlyField(messages.riskIdentifier(), Risk::getId, false);
    addCommonEditableFields(properties);
    form.addCustomWidget(actionsPanel);
    form.setModel(risk);

    updatePreMitigationSeverity();
    updatePostMitigationSeverity();
  }

  private void initMitigationLimits(RiskMitigationProperties properties) {
    severityLowLimit = properties.getSeverityLowLimit();
    severityHighLimit = properties.getSeverityHighLimit();
  }

  private void addCommonEditableFields(RiskMitigationProperties properties) {
    form.addTextField(messages.riskName(), Risk::getName, Risk::setName, true);
    form.addTextArea(messages.riskDescription(), Risk::getDescription, Risk::setDescription, false);
    addIdentifiedOnField();
    addIdentifiedByField();
    addCategoriesField();
    form.addTextArea(messages.riskNotes(), Risk::getNotes, Risk::setNotes, false);
    addPreMitigationSection(properties);
    addMitigationSection();
    addPostMitigationSection(properties);
  }

  private void addPreMitigationSection(RiskMitigationProperties properties) {
    form.addSeparator(messages.riskPreMitigation());

    preMitigationProbability = createMitigationListBox(properties.getProbabilities(), true);
    form.addListBox(messages.riskPreMitigationProbability(), preMitigationProbability,
      r -> String.valueOf(r.getPreMitigationProbability()),
      (r, value) -> r.setPreMitigationProbability(parseInt(value)), true);

    preMitigationImpact = createMitigationListBox(properties.getImpacts(), false);
    form.addListBox(messages.riskPreMitigationImpact(), preMitigationImpact,
      r -> String.valueOf(r.getPreMitigationImpact()), (r, value) -> r.setPreMitigationImpact(parseInt(value)), true);

    preMitigationSeverityValue = new HTML();
    form.addCustomWidget(createSeverityRow(messages.riskPreMitigationSeverity(), preMitigationSeverityValue));

    preMitigationProbability.addChangeHandler(event -> updatePreMitigationSeverity());
    preMitigationImpact.addChangeHandler(event -> updatePreMitigationSeverity());

    form.addTextArea(messages.riskPreMitigationNotes(), Risk::getPreMitigationNotes, Risk::setPreMitigationNotes,
      false);
  }

  private void addPostMitigationSection(RiskMitigationProperties properties) {
    form.addSeparator(messages.riskPostMitigation());

    postMitigationProbability = createMitigationListBox(properties.getProbabilities(), true);
    form.addListBox(messages.riskPostMitigationProbability(), postMitigationProbability,
      r -> String.valueOf(r.getPostMitigationProbability()),
      (r, value) -> r.setPostMitigationProbability(parseInt(value)), true);

    postMitigationImpact = createMitigationListBox(properties.getImpacts(), false);
    form.addListBox(messages.riskPostMitigationImpact(), postMitigationImpact,
      r -> String.valueOf(r.getPostMitigationImpact()), (r, value) -> r.setPostMitigationImpact(parseInt(value)), true);

    postMitigationSeverityValue = new HTML();
    postMitigationSeverityRow = createSeverityRow(messages.riskPostMitigationSeverity(), postMitigationSeverityValue);
    form.addCustomWidget(postMitigationSeverityRow);

    postMitigationProbability.addChangeHandler(event -> updatePostMitigationSeverity());
    postMitigationImpact.addChangeHandler(event -> updatePostMitigationSeverity());

    form.addTextArea(messages.riskPostMitigationNotes(), Risk::getPostMitigationNotes, Risk::setPostMitigationNotes,
      false);
  }

  private void addMitigationSection() {
    form.addSeparator(messages.riskMitigation());

    form.addTextArea(messages.riskMitigationStrategy(), Risk::getMitigationStrategy, Risk::setMitigationStrategy,
      false);

    mitigationOwner = new SearchSuggestBox<>(IndexedRisk.class, RodaConstants.RISK_MITIGATION_OWNER, false);
    form.addSearchSuggestField(messages.riskMitigationOwner(), mitigationOwner, Risk::getMitigationOwner,
      Risk::setMitigationOwner, false);
  }

  private void addIdentifiedOnField() {
    identifiedOn = new DateBox();

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    identifiedOn.setFormat(dateFormat);
    identifiedOn.getDatePicker().setYearArrowsVisible(true);
    identifiedOn.setFireNullValues(true);

    form.addDateField(messages.riskIdentifiedOn(), identifiedOn, Risk::getIdentifiedOn, Risk::setIdentifiedOn, true);
  }

  private void addIdentifiedByField() {
    identifiedBy = new SearchSuggestBox<>(IndexedRisk.class, RodaConstants.RISK_IDENTIFIED_BY, false);

    form.addSearchSuggestField(messages.riskIdentifiedBy(), identifiedBy, Risk::getIdentifiedBy, Risk::setIdentifiedBy,
      true);
  }

  private void addCategoriesField() {
    TagInputWidget tags = new TagInputWidget();

    form.addTagField(messages.riskCategories(), tags, Risk::getCategories, Risk::setCategories, false);
  }

  /**
   * user data panel has been changed
   *
   * @return changed
   */
  public boolean isChanged() {
    return form.isChanged();
  }

  @Override
  public boolean isValid() {
    boolean valid = form.isValid();

    if (identifiedOn.getValue() != null && identifiedOn.getValue().after(new Date())) {
      identifiedOn.addStyleName("isWrong");
      valid = false;
    } else {
      identifiedOn.removeStyleName("isWrong");
    }
    return valid;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Risk> handler) {
    return form.addValueChangeHandler(handler);
  }

  public void setSaveHandler(Runnable onSave) {
    actionsPanel.setVisible(true);
    saveButton.addClickHandler(event -> {
      if (isValid()) {
        onSave.run();
      }
    });
  }

  public void setCancelHandler(Runnable onCancel) {
    actionsPanel.setVisible(true);
    cancelButton.addClickHandler(event -> onCancel.run());
  }

  @Override
  public Risk getValue() {
    Risk risk = form.getValue();

    int preSeverity = risk.getPreMitigationProbability() * risk.getPreMitigationImpact();
    risk.setPreMitigationSeverity(preSeverity);
    risk.setPreMitigationSeverityLevel(
      HtmlSnippetUtils.getSeverityLevel(preSeverity, severityLowLimit, severityHighLimit));

    int postProbability = risk.getPostMitigationProbability();
    int postImpact = risk.getPostMitigationImpact();

    if (postProbability == 0 && postImpact == 0) {
      risk.setPostMitigationSeverity(preSeverity);
      risk.setPostMitigationSeverityLevel(
        HtmlSnippetUtils.getSeverityLevel(preSeverity, severityLowLimit, severityHighLimit));
    } else {
      int postSeverity = postProbability * postImpact;
      risk.setPostMitigationSeverity(postSeverity);
      risk.setPostMitigationSeverityLevel(
        HtmlSnippetUtils.getSeverityLevel(postSeverity, severityLowLimit, severityHighLimit));
    }

    if (editmode) {
      risk.setCreatedOn(createdOn);
      risk.setCreatedBy(createdBy);
    }

    return risk;
  }

  private ListBox createMitigationListBox(List<String> values, boolean probability) {
    ListBox listBox = new ListBox();
    for (int i = values.size() - 1; i >= 0; i--) {
      String label = probability ? messages.riskMitigationProbability(values.get(i))
        : messages.riskMitigationImpact(values.get(i));
      listBox.addItem(label, String.valueOf(i));
    }
    return listBox;
  }

  private FlowPanel createSeverityRow(String labelText, HTML severityValue) {
    FlowPanel row = new FlowPanel();
    row.addStyleName("generic-form-field");

    Label label = new Label(labelText);
    label.addStyleName("form-label");

    FlowPanel input = new FlowPanel();
    input.addStyleName("generic-form-field-input-panel full_width");
    input.add(severityValue);

    FlowPanel left = new FlowPanel();
    left.addStyleName("generic-form-field-left-panel");
    left.add(label);
    left.add(input);

    row.add(left);
    return row;
  }

  private void updatePreMitigationSeverity() {
    int severity = getSelectedInt(preMitigationProbability) * getSelectedInt(preMitigationImpact);
    preMitigationSeverityValue
      .setHTML(HtmlSnippetUtils.getSeverityDefinition(severity, severityLowLimit, severityHighLimit));
  }

  private void updatePostMitigationSeverity() {
    int probability = getSelectedInt(postMitigationProbability);
    int impact = getSelectedInt(postMitigationImpact);
    int severity = probability * impact;

    boolean showPostSeverity = editmode || probability != 0 || impact != 0;
    postMitigationSeverityRow.setVisible(showPostSeverity);

    if (showPostSeverity) {
      postMitigationSeverityValue
        .setHTML(HtmlSnippetUtils.getSeverityDefinition(severity, severityLowLimit, severityHighLimit));
    }
  }

  private int getSelectedInt(ListBox listBox) {
    return parseInt(listBox.getSelectedValue());
  }

  private int parseInt(String value) {
    return value != null && !value.isEmpty() ? Integer.parseInt(value) : 0;
  }
}
