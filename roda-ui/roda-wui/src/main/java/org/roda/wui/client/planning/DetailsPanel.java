/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import org.roda.core.data.v2.ip.IndexedAIP;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class DetailsPanel extends Composite  {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  Label aipID;
  @UiField
  Label aipType;
  @UiField
  Label aipState;
  @UiField
  Label aipCreatedOn;
  @UiField
  Label aipCreatedBy;

  @UiField
  Label modifiedOn;

  @UiField
  Label modifiedBy;

  /*
   * FlowPanel Permissions;
   * 
   * @UiField
   * 
   * Label riskNotesKey, riskNotesValue;
   * 
   * @UiField Label riskPreMitigationKey;
   * 
   * @UiField Label riskPreMitigationProbability;
   * 
   * @UiField Label riskPreMitigationImpact;
   * 
   * @UiField HTML riskPreMitigationSeverity;
   * 
   * @UiField Label riskPreMitigationNotesKey, riskPreMitigationNotesValue;
   * 
   * @UiField Label riskPosMitigationKey;
   * 
   * @UiField Label riskPosMitigationProbabilityKey;
   * 
   * @UiField Label riskPosMitigationProbability;
   * 
   * @UiField Label riskPosMitigationImpactKey;
   * 
   * @UiField Label riskPosMitigationImpact;
   * 
   * @UiField Label riskPosMitigationSeverityKey;
   * 
   * @UiField HTML riskPosMitigationSeverity;
   * 
   * @UiField Label riskPosMitigationNotesKey, riskPosMitigationNotesValue;
   * 
   * @UiField Label riskMitigationKey;
   * 
   * @UiField Label riskMitigationStrategyKey, riskMitigationStrategyValue;
   * 
   * @UiField Label riskMitigationOwnerTypeKey, riskMitigationOwnerTypeValue;
   * 
   * @UiField Label riskMitigationOwnerKey, riskMitigationOwnerValue;
   * 
   * @UiField Label riskMitigationRelatedEventIdentifierTypeKey,
   * riskMitigationRelatedEventIdentifierTypeValue;
   * 
   * @UiField Label riskMitigationRelatedEventIdentifierValueKey,
   * riskMitigationRelatedEventIdentifierValueValue;
   * 
   * @UiField(provided = true) SearchWrapper searchWrapper;
   */
  public DetailsPanel(IndexedAIP aip) {
    initWidget(uiBinder.createAndBindUi(this));
    init(aip);
  }

  public void init(IndexedAIP aip) {
    GWT.log("DetailsPanel init");
    aipID.setText(aip.getId());
    aipType.setText(aip.getType());

    aipState.setText(aip.getState().name());
    aipCreatedOn.setText(String.valueOf(aip.getCreatedOn()));
    aipCreatedBy.setText(aip.getCreatedBy());

    modifiedOn.setText(String.valueOf(aip.getUpdatedOn()));
    modifiedBy.setText(aip.getUpdatedBy());

    /*
     * riskIdentifiedOn.setText(Humanize.formatDate(risk.getIdentifiedOn()));
     * riskIdentifiedBy.setText(risk.getIdentifiedBy());
     * riskNotesValue.setText(risk.getNotes());
     * riskNotesKey.setVisible(StringUtils.isNotBlank(risk.getNotes()));
     * 
     * for (String category : risk.getCategories()) { Label categoryLabel = new
     * Label(category); categoryLabel.addStyleName("value");
     * riskCategories.add(categoryLabel); }
     * 
     * final int preSeverity = risk.getPreMitigationSeverity(); final int
     * posSeverity = risk.getPostMitigationSeverity();
     * 
     * Services services = new Services("Retrieve risk mitigation terms", "get");
     * services.riskResource(s -> s.retrieveRiskMitigationTerms(risk.getUUID()))
     * .whenComplete((riskMitigationTerms, throwable) -> { if (throwable == null) {
     * int severityLowLimit = riskMitigationTerms.getSeverityLowLimit(); int
     * severityHighLimit = riskMitigationTerms.getSeverityHighLimit();
     * 
     * riskPreMitigationProbability.setText(
     * messages.riskMitigationProbability(riskMitigationTerms.
     * getPreMitigationProbability().replace(' ', '_'))); riskPreMitigationImpact
     * .setText(messages.riskMitigationImpact(riskMitigationTerms.
     * getPreMitigationImpact().replace(' ', '_')));
     * 
     * riskPreMitigationSeverity
     * .setHTML(HtmlSnippetUtils.getSeverityDefinition(preSeverity,
     * severityLowLimit, severityHighLimit));
     * 
     * riskPosMitigationProbability.setText(
     * messages.riskMitigationProbability(riskMitigationTerms.
     * getPosMitigationProbability().replace(' ', '_'))); riskPosMitigationImpact
     * .setText(messages.riskMitigationImpact(riskMitigationTerms.
     * getPosMitigationImpact().replace(' ', '_')));
     * 
     * riskPosMitigationKey.setVisible(true);
     * riskPosMitigationProbabilityKey.setVisible(true);
     * riskPosMitigationProbability.setVisible(true);
     * riskPosMitigationImpactKey.setVisible(true);
     * riskPosMitigationImpact.setVisible(true);
     * riskPosMitigationSeverityKey.setVisible(true);
     * riskPosMitigationSeverity.setVisible(true); riskPosMitigationSeverity
     * .setHTML(HtmlSnippetUtils.getSeverityDefinition(posSeverity,
     * severityLowLimit, severityHighLimit)); } });
     * 
     * riskPreMitigationNotesValue.setText(risk.getPreMitigationNotes());
     * riskPreMitigationNotesKey.setVisible(StringUtils.isNotBlank(risk.
     * getPreMitigationNotes()));
     * 
     * riskPosMitigationNotesValue.setText(risk.getPostMitigationNotes());
     * riskPosMitigationNotesKey.setVisible(StringUtils.isNotBlank(risk.
     * getPostMitigationNotes()));
     * 
     * int mitigationCounter = 0;
     * 
     * if (StringUtils.isNotBlank(risk.getMitigationStrategy())) {
     * mitigationCounter++; riskMitigationStrategyKey.setVisible(true);
     * riskMitigationStrategyValue.setText(risk.getMitigationStrategy()); } else {
     * riskMitigationStrategyKey.setVisible(false); }
     * 
     * if (StringUtils.isNotBlank(risk.getMitigationOwnerType())) {
     * mitigationCounter++; riskMitigationOwnerTypeKey.setVisible(true);
     * riskMitigationOwnerTypeValue.setText(risk.getMitigationOwnerType()); } else {
     * riskMitigationOwnerTypeKey.setVisible(false); }
     * 
     * if (StringUtils.isNotBlank(risk.getMitigationOwner())) { mitigationCounter++;
     * riskMitigationOwnerKey.setVisible(true);
     * riskMitigationOwnerValue.setText(risk.getMitigationOwner()); } else {
     * riskMitigationOwnerKey.setVisible(false); }
     * 
     * if (StringUtils.isNotBlank(risk.getMitigationRelatedEventIdentifierType())) {
     * mitigationCounter++;
     * riskMitigationRelatedEventIdentifierTypeKey.setVisible(true);
     * riskMitigationRelatedEventIdentifierTypeValue.setText(risk.
     * getMitigationRelatedEventIdentifierType()); } else {
     * riskMitigationRelatedEventIdentifierTypeKey.setVisible(false); }
     * 
     * if (StringUtils.isNotBlank(risk.getMitigationRelatedEventIdentifierValue()))
     * { mitigationCounter++;
     * riskMitigationRelatedEventIdentifierValueKey.setVisible(true);
     * riskMitigationRelatedEventIdentifierValueValue.setText(risk.
     * getMitigationRelatedEventIdentifierValue()); } else {
     * riskMitigationRelatedEventIdentifierValueKey.setVisible(false); }
     * 
     * riskMitigationKey.setVisible(mitigationCounter != 0);
     * 
     * // FIXME it must be visible later
     * riskMitigationOwnerTypeKey.setVisible(false);
     * riskMitigationOwnerTypeValue.setVisible(false);
     * riskMitigationRelatedEventIdentifierTypeKey.setVisible(false);
     * riskMitigationRelatedEventIdentifierTypeValue.setVisible(false);
     * riskMitigationRelatedEventIdentifierValueKey.setVisible(false);
     * riskMitigationRelatedEventIdentifierValueValue.setVisible(false);
     */}

  public void clear() {
    aipID.setText("");
    aipType.setText("");
    aipState.setText("");
    aipCreatedOn.setText("");
    aipCreatedBy.setText("");
    modifiedOn.setText("");
    modifiedBy.setText("");
    /*
     * riskNotesKey.setVisible(false); riskNotesValue.setText("");
     * 
     * riskPreMitigationProbability.setText("");
     * riskPreMitigationImpact.setText(""); riskPreMitigationSeverity.setText("");
     * riskPreMitigationNotesKey.setVisible(false);
     * riskPreMitigationNotesValue.setText("");
     * 
     * riskPosMitigationKey.setVisible(false);
     * riskPosMitigationProbabilityKey.setVisible(false);
     * riskPosMitigationProbability.setText("");
     * riskPosMitigationImpactKey.setVisible(false);
     * riskPosMitigationImpact.setText("");
     * riskPosMitigationSeverityKey.setVisible(false);
     * riskPosMitigationSeverity.setText("");
     * riskPosMitigationNotesKey.setVisible(false);
     * riskPosMitigationNotesValue.setText("");
     * 
     * riskMitigationKey.setVisible(false);
     * riskMitigationStrategyKey.setVisible(false);
     * riskMitigationStrategyValue.setText("");
     * riskMitigationOwnerTypeKey.setVisible(false);
     * riskMitigationOwnerTypeValue.setText("");
     * riskMitigationOwnerKey.setVisible(false);
     * riskMitigationOwnerValue.setText("");
     * riskMitigationRelatedEventIdentifierTypeKey.setVisible(false);
     * riskMitigationRelatedEventIdentifierTypeValue.setText("");
     * riskMitigationRelatedEventIdentifierValueKey.setVisible(false);
     * riskMitigationRelatedEventIdentifierValueValue.setText("");
     */}

  interface MyUiBinder extends UiBinder<Widget, DetailsPanel> {
    Widget createAndBindUi(DetailsPanel detailsPanel);
  }
}
