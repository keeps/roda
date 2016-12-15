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
package org.roda.wui.client.planning;

import java.util.Arrays;
import java.util.List;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class Planning {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {FormatRegister.RESOLVER, RiskRegister.RESOLVER}, false,
        callback);
    }

    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    public String getHistoryToken() {
      return "planning";
    }
  };

  private static Planning instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Planning getInstance() {
    if (instance == null) {
      instance = new Planning();
    }
    return instance;
  }

  private boolean initialized;

  private HTMLWidgetWrapper page;

  private Planning() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      page = new HTMLWidgetWrapper("Planning.html");
    }
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      init();
      callback.onSuccess(page);
    } else if (historyTokens.get(0).equals(FormatRegister.RESOLVER.getHistoryToken())) {
      FormatRegister.getInstance().resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(RiskRegister.RESOLVER.getHistoryToken())) {
      RiskRegister.getInstance().resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(RiskIncidenceRegister.RESOLVER.getHistoryToken())) {
      RiskIncidenceRegister.getInstance().resolve(HistoryUtils.tail(historyTokens), callback);
    }
  }
}
