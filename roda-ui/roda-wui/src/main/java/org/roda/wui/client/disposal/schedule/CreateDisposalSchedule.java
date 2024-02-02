/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.schedule;

import java.util.List;

import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
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
public class CreateDisposalSchedule extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateDisposalSchedule createDisposalSchedule = new CreateDisposalSchedule(new DisposalSchedule());
      callback.onSuccess(createDisposalSchedule);
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
      return "create_disposal_schedule";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static CreateDisposalSchedule.MyUiBinder uiBinder = GWT.create(CreateDisposalSchedule.MyUiBinder.class);
  @UiField
  Button buttonSave;
  @UiField
  Button buttonCancel;
  @UiField(provided = true)
  DisposalScheduleDataPanel disposalScheduleDataPanel;
  private DisposalSchedule disposalSchedule;

  public CreateDisposalSchedule(DisposalSchedule disposalSchedule) {
    this.disposalSchedule = disposalSchedule;

    this.disposalScheduleDataPanel = new DisposalScheduleDataPanel(disposalSchedule, false);
    this.disposalScheduleDataPanel.setDisposalSchedule(disposalSchedule);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (disposalScheduleDataPanel.isValid()) {
      disposalSchedule = disposalScheduleDataPanel.getDisposalSchedule();

      BrowserService.Util.getInstance().createDisposalSchedule(disposalSchedule,
        new NoAsyncCallback<DisposalSchedule>() {
          @Override
          public void onSuccess(DisposalSchedule createdDisposalSchedule) {
            HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
          }

        });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
  }

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalSchedule> {
  }

}
