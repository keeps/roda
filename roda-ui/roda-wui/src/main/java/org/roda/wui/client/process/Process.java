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
package org.roda.wui.client.process;

import java.util.Arrays;
import java.util.List;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.ingest.process.ShowJob;
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
public class Process {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Process.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    public String getHistoryToken() {
      return "process";
    }
  };

  private static Process instance = null;
  private HTMLWidgetWrapper page;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Process getInstance() {
    if (instance == null) {
      instance = new Process();
    }
    return instance;
  }

  private Process() {
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      callback.onSuccess(page);
    } else if (historyTokens.get(0).equals(CreateJob.RESOLVER.getHistoryToken())) {
      CreateJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowJob.RESOLVER.getHistoryToken())) {
      ShowJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateActionJob.RESOLVER.getHistoryToken())) {
      CreateActionJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      callback.onFailure(new BadHistoryTokenException(historyTokens.get(0)));
    }
  }
}
