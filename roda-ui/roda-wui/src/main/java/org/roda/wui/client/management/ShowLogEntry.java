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
package org.roda.wui.client.management;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

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

import config.i18n.client.ClientMessages;

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

        BrowserService.Util.getInstance().retrieve(LogEntry.class.getName(), logEntryId, fieldsToReturn,
          new AsyncCallback<LogEntry>() {

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
        HistoryUtils.newHistory(UserLog.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return ListUtils.concat(UserLog.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "logentry";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowLogEntry> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.LOG_ID,
    RodaConstants.LOG_ACTION_COMPONENT, RodaConstants.LOG_ACTION_METHOD, RodaConstants.LOG_ADDRESS,
    RodaConstants.LOG_DATETIME, RodaConstants.LOG_RELATED_OBJECT_ID, RodaConstants.LOG_USERNAME,
    RodaConstants.LOG_PARAMETERS, RodaConstants.LOG_STATE);

  @UiField
  Label logIdLabel, logIdValue;

  @UiField
  Label logComponentLabel, logComponentValue;

  @UiField
  Label logMethodLabel, logMethodValue;

  @UiField
  Label logAddressLabel, logAddressValue;

  @UiField
  Label logDatetimeLabel, logDatetimeValue;

  @UiField
  Label logRelatedObjectLabel, logRelatedObjectValue;

  @UiField
  Label logUsernameLabel, logUsernameValue;

  @UiField
  Label logParametersLabel;

  @UiField
  FlowPanel logParametersValue;

  @UiField
  Label logStateLabel;

  @UiField
  HTML logStateValue;

  @UiField
  Button buttonCancel;

  /**
   * Create a new panel to view a log entry
   *
   */
  public ShowLogEntry(LogEntry logEntry) {
    initWidget(uiBinder.createAndBindUi(this));

    logIdValue.setText(logEntry.getId());
    logIdLabel.setVisible(StringUtils.isNotBlank(logEntry.getId()));
    logIdValue.setVisible(StringUtils.isNotBlank(logEntry.getId()));

    logComponentValue.setText(logEntry.getActionComponent());
    logComponentLabel.setVisible(StringUtils.isNotBlank(logEntry.getActionComponent()));
    logComponentValue.setVisible(StringUtils.isNotBlank(logEntry.getActionComponent()));

    logMethodValue.setText(logEntry.getActionMethod());
    logMethodLabel.setVisible(StringUtils.isNotBlank(logEntry.getActionMethod()));
    logMethodValue.setVisible(StringUtils.isNotBlank(logEntry.getActionMethod()));

    logAddressValue.setText(logEntry.getAddress());
    logAddressLabel.setVisible(StringUtils.isNotBlank(logEntry.getAddress()));
    logAddressValue.setVisible(StringUtils.isNotBlank(logEntry.getAddress()));

    logDatetimeValue.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(logEntry.getDatetime()));
    logDatetimeLabel.setVisible(logEntry.getDatetime() != null);
    logDatetimeValue.setVisible(logEntry.getDatetime() != null);

    logRelatedObjectValue.setText(logEntry.getRelatedObjectID());
    logRelatedObjectLabel.setVisible(StringUtils.isNotBlank(logEntry.getRelatedObjectID()));
    logRelatedObjectValue.setVisible(StringUtils.isNotBlank(logEntry.getRelatedObjectID()));

    logUsernameValue.setText(logEntry.getUsername());
    logUsernameLabel.setVisible(StringUtils.isNotBlank(logEntry.getUsername()));
    logUsernameValue.setVisible(StringUtils.isNotBlank(logEntry.getUsername()));

    List<LogEntryParameter> parameters = logEntry.getParameters();

    if (parameters != null && parameters.size() > 0) {
      for (LogEntryParameter par : parameters) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.logParameter(par.getName(), par.getValue()));
        logParametersValue.add(parPanel);
      }
      logParametersLabel.setVisible(true);
      logParametersValue.setVisible(true);
    } else {
      logParametersLabel.setVisible(false);
      logParametersValue.setVisible(false);
    }

    logStateValue.setHTML(HtmlSnippetUtils.getLogEntryStateHtml(logEntry.getState()));
    logStateLabel.setVisible(logEntry.getState() != null);
    logStateValue.setVisible(logEntry.getState() != null);

  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(UserLog.RESOLVER);
  }

}
