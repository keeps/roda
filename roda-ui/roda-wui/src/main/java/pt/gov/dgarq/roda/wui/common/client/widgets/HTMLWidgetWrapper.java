/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import pt.gov.dgarq.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HTML;

import config.i18n.client.CommonConstants;

/**
 * @author Luís Faria
 * 
 */
public class HTMLWidgetWrapper extends HTML {

	private static final CommonConstants constants = GWT
			.create(CommonConstants.class);
	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final String localizedURL;
	private final String genericURL;

	public HTMLWidgetWrapper(String url) {
		if (url.endsWith(".html")) {
			url = url.substring(0, url.length() - 5);
		}

		localizedURL = url + "_" + constants.locale() + ".html";
		genericURL = url + ".html";

		RequestBuilder request = new RequestBuilder(RequestBuilder.GET,
				localizedURL);
		request.setCallback(new RequestCallback() {

			public void onError(Request request, Throwable exception) {
				logger.error("Error sending generic request", exception);
			}

			public void onResponseReceived(Request request, Response response) {
				if (response.getStatusCode() == 404) {
					RequestBuilder request2 = new RequestBuilder(
							RequestBuilder.GET, genericURL);
					request2.setCallback(new RequestCallback() {

						public void onError(Request request, Throwable exception) {
							logger.error("Error sending generic request",
									exception);
						}

						public void onResponseReceived(Request request,
								Response response) {
							HTMLWidgetWrapper.this.setHTML(response.getText());
						}
					});
					try {
						request2.send();
					} catch (RequestException e) {
						logger.error("Error sending request", e);
					}
				} else {
					HTMLWidgetWrapper.this.setHTML(response.getText());
				}

			}

		});
		
		try {
			request.send();
		} catch (RequestException e) {
			logger.error("Error sending request", e);
		}
	}

	public void onCompletion(String responseText) {
		this.setHTML(responseText);
	}

}
