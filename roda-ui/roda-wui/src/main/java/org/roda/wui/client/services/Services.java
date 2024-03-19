package org.roda.wui.client.services;

import com.google.gwt.core.client.GWT;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.DefaultMethodCallback;
import org.roda.wui.client.ingest.transfer.CheckedFunction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Services implements DirectRestService {

  public static class api {
    public static TransferredResourceService transferredresource = GWT.create(TransferredResourceService.class);
  }

  /**
   * @return the singleton instance
   */
  public static TransferredResourceService getTransferredResourceService() {
    return GWT.create(TransferredResourceService.class);
  }

  public static <S extends DirectRestService> S get(Class<S> serviceClass) {
    if (TransferredResourceService.class.equals(serviceClass)) {
      return GWT.create(TransferredResourceService.class);
    } else {
      throw new IllegalArgumentException(serviceClass.getName() + " not supported");
    }
  }


  public <T> TransferredResourceService callTransferredResourceService(MethodCallback<T> callback) {
    return REST.withCallback(callback).call(getTransferredResourceService());
  }

  public static <T> TransferredResourceService callTransferredResourceService(Consumer<T> callback) {
    return REST.withCallback(DefaultMethodCallback.get(callback)).call(getTransferredResourceService());
  }

  public <T> TransferredResourceService callTransferredResourceService(Consumer<T> callback, Consumer<String> errorHandler) {
    return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(getTransferredResourceService());
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




}
