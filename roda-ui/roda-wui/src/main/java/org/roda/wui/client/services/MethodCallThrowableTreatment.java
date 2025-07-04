package org.roda.wui.client.services;

import org.fusesource.restygwt.client.Method;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MethodCallThrowableTreatment {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final ValidationReportMapper VALIDATION_REPORT_MAPPER = GWT.create(ValidationReportMapper.class);

  private MethodCallThrowableTreatment() {
    // empty constructor
  }
  // TODO: Add more common failures

  public static Throwable treatCommonFailures(Method method, Throwable throwable) {
    final JSONValue parse = JSONParser.parseStrict(method.getResponse().getText());
    String throwableMessage;
    String throwableDetails;
    try {
      throwableMessage = parse.isObject().get("message").toString().replace("\"", "");
    } catch (JavaScriptException e) {
      throwableMessage = "";
    }
    try {
      throwableDetails = parse.isObject().get("details").toString().replace("\"", "");
    } catch (JavaScriptException e) {
      throwableDetails = "";
    }

    if (method.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
      throwable = new NotFoundException(throwableDetails);
    } else if (method.getResponse().getStatusCode() == Response.SC_UNAUTHORIZED) {
      throwable = new AuthenticationDeniedException(throwableDetails);
    } else if (method.getResponse().getStatusCode() == Response.SC_FORBIDDEN) {
      throwable = new AuthorizationDeniedException(throwableDetails);
    } else if (method.getResponse().getStatusCode() == Response.SC_CONFLICT) {
      throwable = new AlreadyExistsException(throwableDetails);
    } else if (method.getResponse().getStatusCode() == Response.SC_BAD_REQUEST) {
      if (throwableMessage.equals("Validation error")) {
        ValidationReport details = VALIDATION_REPORT_MAPPER.read(parse.isObject().get("objectDetails").toString());
        throwable = new ValidationException(details);
      }
    }

    return throwable;
  }

  public static interface ValidationReportMapper extends ObjectMapper<ValidationReport> {
  }
}
