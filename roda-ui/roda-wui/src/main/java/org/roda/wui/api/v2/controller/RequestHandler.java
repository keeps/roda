package org.roda.wui.api.v2.controller;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.transaction.RODATransactionException;
import org.roda.core.transaction.RODATransactionManager;
import org.roda.core.transaction.TransactionalContext;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class RequestHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

  @Autowired
  private RODATransactionManager transactionManager;
  @Autowired
  private HttpServletRequest request;

  public <T> T execute(RequestProcess<T> handler) {
    RequestControllerAssistant controllerAssistant = new RequestControllerAssistant(handler);
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    TransactionalContext transactionalContext = null;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // Init transaction
      transactionalContext = transactionManager.beginTransaction(TransactionLog.TransactionRequestType.API);
      requestContext.setTransactionalContext(transactionalContext);

      // execute the request
      T result = handler.execute(requestContext, controllerAssistant);

      // End transaction
      transactionManager.endTransaction(transactionalContext.transactionLog().getId());
      return result;
    }  catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, controllerAssistant.getRelatedObjectId(), state, controllerAssistant.getParameters());
      try {
        if (transactionalContext != null && state != LogEntryState.SUCCESS) {
          transactionManager.rollbackTransaction(transactionalContext.transactionLog().getId());
        }
      } catch (RODATransactionException ex) {
        LOGGER.error("Error rolling back transaction", ex);
      }
    }
  }

  public interface RequestProcess<T> {
    T execute(RequestContext requestContext, RequestControllerAssistant controllerAssistant) throws RODAException, RESTException;
  }
}