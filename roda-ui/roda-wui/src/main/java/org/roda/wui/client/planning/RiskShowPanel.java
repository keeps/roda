/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ViewRepresentation;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.RiskMessages;
import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 *
 */
public class RiskShowPanel extends Composite implements HasValueChangeHandlers<Risk> {

  interface MyUiBinder extends UiBinder<Widget, RiskShowPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private static RiskMessages messages = GWT.create(RiskMessages.class);

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
  Label riskCategory;

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
  RiskIncidenceList incidenceList;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  public RiskShowPanel() {
    incidenceList = new RiskIncidenceList(null, null, "Incidences", false);
    initWidget(uiBinder.createAndBindUi(this));
  }

  public RiskShowPanel(Risk risk, boolean hasTitle) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISKS, risk.getId()));
    incidenceList = new RiskIncidenceList(filter, null, "Incidences", false);

    incidenceList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        final RiskIncidence selected = incidenceList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          if (selected.getObjectClass().equals(AIP.class.getSimpleName())) {
            Tools.newHistory(Browse.RESOLVER, selected.getAipId());
          } else if (selected.getObjectClass().equals(Representation.class.getSimpleName())) {
            BrowserService.Util.getInstance().getRepresentationFromId(selected.getRepresentationId(),
              new AsyncCallback<IndexedRepresentation>() {

                @Override
                public void onFailure(Throwable caught) {
                  Tools.newHistory(Browse.RESOLVER, selected.getAipId());
                }

                @Override
                public void onSuccess(IndexedRepresentation result) {
                  if (result != null) {
                    Tools.newHistory(Browse.RESOLVER, ViewRepresentation.RESOLVER.getHistoryToken(), result.getAipId(),
                      result.getUUID());
                  } else {
                    Tools.newHistory(Browse.RESOLVER, selected.getAipId());
                  }
                }
              });
          } else if (selected.getObjectClass().equals(File.class.getSimpleName())) {
            BrowserService.Util.getInstance().getFileFromId(selected.getFileId(), new AsyncCallback<IndexedFile>() {

              @Override
              public void onFailure(Throwable caught) {
                Tools.newHistory(Browse.RESOLVER, selected.getAipId());
              }

              @Override
              public void onSuccess(IndexedFile result) {
                if (result != null) {
                  Tools.newHistory(Browse.RESOLVER, ViewRepresentation.RESOLVER.getHistoryToken(), result.getAipId(),
                    result.getRepresentationUUID(), result.getUUID());

                } else {
                  Tools.newHistory(Browse.RESOLVER, selected.getAipId());
                }
              }
            });
          }
        }
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

    riskIdentifiedOn.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(risk.getIdentifiedOn()));
    riskIdentifiedBy.setText(risk.getIdentifiedBy());
    riskCategory.setText(risk.getCategory());
    riskNotesValue.setText(risk.getNotes());
    riskNotesKey.setVisible(StringUtils.isNotBlank(risk.getNotes()));

    final int preProbability = risk.getPreMitigationProbability();
    final int preImpact = risk.getPreMitigationImpact();
    final int preSeverity = risk.getPreMitigationSeverity();
    final int posProbability = risk.getPosMitigationProbability();
    final int posImpact = risk.getPosMitigationImpact();
    final int posSeverity = risk.getPosMitigationSeverity();

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

          riskPreMitigationProbability.setText(terms.getPreMitigationProbability());
          riskPreMitigationImpact.setText(terms.getPreMitigationImpact());

          riskPreMitigationSeverity
            .setHTML(RiskShowPanel.this.getSeverityDefinition(preSeverity, severityLowLimit, severityHighLimit));

          riskPosMitigationProbability.setText(terms.getPosMitigationProbability());
          riskPosMitigationImpact.setText(terms.getPosMitigationImpact());

          riskPosMitigationKey.setVisible(true);
          riskPosMitigationProbabilityKey.setVisible(true);
          riskPosMitigationProbability.setVisible(true);
          riskPosMitigationImpactKey.setVisible(true);
          riskPosMitigationImpact.setVisible(true);
          riskPosMitigationSeverityKey.setVisible(true);
          riskPosMitigationSeverity.setVisible(true);
          riskPosMitigationSeverity
            .setHTML(RiskShowPanel.this.getSeverityDefinition(posSeverity, severityLowLimit, severityHighLimit));
        }
      });

    riskPreMitigationNotesValue.setText(risk.getPreMitigationNotes());
    riskPreMitigationNotesKey.setVisible(StringUtils.isNotBlank(risk.getPreMitigationNotes()));

    riskPosMitigationNotesValue.setText(risk.getPosMitigationNotes());
    riskPosMitigationNotesKey.setVisible(StringUtils.isNotBlank(risk.getPosMitigationNotes()));

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
    riskCategory.setText("");
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

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Risk> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }
}
