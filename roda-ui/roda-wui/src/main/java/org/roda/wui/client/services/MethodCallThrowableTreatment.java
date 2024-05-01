package org.roda.wui.client.services;

import org.fusesource.restygwt.client.Method;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MethodCallThrowableTreatment {

  private MethodCallThrowableTreatment() {
    // empty constructor
  }

  // TODO: Add more common failures

  public static void treatCommonFailures(Method method, Throwable throwable) {
    final JSONValue parse = JSONParser.parseStrict(method.getResponse().getText());

    if (method.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
      String message = parse.isObject().get("message").isString().stringValue();
      Toast.showError(throwable.getClass().getSimpleName(), message);

      HistoryUtils.newHistory(Welcome.RESOLVER);
    }
  }
}
