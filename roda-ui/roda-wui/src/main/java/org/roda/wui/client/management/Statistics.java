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

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class Statistics {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Statistics.RESOLVER}, false, callback);
    }

    @Override
    public String getHistoryToken() {
      return "statistics";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static Statistics instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Statistics getInstance() {
    if (instance == null) {
      instance = new Statistics();
    }
    return instance;
  }

  private boolean initialized;

  private HTMLWidgetWrapper layout;

  private Statistics() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      JavascriptUtils.expose("locale", LocaleInfo.getCurrentLocale().getLocaleName());
      layout = new HTMLWidgetWrapper("Statistics.html", new AsyncCallback<Void>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(caught);
        }

        @Override
        public void onSuccess(Void result) {
          JavascriptUtils.runHighlighter();
        }
      });
      layout.addStyleName("wui-home");
    }
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      init();
      callback.onSuccess(layout);
    } else {
      HistoryUtils.newHistory(Statistics.RESOLVER.getHistoryPath());
      callback.onSuccess(null);
    }
  }

}
