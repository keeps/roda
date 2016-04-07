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
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

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

  // private static CommonMessages messages = GWT.create(CommonMessages.class);

  @UiField
  Label riskId;

  @UiField
  Label riskName;

  @UiField
  Label riskDescription;

  @UiField
  Label riskIdentifiedOn;

  @UiField
  Label riskIdentifiedBy;

  @UiField
  Label riskCategory;

  @UiField
  Label riskNotes;

  @UiField
  Label riskPreMitigationProbability;

  @UiField
  Label riskPreMitigationImpact;

  @UiField
  Label riskPreMitigationSeverity;

  @UiField
  Label riskPreMitigationNotes;

  @UiField
  Label riskPosMitigationProbability;

  @UiField
  Label riskPosMitigationImpact;

  @UiField
  Label riskPosMitigationSeverity;

  @UiField
  Label riskPosMitigationNotes;

  @UiField
  Label riskMitigationStrategy;

  @UiField
  Label riskMitigationOwnerType;

  @UiField
  Label riskMitigationOwner;

  @UiField
  Label riskMitigationRelatedEventIdentifierType;

  @UiField
  Label riskMitigationRelatedEventIdentifierValue;

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
    initWidget(uiBinder.createAndBindUi(this));
    this.risk = risk;

    riskId.setText(risk.getId());
    riskName.setText(risk.getName());
    riskDescription.setText(risk.getDescription());
    riskIdentifiedOn.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(risk.getIdentifiedOn()));
    riskIdentifiedBy.setText(risk.getIdentifiedBy());
    riskCategory.setText(risk.getCategory());
    riskNotes.setText(risk.getNotes());

    riskPreMitigationProbability.setText(Integer.toString(risk.getPreMitigationProbability()));
    riskPreMitigationImpact.setText(Integer.toString(risk.getPreMitigationImpact()));
    riskPreMitigationSeverity.setText(Integer.toString(risk.getPreMitigationSeverity()));
    riskPreMitigationNotes.setText(risk.getPreMitigationNotes());

    riskPosMitigationProbability.setText(Integer.toString(risk.getPosMitigationProbability()));
    riskPosMitigationImpact.setText(Integer.toString(risk.getPosMitigationImpact()));
    riskPosMitigationSeverity.setText(Integer.toString(risk.getPosMitigationSeverity()));
    riskPosMitigationNotes.setText(risk.getPosMitigationNotes());

    riskMitigationStrategy.setText(risk.getMitigationStrategy());
    riskMitigationOwnerType.setText(risk.getMitigationOwnerType());
    riskMitigationOwner.setText(risk.getMitigationOwner());
    riskMitigationRelatedEventIdentifierType.setText(risk.getMitigationRelatedEventIdentifierType());
    riskMitigationRelatedEventIdentifierValue.setText(risk.getMitigationRelatedEventIdentifierValue());
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {

    if (historyTokens.size() == 1) {
      String riskId = historyTokens.get(0);
      UserManagementService.Util.getInstance().retrieveRisk(riskId, new AsyncCallback<Risk>() {

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
