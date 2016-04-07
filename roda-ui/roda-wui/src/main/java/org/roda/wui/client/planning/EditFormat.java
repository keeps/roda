package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.formats.Format;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.FormatMessages;

public class EditFormat extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String formatId = historyTokens.get(0);
        UserManagementService.Util.getInstance().retrieveFormat(formatId, new AsyncCallback<Format>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(Format format) {
            EditFormat editFormat = new EditFormat(format);
            callback.onSuccess(editFormat);
          }
        });
      } else {
        Tools.newHistory(FormatRegister.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(FormatRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "edit_format";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditFormat> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private Format format;
  private static FormatMessages messages = GWT.create(FormatMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  FormatDataPanel formatDataPanel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public EditFormat(Format format) {
    this.format = format;

    this.formatDataPanel = new FormatDataPanel(true, false);
    this.formatDataPanel.setFormat(format);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (formatDataPanel.isChanged()) {
      if (formatDataPanel.isValid()) {
        String agentId = format.getId();
        format = formatDataPanel.getFormat();
        format.setId(agentId);
        UserManagementService.Util.getInstance().modifyFormat(format, new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            errorMessage(caught);
          }

          @Override
          public void onSuccess(Void result) {
            Tools.newHistory(ShowFormat.RESOLVER, format.getId());
          }

        });
      }
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(FormatRegister.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editFormatNotFound(format.getName()));
      cancel();
    } else {
      Toast.showError(messages.editFormatFailure(caught.getMessage()));
    }
  }

}
