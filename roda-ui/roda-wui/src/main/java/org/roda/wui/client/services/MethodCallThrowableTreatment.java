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

    if (method.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
      throwable = new NotFoundException();
    } else if (method.getResponse().getStatusCode() == Response.SC_UNAUTHORIZED) {
      throwable = new AuthenticationDeniedException();
    } else if (method.getResponse().getStatusCode() ==  Response.SC_FORBIDDEN) {
      throwable = new AuthorizationDeniedException();
    } else if (method.getResponse().getStatusCode() == Response.SC_CONFLICT) {
      throwable = new AlreadyExistsException();
    } else if (method.getResponse().getStatusCode() == Response.SC_BAD_REQUEST) {
      if (parse.isObject().get("message").toString().replace("\"", "").equals("Validation error")) {
        ValidationReport details = VALIDATION_REPORT_MAPPER.read(parse.isObject().get("objectDetails").toString());
        throwable = new ValidationException(details);
      }
    }

    return throwable;
  }

  public static interface ValidationReportMapper extends ObjectMapper<ValidationReport> {
  }
}
