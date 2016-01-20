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
package org.roda.wui.management.user.client;

import java.util.List;

import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.LogEntryParameter;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.CommonMessages;

/**
 * @author Luis Faria
 * 
 */
public class ShowLogEntry extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String logEntryId = historyTokens.get(0);
        UserManagementService.Util.getInstance().retrieveLogEntry(logEntryId, new AsyncCallback<LogEntry>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(LogEntry result) {
            ShowLogEntry logEntryPanel = new ShowLogEntry(result);
            callback.onSuccess(logEntryPanel);
          }
        });
      } else {
        Tools.newHistory(UserLog.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(UserLog.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "logentry";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowLogEntry> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static CommonMessages messages = GWT.create(CommonMessages.class);

  @UiField
  Label logId;

  @UiField
  Label logComponent;

  @UiField
  Label logMethod;

  @UiField
  Label logAddress;

  @UiField
  Label logDatetime;

  @UiField
  Label logRelatedObjectLabel;

  @UiField
  Label logRelatedObject;

  @UiField
  Label logUsername;

  @UiField
  Label logParametersLabel;

  @UiField
  FlowPanel logParameters;

  @UiField
  Button buttonCancel;

  /**
   * Create a new panel to view a log entry
   * 
   */
  public ShowLogEntry(LogEntry logEntry) {
    initWidget(uiBinder.createAndBindUi(this));

    logId.setText(logEntry.getId());
    logComponent.setText(logEntry.getActionComponent());
    logMethod.setText(logEntry.getActionMethod());
    logAddress.setText(logEntry.getAddress());
    logDatetime.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(logEntry.getDatetime()));
    logRelatedObjectLabel.setVisible(logEntry.getRelatedObjectID() != null && !logEntry.getRelatedObjectID().isEmpty());
    logRelatedObject.setText(logEntry.getRelatedObjectID());
    logUsername.setText(logEntry.getUsername());

    List<LogEntryParameter> parameters = logEntry.getParameters();

    logParametersLabel.setVisible(parameters != null && !parameters.isEmpty());

    if (parameters != null) {
      for (LogEntryParameter par : parameters) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.logParameter(par.getName(), par.getValue()));
        logParameters.add(parPanel);
      }
    }

  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(UserLog.RESOLVER);
  }

}
