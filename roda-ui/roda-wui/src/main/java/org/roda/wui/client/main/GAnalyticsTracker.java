/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GAnalyticsTracker {

  /**
   * constructor - nothing to do
   */
  public GAnalyticsTracker() {
  }

  private static String ACCOUNT_ID = null;

  private static void getAccountId(final AsyncCallback<String> callback) {
    if (ACCOUNT_ID == null) {
      BrowserService.Util.getInstance().retrieveGoogleAnalyticsAccount(new AsyncCallback<String>() {

        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(String result) {
          ACCOUNT_ID = result;
          callback.onSuccess(ACCOUNT_ID);
        }
      });
    } else {
      callback.onSuccess(ACCOUNT_ID);
    }
  }

  /**
   * track an event
   * 
   * @param historyToken
   */
  public static void track(String historyToken) {

    String baseURL = GWT.getHostPageBaseURL();

    String urlPath = baseURL.substring(baseURL.indexOf('/', "https://".length() + 1));
    final String page = urlPath + "#" + historyToken;

    getAccountId(new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        // do nothing
      }

      public void onSuccess(String accountId) {
        if (!"".equals(accountId)) {
          trackGoogleAnalytics(page, accountId);
        }
      }
    });

  }

  /**
   * trigger google analytic native js
   * 
   * https://developers.google.com/analytics/devguides/collection/gajs/methods/
   * gaJSApiEventTracking?hl=en-US
   * 
   * @param historyToken
   */
  public static native void trackGoogleAnalytics(String historyToken, String accountId) /*-{
		try {

			// setup tracking object with account
			//var pageTracker = $wnd._gat._getTracker(accountId);

			//pageTracker._setRemoteServerMode();

			// turn on anchor observing
			//pageTracker._setAllowAnchor(true)

			// send event to google server
			//pageTracker._trackPageview(historyToken);

			$wnd._gaq.push([ '_setAccount', accountId ]);
			$wnd._gaq.push([ '_trackPageview', historyToken ]);

		} catch (err) {

			// debug
			alert('FAILURE: to send in event to google analytics: ' + err);
		}
  }-*/;
}