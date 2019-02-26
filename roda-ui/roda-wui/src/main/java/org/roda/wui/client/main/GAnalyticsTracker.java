/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import org.roda.core.data.common.RodaConstants;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.StringUtils;

public class GAnalyticsTracker {

  private static String accountId = null;

  /**
   * constructor - nothing to do
   */
  private GAnalyticsTracker() {
    // do nothing
  }

  /**
   * track an event
   * 
   * @param historyToken
   */
  public static void track(String historyToken) {

    if (StringUtils.isBlank(accountId)) {
      accountId = ConfigurationManager.getString(RodaConstants.UI_GOOGLE_ANALYTICS_CODE_PROPERTY);
      if (StringUtils.isNotBlank(accountId)) {
        setAccount(accountId);
      }
    }

    if (StringUtils.isNotBlank(accountId)) {
      pageview("/#" + historyToken);
    }
  }

  private static native void setAccount(String accountId)
  /*-{
		$wnd.ga('create', accountId, 'auto');
  }-*/;

  /**
   * https://developers.google.com/analytics/devguides/collection/analyticsjs/single-page-applications
   *
   * @param page
   */
  private static native void pageview(String page)
  /*-{
		$wnd.ga('set', 'page', page);
		$wnd.ga('send', 'pageview');
  }-*/;

}
