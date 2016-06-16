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
package org.roda.wui.common.client.widgets;

import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.HTML;

import config.i18n.client.ClientMessages;

/**
 * @author Luís Faria
 * 
 */
public class HTMLWidgetWrapper extends HTML {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ClientLogger logger = new ClientLogger(getClass().getName());

  public HTMLWidgetWrapper(String resourceId) {
    if (resourceId.endsWith(".html")) {
      resourceId = resourceId.substring(0, resourceId.length() - 5);
    }

    String locale = LocaleInfo.getCurrentLocale().getLocaleName();
    String localizedResourceId = resourceId + "_" + locale + ".html";
    String defaultResourceId = resourceId + ".html";

    RequestBuilder request = new RequestBuilder(RequestBuilder.GET,
      RestUtils.createThemeResourceUri(localizedResourceId, defaultResourceId, false).asString());

    try {
      request.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == 200) {
            HTMLWidgetWrapper.this.setHTML(response.getText());
          } else {
            logger.error("Error sending request");
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          logger.error("Error sending request", exception);
        }
      });
    } catch (RequestException exception) {
      logger.error("Error sending request", exception);
    }
  }

  public void onCompletion(String responseText) {
    this.setHTML(responseText);
  }

}
