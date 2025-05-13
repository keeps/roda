package org.roda.wui.common.model;

import org.roda.core.data.v2.user.User;
import org.roda.core.transaction.TransactionalContext;


public class RequestContext {

  private User user;
  private RequestHeaders request;
  private TransactionalContext transactionalContext;


  public RequestContext() {
    //empty constructor
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public RequestHeaders getRequest() {
    return request;
  }

  public void setRequestHeaders(RequestHeaders request) {
    this.request = request;
  }

  public TransactionalContext getTransactionalContext() {
    return transactionalContext;
  }

  public void setTransactionalContext(TransactionalContext transactionalContext) {
    this.transactionalContext = transactionalContext;
  }
}
