/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.roda.core.common.ReturnWithExceptionsWrapper;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;

public abstract class ModelObservable {
  private final List<ModelObserver> observers;
  private Logger logger;

  public ModelObservable(Logger logger) {
    super();
    this.observers = new ArrayList<>();
    this.logger = logger;
  }

  public void addModelObserver(ModelObserver observer) {
    observers.add(observer);
  }

  public void removeModelObserver(ModelObserver observer) {
    observers.remove(observer);
  }

  private ReturnWithExceptionsWrapper notifyObserversSafely(Function<ModelObserver, ReturnWithExceptions<?, ?>> func) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      try {
        wrapper.addToList(func.apply(observer));
      } catch (Exception e) {
        logger.error("Error invoking method in observer {}", observer.getClass().getSimpleName(), e);
        // do nothing, just want to sandbox observer method invocation
      }
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyAipCreated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipCreated(aip));
  }

  public ReturnWithExceptionsWrapper notifyAipUpdated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipUpdated(aip));
  }

  public ReturnWithExceptionsWrapper notifyAipMoved(AIP aip, String oldParentId, String newParentId) {
    return notifyObserversSafely(observer -> observer.aipMoved(aip, oldParentId, newParentId));
  }

  public ReturnWithExceptionsWrapper notifyAipStateUpdated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipStateUpdated(aip));
  }

  public ReturnWithExceptionsWrapper notifyAipDeleted(String aipId) {
    return notifyObserversSafely(observer -> observer.aipDeleted(aipId, true));
  }

  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    return notifyObserversSafely(observer -> observer.descriptiveMetadataCreated(descriptiveMetadata));
  }

  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    return notifyObserversSafely(observer -> observer.descriptiveMetadataUpdated(descriptiveMetadata));
  }

  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataDeleted(String aipId, String representationId,
    String descriptiveMetadataBinaryId) {
    return notifyObserversSafely(
      observer -> observer.descriptiveMetadataDeleted(aipId, representationId, descriptiveMetadataBinaryId));
  }

  public ReturnWithExceptionsWrapper notifyRepresentationCreated(Representation representation) {
    return notifyObserversSafely(observer -> observer.representationCreated(representation));
  }

  public ReturnWithExceptionsWrapper notifyRepresentationUpdated(Representation representation) {
    return notifyObserversSafely(observer -> observer.representationUpdated(representation));
  }

  public ReturnWithExceptionsWrapper notifyRepresentationDeleted(String aipId, String representationId) {
    return notifyObserversSafely(observer -> observer.representationDeleted(aipId, representationId, true));
  }

  public ReturnWithExceptionsWrapper notifyFileCreated(File file) {
    return notifyObserversSafely(observer -> observer.fileCreated(file));
  }

  public ReturnWithExceptionsWrapper notifyFileUpdated(File file) {
    return notifyObserversSafely(observer -> observer.fileUpdated(file));
  }

  public ReturnWithExceptionsWrapper notifyFileDeleted(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    return notifyObserversSafely(
      observer -> observer.fileDeleted(aipId, representationId, fileDirectoryPath, fileId, true));
  }

  public ReturnWithExceptionsWrapper notifyLogEntryCreated(LogEntry entry) {
    return notifyObserversSafely(observer -> observer.logEntryCreated(entry));
  }

  public ReturnWithExceptionsWrapper notifyUserCreated(User user) {
    return notifyObserversSafely(observer -> observer.userCreated(user));
  }

  public ReturnWithExceptionsWrapper notifyUserUpdated(User user) {
    return notifyObserversSafely(observer -> observer.userUpdated(user));
  }

  public ReturnWithExceptionsWrapper notifyUserDeleted(String userID) {
    return notifyObserversSafely(observer -> observer.userDeleted(userID));
  }

  public ReturnWithExceptionsWrapper notifyGroupCreated(Group group) {
    return notifyObserversSafely(observer -> observer.groupCreated(group));
  }

  public ReturnWithExceptionsWrapper notifyGroupUpdated(Group group) {
    return notifyObserversSafely(observer -> observer.groupUpdated(group));
  }

  public ReturnWithExceptionsWrapper notifyGroupDeleted(String groupID) {
    return notifyObserversSafely(observer -> observer.groupDeleted(groupID));
  }

  public ReturnWithExceptionsWrapper notifyPreservationMetadataCreated(
    PreservationMetadata preservationMetadataBinary) {
    return notifyObserversSafely(observer -> observer.preservationMetadataCreated(preservationMetadataBinary));
  }

  public ReturnWithExceptionsWrapper notifyPreservationMetadataUpdated(
    PreservationMetadata preservationMetadataBinary) {
    return notifyObserversSafely(observer -> observer.preservationMetadataUpdated(preservationMetadataBinary));
  }

  public ReturnWithExceptionsWrapper notifyPreservationMetadataDeleted(PreservationMetadata pm) {
    return notifyObserversSafely(observer -> observer.preservationMetadataDeleted(pm));
  }

  public ReturnWithExceptionsWrapper notifyOtherMetadataCreated(OtherMetadata otherMetadataBinary) {
    return notifyObserversSafely(observer -> observer.otherMetadataCreated(otherMetadataBinary));
  }

  public ReturnWithExceptionsWrapper notifyJobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    return notifyObserversSafely(observer -> observer.jobCreatedOrUpdated(job, reindexJobReports));
  }

  public ReturnWithExceptionsWrapper notifyJobDeleted(String jobId) {
    return notifyObserversSafely(observer -> observer.jobDeleted(jobId));
  }

  public ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, Job cachedJob) {
    return notifyObserversSafely(observer -> observer.jobReportCreatedOrUpdated(jobReport, cachedJob));
  }

  public ReturnWithExceptionsWrapper notifyJobReportDeleted(String jobReportId) {
    return notifyObserversSafely(observer -> observer.jobReportDeleted(jobReportId));
  }

  public ReturnWithExceptionsWrapper notifyAipPermissionsUpdated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipPermissionsUpdated(aip));
  }

  public ReturnWithExceptionsWrapper notifyDipPermissionsUpdated(DIP dip) {
    return notifyObserversSafely(observer -> observer.dipPermissionsUpdated(dip));
  }

  public ReturnWithExceptionsWrapper notifyTransferredResourceDeleted(String transferredResourceID) {
    return notifyObserversSafely(observer -> observer.transferredResourceDeleted(transferredResourceID));
  }

  public ReturnWithExceptionsWrapper notifyRiskCreatedOrUpdated(Risk risk, int incidences, boolean commit) {
    return notifyObserversSafely(observer -> observer.riskCreatedOrUpdated(risk, incidences, commit));
  }

  public ReturnWithExceptionsWrapper notifyRiskDeleted(String riskId, boolean commit) {
    return notifyObserversSafely(observer -> observer.riskDeleted(riskId, commit));
  }

  public ReturnWithExceptionsWrapper notifyRiskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit) {
    return notifyObserversSafely(observer -> observer.riskIncidenceCreatedOrUpdated(riskIncidence, commit));
  }

  public ReturnWithExceptionsWrapper notifyRiskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    return notifyObserversSafely(observer -> observer.riskIncidenceDeleted(riskIncidenceId, commit));
  }

  public ReturnWithExceptionsWrapper notifyRepresentationInformationCreatedOrUpdated(RepresentationInformation ri,
    boolean commit) {
    return notifyObserversSafely(observer -> observer.representationInformationCreatedOrUpdated(ri, commit));
  }

  public ReturnWithExceptionsWrapper notifyRepresentationInformationDeleted(String representationInformationId,
    boolean commit) {
    return notifyObserversSafely(
      observer -> observer.representationInformationDeleted(representationInformationId, commit));
  }

  public ReturnWithExceptionsWrapper notifyNotificationCreatedOrUpdated(Notification notification) {
    return notifyObserversSafely(observer -> observer.notificationCreatedOrUpdated(notification));
  }

  public ReturnWithExceptionsWrapper notifyNotificationDeleted(String notificationId) {
    return notifyObserversSafely(observer -> observer.notificationDeleted(notificationId));
  }

  public ReturnWithExceptionsWrapper notifyDIPCreated(DIP dip, boolean commit) {
    return notifyObserversSafely(observer -> observer.dipCreated(dip, commit));
  }

  public ReturnWithExceptionsWrapper notifyDIPUpdated(DIP dip, boolean commit) {
    return notifyObserversSafely(observer -> observer.dipUpdated(dip, commit));
  }

  public ReturnWithExceptionsWrapper notifyDIPDeleted(String dipId, boolean commit) {
    return notifyObserversSafely(observer -> observer.dipDeleted(dipId, commit));
  }

  public ReturnWithExceptionsWrapper notifyDIPFileCreated(DIPFile file) {
    return notifyObserversSafely(observer -> observer.dipFileCreated(file));
  }

  public ReturnWithExceptionsWrapper notifyDIPFileUpdated(DIPFile file) {
    return notifyObserversSafely(observer -> observer.dipFileUpdated(file));
  }

  public ReturnWithExceptionsWrapper notifyDIPFileDeleted(String dipId, List<String> path, String fileId) {
    return notifyObserversSafely(observer -> observer.dipFileDeleted(dipId, path, fileId));
  }

  public ReturnWithExceptionsWrapper notifyDisposalConfirmationCreatedOrUpdated(DisposalConfirmation confirmation) {
    return notifyObserversSafely(observer -> observer.disposalConfirmationCreateOrUpdate(confirmation));
  }

  public ReturnWithExceptionsWrapper notifyDisposalConfirmationDeleted(String disposalConfirmationId, boolean commit) {
    return notifyObserversSafely(observer -> observer.disposalConfirmationDeleted(disposalConfirmationId, commit));
  }
}
