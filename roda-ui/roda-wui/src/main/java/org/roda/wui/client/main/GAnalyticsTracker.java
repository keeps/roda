/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GAnalyticsTracker {

  private static String accountId = null;

  /**
   * constructor - nothing to do
   */
  private GAnalyticsTracker() {
    // do nothing
  }

  private static void getAccountId(final AsyncCallback<String> callback) {
    if (accountId == null) {
      BrowserService.Util.getInstance().retrieveGoogleAnalyticsAccount(new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(String result) {
          accountId = result;

          // setting account only once
          setAccount(accountId);

          callback.onSuccess(accountId);
        }
      });
    } else {
      callback.onSuccess(accountId);
    }
  }

  /**
   * track an event
   * 
   * @param historyToken
   */
  public static void track(String historyToken) {
    final String page = "/#" + historyToken;

    getAccountId(new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(String accountId) {
        pageview(page);
      }
    });

  }

  private static native void setAccount(String accountId) /*-{
		$wnd.ga('create', accountId, 'auto');
  }-*/;

  /**
   * https://developers.google.com/analytics/devguides/collection/analyticsjs/single-page-applications
   * 
   * @param historyToken
   */
  private static native void pageview(String page) /*-{
		$wnd.ga('set', 'page', page);
		$wnd.ga('send', 'pageview');
  }-*/;

}