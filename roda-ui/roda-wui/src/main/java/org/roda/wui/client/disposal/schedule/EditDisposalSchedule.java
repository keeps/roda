/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.schedule;

import java.util.List;

import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
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
public class EditDisposalSchedule extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve disposal schedule", "get");
        services.disposalScheduleResource(s -> s.retrieveDisposalSchedule(historyTokens.get(0)))
          .whenComplete((result, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              if (DisposalScheduleState.INACTIVE.equals(result.getState())) {
                HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
              } else {
                EditDisposalSchedule panel = new EditDisposalSchedule(result);
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
      return "edit_disposal_schedule";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static EditDisposalSchedule instance = null;

  private static EditDisposalSchedule.MyUiBinder uiBinder = GWT.create(EditDisposalSchedule.MyUiBinder.class);
  @UiField
  Button buttonApply;
  @UiField
  Button buttonCancel;
  @UiField(provided = true)
  DisposalScheduleDataPanel disposalScheduleDataPanel;
  private DisposalSchedule disposalSchedule;

  public EditDisposalSchedule() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public EditDisposalSchedule(DisposalSchedule disposalSchedule) {
    this.disposalSchedule = disposalSchedule;
    this.disposalScheduleDataPanel = new DisposalScheduleDataPanel(disposalSchedule, true);

    initWidget(uiBinder.createAndBindUi(this));
  }

  public static EditDisposalSchedule getInstance() {
    if (instance == null) {
      instance = new EditDisposalSchedule();
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
    if (disposalScheduleDataPanel.isChanged() && disposalScheduleDataPanel.isValid()) {
      DisposalSchedule disposalScheduleUpdated = disposalScheduleDataPanel.getDisposalSchedule();
      disposalSchedule.setTitle(disposalScheduleUpdated.getTitle());
      disposalSchedule.setMandate(disposalScheduleUpdated.getMandate());
      disposalSchedule.setDescription(disposalScheduleUpdated.getDescription());
      disposalSchedule.setScopeNotes(disposalScheduleUpdated.getScopeNotes());

      Services services = new Services("Update disposal schedule", "update");
      services.disposalScheduleResource(s -> s.updateDisposalSchedule(disposalSchedule))
        .whenComplete((updatedDisposalSchedule, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, updatedDisposalSchedule.getId());
          }
        });
    } else {
      HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, disposalSchedule.getId());
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, disposalSchedule.getId());
  }

  interface MyUiBinder extends UiBinder<Widget, EditDisposalSchedule> {
  }

}
