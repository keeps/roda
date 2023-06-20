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
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
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
      return "reporting";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static Statistics instance = null;

  public static final String cardIdentifier = "collapsable-statistics-card";

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
      layout = new HTMLWidgetWrapper("Statistics.html", null, RodaConstants.ResourcesTypes.INTERNAL,
        new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError(caught);
          }

          @Override
          public void onSuccess(Void result) {

            JavascriptUtils.runHighlighter();

            Element panelStatistic = DOM.getElementById("panelStatistic");

            if (!JavascriptUtils.accessLocalStorage(cardIdentifier)) {
              panelStatistic.setAttribute("hidden", "");
            } else {
              Element panelStatisticsButton = DOM.getElementById("reporting-action-button");
              JavascriptUtils.handleClickLeanMore(panelStatisticsButton,
                  ConfigurationManager.getString(RodaConstants.UI_SERVICE_REPORTING_URL));
            }

            Element panelCloseButton = DOM.getElementById("closeButton");
            JavascriptUtils.handleClickClose(panelCloseButton, panelStatistic, cardIdentifier);
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
