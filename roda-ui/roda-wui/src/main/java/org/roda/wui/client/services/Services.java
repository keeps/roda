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
import org.roda.core.data.v2.notifications.Notification;

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
    } else if (NotificationRestService.class.equals(serviceClass)) {
      return GWT.create(NotificationRestService.class);
    } else {
      throw new IllegalArgumentException(serviceClass.getName() + " not supported");
    }
  }

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

  public <S extends RODAEntityRestService<O>, O extends IsIndexed, T> CompletableFuture<T> futureFromObjectClass(
    String objectClassString, CheckedFunction<S, T> method) {
    S service;
    if (TransferredResource.class.getName().equals(objectClassString)) {
      service = GWT.create(TransferredResourceRestService.class);
    } else if (Notification.class.getName().equals(objectClassString)) {
      service = GWT.create(NotificationRestService.class);
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
      }).call(service));
    } catch (RODAException e) {
      result.completeExceptionally(e);
    }
    return result;

  }

  public <T, O extends IsIndexed> CompletableFuture<T> rodaEntityRestService(
    CheckedFunction<RODAEntityRestService<O>, T> method, Class<O> objectClass) {
    return futureFromObjectClass(objectClass.getName(), method);
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

  public <T> CompletableFuture<T> notificationResource(CheckedFunction<NotificationRestService, T> method) {
    return future(NotificationRestService.class, method);
  }
}
