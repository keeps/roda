package org.roda.wui.client.services;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;

import com.google.gwt.core.client.GWT;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Services implements DirectRestService {

  private Services() {
    // Empty constructor
  }

  public Services(String operationReason, String operationType) {
    RODADispatcher.INSTANCE.setOperationUUID(UUID.randomUUID().toString());
    RODADispatcher.INSTANCE.setOperationReason(operationReason);
    RODADispatcher.INSTANCE.setOperationType(operationType);
  }

  private <S extends DirectRestService> S get(Class<S> serviceClass) {
    if (TransferredResourceRestService.class.equals(serviceClass)) {
      return GWT.create(TransferredResourceRestService.class);
    } else if (JobsRestService.class.equals(serviceClass)) {
      return GWT.create(JobsRestService.class);
    } else if (JobReportRestService.class.equals(serviceClass)) {
      return GWT.create(JobReportRestService.class);
    } else if (NotificationRestService.class.equals(serviceClass)) {
      return GWT.create(NotificationRestService.class);
    } else if (DisposalScheduleRestService.class.equals(serviceClass)) {
      return GWT.create(DisposalScheduleRestService.class);
    } else if (DisposalHoldRestService.class.equals(serviceClass)) {
      return GWT.create(DisposalHoldRestService.class);
    } else if (DisposalRuleRestService.class.equals(serviceClass)) {
      return GWT.create(DisposalRuleRestService.class);
    } else if (DisposalConfirmationRestService.class.equals(serviceClass)) {
      return GWT.create(DisposalConfirmationRestService.class);
    } else if (RiskIncidenceRestService.class.equals(serviceClass)) {
      return GWT.create(RiskIncidenceRestService.class);
    } else if (RiskRestService.class.equals(serviceClass)) {
      return GWT.create(RiskRestService.class);
    } else if (PreservationEventRestService.class.equals(serviceClass)) {
      return GWT.create(PreservationEventRestService.class);
    } else if (AuditLogRestService.class.equals(serviceClass)) {
      return GWT.create(AuditLogRestService.class);
    } else if (RepresentationInformationRestService.class.equals(serviceClass)) {
      return GWT.create(RepresentationInformationRestService.class);
    } else if (MembersRestService.class.equals(serviceClass)) {
      return GWT.create(MembersRestService.class);
    } else if (ConfigurationRestService.class.equals(serviceClass)) {
      return GWT.create(ConfigurationRestService.class);
    } else if (FileRestService.class.equals(serviceClass)) {
      return GWT.create(FileRestService.class);
    } else if (RepresentationRestService.class.equals(serviceClass)) {
      return GWT.create(RepresentationRestService.class);
    } else if (AIPRestService.class.equals(serviceClass)) {
      return GWT.create(AIPRestService.class);
    } else if (DIPRestService.class.equals(serviceClass)) {
      return GWT.create(DIPRestService.class);
    } else if (DIPFileRestService.class.equals(serviceClass)) {
      return GWT.create(DIPFileRestService.class);
    } else if (DistributedInstancesRestService.class.equals(serviceClass)) {
      return GWT.create(DistributedInstancesRestService.class);
    } else if (ClientLoggerRestService.class.equals(serviceClass)) {
      return GWT.create(ClientLoggerRestService.class);
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
          Throwable handledThrowable = MethodCallThrowableTreatment.treatCommonFailures(method, throwable);
          result.completeExceptionally(handledThrowable);
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

  private <S extends RODAEntityRestService<O>, O extends IsIndexed, T> CompletableFuture<T> futureFromObjectClass(
    String objectClassString, CheckedFunction<S, T> method) {
    S service;
    if (TransferredResource.class.getName().equals(objectClassString)) {
      service = GWT.create(TransferredResourceRestService.class);
    } else if (Notification.class.getName().equals(objectClassString)) {
      service = GWT.create(NotificationRestService.class);
    } else if (IndexedAIP.class.getName().equals(objectClassString) || AIP.class.getName().equals(objectClassString)) {
      service = GWT.create(AIPRestService.class);
    } else if (DisposalConfirmation.class.getName().equals(objectClassString)) {
      service = GWT.create(DisposalConfirmationRestService.class);
    } else if (RiskIncidence.class.getName().equals(objectClassString)) {
      service = GWT.create(RiskIncidenceRestService.class);
    } else if (IndexedRisk.class.getName().equals(objectClassString)) {
      service = GWT.create(RiskRestService.class);
    } else if (IndexedPreservationEvent.class.getName().equals(objectClassString)) {
      service = GWT.create(PreservationEventRestService.class);
    } else if (IndexedPreservationAgent.class.getName().equals(objectClassString)) {
      service = GWT.create(PreservationAgentRestService.class);
    } else if (RepresentationInformation.class.getName().equals(objectClassString)) {
      service = GWT.create(RepresentationInformationRestService.class);
    } else if (IndexedJob.class.getName().equals(objectClassString)) {
      service = GWT.create(JobsRestService.class);
    } else if (IndexedReport.class.getName().equals(objectClassString)) {
      service = GWT.create(JobReportRestService.class);
    } else if (RODAMember.class.getName().equals(objectClassString)) {
      service = GWT.create(MembersRestService.class);
    } else if (IndexedDIP.class.getName().equals(objectClassString)) {
      service = GWT.create(DIPRestService.class);
    } else if (IndexedRepresentation.class.getName().equals(objectClassString)
      || Representation.class.getName().equals(objectClassString)) {
      service = GWT.create(RepresentationRestService.class);
    } else if (IndexedFile.class.getName().equals(objectClassString)
      || File.class.getName().equals(objectClassString)) {
      service = GWT.create(FileRestService.class);
    } else if (LogEntry.class.getName().equals(objectClassString)) {
      service = GWT.create(AuditLogRestService.class);
    } else if (DIPFile.class.getName().equals(objectClassString)) {
      service = GWT.create(DIPFileRestService.class);
    } else {
      throw new IllegalArgumentException(objectClassString + " not supported");
    }

    CompletableFuture<T> result = new CompletableFuture<>();
    try {
      method.apply(REST.withCallback(new MethodCallback<T>() {
        @Override
        public void onFailure(Method method, Throwable throwable) {
          Throwable handledThrowable = MethodCallThrowableTreatment.treatCommonFailures(method, throwable);
          result.completeExceptionally(handledThrowable);
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

  public <T, O extends IsIndexed> CompletableFuture<T> rodaEntityRestService(
    CheckedFunction<RODAEntityRestService<O>, T> method, String objectClassName) {
    return futureFromObjectClass(objectClassName, method);
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

  public <T> CompletableFuture<T> disposalScheduleResource(CheckedFunction<DisposalScheduleRestService, T> method) {
    return future(DisposalScheduleRestService.class, method);
  }

  public <T> CompletableFuture<T> disposalHoldResource(CheckedFunction<DisposalHoldRestService, T> method) {
    return future(DisposalHoldRestService.class, method);
  }

  public <T> CompletableFuture<T> disposalRuleResource(CheckedFunction<DisposalRuleRestService, T> method) {
    return future(DisposalRuleRestService.class, method);
  }

  public <T> CompletableFuture<T> disposalConfirmationResource(
    CheckedFunction<DisposalConfirmationRestService, T> method) {
    return future(DisposalConfirmationRestService.class, method);
  }

  public <T> CompletableFuture<T> riskResource(CheckedFunction<RiskRestService, T> method) {
    return future(RiskRestService.class, method);
  }

  public <T> CompletableFuture<T> riskIncidenceResource(CheckedFunction<RiskIncidenceRestService, T> method) {
    return future(RiskIncidenceRestService.class, method);
  }

  public <T> CompletableFuture<T> preservationEventsResource(CheckedFunction<PreservationEventRestService, T> method) {
    return future(PreservationEventRestService.class, method);
  }

  public <T> CompletableFuture<T> membersResource(CheckedFunction<MembersRestService, T> method) {
    return future(MembersRestService.class, method);
  }

  public <T> CompletableFuture<T> representationInformationResource(
    CheckedFunction<RepresentationInformationRestService, T> method) {
    return future(RepresentationInformationRestService.class, method);
  }

  public <T> CompletableFuture<T> representationResource(CheckedFunction<RepresentationRestService, T> method) {
    return future(RepresentationRestService.class, method);
  }

  public <T> CompletableFuture<T> fileResource(CheckedFunction<FileRestService, T> method) {
    return future(FileRestService.class, method);
  }

  public <T> CompletableFuture<T> configurationsResource(CheckedFunction<ConfigurationRestService, T> method) {
    return future(ConfigurationRestService.class, method);
  }

  public <T> CompletableFuture<T> aipResource(CheckedFunction<AIPRestService, T> method) {
    return future(AIPRestService.class, method);
  }

  public <T> CompletableFuture<T> dipResource(CheckedFunction<DIPRestService, T> method) {
    return future(DIPRestService.class, method);
  }

  public <T> CompletableFuture<T> dipFileResource(CheckedFunction<DIPFileRestService, T> method) {
    return future(DIPFileRestService.class, method);
  }

  public <T> CompletableFuture<T> distributedInstanceResource(
    CheckedFunction<DistributedInstancesRestService, T> method) {
    return future(DistributedInstancesRestService.class, method);
  }

  public <T> CompletableFuture<T> clientLoggerResource(CheckedFunction<ClientLoggerRestService, T> method) {
    return future(ClientLoggerRestService.class, method);
  }
}
