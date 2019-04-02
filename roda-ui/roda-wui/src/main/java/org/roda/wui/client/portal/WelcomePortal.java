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
package org.roda.wui.client.portal;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class WelcomePortal {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      callback.onSuccess(Boolean.TRUE);
    }

    @Override
    public String getHistoryToken() {
      return "welcome";
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }
  };

  private static WelcomePortal instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static WelcomePortal getInstance() {
    if (instance == null) {
      instance = new WelcomePortal();
    }
    return instance;
  }

  private boolean initialized;
  private HTMLWidgetWrapper layout;

  private WelcomePortal() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      layout = new HTMLWidgetWrapper("WelcomePortal.html");
      layout.addStyleName("wui-home");
    }
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      init();
      callback.onSuccess(layout);
    } else {
      HistoryUtils.newHistory(WelcomePortal.RESOLVER);
      callback.onSuccess(null);
    }
  }
}
