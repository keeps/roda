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

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.EditMultipleRiskIncidenceDialog;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

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

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "risk";
    }
  };

  private static ShowRisk instance = null;
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, ShowRisk> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_ID,
    RodaConstants.RISK_NAME, RodaConstants.RISK_DESCRIPTION, RodaConstants.RISK_IDENTIFIED_ON,
    RodaConstants.RISK_IDENTIFIED_BY, RodaConstants.RISK_CATEGORY, RodaConstants.RISK_NOTES,
    RodaConstants.RISK_PRE_MITIGATION_PROBABILITY, RodaConstants.RISK_PRE_MITIGATION_IMPACT,
    RodaConstants.RISK_PRE_MITIGATION_SEVERITY, RodaConstants.RISK_POST_MITIGATION_PROBABILITY,
    RodaConstants.RISK_POST_MITIGATION_IMPACT, RodaConstants.RISK_POST_MITIGATION_SEVERITY,
    RodaConstants.RISK_PRE_MITIGATION_NOTES, RodaConstants.RISK_POST_MITIGATION_NOTES,
    RodaConstants.RISK_MITIGATION_STRATEGY, RodaConstants.RISK_MITIGATION_OWNER,
    RodaConstants.RISK_MITIGATION_OWNER_TYPE, RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE,
    RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE);

  private Risk risk;

  @UiField(provided = true)
  RiskShowPanel riskShowPanel;

  @UiField
  Button buttonHistory;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonCancel;

  @UiField
  Button buttonRemove;

  @UiField
  Button buttonProcess;

  @UiField
  Button buttonEditIncidence;

  @UiField(provided = true)
  FlowPanel facetIncidenceStatus;

  /**
   * Create a new panel to view a risk
   *
   *
   */

  public ShowRisk() {
    this.risk = new Risk();
    this.riskShowPanel = new RiskShowPanel();
    this.facetIncidenceStatus = this.riskShowPanel.getFacetIncidenceStatus();
    initWidget(uiBinder.createAndBindUi(this));
    buttonProcess.setEnabled(false);
    buttonEditIncidence.setEnabled(false);
    buttonRemove.setEnabled(false);
  }

  public ShowRisk(Risk risk) {
    this.risk = risk;
    this.riskShowPanel = new RiskShowPanel(risk, true);
    this.facetIncidenceStatus = this.riskShowPanel.getFacetIncidenceStatus();
    initWidget(uiBinder.createAndBindUi(this));
    buttonProcess.setEnabled(false);
    buttonEditIncidence.setEnabled(false);
    buttonRemove.setEnabled(false);

    BrowserService.Util.getInstance().hasRiskVersions(risk.getId(), new AsyncCallback<Boolean>() {

      @Override
      public void onFailure(Throwable caught) {
        buttonHistory.setVisible(false);
      }

      @Override
      public void onSuccess(Boolean bundle) {
        buttonHistory.setVisible(bundle.booleanValue());
      }
    });
  }

  public static ShowRisk getInstance() {
    if (instance == null) {
      instance = new ShowRisk();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      String riskId = historyTokens.get(0);
      BrowserService.Util.getInstance().retrieve(IndexedRisk.class.getName(), riskId, fieldsToReturn,
        new AsyncCallback<IndexedRisk>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(IndexedRisk result) {
            instance = new ShowRisk(result);
            callback.onSuccess(instance);
          }
        });
    } else {
      HistoryUtils.newHistory(RiskRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonHistory")
  void handleButtonHistory(ClickEvent e) {
    HistoryUtils.newHistory(RiskRegister.RESOLVER, RiskHistory.RESOLVER.getHistoryToken(), risk.getId());
  }

  @UiHandler("buttonEdit")
  void handleButtonEdit(ClickEvent e) {
    HistoryUtils.newHistory(RiskRegister.RESOLVER, EditRisk.RESOLVER.getHistoryToken(), risk.getId());
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    cancel();
  }

  @UiHandler("buttonRemove")
  void handleButtonRemove(ClickEvent e) {
    SelectedItems<RiskIncidence> incidences = riskShowPanel.getSelectedIncidences();

    BrowserService.Util.getInstance().deleteRiskIncidences(incidences, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Void result) {
        riskShowPanel.refreshList();
        Toast.showInfo(messages.removeSuccessTitle(), messages.removeAllSuccessMessage());
      }
    });
  }

  @UiHandler("buttonProcess")
  void handleButtonProcess(ClickEvent e) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(riskShowPanel.getSelectedIncidences());
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @UiHandler("buttonEditIncidence")
  void handleButtonEditIncidence(ClickEvent e) {
    final SelectedItems<RiskIncidence> selected = riskShowPanel.getSelectedIncidences();
    EditMultipleRiskIncidenceDialog dialog = new EditMultipleRiskIncidenceDialog();
    dialog.showAndCenter();
    dialog.addValueChangeHandler(new ValueChangeHandler<RiskIncidence>() {

      @Override
      public void onValueChange(ValueChangeEvent<RiskIncidence> event) {
        EditMultipleRiskIncidenceDialog editDialog = (EditMultipleRiskIncidenceDialog) event.getSource();

        BrowserService.Util.getInstance().updateMultipleIncidences(selected, editDialog.getStatus(),
          editDialog.getSeverity(), editDialog.getMitigatedOn(), editDialog.getMitigatedBy(),
          editDialog.getMitigatedDescription(), new LoadingAsyncCallback<Void>() {

            @Override
            public void onSuccessImpl(Void result) {
              riskShowPanel.incidenceList.refresh();
            }

            @Override
            public void onFailureImpl(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }
          });
      }
    });
  }

  private void cancel() {
    HistoryUtils.newHistory(RiskRegister.RESOLVER);
  }

  public void enableProcessButton(boolean enable) {
    buttonProcess.setEnabled(enable);
  }

  public void enableEditIncidenceButton(boolean enable) {
    buttonEditIncidence.setEnabled(enable);
  }

  public void enableRemoveButton(boolean enable) {
    buttonRemove.setEnabled(enable);
  }

}
