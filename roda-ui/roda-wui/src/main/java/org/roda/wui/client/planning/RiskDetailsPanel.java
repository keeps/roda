/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RiskActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskDetailsPanel extends GenericMetadataCardPanel<IndexedRisk> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public RiskDetailsPanel() {
    this(new IndexedRisk(), null);
  }

  public RiskDetailsPanel(IndexedRisk risk, AsyncCallback<Actionable.ActionImpact> callback) {
    super(createConfiguredToolbar(risk));
    setData(risk);
  }

  private static FlowPanel createConfiguredToolbar(IndexedRisk risk) {
    if (risk == null) {
      return null;
    }

    RiskActions riskActions = risk.hasVersions() ? RiskActions.getWithHistory() : RiskActions.get();

    return new ActionableWidgetBuilder<IndexedRisk>(riskActions)
      .buildGroupedListWithObjects(new ActionableObject<>(risk),
        List.of(RiskActions.IndexedRiskAction.EDIT_DETAILS, RiskActions.IndexedRiskAction.EDIT_PRE_MITIGATION,
          RiskActions.IndexedRiskAction.EDIT_MITIGATION, RiskActions.IndexedRiskAction.EDIT_POST_MITIGATION),
        List.of());

  }

  @Override
  public void setData(IndexedRisk risk) {
    metadataContainer.clear();
    if (risk == null) {
      return;
    }

    addFieldIfNotNull(messages.riskIdentifier(), risk.getId());
    addFieldIfNotNull(messages.riskName(), risk.getName());
    addFieldIfNotNull(messages.riskDescription(), risk.getDescription());
    addFieldIfNotNull(messages.riskIdentifiedOn(), Humanize.formatDate(risk.getIdentifiedOn()));
    addFieldIfNotNull(messages.riskIdentifiedBy(), risk.getIdentifiedBy());
    addFieldIfNotNull(messages.riskNotes(), risk.getNotes());
    //addFieldIfNotNull(messages.riskCategories(), risk.getCategories());

    addSeparator(messages.riskPreMitigation());
    addMitigationTermsFields(risk);
    addFieldIfNotNull(messages.riskPreMitigationNotes(), risk.getPreMitigationNotes(), "text-area-whitespace-wrap");

    addSeparator(messages.riskMitigation());
    addFieldIfNotNull(messages.riskMitigationStrategy(), risk.getMitigationStrategy());
    addFieldIfNotNull(messages.riskMitigationOwner(), risk.getMitigationOwner());

    addSeparator(messages.riskPostMitigation());
    addFieldIfNotNull(messages.riskPostMitigationNotes(), risk.getPostMitigationNotes(), "text-area-whitespace-wrap");
  }

  private void addMitigationTermsFields(Risk risk) {
    final int preSeverity = risk.getPreMitigationSeverity();
    final int postSeverity = risk.getPostMitigationSeverity();

    Services services = new Services("Retrieve risk mitigation terms", "get");
    services.riskResource(s -> s.retrieveRiskMitigationTerms(risk.getUUID())).whenComplete((terms, throwable) -> {
      if (throwable != null || terms == null) {
        return;
      }

      int severityLowLimit = terms.getSeverityLowLimit();
      int severityHighLimit = terms.getSeverityHighLimit();

      addFieldIfNotNull(messages.riskPreMitigationProbability(),
        messages.riskMitigationProbability(terms.getPreMitigationProbability().replace(' ', '_')));
      addFieldIfNotNull(messages.riskPreMitigationImpact(),
        messages.riskMitigationImpact(terms.getPreMitigationImpact().replace(' ', '_')));
      addFieldIfNotNull(messages.riskPreMitigationSeverity(),
        HtmlSnippetUtils.getSeverityDefinition(preSeverity, severityLowLimit, severityHighLimit));

      addFieldIfNotNull(messages.riskPostMitigationProbability(),
        messages.riskMitigationProbability(terms.getPosMitigationProbability().replace(' ', '_')));
      addFieldIfNotNull(messages.riskPostMitigationImpact(),
        messages.riskMitigationImpact(terms.getPosMitigationImpact().replace(' ', '_')));
      addFieldIfNotNull(messages.riskPostMitigationSeverity(),
        HtmlSnippetUtils.getSeverityDefinition(postSeverity, severityLowLimit, severityHighLimit));
    });
  }

}
