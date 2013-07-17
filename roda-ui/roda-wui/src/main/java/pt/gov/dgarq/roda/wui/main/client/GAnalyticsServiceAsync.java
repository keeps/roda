package pt.gov.dgarq.roda.wui.main.client;

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
