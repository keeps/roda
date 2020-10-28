package org.roda.wui.client.disposal.schedule;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import org.roda.core.data.exceptions.DisposalHoldAlreadyExistsException;
import org.roda.core.data.exceptions.DisposalScheduleAlreadyExistsException;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.DisposalPolicy;
import org.roda.wui.client.disposal.hold.DisposalHoldDataPanel;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.server.browse.BrowserServiceImpl;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class EditDisposalSchedule extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        BrowserService.Util.getInstance().retrieveDisposalSchedule(historyTokens.get(0),
          new AsyncCallback<DisposalSchedule>() {
            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            @Override
            public void onSuccess(DisposalSchedule result) {
              EditDisposalSchedule panel = new EditDisposalSchedule(result);
              callback.onSuccess(panel);
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

  interface MyUiBinder extends UiBinder<Widget, EditDisposalSchedule> {
  }

  private static EditDisposalSchedule instance = null;

  private static EditDisposalSchedule.MyUiBinder uiBinder = GWT.create(EditDisposalSchedule.MyUiBinder.class);

  private DisposalSchedule disposalSchedule;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static EditDisposalSchedule getInstance() {
    if (instance == null) {
      instance = new EditDisposalSchedule();
    }
    return instance;
  }

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  DisposalScheduleDataPanel disposalScheduleDataPanel;

  public EditDisposalSchedule(){
    initWidget(uiBinder.createAndBindUi(this));
  }

  public EditDisposalSchedule(DisposalSchedule disposalSchedule) {
    this.disposalSchedule = disposalSchedule;
    this.disposalScheduleDataPanel = new DisposalScheduleDataPanel(disposalSchedule, true);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if(disposalScheduleDataPanel.isChanged() && disposalScheduleDataPanel.isValid()) {
      DisposalSchedule disposalScheduleUpdated = disposalScheduleDataPanel.getDisposalSchedule();
      disposalSchedule.setTitle(disposalScheduleUpdated.getTitle());
      disposalSchedule.setMandate(disposalScheduleUpdated.getMandate());
      disposalSchedule.setDescription(disposalScheduleUpdated.getDescription());
      disposalSchedule.setScopeNotes(disposalScheduleUpdated.getScopeNotes());
      BrowserServiceImpl.Util.getInstance().updateDisposalSchedule(disposalSchedule, new AsyncCallback<DisposalSchedule>() {
        @Override
        public void onFailure(Throwable caught) {
          errorMessage(caught);
        }

        @Override
        public void onSuccess(DisposalSchedule disposalSchedule) {
          HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
        }
      });
    }else{
      HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER,disposalSchedule.getId());
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof DisposalScheduleAlreadyExistsException) {
      Toast.showError(messages.createDisposalScheduleAlreadyExists(disposalSchedule.getTitle()));
    } else {
      Toast.showError(messages.createDisposalScheduleFailure(caught.getMessage()));
    }
  }

}
