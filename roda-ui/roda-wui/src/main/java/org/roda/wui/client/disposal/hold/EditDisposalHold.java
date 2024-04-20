/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.hold;

import java.util.List;

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class EditDisposalHold extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve disposal hold", "get");
        services.disposalHoldResource(s -> s.retrieveDisposalHold(historyTokens.get(0)))
          .whenComplete((hold, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              if (DisposalHoldState.LIFTED.equals(hold.getState())) {
                HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
              } else {
                EditDisposalHold panel = new EditDisposalHold(hold);
                callback.onSuccess(panel);
              }
            }
          });
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DisposalPolicy.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalPolicy.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_disposal_hold";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static EditDisposalHold instance = null;

  private static EditDisposalHold.MyUiBinder uiBinder = GWT.create(EditDisposalHold.MyUiBinder.class);
  @UiField
  Button buttonApply;
  @UiField
  Button buttonCancel;
  @UiField(provided = true)
  DisposalHoldDataPanel disposalHoldDataPanel;
  private DisposalHold disposalHold;

  public EditDisposalHold() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public EditDisposalHold(DisposalHold disposalHold) {
    this.disposalHold = disposalHold;
    this.disposalHoldDataPanel = new DisposalHoldDataPanel(disposalHold, true);

    initWidget(uiBinder.createAndBindUi(this));
  }

  public static EditDisposalHold getInstance() {
    if (instance == null) {
      instance = new EditDisposalHold();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (disposalHoldDataPanel.isChanged() && disposalHoldDataPanel.isValid()) {
      DisposalHold disposalHoldUpdated = disposalHoldDataPanel.getDisposalHold();
      disposalHold.setTitle(disposalHoldUpdated.getTitle());
      disposalHold.setMandate(disposalHoldUpdated.getMandate());
      disposalHold.setDescription(disposalHoldUpdated.getDescription());
      disposalHold.setScopeNotes(disposalHoldUpdated.getScopeNotes());
      Services services = new Services("Update disposal hold", "update");
      services.disposalHoldResource(s -> s.updateDisposalHold(disposalHold)).whenComplete((hold, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, hold.getId());
        }
      });
    } else {
      HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, disposalHold.getId());
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, disposalHold.getId());
  }

  interface MyUiBinder extends UiBinder<Widget, EditDisposalHold> {
  }
}
