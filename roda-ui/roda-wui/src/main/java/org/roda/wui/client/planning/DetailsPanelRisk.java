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
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RiskActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class DetailsPanelRisk extends Composite {

  private final AsyncCallback<Actionable.ActionImpact> actionCallback;
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  ActionsToolbar riskActionsPanel;

  @UiField
  FlowPanel detailsPanel;

  @UiField
  FlowPanel preMitigationPanel;

  @UiField
  FlowPanel mitigationPanel;

  @UiField
  FlowPanel postMitigationPanel;

  public DetailsPanelRisk() {
    this(new IndexedRisk(), null);
  }

  public DetailsPanelRisk(IndexedRisk risk, AsyncCallback<Actionable.ActionImpact> callback) {
    initWidget(uiBinder.createAndBindUi(this));
    this.actionCallback = callback;
    initEditActions(risk);
    init(risk);
  }

  public void init(Risk risk) {
    addIfNotBlank(detailsPanel, messages.riskIdentifier(), risk.getId());
    addIfNotBlank(detailsPanel, messages.riskName(), risk.getName());
    addIfNotBlank(detailsPanel, messages.riskDescription(), risk.getDescription());
    addIfNotBlank(detailsPanel, messages.riskIdentifiedOn(), Humanize.formatDate(risk.getIdentifiedOn()));
    addIfNotBlank(detailsPanel, messages.riskIdentifiedBy(), risk.getIdentifiedBy());
    addIfNotBlankMultiline(detailsPanel, messages.riskNotes(), risk.getNotes());

    addCategoriesField(detailsPanel, risk.getCategories());

    addMitigationTermsFields(risk);
    addIfNotBlankMultiline(preMitigationPanel, messages.riskPreMitigationNotes(), risk.getPreMitigationNotes());

    addIfNotBlankMultiline(mitigationPanel, messages.riskMitigationStrategy(), risk.getMitigationStrategy());
    addIfNotBlank(mitigationPanel, messages.riskMitigationOwner(), risk.getMitigationOwner());

    addIfNotBlankMultiline(postMitigationPanel, messages.riskPostMitigationNotes(), risk.getPostMitigationNotes());

  }

  public void initEditActions(IndexedRisk risk) {
    RiskActions riskActions = risk.hasVersions() ? RiskActions.getWithHistory() : RiskActions.get();

    ActionableWidgetBuilder<IndexedRisk> builder = new ActionableWidgetBuilder<>(riskActions);

    if (actionCallback != null) {
      builder.withActionCallback(actionCallback);
    }

    FlowPanel editActionMenu = builder
      .buildGroupedListWithObjects(new ActionableObject<>(risk),
        List.of(RiskActions.IndexedRiskAction.EDIT_DETAILS, RiskActions.IndexedRiskAction.EDIT_PRE_MITIGATION,
          RiskActions.IndexedRiskAction.EDIT_MITIGATION, RiskActions.IndexedRiskAction.EDIT_POST_MITIGATION),
        List.of());

    riskActionsPanel.setActionableMenu(editActionMenu, true);
    riskActionsPanel.setTagsVisible(false);
    riskActionsPanel.setLabelVisible(false);
  }

  public void clear() {
    detailsPanel.clear();
  }

  private void addIfNotBlank(FlowPanel panel, String label, String value) {
    if (StringUtils.isNotBlank(value)) {
      panel.add(buildField(label, new InlineHTML(SafeHtmlUtils.htmlEscape(value))));
    }
  }

  private void addIfNotBlankMultiline(FlowPanel panel, String label, String value) {
    if (StringUtils.isNotBlank(value)) {
      InlineHTML html = new InlineHTML(SafeHtmlUtils.htmlEscape(value));
      html.addStyleName("text-area-whitespace-wrap");
      panel.add(buildField(label, html));
    }
  }

  private void addField(FlowPanel target, String label, Widget valueWidget) {
    target.add(buildField(label, valueWidget));
  }

  private void addCategoriesField(FlowPanel panel, List<String> categories) {
    if (categories == null || categories.isEmpty()) {
      return;
    }

    FlowPanel categoriesPanel = new FlowPanel();
    for (String category : categories) {
      if (StringUtils.isBlank(category)) {
        continue;
      }
      InlineHTML tag = new InlineHTML(
        "<span class='label label-info btn-separator-right'>" + SafeHtmlUtils.htmlEscape(category) + "</span>");
      categoriesPanel.add(tag);
    }

    if (categoriesPanel.getWidgetCount() > 0) {
      addField(panel, messages.riskCategories(), categoriesPanel);
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

      addIfNotBlank(preMitigationPanel, messages.riskPreMitigationProbability(),
        messages.riskMitigationProbability(terms.getPreMitigationProbability().replace(' ', '_')));
      addIfNotBlank(preMitigationPanel, messages.riskPreMitigationImpact(),
        messages.riskMitigationImpact(terms.getPreMitigationImpact().replace(' ', '_')));

      addField(preMitigationPanel, messages.riskPreMitigationSeverity(),
        new InlineHTML(HtmlSnippetUtils.getSeverityDefinition(preSeverity, severityLowLimit, severityHighLimit)));

      addIfNotBlank(postMitigationPanel, messages.riskPostMitigationProbability(),
        messages.riskMitigationProbability(terms.getPosMitigationProbability().replace(' ', '_')));
      addIfNotBlank(postMitigationPanel, messages.riskPostMitigationImpact(),
        messages.riskMitigationImpact(terms.getPosMitigationImpact().replace(' ', '_')));

      addField(postMitigationPanel, messages.riskPostMitigationSeverity(),
        new InlineHTML(HtmlSnippetUtils.getSeverityDefinition(posSeverity, severityLowLimit, severityHighLimit)));
    });
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelRisk> {
    Widget createAndBindUi(DetailsPanelRisk detailsPanelRisk);
  }
}
