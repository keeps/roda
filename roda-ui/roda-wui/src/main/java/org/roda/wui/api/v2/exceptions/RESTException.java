package org.roda.wui.api.v2.exceptions;

import com.google.gwt.http.client.Response;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;

import java.io.Serial;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class RESTException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 8163053720688814940L;
  private int status = Response.SC_INTERNAL_SERVER_ERROR;

  public RESTException() {
  }

  public RESTException(String message) {
    super(message);
  }

  public RESTException(String message, Throwable cause) {
    super(message + getCauseMessage(cause));
    this.status = getResponseStatusCode(cause);
  }

  public RESTException(Throwable cause) {
    super("Remote exception" + getCauseMessage(cause));
    this.status = getResponseStatusCode(cause);
  }

  public RESTException(String message, int status) {
    super(message);
    this.status = status;
  }

  public RESTException(Throwable cause, int status) {
    super("Remote exception" + getCauseMessage(cause));
    this.status = status;
  }

  private static String getCauseMessage(Throwable e) {
    StringBuilder message = new StringBuilder();
    Throwable cause = e;

    while (cause != null) {
      message.append(" caused by ").append(cause.getClass().getSimpleName()).append(": ");
      if (cause.getMessage() != null) {
        message.append(cause.getMessage());
      }
      cause = cause.getCause();
    }
    return message.toString();
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public RESTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  private int getResponseStatusCode(Throwable cause) {
    if (cause instanceof AuthorizationDeniedException) {
      return Response.SC_UNAUTHORIZED;
    } else if (cause instanceof NotFoundException) {
      return Response.SC_NOT_FOUND;
    } else if (cause instanceof AlreadyExistsException) {
      return Response.SC_CONFLICT;
    } else if (cause instanceof GenericException) {
      return Response.SC_BAD_REQUEST;
    } else if (cause instanceof RequestNotValidException) {
      return Response.SC_BAD_REQUEST;
    }
    return Response.SC_INTERNAL_SERVER_ERROR;
  }
}
