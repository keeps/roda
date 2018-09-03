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
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class UserLog extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {UserLog.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "log";
    }
  };

  private static UserLog instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static UserLog getInstance() {
    if (instance == null) {
      instance = new UserLog();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, UserLog> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel userLogDescription;

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public UserLog() {
    // TODO tmp why no bindOpener?
    ListBuilder<LogEntry> logEntryListBuilder = new ListBuilder<>(() -> new LogEntryList(),
      new AsyncTableCellOptions<>(LogEntry.class, "UserLog_logEntries"));
    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(logEntryListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    userLogDescription.add(new HTMLWidgetWrapper("UserLogDescription.html"));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      searchWrapper.resetToDefaultFilter(LogEntry.class);
      callback.onSuccess(this);
    } else if (historyTokens.size() > 1 && ShowLogEntry.RESOLVER.getHistoryToken().equals(historyTokens.get(0))) {
      ShowLogEntry.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1) {
      final String aipId = historyTokens.get(0);
      searchWrapper.setFilter(LogEntry.class,
        new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, aipId)));
      callback.onSuccess(this);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
