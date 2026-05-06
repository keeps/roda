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
import org.roda.core.data.v2.risks.RiskMitigationTerms;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RiskActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskDetailsPanel extends GenericMetadataCardPanel<Risk> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final AsyncCallback<Actionable.ActionImpact> actionCallback;
  private final RiskMitigationTerms riskMitigationTerms;

  public RiskDetailsPanel() {
    this.riskMitigationTerms = null;
    this.actionCallback = null;
  }

  public RiskDetailsPanel(Risk risk, RiskMitigationTerms terms, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    this.actionCallback = actionCallback;
    this.riskMitigationTerms = terms;
    setData(risk);
  }

  @Override
  protected FlowPanel createHeaderWidget(Risk risk) {
    if (!(risk instanceof IndexedRisk)) {
      return null;
    }

    IndexedRisk indexedRisk = (IndexedRisk) risk;
    RiskActions riskActions = indexedRisk.hasVersions() ? RiskActions.getWithHistory() : RiskActions.get();
    ActionableWidgetBuilder<IndexedRisk> builder = new ActionableWidgetBuilder<IndexedRisk>(riskActions);

    if (actionCallback != null) {
      builder.withActionCallback(actionCallback);
    }

    return builder
      .buildGroupedListWithObjects(new ActionableObject<>(indexedRisk),
        List.of(RiskActions.IndexedRiskAction.EDIT_DETAILS, RiskActions.IndexedRiskAction.EDIT_PRE_MITIGATION,
          RiskActions.IndexedRiskAction.EDIT_MITIGATION, RiskActions.IndexedRiskAction.EDIT_POST_MITIGATION),
        List.of());
  }

  @Override
  protected void buildFields(Risk risk) {
    buildField(messages.riskIdentifier()).withValue(risk.getId()).build();
    buildField(messages.riskName()).withValue(risk.getName()).build();
    buildField(messages.riskDescription()).withValue(risk.getDescription()).build();
    buildField(messages.riskIdentifiedOn()).withValue(Humanize.formatDate(risk.getIdentifiedOn())).build();
    buildField(messages.riskIdentifiedBy()).withValue(risk.getIdentifiedBy()).build();
    buildField(messages.riskNotes()).withValue(risk.getNotes()).withValueStyleName("text-area-whitespace-wrap").build();

    addCategoriesField(risk.getCategories());

    addSeparator(messages.riskPreMitigation());
    addPreMitigationTermsFields(risk);
    buildField(messages.riskPreMitigationNotes()).withValue(risk.getPreMitigationNotes())
      .withValueStyleName("text-area-whitespace-wrap").build();

    addSeparator(messages.riskMitigation());
    buildField(messages.riskMitigationStrategy()).withValue(risk.getMitigationStrategy())
      .withValueStyleName("text-area-whitespace-wrap").build();
    buildField(messages.riskMitigationOwner()).withValue(risk.getMitigationOwner()).build();

    addSeparator(messages.riskPostMitigation());
    addPostMitigationTermsFields(risk);
    buildField(messages.riskPostMitigationNotes()).withValue(risk.getPostMitigationNotes())
      .withValueStyleName("text-area-whitespace-wrap").build();
  }

  private void addCategoriesField(List<String> categories) {
    if (categories == null || categories.isEmpty()) {
      return;
    }

    FlowPanel categoriesPanel = new FlowPanel();

    for (String category : categories) {
      if (StringUtils.isBlank(category)) {
        continue;
      }

      categoriesPanel.add(new InlineHTML(
        "<span class='label label-info btn-separator-right'>" + SafeHtmlUtils.htmlEscape(category) + "</span>"));
    }

    if (categoriesPanel.getWidgetCount() > 0) {
      buildField(messages.riskCategories()).withWidget(categoriesPanel).build();
    }
  }

  private void addPreMitigationTermsFields(Risk risk) {
    if (riskMitigationTerms == null) {
      return;
    }

    buildField(messages.riskPreMitigationProbability())
      .withValue(
        messages.riskMitigationProbability(riskMitigationTerms.getPreMitigationProbability().replace(' ', '_')))
      .build();

    buildField(messages.riskPreMitigationImpact())
      .withValue(messages.riskMitigationImpact(riskMitigationTerms.getPreMitigationImpact().replace(' ', '_'))).build();

    buildField(messages.riskPreMitigationSeverity())
      .withHtml(HtmlSnippetUtils.getSeverityDefinition(risk.getPreMitigationSeverity(),
        riskMitigationTerms.getSeverityLowLimit(), riskMitigationTerms.getSeverityHighLimit()))
      .build();
  }

  private void addPostMitigationTermsFields(Risk risk) {
    if (riskMitigationTerms == null) {
      return;
    }
    buildField(messages.riskPostMitigationProbability())
      .withValue(
        messages.riskMitigationProbability(riskMitigationTerms.getPosMitigationProbability().replace(' ', '_')))
      .build();

    buildField(messages.riskPostMitigationImpact())
      .withValue(messages.riskMitigationImpact(riskMitigationTerms.getPosMitigationImpact().replace(' ', '_'))).build();

    buildField(messages.riskPostMitigationSeverity())
      .withHtml(HtmlSnippetUtils.getSeverityDefinition(risk.getPostMitigationSeverity(),
        riskMitigationTerms.getSeverityLowLimit(), riskMitigationTerms.getSeverityHighLimit()))
      .build();
  }

}
