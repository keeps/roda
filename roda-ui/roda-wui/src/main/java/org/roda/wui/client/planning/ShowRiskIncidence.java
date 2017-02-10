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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowRiskIncidence extends Composite {

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
      return ListUtils.concat(RiskIncidenceRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "riskincidence";
    }
  };

  private static ShowRiskIncidence instance = null;

  public static ShowRiskIncidence getInstance() {
    if (instance == null) {
      instance = new ShowRiskIncidence();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, ShowRiskIncidence> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.RISK_INCIDENCE_ID, RodaConstants.RISK_INCIDENCE_RISK_ID, RodaConstants.RISK_INCIDENCE_DESCRIPTION,
    RodaConstants.RISK_INCIDENCE_STATUS, RodaConstants.RISK_INCIDENCE_SEVERITY,
    RodaConstants.RISK_INCIDENCE_DETECTED_BY, RodaConstants.RISK_INCIDENCE_DETECTED_ON,
    RodaConstants.RISK_INCIDENCE_MITIGATED_ON, RodaConstants.RISK_INCIDENCE_MITIGATED_BY,
    RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION);

  private RiskIncidence incidence;
  private String relatedObject = "";

  @UiField
  Label incidenceId;

  @UiField
  Label objectLabel;

  @UiField
  Anchor objectLink;

  @UiField
  Anchor riskLink;

  @UiField
  Label descriptionKey, descriptionValue;

  @UiField
  Label status;

  @UiField
  HTML severity;

  @UiField
  Label detectedOn;

  @UiField
  Label detectedBy;

  @UiField
  Label mitigatedOnKey, mitigatedOnValue;

  @UiField
  Label mitigatedByKey, mitigatedByValue;

  @UiField
  Label mitigatedDescriptionKey, mitigatedDescriptionValue;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonCancel;

  /**
   * Create a new panel to view a risk incidence
   *
   *
   */

  public ShowRiskIncidence() {
    this.incidence = new RiskIncidence();
    initWidget(uiBinder.createAndBindUi(this));
  }

  public ShowRiskIncidence(String relatedObject, RiskIncidence incidence) {
    initWidget(uiBinder.createAndBindUi(this));
    this.incidence = incidence;
    this.relatedObject = relatedObject;

    incidenceId.setText(incidence.getId());
    HtmlSnippetUtils.addRiskIncidenceObjectLinks(incidence, objectLabel, objectLink);

    String riskId = incidence.getRiskId();
    riskLink.setText(riskId);
    riskLink
      .setHref(HistoryUtils.createHistoryHashLink(RiskRegister.RESOLVER, ShowRisk.RESOLVER.getHistoryToken(), riskId));

    if (incidence.getDescription() != null) {
      descriptionKey.setVisible(true);
      descriptionValue.setText(incidence.getDescription());
    } else {
      descriptionKey.setVisible(false);
    }

    status.setText(messages.riskIncidenceStatusValue(incidence.getStatus()));
    severity.setHTML(HtmlSnippetUtils.getSeverityDefinition(incidence.getSeverity()));
    detectedOn
      .setText(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT).format(incidence.getDetectedOn()));
    detectedBy.setText(incidence.getDetectedBy());

    if (incidence.getMitigatedOn() != null) {
      mitigatedOnKey.setVisible(true);
      mitigatedOnValue
        .setText(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT).format(incidence.getMitigatedOn()));
    } else {
      mitigatedOnKey.setVisible(false);
    }

    if (StringUtils.isNotBlank(incidence.getMitigatedBy())) {
      mitigatedByKey.setVisible(true);
      mitigatedByValue.setText(incidence.getMitigatedBy());
    } else {
      mitigatedByKey.setVisible(false);
    }

    if (StringUtils.isNotBlank(incidence.getMitigatedDescription())) {
      mitigatedDescriptionKey.setVisible(true);
      mitigatedDescriptionValue.setText(incidence.getMitigatedDescription());
    } else {
      mitigatedDescriptionKey.setVisible(false);
    }

  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  // Java method
  public native boolean isValidUrl(String url) /*-{
		var pattern = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
		return pattern.test(url);
  }-*/;

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      String riskIncidenceId = historyTokens.get(0);
      BrowserService.Util.getInstance().retrieve(RiskIncidence.class.getName(), riskIncidenceId, fieldsToReturn,
        new AsyncCallback<RiskIncidence>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(RiskIncidence result) {
            ShowRiskIncidence incidencePanel = new ShowRiskIncidence(relatedObject, result);
            callback.onSuccess(incidencePanel);
          }
        });
    } else {
      HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonEdit")
  void handleButtonEdit(ClickEvent e) {
    HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER, EditRiskIncidence.RESOLVER.getHistoryToken(),
      incidence.getId());
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    List<String> lastHistory = new ArrayList<>(LastSelectedItemsSingleton.getInstance().getLastHistory());
    LastSelectedItemsSingleton.getInstance().clearLastHistory();
    HistoryUtils.newHistory(lastHistory);
  }

}
