package org.roda.wui.client.services;

import com.google.gwt.core.client.GWT;
import config.i18n.client.ClientMessages;
import org.fusesource.restygwt.client.Method;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.wui.client.main.Login;
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private MethodCallThrowableTreatment() {
    // empty constructor
  }

  // TODO: Add more common failures

  public static Throwable treatCommonFailures(Method method, Throwable throwable) {
    final JSONValue parse = JSONParser.parseStrict(method.getResponse().getText());

    if (method.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
      throwable = new NotFoundException();
    } else if (method.getResponse().getStatusCode() == Response.SC_UNAUTHORIZED) {
      throwable = new AuthenticationDeniedException();
    } else if (method.getResponse().getStatusCode() ==  Response.SC_FORBIDDEN) {
      throwable = new AuthorizationDeniedException();
    } else if (method.getResponse().getStatusCode() == Response.SC_CONFLICT) {
      throwable = new AlreadyExistsException();
    }
    return throwable;
  }
}
