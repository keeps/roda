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

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.distributed.DistributedInstancesManagement;
import org.roda.wui.client.management.distributed.LocalInstanceManagement;
import org.roda.wui.client.process.ActionProcess;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.welcome.Help;
import org.roda.wui.common.client.BadHistoryTokenException;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 *
 */
public class Management {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER, UserLog.RESOLVER,
        NotificationRegister.RESOLVER, ActionProcess.RESOLVER, InternalProcess.RESOLVER, Statistics.RESOLVER}, false,
        callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "administration";
    }
  };

  private static Management instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static Management getInstance() {
    if (instance == null) {
      instance = new Management();
    }
    return instance;
  }

  private boolean initialized;
  private HTMLWidgetWrapper page;
  private HTMLWidgetWrapper help = null;

  private Management() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      page = new HTMLWidgetWrapper("Management.html");
    }
  }

  private HTMLWidgetWrapper getHelp() {
    if (help == null) {
      help = new HTMLWidgetWrapper("ManagementHelp.html");
    }
    return help;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      init();
      callback.onSuccess(page);
    } else {
      if (historyTokens.get(0).equals(MemberManagement.RESOLVER.getHistoryToken())) {
        MemberManagement.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(UserLog.RESOLVER.getHistoryToken())) {
        UserLog.getInstance().resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(NotificationRegister.RESOLVER.getHistoryToken())) {
        NotificationRegister.getInstance().resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(ActionProcess.RESOLVER.getHistoryToken())) {
        ActionProcess.getInstance().resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(InternalProcess.RESOLVER.getHistoryToken())) {
        InternalProcess.getInstance().resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(CreateSelectedJob.RESOLVER.getHistoryToken())) {
        CreateSelectedJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(Statistics.RESOLVER.getHistoryToken())) {
        Statistics.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(DistributedInstancesManagement.RESOLVER.getHistoryToken())) {
        DistributedInstancesManagement.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(LocalInstanceManagement.RESOLVER.getHistoryToken())) {
        LocalInstanceManagement.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(Help.RESOLVER.getHistoryToken())) {
        callback.onSuccess(getHelp());
      } else {
        callback.onFailure(new BadHistoryTokenException(historyTokens.get(0)));
      }
    }
  }
}
