package org.roda.wui.client.planning;

import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import config.i18n.client.RiskMessages;

public class CreateRisk extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      Risk risk = new Risk();
      CreateRisk createRisk = new CreateRisk(risk);
      callback.onSuccess(createRisk);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      Toast.showInfo("Refresh", "Bla bla bla");
      return Tools.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "create_risk";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateRisk> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static RiskMessages messages = GWT.create(RiskMessages.class);

  @UiField
  TextBox name;

  @UiField
  TextArea description;

  @UiField
  DateBox identifiedOn;

  @UiField
  TextBox identifiedBy;

  @UiField
  TextBox category;

  @UiField
  TextArea notes;

  @UiField
  IntegerBox preMitigationProbability;

  @UiField
  IntegerBox preMitigationImpact;

  @UiField
  IntegerBox preMitigationSeverity;

  @UiField
  TextArea preMitigationNotes;

  @UiField
  IntegerBox posMitigationProbability;

  @UiField
  IntegerBox posMitigationImpact;

  @UiField
  IntegerBox posMitigationSeverity;

  @UiField
  TextArea posMitigationNotes;

  @UiField
  TextBox mitigationStrategy;

  @UiField
  TextBox mitigationOwnerType;

  @UiField
  TextBox mitigationOwner;

  @UiField
  TextBox mitigationRelatedEventIdentifierType;

  @UiField
  TextBox mitigationRelatedEventIdentifierValue;

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public CreateRisk(Risk risk) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public boolean isValid() {
    boolean valid = true;

    if (name.getText().length() == 0) {
      valid = false;
      name.addStyleName("isWrong");
    } else {
      name.removeStyleName("isWrong");
    }

    if (identifiedOn.getValue() != null && identifiedOn.getValue().after(new Date())) {
      valid = false;
      identifiedOn.addStyleName("isWrong");
    } else {
      identifiedOn.removeStyleName("isWrong");
    }

    if (identifiedBy.getText().length() == 0) {
      valid = false;
      identifiedBy.addStyleName("isWrong");
    } else {
      identifiedBy.removeStyleName("isWrong");
    }

    if (category.getText().length() == 0) {
      valid = false;
      category.addStyleName("isWrong");
    } else {
      category.removeStyleName("isWrong");
    }

    if (preMitigationProbability.getText().length() == 0) {
      valid = false;
      preMitigationProbability.addStyleName("isWrong");
    } else {
      preMitigationProbability.removeStyleName("isWrong");
    }

    if (preMitigationImpact.getText().length() == 0) {
      valid = false;
      preMitigationImpact.addStyleName("isWrong");
    } else {
      preMitigationImpact.removeStyleName("isWrong");
    }

    if (preMitigationSeverity.getText().length() == 0) {
      valid = false;
      preMitigationSeverity.addStyleName("isWrong");
    } else {
      preMitigationSeverity.removeStyleName("isWrong");
    }

    return valid;
  }

  public void setRisk(Risk risk) {
    this.name.setText(risk.getName());
    this.description.setText(risk.getDescription());
    this.identifiedOn.setValue(risk.getIdentifiedOn());
    this.identifiedBy.setText(risk.getIdentifiedBy());
    this.category.setText(risk.getCategory());
    this.notes.setText(risk.getNotes());

    this.preMitigationProbability.setValue(risk.getPreMitigationProbability());
    this.preMitigationImpact.setValue(risk.getPreMitigationImpact());
    this.preMitigationSeverity.setValue(risk.getPreMitigationSeverity());
    this.preMitigationNotes.setText(risk.getPreMitigationNotes());

    this.posMitigationProbability.setValue(risk.getPosMitigationProbability());
    this.posMitigationImpact.setValue(risk.getPosMitigationImpact());
    this.posMitigationSeverity.setValue(risk.getPosMitigationSeverity());
    this.posMitigationNotes.setText(risk.getPosMitigationNotes());

    this.mitigationStrategy.setText(risk.getMitigationStrategy());
    this.mitigationOwnerType.setText(risk.getMitigationOwnerType());
    this.mitigationOwner.setText(risk.getMitigationOwner());
    this.mitigationRelatedEventIdentifierType.setText(risk.getMitigationRelatedEventIdentifierType());
    this.mitigationRelatedEventIdentifierValue.setText(risk.getMitigationRelatedEventIdentifierValue());
  }

  public Risk getRisk() {

    Risk risk = new Risk();
    risk.setName(name.getText());
    risk.setDescription(description.getText());
    risk.setIdentifiedOn(identifiedOn.getValue());
    risk.setIdentifiedBy(identifiedBy.getText());
    risk.setCategory(category.getText());
    risk.setNotes(notes.getText());

    risk.setPreMitigationProbability(preMitigationProbability.getValue());
    risk.setPreMitigationImpact(preMitigationImpact.getValue());
    risk.setPreMitigationSeverity(preMitigationSeverity.getValue());
    risk.setPreMitigationNotes(preMitigationNotes.getText());

    risk.setPosMitigationProbability(posMitigationProbability.getValue());
    risk.setPosMitigationImpact(posMitigationImpact.getValue());
    risk.setPosMitigationSeverity(posMitigationSeverity.getValue());
    risk.setPosMitigationNotes(posMitigationNotes.getText());

    risk.setMitigationStrategy(mitigationStrategy.getText());
    risk.setMitigationOwnerType(mitigationOwnerType.getText());
    risk.setMitigationOwner(mitigationOwner.getText());
    risk.setMitigationRelatedEventIdentifierType(mitigationRelatedEventIdentifierType.getText());
    risk.setMitigationRelatedEventIdentifierValue(mitigationRelatedEventIdentifierValue.getText());

    return risk;
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (isValid()) {
      Risk risk = getRisk();
      UserManagementService.Util.getInstance().addRisk(risk, new AsyncCallback<Void>() {

        public void onFailure(Throwable caught) {
          errorMessage(caught);
        }

        @Override
        public void onSuccess(Void result) {
          Tools.newHistory(RiskRegister.RESOLVER);
        }

      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(RiskRegister.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    Toast.showError(messages.createRiskFailure(caught.getMessage()));
  }

}
