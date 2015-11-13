/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import javax.jws.WebService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>WebService</code>.
 * 
 * @see WebService
 */
public interface GAnalyticsServiceAsync {

  GAnalyticsServiceAsync INSTANCE = GWT.create(GAnalyticsService.class);

  void getGoogleAnalyticsAccount(AsyncCallback<String> callback);

}
