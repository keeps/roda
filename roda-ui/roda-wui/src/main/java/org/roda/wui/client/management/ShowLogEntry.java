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

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
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
        Services services = new Services("Retrieve audit log", "get");
        services
          .rodaEntityRestService(s -> s.findByUuid(historyTokens.get(0), LocaleInfo.getCurrentLocale().getLocaleName()),
            LogEntry.class)
          .whenComplete((logEntry, throwable) -> {
            if (throwable == null) {
              ShowLogEntry logEntryPanel = new ShowLogEntry(logEntry);
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

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(UserLog.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "logentry";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowLogEntry> {
  }

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Label logIdLabel;
  @UiField
  Label logIdValue;

  @UiField
  Label logComponentLabel;
  @UiField
  Label logComponentValue;
  @UiField
  Label logMethodLabel;
  @UiField
  Label logMethodValue;
  @UiField
  Label logAddressLabel;
  @UiField
  Label logAddressValue;
  @UiField
  Label logDatetimeLabel;
  @UiField
  Label logDatetimeValue;
  @UiField
  Label logDurationValue;
  @UiField
  Label logRelatedObjectLabel;
  @UiField
  Label logRelatedObjectValue;
  @UiField
  Label logUsernameLabel;
  @UiField
  Label logUsernameValue;
  @UiField
  Label logParametersLabel;
  @UiField
  FlowPanel logParametersValue;
  @UiField
  Label logStateLabel;
  @UiField
  HTML logStateValue;
  @UiField
  Label logInstanceIdLabel;
  @UiField
  Label logInstanceIdValue;
  @UiField
  SimplePanel expandedAuditLogs;
  @UiField
  SimplePanel expandedAuditLogsList;

  /**
   * Create a new panel to view a log entry
   *
   */
  public ShowLogEntry(LogEntry logEntry) {
    initWidget(uiBinder.createAndBindUi(this));

    logIdValue.setText(logEntry.getId());
    logIdLabel.setVisible(StringUtils.isNotBlank(logEntry.getId()));
    logIdValue.setVisible(StringUtils.isNotBlank(logEntry.getId()));

    logInstanceIdValue.setText(logEntry.getInstanceId());
    logInstanceIdLabel.setVisible(StringUtils.isNotBlank(logEntry.getInstanceId()));
    logInstanceIdValue.setVisible(StringUtils.isNotBlank(logEntry.getInstanceId()));

    logComponentValue.setText(logEntry.getActionComponent());
    logComponentLabel.setVisible(StringUtils.isNotBlank(logEntry.getActionComponent()));
    logComponentValue.setVisible(StringUtils.isNotBlank(logEntry.getActionComponent()));

    logMethodValue.setText(logEntry.getActionMethod());
    logMethodLabel.setVisible(StringUtils.isNotBlank(logEntry.getActionMethod()));
    logMethodValue.setVisible(StringUtils.isNotBlank(logEntry.getActionMethod()));

    logAddressValue.setText(logEntry.getAddress());
    logAddressLabel.setVisible(StringUtils.isNotBlank(logEntry.getAddress()));
    logAddressValue.setVisible(StringUtils.isNotBlank(logEntry.getAddress()));

    logDatetimeValue.setText(Humanize.formatDateTime(logEntry.getDatetime()));
    logDatetimeLabel.setVisible(logEntry.getDatetime() != null);
    logDatetimeValue.setVisible(logEntry.getDatetime() != null);

    logDurationValue.setText(Humanize.durationMillisToShortDHMS(logEntry.getDuration()));

    logRelatedObjectValue.setText(logEntry.getRelatedObjectID());
    logRelatedObjectLabel.setVisible(StringUtils.isNotBlank(logEntry.getRelatedObjectID()));
    logRelatedObjectValue.setVisible(StringUtils.isNotBlank(logEntry.getRelatedObjectID()));

    logUsernameValue.setText(logEntry.getUsername());
    logUsernameLabel.setVisible(StringUtils.isNotBlank(logEntry.getUsername()));
    logUsernameValue.setVisible(StringUtils.isNotBlank(logEntry.getUsername()));

    List<LogEntryParameter> parameters = logEntry.getParameters();

    if (parameters != null && !parameters.isEmpty()) {
      for (LogEntryParameter par : parameters) {
        HTML parPanel = new HTML();
        parPanel.setHTML(SafeHtmlUtils.fromString(messages.logParameter(par.getName(), par.getValue())));
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

    expandedAuditLogsList.setVisible(false);

    GWT.log(logEntry.getAuditLogRequestHeaders().toString());

    if (logEntry.getAuditLogRequestHeaders() != null) {
      Label relatedAuditLogs = new Label();
      relatedAuditLogs.addStyleName("h5");
      relatedAuditLogs.setText(messages.relatedAuditLogs());
      expandedAuditLogs.add(relatedAuditLogs);

      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.LOG_REQUEST_HEADER_UUID,
        logEntry.getAuditLogRequestHeaders().getUuid()));

      ListBuilder<LogEntry> auditLogListBuilder = new ListBuilder<>(() -> new LogEntryList(false),
        new AsyncTableCellOptions<>(LogEntry.class, "AuditLogs_triggeredLogs").withFilter(filter)
          .withSummary(messages.listOfAIPs()).bindOpener());

      SearchWrapper aipsSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(auditLogListBuilder);
      expandedAuditLogsList.setWidget(aipsSearchWrapper);
      expandedAuditLogsList.setVisible(true);
    }
  }

}
