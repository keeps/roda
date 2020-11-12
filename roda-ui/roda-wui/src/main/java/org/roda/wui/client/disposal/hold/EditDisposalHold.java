package org.roda.wui.client.disposal.hold;

import java.util.List;

import org.roda.core.data.exceptions.DisposalHoldAlreadyExistsException;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldState;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.DisposalPolicy;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.server.browse.BrowserServiceImpl;

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
        BrowserService.Util.getInstance().retrieveDisposalHold(historyTokens.get(0), new AsyncCallback<DisposalHold>() {
          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(DisposalHold result) {
            if (DisposalHoldState.LIFTED.equals(result.getState())) {
              HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
            } else {
              EditDisposalHold panel = new EditDisposalHold(result);
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

  interface MyUiBinder extends UiBinder<Widget, EditDisposalHold> {
  }

  private static EditDisposalHold instance = null;

  private static EditDisposalHold.MyUiBinder uiBinder = GWT.create(EditDisposalHold.MyUiBinder.class);

  private DisposalHold disposalHold;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static EditDisposalHold getInstance() {
    if (instance == null) {
      instance = new EditDisposalHold();
    }
    return instance;
  }

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  DisposalHoldDataPanel disposalHoldDataPanel;

  public EditDisposalHold() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public EditDisposalHold(DisposalHold disposalHold) {
    this.disposalHold = disposalHold;
    this.disposalHoldDataPanel = new DisposalHoldDataPanel(disposalHold, true);

    initWidget(uiBinder.createAndBindUi(this));
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
      BrowserServiceImpl.Util.getInstance().updateDisposalHold(disposalHold, new AsyncCallback<DisposalHold>() {
        @Override
        public void onFailure(Throwable caught) {
          errorMessage(caught);
        }

        @Override
        public void onSuccess(DisposalHold disposalHold) {
          HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, disposalHold.getId());
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

  private void errorMessage(Throwable caught) {
    if (caught instanceof DisposalHoldAlreadyExistsException) {
      Toast.showError(messages.createDisposalHoldAlreadyExists(disposalHold.getTitle()));
    } else {
      Toast.showError(messages.createDisposalHoldFailure(caught.getMessage()));
    }
  }
}
