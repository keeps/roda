/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.InlineHTML;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

import java.util.List;

/**
 * @author Luis Faria
 *
 */
public class DetailsPanelRisk extends Composite implements HasValueChangeHandlers<Risk> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel detailsPanel;


  public DetailsPanelRisk() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public DetailsPanelRisk(String listId) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public DetailsPanelRisk(Risk risk, String listId) {
    initWidget(uiBinder.createAndBindUi(this));
    init(risk);
  }

  public void init(Risk risk) {
    // riskId.setText(risk.getId());
    addIfNotBlank(messages.riskIdentifier(), risk.getId());
    addIfNotBlank(messages.riskName(), risk.getName());
    addIfNotBlank(messages.riskDescription(), risk.getDescription());
    addIfNotBlank(messages.riskIdentifiedOn(), Humanize.formatDate(risk.getIdentifiedOn()));
    addIfNotBlank(messages.riskIdentifiedBy(), risk.getIdentifiedBy());
    addIfNotBlank(messages.riskNotes(), risk.getNotes());

    addCategoriesField(risk.getCategories());

    addMitigationTermsFields(risk);

    // Pre-mitigation notes (termos vêm async abaixo)
    addIfNotBlank(messages.riskPreMitigationNotes(), risk.getPreMitigationNotes());

    // Post-mitigation notes
    addIfNotBlank(messages.riskPostMitigationNotes(), risk.getPostMitigationNotes());

    // Mitigation metadata
    addIfNotBlank(messages.riskMitigationStrategy(), risk.getMitigationStrategy());
    addIfNotBlank(messages.riskMitigationOwner(), risk.getMitigationOwner());

  }

  // public void clear() {
  // riskId.setText("");
  // riskName.setText("");
  // riskDescriptionKey.setVisible(false);
  // riskDescriptionValue.setText("");
  // riskIdentifiedOn.setText("");
  // riskIdentifiedBy.setText("");
  // riskCategories.clear();
  // riskNotesKey.setVisible(false);
  // riskNotesValue.setText("");
  //
  // riskPreMitigationProbability.setText("");
  // riskPreMitigationImpact.setText("");
  // riskPreMitigationSeverity.setText("");
  // riskPreMitigationNotesKey.setVisible(false);
  // riskPreMitigationNotesValue.setText("");
  //
  // riskPosMitigationKey.setVisible(false);
  // riskPosMitigationProbabilityKey.setVisible(false);
  // riskPosMitigationProbability.setText("");
  // riskPosMitigationImpactKey.setVisible(false);
  // riskPosMitigationImpact.setText("");
  // riskPosMitigationSeverityKey.setVisible(false);
  // riskPosMitigationSeverity.setText("");
  // riskPosMitigationNotesKey.setVisible(false);
  // riskPosMitigationNotesValue.setText("");
  //
  // riskMitigationKey.setVisible(false);
  // riskMitigationStrategyKey.setVisible(false);
  // riskMitigationStrategyValue.setText("");
  // riskMitigationOwnerTypeKey.setVisible(false);
  // riskMitigationOwnerTypeValue.setText("");
  // riskMitigationOwnerKey.setVisible(false);
  // riskMitigationOwnerValue.setText("");
  // riskMitigationRelatedEventIdentifierTypeKey.setVisible(false);
  // riskMitigationRelatedEventIdentifierTypeValue.setText("");
  // riskMitigationRelatedEventIdentifierValueKey.setVisible(false);
  // riskMitigationRelatedEventIdentifierValueValue.setText("");
  // }

  public void clear() {
    detailsPanel.clear();
  }

  private void addIfNotBlank(String label, String value) {
    if (StringUtils.isNotBlank(value)) {
      detailsPanel.add(buildField(label, new InlineHTML(SafeHtmlUtils.htmlEscape(value))));
    }
  }

  private void addCategoriesField(List<String> categories) {
    if (categories == null || categories.isEmpty()) {
      return;
    }

    FlowPanel categoriesPanel = new FlowPanel();
    categoriesPanel.addStyleName("risk-categories");
    for (String category : categories) {
      if (StringUtils.isBlank(category)) {
        continue;
      }
      InlineHTML tag = new InlineHTML(
        "<span class='label label-info btn-separator-right'>" + SafeHtmlUtils.htmlEscape(category) + "</span>");
      categoriesPanel.add(tag);
    }

    if (categoriesPanel.getWidgetCount() > 0) {
      detailsPanel.add(buildField(messages.riskCategories(), categoriesPanel));
    }
  }

  private Widget buildField(String label, Widget valueWidget) {
    FlowPanel field = new FlowPanel();
    field.setStyleName("field");

    Label l = new Label(label);
    l.setStyleName("label");

    FlowPanel value = new FlowPanel();
    value.setStyleName("value");
    value.add(valueWidget);

    field.add(l);
    field.add(value);
    return field;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Risk> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  private void addMitigationTermsFields(Risk risk) {
    final int preSeverity = risk.getPreMitigationSeverity();
    final int posSeverity = risk.getPostMitigationSeverity();

    Services services = new Services("Retrieve risk mitigation terms", "get");
    services.riskResource(s -> s.retrieveRiskMitigationTerms(risk.getUUID())).whenComplete((terms, throwable) -> {
      if (throwable != null || terms == null) {
        return;
      }

      int severityLowLimit = terms.getSeverityLowLimit();
      int severityHighLimit = terms.getSeverityHighLimit();

      addIfNotBlank(messages.riskPreMitigationProbability(),
        messages.riskMitigationProbability(terms.getPreMitigationProbability().replace(' ', '_')));
      addIfNotBlank(messages.riskPreMitigationImpact(),
        messages.riskMitigationImpact(terms.getPreMitigationImpact().replace(' ', '_')));

      InlineHTML preSeverityHtml = new InlineHTML(
        HtmlSnippetUtils.getSeverityDefinition(preSeverity, severityLowLimit, severityHighLimit));
      detailsPanel.add(buildField(messages.riskPreMitigationSeverity(), preSeverityHtml));

      addIfNotBlank(messages.riskPostMitigationProbability(),
        messages.riskMitigationProbability(terms.getPosMitigationProbability().replace(' ', '_')));
      addIfNotBlank(messages.riskPostMitigationImpact(),
        messages.riskMitigationImpact(terms.getPosMitigationImpact().replace(' ', '_')));

      InlineHTML posSeverityHtml = new InlineHTML(
        HtmlSnippetUtils.getSeverityDefinition(posSeverity, severityLowLimit, severityHighLimit));
      detailsPanel.add(buildField(messages.riskPostMitigationSeverity(), posSeverityHtml));
    });
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelRisk> {
    Widget createAndBindUi(DetailsPanelRisk detailsPanelRisk);
  }
}
