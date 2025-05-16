package org.roda.wui.common.model;

import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.transaction.TransactionalContext;


public class RequestContext {

  private User user;
  private RequestHeaders request;
  private ModelService modelService;


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

  public ModelService getModelService() {
    return modelService;
  }

  public void setModelService(ModelService modelService) {
    this.modelService = modelService;
  }
}
