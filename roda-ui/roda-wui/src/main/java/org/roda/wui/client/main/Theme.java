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
package org.roda.wui.client.main;

import java.util.Arrays;
import java.util.List;

import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class Theme {

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
      return "theme";
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }
  };

  private static Theme instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Theme getInstance() {
    if (instance == null) {
      instance = new Theme();
    }
    return instance;
  }

  private boolean initialized;

  private HTMLWidgetWrapper layout;

  private Theme() {
    initialized = false;
  }

  private void init(String htmlPage) {
    if (!initialized) {
      initialized = true;
      layout = new HTMLWidgetWrapper(htmlPage);
      layout.addStyleName("wui-home");
    }
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      init(historyTokens.get(0));
      callback.onSuccess(layout);
    } else {
      Tools.newHistory(Theme.RESOLVER);
      callback.onSuccess(null);
    }
  }

}
