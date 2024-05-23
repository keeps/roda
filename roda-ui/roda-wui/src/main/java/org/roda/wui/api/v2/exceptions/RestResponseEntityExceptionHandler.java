package org.roda.wui.api.v2.exceptions;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.DisposalHoldNotValidException;
import org.roda.core.data.exceptions.DisposalScheduleNotValidException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobStateNotPendingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {RESTException.class})
  protected ResponseEntity<Object> handleRestException(RuntimeException ex, WebRequest request) {
    String message = "Internal server error";
    String details = "";
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    UUID errorID = UUID.randomUUID();
    if (ex.getCause() instanceof AuthorizationDeniedException || ex.getCause() instanceof AuthenticationDeniedException) {
      message = "Unauthorized access";
      details = ex.getCause().getMessage();
      httpStatus = HttpStatus.UNAUTHORIZED;
    } else if (ex.getCause() instanceof NotFoundException) {
      message = "Resource not found";
      details = ex.getCause().getMessage();
      httpStatus = HttpStatus.NOT_FOUND;
    } else if (ex.getCause() instanceof AlreadyExistsException) {
      message = "Resource already exists";
      details = ex.getCause().getMessage();
      httpStatus = HttpStatus.CONFLICT;
    } else if (ex.getCause() instanceof IsStillUpdatingException) {
      message = "Resource still updating";
      details = ex.getCause().getMessage();
      httpStatus = HttpStatus.CONFLICT;
    } else if (ex.getCause() instanceof IllegalOperationException) {
      message = "Operation is forbidden";
      details = ex.getCause().getMessage();
      httpStatus = HttpStatus.FORBIDDEN;
    } else if (ex.getCause() instanceof GenericException || ex.getCause() instanceof RequestNotValidException
      || ex.getCause() instanceof JobStateNotPendingException
      || ex.getCause() instanceof DisposalScheduleNotValidException
      || ex.getCause() instanceof DisposalHoldNotValidException
      || ex.getCause() instanceof InvalidTokenException) {
      message = "Request was not valid";
      details = ex.getCause().getMessage();
      httpStatus = HttpStatus.BAD_REQUEST;
    }

    String warn = "ERROR_ID: " + errorID + " - " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
    LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class).warn(warn);

    ErrorResponseMessage body = new ErrorResponseMessage(httpStatus.value(), errorID.toString(), message, details,
      ((ServletWebRequest) request).getRequest().getRequestURI());

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.APPLICATION_JSON);

    return handleExceptionInternal(ex, body, responseHeaders, httpStatus, request);
  }
}
