/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.dialogs.ClosableDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class EditRisk extends ClosableDialog {

  interface MyUiBinder extends UiBinder<Widget, EditRisk> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final String riskId;
  private final AsyncCallback<Risk> callback;
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  RiskDataPanel riskDataPanel;

  /**
   * Create a panel to edit Risk
   *
   * @param risk
   * 
   */
  public EditRisk(IndexedRisk risk, RiskDataPanel.RiskSectionMode riskSectionMode, AsyncCallback<Risk> callback) {
    this.riskId = risk.getId();
    this.callback = callback;
    this.riskDataPanel = new RiskDataPanel(risk, true, riskSectionMode);

    setWidget(uiBinder.createAndBindUi(this));
    this.addStyleName("risk-edit-dialog");
    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);
    setEditSectionTitle(riskSectionMode);
  }

  private void setEditSectionTitle(RiskDataPanel.RiskSectionMode riskSectionMode) {
    String title;
    if (riskSectionMode == RiskDataPanel.RiskSectionMode.DETAILS) {
      title = messages.editRiskDetailsTitle();
    } else if (riskSectionMode == RiskDataPanel.RiskSectionMode.PRE_MITIGATION) {
      title = messages.editRiskPreMitigationTitle();
    } else if (riskSectionMode == RiskDataPanel.RiskSectionMode.MITIGATION) {
      title = messages.editRiskMitigationTitle();
    } else if (riskSectionMode == RiskDataPanel.RiskSectionMode.POST_MITIGATION) {
      title = messages.editRiskPostMitigationTitle();
    } else {
      title = messages.editRiskTitle();
    }
    setText(title);
  }

  @UiHandler("buttonApply")
  void buttonApplyClick(ClickEvent event) {
    if (!riskDataPanel.isChanged()) {
      hide();
      callback.onFailure(null);
      return;
    }

    if (!riskDataPanel.isValid()) {
      return;
    }

    Risk updatedRisk = riskDataPanel.getRisk();
    updatedRisk.setId(riskId);
    hide();
    callback.onSuccess(updatedRisk);
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    hide();
    callback.onFailure(null);
  }

  public void showAndCenter() {
    if (getElement().getClientWidth() < 800) {
      setWidth(getElement().getClientWidth() + "px");
    }
    show();
    center();
  }

}
