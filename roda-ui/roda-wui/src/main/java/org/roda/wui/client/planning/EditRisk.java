/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class EditRisk extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services service = new Services("Retrieve indexed risk", "get");
        String riskId = historyTokens.get(0);

        service.rodaEntityRestService(s -> s.findByUuid(riskId, LocaleInfo.getCurrentLocale().getLocaleName()), IndexedRisk.class).whenComplete((value, error) -> {
          if (error != null) {
            callback.onFailure(error);
          } else if (value != null) {
            EditRisk editRisk = new EditRisk(value);
            callback.onSuccess(editRisk);
          }
        });

      } else {
        HistoryUtils.newHistory(RiskRegister.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_risk";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditRisk> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_ID,
    RodaConstants.RISK_NAME, RodaConstants.RISK_DESCRIPTION, RodaConstants.RISK_IDENTIFIED_ON,
    RodaConstants.RISK_IDENTIFIED_BY, RodaConstants.RISK_CATEGORIES, RodaConstants.RISK_NOTES,
    RodaConstants.RISK_PRE_MITIGATION_PROBABILITY, RodaConstants.RISK_PRE_MITIGATION_IMPACT,
    RodaConstants.RISK_PRE_MITIGATION_SEVERITY, RodaConstants.RISK_POST_MITIGATION_PROBABILITY,
    RodaConstants.RISK_POST_MITIGATION_IMPACT, RodaConstants.RISK_POST_MITIGATION_SEVERITY,
    RodaConstants.RISK_PRE_MITIGATION_NOTES, RodaConstants.RISK_POST_MITIGATION_NOTES,
    RodaConstants.RISK_MITIGATION_STRATEGY, RodaConstants.RISK_MITIGATION_OWNER,
    RodaConstants.RISK_MITIGATION_OWNER_TYPE, RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE,
    RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE);

  private Risk risk;
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private int incidences = 0;

  @UiField
  Button buttonApply;

  @UiField
  Button buttonRemove;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  RiskDataPanel riskDataPanel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public EditRisk(IndexedRisk risk) {
    this.risk = risk;
    this.incidences = risk.getIncidencesCount();
    this.riskDataPanel = new RiskDataPanel(risk, true);
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (riskDataPanel.isChanged() && riskDataPanel.isValid()) {
      Services services = new Services("Edit indexed risk", "update");
      final String riskId = risk.getId();
      risk = riskDataPanel.getRisk();
      risk.setId(riskId);

      services.riskResource(s -> s.updateRisk(risk)).whenComplete((value, error) -> {
        if (error != null) {
          errorMessage(error);
        } else {
          HistoryUtils.newHistory(ShowRisk.RESOLVER, riskId);
        }
      });
    } else {
      HistoryUtils.newHistory(ShowRisk.RESOLVER, risk.getId());
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    Services service = new Services("Remove risk", "remove");
    service
      .riskResource(s -> s
        .deleteRisk(new SelectedItemsListRequest(Collections.singletonList(risk.getUUID()))))
      .whenComplete((value, error) -> {
        if (error != null) {
          HistoryUtils.newHistory(InternalProcess.RESOLVER);
        } else if (value != null) {
          Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
              Timer timer = new Timer() {
                @Override
                public void run() {
                  HistoryUtils.newHistory(RiskRegister.RESOLVER);
                }
              };

              timer.schedule(RodaConstants.ACTION_TIMEOUT);
            }

            @Override
            public void onSuccess(final Void nothing) {
              HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
            }
          });
        }
      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowRisk.RESOLVER, risk.getId());
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editRiskNotFound(risk.getName()));
      cancel();
    } else {
      AsyncCallbackUtils.defaultFailureTreatment(caught);
    }
  }

}
