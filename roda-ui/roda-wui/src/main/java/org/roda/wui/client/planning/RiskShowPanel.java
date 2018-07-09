/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.RiskIncidenceActions;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskShowPanel extends Composite implements HasValueChangeHandlers<Risk> {

  interface MyUiBinder extends UiBinder<Widget, RiskShowPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Label title;

  @UiField
  Label riskId;

  @UiField
  Label riskName;

  @UiField
  Label riskDescriptionKey, riskDescriptionValue;

  @UiField
  Label riskIdentifiedOn;

  @UiField
  Label riskIdentifiedBy;

  @UiField
  FlowPanel riskCategories;

  @UiField
  Label riskNotesKey, riskNotesValue;

  @UiField
  Label riskPreMitigationKey;

  @UiField
  Label riskPreMitigationProbability;

  @UiField
  Label riskPreMitigationImpact;

  @UiField
  HTML riskPreMitigationSeverity;

  @UiField
  Label riskPreMitigationNotesKey, riskPreMitigationNotesValue;

  @UiField
  Label riskPosMitigationKey;

  @UiField
  Label riskPosMitigationProbabilityKey;

  @UiField
  Label riskPosMitigationProbability;

  @UiField
  Label riskPosMitigationImpactKey;

  @UiField
  Label riskPosMitigationImpact;

  @UiField
  Label riskPosMitigationSeverityKey;

  @UiField
  HTML riskPosMitigationSeverity;

  @UiField
  Label riskPosMitigationNotesKey, riskPosMitigationNotesValue;

  @UiField
  Label riskMitigationKey;

  @UiField
  Label riskMitigationStrategyKey, riskMitigationStrategyValue;

  @UiField
  Label riskMitigationOwnerTypeKey, riskMitigationOwnerTypeValue;

  @UiField
  Label riskMitigationOwnerKey, riskMitigationOwnerValue;

  @UiField
  Label riskMitigationRelatedEventIdentifierTypeKey, riskMitigationRelatedEventIdentifierTypeValue;

  @UiField
  Label riskMitigationRelatedEventIdentifierValueKey, riskMitigationRelatedEventIdentifierValueValue;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  RiskIncidenceList incidenceList;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(RiskIncidence.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(RiskIncidence.class.getName());

  public RiskShowPanel() {
    incidenceList = new RiskIncidenceList("RiskShowPanel_riskIncidences", Filter.NULL, messages.riskIncidences(), true);
    incidenceList.setActionable(RiskIncidenceActions.getForMultipleEdit());

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true, messages.riskIncidenceRegisterSearchPlaceHolder(),
      false, false, false);
    searchPanel.setList(incidenceList);

    initWidget(uiBinder.createAndBindUi(this));
  }

  public RiskShowPanel(Risk risk, boolean hasTitle) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISK_ID, risk.getId()));
    incidenceList = new RiskIncidenceList("RiskShowPanel_riskIncidences", filter, messages.riskIncidences(), hasTitle);
    incidenceList.setActionable(RiskIncidenceActions.getForMultipleEdit());

    Filter incidenceFilter = new Filter(new BasicSearchFilterParameter(RodaConstants.RISK_INCIDENCE_SEARCH, "*"),
      new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISK_ID, risk.getId()));

    searchPanel = new SearchPanel(incidenceFilter, ALL_FILTER, true, messages.riskIncidenceRegisterSearchPlaceHolder(),
      false, false, false);
    searchPanel.setList(incidenceList);

    incidenceList.getSelectionModel().addSelectionChangeHandler(event -> {
      final RiskIncidence selected = incidenceList.getSelectionModel().getSelectedObject();
      if (selected != null) {
        LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
        HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER, ShowRiskIncidence.RESOLVER.getHistoryToken(),
          selected.getId());
      }
    });

    initWidget(uiBinder.createAndBindUi(this));
    title.setVisible(hasTitle);
    init(risk);
  }

  public void init(Risk risk) {
    riskId.setText(risk.getId());
    riskName.setText(risk.getName());

    riskDescriptionValue.setText(risk.getDescription());
    riskDescriptionKey.setVisible(StringUtils.isNotBlank(risk.getDescription()));

    riskIdentifiedOn.setText(Humanize.formatDate(risk.getIdentifiedOn()));
    riskIdentifiedBy.setText(risk.getIdentifiedBy());
    riskNotesValue.setText(risk.getNotes());
    riskNotesKey.setVisible(StringUtils.isNotBlank(risk.getNotes()));

    for(String category : risk.getCategories()) {
      Label categoryLabel = new Label(category);
      categoryLabel.addStyleName("value");
      riskCategories.add(categoryLabel);
    }

    final int preProbability = risk.getPreMitigationProbability();
    final int preImpact = risk.getPreMitigationImpact();
    final int preSeverity = risk.getPreMitigationSeverity();
    final int posProbability = risk.getPostMitigationProbability();
    final int posImpact = risk.getPostMitigationImpact();
    final int posSeverity = risk.getPostMitigationSeverity();

    BrowserService.Util.getInstance().retrieveShowMitigationTerms(preProbability, preImpact, posProbability, posImpact,
      new AsyncCallback<RiskMitigationBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(RiskMitigationBundle terms) {
          int severityLowLimit = terms.getSeverityLowLimit();
          int severityHighLimit = terms.getSeverityHighLimit();

          riskPreMitigationProbability
            .setText(messages.riskMitigationProbability(terms.getPreMitigationProbability().replace(' ', '_')));
          riskPreMitigationImpact
            .setText(messages.riskMitigationImpact(terms.getPreMitigationImpact().replace(' ', '_')));

          riskPreMitigationSeverity
            .setHTML(HtmlSnippetUtils.getSeverityDefinition(preSeverity, severityLowLimit, severityHighLimit));

          riskPosMitigationProbability
            .setText(messages.riskMitigationProbability(terms.getPosMitigationProbability().replace(' ', '_')));
          riskPosMitigationImpact
            .setText(messages.riskMitigationImpact(terms.getPosMitigationImpact().replace(' ', '_')));

          riskPosMitigationKey.setVisible(true);
          riskPosMitigationProbabilityKey.setVisible(true);
          riskPosMitigationProbability.setVisible(true);
          riskPosMitigationImpactKey.setVisible(true);
          riskPosMitigationImpact.setVisible(true);
          riskPosMitigationSeverityKey.setVisible(true);
          riskPosMitigationSeverity.setVisible(true);
          riskPosMitigationSeverity
            .setHTML(HtmlSnippetUtils.getSeverityDefinition(posSeverity, severityLowLimit, severityHighLimit));
        }
      });

    riskPreMitigationNotesValue.setText(risk.getPreMitigationNotes());
    riskPreMitigationNotesKey.setVisible(StringUtils.isNotBlank(risk.getPreMitigationNotes()));

    riskPosMitigationNotesValue.setText(risk.getPostMitigationNotes());
    riskPosMitigationNotesKey.setVisible(StringUtils.isNotBlank(risk.getPostMitigationNotes()));

    int mitigationCounter = 0;

    if (StringUtils.isNotBlank(risk.getMitigationStrategy())) {
      mitigationCounter++;
      riskMitigationStrategyKey.setVisible(true);
      riskMitigationStrategyValue.setText(risk.getMitigationStrategy());
    } else {
      riskMitigationStrategyKey.setVisible(false);
    }

    if (StringUtils.isNotBlank(risk.getMitigationOwnerType())) {
      mitigationCounter++;
      riskMitigationOwnerTypeKey.setVisible(true);
      riskMitigationOwnerTypeValue.setText(risk.getMitigationOwnerType());
    } else {
      riskMitigationOwnerTypeKey.setVisible(false);
    }

    if (StringUtils.isNotBlank(risk.getMitigationOwner())) {
      mitigationCounter++;
      riskMitigationOwnerKey.setVisible(true);
      riskMitigationOwnerValue.setText(risk.getMitigationOwner());
    } else {
      riskMitigationOwnerKey.setVisible(false);
    }

    if (StringUtils.isNotBlank(risk.getMitigationRelatedEventIdentifierType())) {
      mitigationCounter++;
      riskMitigationRelatedEventIdentifierTypeKey.setVisible(true);
      riskMitigationRelatedEventIdentifierTypeValue.setText(risk.getMitigationRelatedEventIdentifierType());
    } else {
      riskMitigationRelatedEventIdentifierTypeKey.setVisible(false);
    }

    if (StringUtils.isNotBlank(risk.getMitigationRelatedEventIdentifierValue())) {
      mitigationCounter++;
      riskMitigationRelatedEventIdentifierValueKey.setVisible(true);
      riskMitigationRelatedEventIdentifierValueValue.setText(risk.getMitigationRelatedEventIdentifierValue());
    } else {
      riskMitigationRelatedEventIdentifierValueKey.setVisible(false);
    }

    if (mitigationCounter == 0) {
      riskMitigationKey.setVisible(false);
    } else {
      riskMitigationKey.setVisible(true);
    }

    // FIXME it must be visible later
    riskMitigationOwnerTypeKey.setVisible(false);
    riskMitigationOwnerTypeValue.setVisible(false);
    riskMitigationRelatedEventIdentifierTypeKey.setVisible(false);
    riskMitigationRelatedEventIdentifierTypeValue.setVisible(false);
    riskMitigationRelatedEventIdentifierValueKey.setVisible(false);
    riskMitigationRelatedEventIdentifierValueValue.setVisible(false);
  }

  public void clear() {
    riskId.setText("");
    riskName.setText("");
    riskDescriptionKey.setVisible(false);
    riskDescriptionValue.setText("");
    riskIdentifiedOn.setText("");
    riskIdentifiedBy.setText("");
    riskCategories.clear();
    riskNotesKey.setVisible(false);
    riskNotesValue.setText("");

    riskPreMitigationProbability.setText("");
    riskPreMitigationImpact.setText("");
    riskPreMitigationSeverity.setText("");
    riskPreMitigationNotesKey.setVisible(false);
    riskPreMitigationNotesValue.setText("");

    riskPosMitigationKey.setVisible(false);
    riskPosMitigationProbabilityKey.setVisible(false);
    riskPosMitigationProbability.setText("");
    riskPosMitigationImpactKey.setVisible(false);
    riskPosMitigationImpact.setText("");
    riskPosMitigationSeverityKey.setVisible(false);
    riskPosMitigationSeverity.setText("");
    riskPosMitigationNotesKey.setVisible(false);
    riskPosMitigationNotesValue.setText("");

    riskMitigationKey.setVisible(false);
    riskMitigationStrategyKey.setVisible(false);
    riskMitigationStrategyValue.setText("");
    riskMitigationOwnerTypeKey.setVisible(false);
    riskMitigationOwnerTypeValue.setText("");
    riskMitigationOwnerKey.setVisible(false);
    riskMitigationOwnerValue.setText("");
    riskMitigationRelatedEventIdentifierTypeKey.setVisible(false);
    riskMitigationRelatedEventIdentifierTypeValue.setText("");
    riskMitigationRelatedEventIdentifierValueKey.setVisible(false);
    riskMitigationRelatedEventIdentifierValueValue.setText("");
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Risk> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public SelectedItems<RiskIncidence> getSelectedIncidences() {
    return incidenceList.getSelected();
  }

  public void refreshList() {
    incidenceList.refresh();
  }
}
