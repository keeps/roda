package org.roda.wui.client.services;

import com.google.gwt.core.client.GWT;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Services implements DirectRestService {

  public Services() {
  }

  public Services(String operationReason, String operationType) {
    RODADispatcher.INSTANCE.setOperationUUID(UUID.randomUUID().toString());
    RODADispatcher.INSTANCE.setOperationReason(operationReason);
    RODADispatcher.INSTANCE.setOperationType(operationType);
  }

  public <S extends DirectRestService> S get(Class<S> serviceClass) {
    if (TransferredResourceService.class.equals(serviceClass)) {
      return GWT.create(TransferredResourceService.class);
    } else if (JobsService.class.equals(serviceClass)) {
      return GWT.create(JobsService.class);
    } else {
      throw new IllegalArgumentException(serviceClass.getName() + " not supported");
    }
  }

  public <S extends RODAEntityService<O>, O extends IsIndexed> S getFromObjectClass(Class<O> objectClass) {
    if (TransferredResource.class.equals(objectClass)) {
      return GWT.create(TransferredResourceService.class);
    } else if (Job.class.equals(objectClass)) {
      return GWT.create(JobsService.class);
    } else {
      throw new IllegalArgumentException(objectClass.getName() + " not supported");
    }
  }

//  public <S extends RODAEntityService<O>, O extends IsIndexed> S getFromObjectClass(String objectClass) {
//    try {
//      if (objectClass.equals("org.roda.core.data.v2.jobs.IndexedReport")) {
//        return GWT.create(IndexedReport.class);
//      }
//      Class<O> clazz = (Class<O>) Class.forName(objectClass);
//      return getFromObjectClass(clazz);
//    } catch (ClassNotFoundException e) {
//      throw new IllegalArgumentException(objectClass + " not supported");
//    }
//  }

  public <S extends DirectRestService, T> CompletableFuture<T> future(Class<S> serviceClass,
    CheckedFunction<S, T> method) {

    CompletableFuture<T> result = new CompletableFuture<>();
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


  public <S extends RODAEntityService<O>, O extends IsIndexed, T> CompletableFuture<T> futureFromObjectClass(String objectClassString,
                                                                                                                            CheckedFunction<S, T> method) {
    Class<O> objectClass;
    if (objectClassString.equals("org.roda.core.data.v2.jobs.IndexedReport")) {
      objectClass = GWT.create(IndexedReport.class).getClass();
    } else if (objectClassString.equals("org.roda.core.data.v2.ip.AIP")) {
      objectClass = GWT.create(IndexedAIP.class);
    } else {
      throw new IllegalArgumentException(objectClassString + " not supported");
    }

    CompletableFuture<T> result = new CompletableFuture<>();
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
      }).call(getFromObjectClass(objectClass)));
    } catch (RODAException e) {
      result.completeExceptionally(e);
    }
    return result;

  }

  public <T> CompletableFuture<T> transferredResource(CheckedFunction<TransferredResourceService, T> method) {
    return future(TransferredResourceService.class, method);
  }

  public <T> CompletableFuture<T> jobsResource(CheckedFunction<JobsService, T> method) {
    return future(JobsService.class, method);
  }

}
