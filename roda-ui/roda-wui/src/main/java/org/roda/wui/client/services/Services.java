package org.roda.wui.client.services;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;

import com.google.gwt.core.client.GWT;

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
    if (TransferredResourceRestService.class.equals(serviceClass)) {
      return GWT.create(TransferredResourceRestService.class);
    } else if (JobsRestService.class.equals(serviceClass)) {
      return GWT.create(JobsRestService.class);
    } else if (JobReportRestService.class.equals(serviceClass)) {
      return GWT.create(JobReportRestService.class);
    } else {
      throw new IllegalArgumentException(serviceClass.getName() + " not supported");
    }
  }

  public <S extends RODAEntityRestService<O>, O extends IsIndexed> S getFromObjectClass(Class<O> objectClass) {
    if (TransferredResource.class.equals(objectClass)) {
      return GWT.create(TransferredResourceRestService.class);
    } else if (Job.class.equals(objectClass)) {
      return GWT.create(JobsRestService.class);
    } else if (Report.class.equals(objectClass)) {
      return GWT.create(JobReportRestService.class);
    } else {
      throw new IllegalArgumentException(objectClass.getName() + " not supported");
    }
  }

  // public <S extends RODAEntityService<O>, O extends IsIndexed> S
  // getFromObjectClass(String objectClass) {
  // try {
  // if (objectClass.equals("org.roda.core.data.v2.jobs.IndexedReport")) {
  // return GWT.create(IndexedReport.class);
  // }
  // Class<O> clazz = (Class<O>) Class.forName(objectClass);
  // return getFromObjectClass(clazz);
  // } catch (ClassNotFoundException e) {
  // throw new IllegalArgumentException(objectClass + " not supported");
  // }
  // }

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

  public <T> CompletableFuture<T> transferredResource(CheckedFunction<TransferredResourceRestService, T> method) {
    return future(TransferredResourceRestService.class, method);
  }

  public <T> CompletableFuture<T> jobsResource(CheckedFunction<JobsRestService, T> method) {
    return future(JobsRestService.class, method);
  }

  public <T> CompletableFuture<T> jobReportResource(CheckedFunction<JobReportRestService, T> method) {
    return future(JobReportRestService.class, method);
  }
}
