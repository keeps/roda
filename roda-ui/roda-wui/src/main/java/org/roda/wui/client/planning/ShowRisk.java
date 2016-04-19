/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.RiskVersionsBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.RiskMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowRisk extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "risk";
    }
  };

  private static ShowRisk instance = null;

  public static ShowRisk getInstance() {
    if (instance == null) {
      instance = new ShowRisk();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, ShowRisk> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static RiskMessages messages = GWT.create(RiskMessages.class);

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

  @UiField
  Button buttonHistory;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonCancel;

  private Risk risk;

  /**
   * Create a new panel to view a risk
   *
   *
   */

  public ShowRisk() {
    this.risk = new Risk();
    initWidget(uiBinder.createAndBindUi(this));
  }

  public ShowRisk(Risk risk) {
    this.risk = risk;
    initWidget(uiBinder.createAndBindUi(this));

    riskId.setText(risk.getId());
    riskName.setText(risk.getName());

    riskDescriptionValue.setText(risk.getDescription());
    riskDescriptionKey.setVisible(risk.getDescription().length() > 0);

    riskIdentifiedOn.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(risk.getIdentifiedOn()));
    riskIdentifiedBy.setText(risk.getIdentifiedBy());
    riskCategory.setText(risk.getCategory());
    riskNotesValue.setText(risk.getNotes());
    riskNotesKey.setVisible(risk.getNotes().length() > 0);

    final int preProbability = risk.getPreMitigationProbability();
    final int preImpact = risk.getPreMitigationImpact();
    final int preSeverity = risk.getPreMitigationSeverity();
    final int posProbability = risk.getPosMitigationProbability();
    final int posImpact = risk.getPosMitigationImpact();
    final int posSeverity = risk.getPosMitigationSeverity();

    BrowserService.Util.getInstance().retrieveShowMitigationTerms(preProbability, preImpact, posProbability, posImpact,
      new AsyncCallback<List<String>>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(List<String> terms) {

          if (terms.size() == 6) {
            int severityHighLimit = Integer.parseInt(terms.get(0));
            int severityLowLimit = Integer.parseInt(terms.get(1));

            riskPreMitigationProbability.setText(terms.get(2));
            riskPreMitigationImpact.setText(terms.get(3));

            riskPreMitigationSeverity
              .setHTML(ShowRisk.this.getSeverityDefinition(preSeverity, severityHighLimit, severityLowLimit));

            riskPosMitigationProbability.setText(terms.get(4));
            riskPosMitigationImpact.setText(terms.get(5));

            if (posProbability == 0 && posImpact == 0) {
              riskPosMitigationKey.setVisible(false);
              riskPosMitigationProbabilityKey.setVisible(false);
              riskPosMitigationProbability.setVisible(false);
              riskPosMitigationImpactKey.setVisible(false);
              riskPosMitigationImpact.setVisible(false);
              riskPosMitigationSeverityKey.setVisible(false);
              riskPosMitigationSeverity.setVisible(false);
            } else {
              riskPosMitigationSeverity
                .setHTML(ShowRisk.this.getSeverityDefinition(posSeverity, severityHighLimit, severityLowLimit));
            }
          }
        }
      });

    riskPreMitigationNotesValue.setText(risk.getPreMitigationNotes());
    riskPreMitigationNotesKey.setVisible(risk.getPreMitigationNotes().length() > 0);

    riskPosMitigationNotesValue.setText(risk.getPosMitigationNotes());
    riskPosMitigationNotesKey.setVisible(risk.getPosMitigationNotes().length() > 0);

    int mitigationCounter = 0;

    if (risk.getMitigationStrategy().length() > 0) {
      mitigationCounter++;
      riskMitigationStrategyValue.setText(risk.getMitigationStrategy());
    } else {
      riskMitigationStrategyKey.setVisible(false);
    }

    if (risk.getMitigationOwnerType().length() > 0) {
      mitigationCounter++;
      riskMitigationOwnerTypeValue.setText(risk.getMitigationOwnerType());
    } else {
      riskMitigationOwnerTypeKey.setVisible(false);
    }

    if (risk.getMitigationOwner().length() > 0) {
      mitigationCounter++;
      riskMitigationOwnerValue.setText(risk.getMitigationOwner());
    } else {
      riskMitigationOwnerKey.setVisible(false);
    }

    if (risk.getMitigationRelatedEventIdentifierType().length() > 0) {
      mitigationCounter++;
      riskMitigationRelatedEventIdentifierTypeValue.setText(risk.getMitigationRelatedEventIdentifierType());
    } else {
      riskMitigationRelatedEventIdentifierTypeKey.setVisible(false);
    }

    if (risk.getMitigationRelatedEventIdentifierValue().length() > 0) {
      mitigationCounter++;
      riskMitigationRelatedEventIdentifierValueValue.setText(risk.getMitigationRelatedEventIdentifierValue());
    } else {
      riskMitigationRelatedEventIdentifierValueKey.setVisible(false);
    }

    if (mitigationCounter == 0) {
      riskMitigationKey.setVisible(false);
    }

    BrowserService.Util.getInstance().retrieveRiskVersions(risk.getId(), new AsyncCallback<RiskVersionsBundle>() {

      @Override
      public void onFailure(Throwable caught) {
        buttonHistory.setVisible(false);
      }

      @Override
      public void onSuccess(RiskVersionsBundle bundle) {
        if (bundle.getVersions().isEmpty()) {
          buttonHistory.setVisible(false);
        } else {
          buttonHistory.setVisible(true);
        }
      }
    });

    // FIXME it must be visible later
    riskMitigationRelatedEventIdentifierTypeKey.setVisible(false);
    riskMitigationRelatedEventIdentifierTypeValue.setVisible(false);
    riskMitigationRelatedEventIdentifierValueKey.setVisible(false);
    riskMitigationRelatedEventIdentifierValueValue.setVisible(false);
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {

    if (historyTokens.size() == 1) {
      String riskId = historyTokens.get(0);
      BrowserService.Util.getInstance().retrieve(Risk.class.getName(), riskId, new AsyncCallback<Risk>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Risk result) {
          ShowRisk riskPanel = new ShowRisk(result);
          callback.onSuccess(riskPanel);
        }
      });
    } else {
      Tools.newHistory(RiskRegister.RESOLVER);
      callback.onSuccess(null);
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

  @UiHandler("buttonHistory")
  void handleButtonHistory(ClickEvent e) {
    Tools.newHistory(RiskRegister.RESOLVER, RiskHistory.RESOLVER.getHistoryToken(), risk.getId());
  }

  @UiHandler("buttonEdit")
  void handleButtonEdit(ClickEvent e) {
    Tools.newHistory(RiskRegister.RESOLVER, EditRisk.RESOLVER.getHistoryToken(), risk.getId());
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(RiskRegister.RESOLVER);
  }

}
