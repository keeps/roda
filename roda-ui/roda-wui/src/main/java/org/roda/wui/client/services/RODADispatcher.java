package org.roda.wui.client.services;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import org.fusesource.restygwt.client.Dispatcher;
import org.fusesource.restygwt.client.Method;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class RODADispatcher implements Dispatcher {
  public static final RODADispatcher INSTANCE = new RODADispatcher();

  public String getOperationUUID() {
    return operationUUID;
  }

  public void setOperationUUID(String operationUUID) {
    this.operationUUID = operationUUID;
  }

  public String getOperationReason() {
    return operationReason;
  }

  public void setOperationReason(String operationReason) {
    this.operationReason = operationReason;
  }

  public String getOperationType() {
    return operationType;
  }

  public void setOperationType(String operationType) {
    this.operationType = operationType;
  }

  private String operationUUID;
  private String operationReason;

  private String operationType;



  @Override
  public Request send(Method method, RequestBuilder requestBuilder) throws RequestException {
    requestBuilder.setHeader("X-REQUEST-UUID", operationUUID);

    return requestBuilder.send();
  }
}
