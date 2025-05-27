package org.roda.wui.common.model;

import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;

public class RequestContext {

  private User user;
  private RequestHeaders request;
  private ModelService modelService;
  private IndexService indexService;

  public RequestContext() {
    // empty constructor
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

  public void setRequest(RequestHeaders request) {
    this.request = request;
  }

  public IndexService getIndexService() {
    return indexService;
  }

  public void setIndexService(IndexService indexService) {
    this.indexService = indexService;
  }
}
