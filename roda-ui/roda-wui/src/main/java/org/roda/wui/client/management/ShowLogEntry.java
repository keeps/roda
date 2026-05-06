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

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.browse.tabs.BrowseLogEntryTabs;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.InternalLogEntryList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
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
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Management.RESOLVER}, false, callback);
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
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<LogEntry> navigationToolbar;
  @UiField
  ActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  BrowseLogEntryTabs browseTab;
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
    navigationToolbar.withObject(logEntry).build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getLogEntryBreadcrumbs(logEntry));

    actionsToolbar.setLabel(messages.showLogEntryTitle());
    browseTab.init(logEntry);
    title.setText(StringUtils.isNotBlank(logEntry.getId()) ? logEntry.getId() : logEntry.getUUID());
    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse browse-file browse_main_panel");

    if (logEntry.getAuditLogRequestHeaders() != null) {
      Label relatedAuditLogs = new Label();
      relatedAuditLogs.addStyleName("h5");
      relatedAuditLogs.setText(messages.relatedAuditLogs());
      expandedAuditLogs.add(relatedAuditLogs);

      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.LOG_REQUEST_HEADER_UUID,
              logEntry.getAuditLogRequestHeaders().getUuid()));

      ListBuilder<LogEntry> auditLogListBuilder = new ListBuilder<>(() -> new InternalLogEntryList(),
              new AsyncTableCellOptions<>(LogEntry.class, "AuditLogs_triggeredLogs").withFilter(filter)
                      .withSummary(messages.listOfAIPs()).bindOpener());

      SearchWrapper aipsSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(auditLogListBuilder);
      expandedAuditLogsList.setWidget(aipsSearchWrapper);
      expandedAuditLogsList.setVisible(true);
    }

  }

  interface MyUiBinder extends UiBinder<Widget, ShowLogEntry> {
  }

}
