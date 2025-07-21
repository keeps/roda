/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.ClientMessages;

public class EditRiskIncidence extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services service = new Services("Retrieve incidence risk", "get");
        String riskIncidenceId = historyTokens.get(0);
        service.rodaEntityRestService(s -> s.findByUuid(riskIncidenceId, LocaleInfo.getCurrentLocale().getLocaleName()), RiskIncidence.class)
          .whenComplete((value, error) -> {
            if (error != null) {
              callback.onFailure(error);
            } else if (value != null) {
              EditRiskIncidence editIncidence = new EditRiskIncidence(value);
              callback.onSuccess(editIncidence);
            }
          });

      } else {
        HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RiskIncidenceRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_riskincidence";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditRiskIncidence> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.RISK_INCIDENCE_ID, RodaConstants.RISK_INCIDENCE_RISK_ID, RodaConstants.RISK_INCIDENCE_DESCRIPTION,
    RodaConstants.RISK_INCIDENCE_STATUS, RodaConstants.RISK_INCIDENCE_SEVERITY,
    RodaConstants.RISK_INCIDENCE_DETECTED_BY, RodaConstants.RISK_INCIDENCE_DETECTED_ON,
    RodaConstants.RISK_INCIDENCE_MITIGATED_ON, RodaConstants.RISK_INCIDENCE_MITIGATED_BY,
    RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION);

  private RiskIncidence incidence;
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField
  Label incidenceId;

  @UiField
  Label objectLabel;

  @UiField
  Anchor objectLink;

  @UiField
  Anchor riskLink;

  @UiField
  Label detectedOn, detectedBy;

  @UiField
  TextArea description;

  @UiField
  ListBox status, severity;

  @UiField
  TextArea mitigatedDescription;

  /**
   * Create a new panel to edit incidence
   *
   * @param incidence
   *          the incidence to edit
   */
  public EditRiskIncidence(RiskIncidence incidence) {
    this.incidence = incidence;

    initWidget(uiBinder.createAndBindUi(this));

    incidenceId.setText(incidence.getId());

    HtmlSnippetUtils.addRiskIncidenceObjectLinks(incidence, objectLabel, objectLink);

    String riskId = incidence.getRiskId();
    riskLink.setText(riskId);
    riskLink
      .setHref(HistoryUtils.createHistoryHashLink(RiskRegister.RESOLVER, ShowRisk.RESOLVER.getHistoryToken(), riskId));

    detectedOn
      .setText(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT).format(incidence.getDetectedOn()));
    detectedBy.setText(incidence.getDetectedBy());

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));

    description.setText(incidence.getDescription());
    mitigatedDescription.setText(incidence.getMitigatedDescription());

    int selectedIndex = 0;
    for (IncidenceStatus istatus : IncidenceStatus.values()) {
      status.addItem(messages.riskIncidenceStatusValue(istatus), istatus.toString());

      if (istatus.equals(incidence.getStatus())) {
        selectedIndex = status.getItemCount();
      }
    }

    status.setSelectedIndex(selectedIndex - 1);

    selectedIndex = 0;
    for (SeverityLevel iseverity : SeverityLevel.values()) {
      severity.addItem(messages.severityLevel(iseverity), iseverity.toString());

      if (iseverity.equals(incidence.getSeverity())) {
        selectedIndex = severity.getItemCount();
      }
    }

    severity.setSelectedIndex(selectedIndex - 1);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void getRiskIncidence() {
    incidence.setDescription(description.getText());
    incidence.setStatus(IncidenceStatus.valueOf(status.getSelectedValue()));
    incidence.setSeverity(SeverityLevel.valueOf(severity.getSelectedValue()));
    incidence.setMitigatedDescription(mitigatedDescription.getText());
  }

  public void clear() {
    description.setText("");
    mitigatedDescription.setText("");
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    getRiskIncidence();

    Services service = new Services("Edit risk incidence", "update");

    service.riskIncidenceResource(s -> s.updateRiskIncidence(incidence)).whenComplete((value, error) -> {
      if (error != null) {
        errorMessage(error);
      } else {
        HistoryUtils.newHistory(ShowRiskIncidence.RESOLVER, incidence.getId());
      }
    });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowRiskIncidence.RESOLVER, incidence.getId());
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editIncidenceNotFound(incidence.getId()));
      cancel();
    } else {
      Toast.showError(messages.editIncidenceFailure(caught.getMessage()));
    }
  }

}
