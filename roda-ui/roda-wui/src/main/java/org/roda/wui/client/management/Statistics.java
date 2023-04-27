/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.management;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import java.util.List;

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
      return "statistics";
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

            Boolean reportingActive = ConfigurationManager.getBoolean(false, RodaConstants.UI_REPORTING_STATUS);
            Element panelStatistic = DOM.getElementById("panelStatistic");

            if (reportingActive) {
              panelStatistic.setAttribute("hidden", "");
            } else {
              if (!JavascriptUtils.accessLocalStorage(cardIdentifier)) {
                panelStatistic.setAttribute("hidden", "");
              } else {
                Element panelStatisticsButton = DOM.getElementById("reporting-action-button");
                JavascriptUtils.handleClickLeanMore(panelStatisticsButton,
                  ConfigurationManager.getString(RodaConstants.UI_DROPFOLDER_URL));
              }

              Element panelCloseButton = DOM.getElementById("closeButton");
              JavascriptUtils.handleClickClose(panelCloseButton, panelStatistic, cardIdentifier);
            }
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
