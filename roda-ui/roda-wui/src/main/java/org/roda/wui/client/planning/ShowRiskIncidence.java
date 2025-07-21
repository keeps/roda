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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RiskIncidenceActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
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

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RiskIncidenceRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "riskincidence";
    }
  };

  private static ShowRiskIncidence instance = null;

  interface MyUiBinder extends UiBinder<Widget, ShowRiskIncidence> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private RiskIncidence incidence;

  @UiField
  Label incidenceId;

  @UiField
  Label instanceIdLabel;

  @UiField
  Label instanceIdValue;

  @UiField
  Label objectLabel;

  @UiField
  Anchor objectLink;

  @UiField
  Anchor riskLink;

  @UiField
  Label descriptionKey;

  @UiField
  Label descriptionValue;

  @UiField
  Label status;

  @UiField
  HTML severity;

  @UiField
  Label detectedOn;

  @UiField
  Label detectedBy;

  @UiField
  Label mitigatedOnKey;

  @UiField
  Label mitigatedOnValue;

  @UiField
  Label mitigatedByKey;

  @UiField
  Label mitigatedByValue;

  @UiField
  Label mitigatedDescriptionKey;

  @UiField
  Label mitigatedDescriptionValue;

  @UiField
  SimplePanel actionsSidebar;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  private ShowRiskIncidence() {
    this.incidence = new RiskIncidence();
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Create a new panel to view a risk incidence
   */
  public ShowRiskIncidence(RiskIncidence incidence) {
    initWidget(uiBinder.createAndBindUi(this));
    this.incidence = incidence;

    if (this.incidence.getInstanceId() != null) {
      instanceIdValue.setText(incidence.getInstanceId());
    } else {
      instanceIdLabel.setVisible(false);
      instanceIdValue.setVisible(false);
    }

    RiskIncidenceActions riskIncidenceActions = RiskIncidenceActions.get();
    SidebarUtils.toggleSidebar(contentFlowPanel, sidebarFlowPanel, riskIncidenceActions.hasAnyRoles());
    actionsSidebar.setWidget(new ActionableWidgetBuilder<>(riskIncidenceActions).withBackButton()
      .withActionCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
        @Override
        public void onSuccess(Actionable.ActionImpact result) {
          if (result.equals(Actionable.ActionImpact.DESTROYED)) {
            HistoryUtils.newHistory(ShowRisk.RESOLVER, incidence.getRiskId());
          }
        }
      }).buildListWithObjects(new ActionableObject<>(incidence)));

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

  public static ShowRiskIncidence getInstance() {
    if (instance == null) {
      instance = new ShowRiskIncidence();
    }
    return instance;
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {

      Services services = new Services("Retrieve risk incidences", "get");

      String riskIncidenceId = historyTokens.get(0);

      services.rodaEntityRestService(s -> s.findByUuid(riskIncidenceId, LocaleInfo.getCurrentLocale().getLocaleName()),
        RiskIncidence.class).whenComplete((value, error) -> {
          if (error != null) {
            callback.onFailure(error);
          } else if (value != null) {
            ShowRiskIncidence incidencePanel = new ShowRiskIncidence(value);
            callback.onSuccess(incidencePanel);
          }
        });

    } else {
      HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }
}
