package org.roda.wui.client.services;

import com.google.gwt.core.client.GWT;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.DefaultMethodCallback;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Services implements DirectRestService {

  private String operationUUID;
  private String operationReason;

  private String operationType;

  public Services(String operationReason, String operationType) {
    this.operationUUID = UUID.randomUUID().toString();
    this.operationReason = operationReason;
    this.operationType = operationType;

    RODADispatcher.INSTANCE.setOperationType(operationType);

    // TODO inject this info in the request builder
  }

  /**
   * @return the singleton instance
   */
  public static TransferredResourceService getTransferredResourceService() {
    return GWT.create(TransferredResourceService.class);
  }

  public static IndexService getIndexService() {
    return GWT.create(IndexService.class);
  }

  public static <S extends DirectRestService> S get(Class<S> serviceClass) {
    if (TransferredResourceService.class.equals(serviceClass)) {
      return GWT.create(TransferredResourceService.class);
    } else if (IndexService.class.equals(serviceClass)) {
      return GWT.create(IndexService.class);
    } else {
      throw new IllegalArgumentException(serviceClass.getName() + " not supported");
    }
  }

  public static <S extends DirectRestService, T> CompletableFuture<T> future(Class<S> serviceClass, CheckedFunction<S, T> method) {

    CompletableFuture<T> result = new CompletableFuture<T>();
    try {
      method.apply(REST.withCallback(new MethodCallback<T>() {
        @Override
        public void onFailure(Method method, Throwable throwable) {
          result.completeExceptionally(throwable);
        }

        @Override
        public void onSuccess(Method method, T t) {
          result.complete(t);
        }
      }).call(get(serviceClass)));
    } catch (RODAException e) {
     result.completeExceptionally(e);
    }
    return result;

  }


  public static <T> CompletableFuture<T> transferredResource(CheckedFunction<TransferredResourceService, T> method) {
    return future(TransferredResourceService.class, method);
  }

  public static <T> CompletableFuture<T> index(CheckedFunction<IndexService, T> method) {
    return future(IndexService.class, method);
  }

}
