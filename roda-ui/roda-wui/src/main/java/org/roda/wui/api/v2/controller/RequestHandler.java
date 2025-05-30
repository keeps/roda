package org.roda.wui.api.v2.controller;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
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

import java.io.IOException;

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

  public <T> T processRequestWithTransaction(RequestProcessor<T> handler) {
    return processRequest(handler, null, true);
  }

  public <T> T processRequest(RequestProcessor<T> handler) {
    return processRequest(handler, null, false);
  }

  public <T> T processRequest(RequestProcessor<T> handler, Class<?> returnClass) {
    return processRequest(handler, returnClass, false);
  }

  private <T> T processRequest(RequestProcessor<T> processor, Class<?> returnClass, boolean isTransactional) {
    RequestControllerAssistant controllerAssistant = new RequestControllerAssistant(processor);
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    TransactionalContext transactionalContext = null;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser(), returnClass);

      if (isAValidTransactionalContext(isTransactional)) {
        // Init transaction
        transactionalContext = transactionManager.beginTransaction(TransactionLog.TransactionRequestType.API);
        requestContext.setModelService(transactionalContext.transactionalModelService());
        requestContext.setIndexService(transactionalContext.indexService());
        // execute the request
        T result = processor.process(requestContext, controllerAssistant);

        // End transaction
        transactionManager.endTransaction(transactionalContext.transactionLog().getId());
        return result;
      } else {
        requestContext.setModelService(RodaCoreFactory.getModelService());
        requestContext.setIndexService(RodaCoreFactory.getIndexService());
        return processor.process(requestContext, controllerAssistant);
      }
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, controllerAssistant.getRelatedObjectId(), state,
        controllerAssistant.getParameters());
      try {
        if (isAValidTransactionalContext(isTransactional) && transactionalContext != null
          && state != LogEntryState.SUCCESS) {
          transactionManager.rollbackTransaction(transactionalContext.transactionLog().getId());
        }
      } catch (RODATransactionException ex) {
        LOGGER.error("Error rolling back transaction", ex);
      }
    }
  }

  private boolean isAValidTransactionalContext(boolean isTransactional) {
    // Check if the current node is not a read-only node
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(RodaCoreFactory.getNodeType());
    return writeIsAllowed && isTransactional;
  }

  public interface RequestProcessor<T> {
    T process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
            throws RODAException, RESTException, IOException;
  }
}