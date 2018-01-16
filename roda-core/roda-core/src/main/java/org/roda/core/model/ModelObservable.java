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

import org.roda.core.common.ReturnWithExceptionsWrapper;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
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

public abstract class ModelObservable {
  private final List<ModelObserver> observers;

  public ModelObservable() {
    super();
    this.observers = new ArrayList<>();
  }

  public void addModelObserver(ModelObserver observer) {
    observers.add(observer);
  }

  public void removeModelObserver(ModelObserver observer) {
    observers.remove(observer);
  }

  public ReturnWithExceptionsWrapper notifyAipCreated(AIP aip) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.aipCreated(aip));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyAipUpdated(AIP aip) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.aipUpdated(aip));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyAipMoved(AIP aip, String oldParentId, String newParentId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.aipMoved(aip, oldParentId, newParentId));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyAipStateUpdated(AIP aip) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.aipStateUpdated(aip));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyAipDeleted(String aipId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.aipDeleted(aipId, true));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.descriptiveMetadataCreated(descriptiveMetadata));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.descriptiveMetadataUpdated(descriptiveMetadata));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataDeleted(String aipId, String representationId,
    String descriptiveMetadataBinaryId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.descriptiveMetadataDeleted(aipId, representationId, descriptiveMetadataBinaryId));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRepresentationCreated(Representation representation) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.representationCreated(representation));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRepresentationUpdated(Representation representation) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.representationUpdated(representation));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRepresentationDeleted(String aipId, String representationId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.representationDeleted(aipId, representationId, true));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyFileCreated(File file) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.fileCreated(file));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyFileUpdated(File file) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.fileUpdated(file));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyFileDeleted(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.fileDeleted(aipId, representationId, fileDirectoryPath, fileId, true));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyLogEntryCreated(LogEntry entry) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.logEntryCreated(entry));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyUserCreated(User user) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.userCreated(user));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyUserUpdated(User user) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.userUpdated(user));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyUserDeleted(String userID) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.userDeleted(userID));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyGroupCreated(Group group) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.groupCreated(group));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyGroupUpdated(Group group) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.groupUpdated(group));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyGroupDeleted(String groupID) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.groupDeleted(groupID));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyPreservationMetadataCreated(
    PreservationMetadata preservationMetadataBinary) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.preservationMetadataCreated(preservationMetadataBinary));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyPreservationMetadataUpdated(
    PreservationMetadata preservationMetadataBinary) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.preservationMetadataUpdated(preservationMetadataBinary));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyPreservationMetadataDeleted(PreservationMetadata pm) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.preservationMetadataDeleted(pm));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyOtherMetadataCreated(OtherMetadata otherMetadataBinary) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.otherMetadataCreated(otherMetadataBinary));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyJobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.jobCreatedOrUpdated(job, reindexJobReports));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyJobDeleted(String jobId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.jobDeleted(jobId));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, Job job) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.jobReportCreatedOrUpdated(jobReport, job));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyJobReportDeleted(String jobReportId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.jobReportDeleted(jobReportId));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyAipPermissionsUpdated(AIP aip) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.aipPermissionsUpdated(aip));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDipPermissionsUpdated(DIP dip) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.dipPermissionsUpdated(dip));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyTransferredResourceDeleted(String transferredResourceID) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.transferredResourceDeleted(transferredResourceID));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRiskCreatedOrUpdated(Risk risk, int incidences, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.riskCreatedOrUpdated(risk, incidences, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRiskDeleted(String riskId, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.riskDeleted(riskId, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRiskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.riskIncidenceCreatedOrUpdated(riskIncidence, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRiskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.riskIncidenceDeleted(riskIncidenceId, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRepresentationInformationCreatedOrUpdated(RepresentationInformation ri,
    boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.representationInformationCreatedOrUpdated(ri, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyRepresentationInformationDeleted(String representationInformationId,
    boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.representationInformationDeleted(representationInformationId, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyFormatCreatedOrUpdated(Format f, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.formatCreatedOrUpdated(f, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyFormatDeleted(String formatId, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.formatDeleted(formatId, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyNotificationCreatedOrUpdated(Notification notification) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.notificationCreatedOrUpdated(notification));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyNotificationDeleted(String notificationId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.notificationDeleted(notificationId));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDIPCreated(DIP dip, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.dipCreated(dip, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDIPUpdated(DIP dip, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.dipUpdated(dip, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDIPDeleted(String dipId, boolean commit) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.dipDeleted(dipId, commit));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDIPFileCreated(DIPFile file) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.dipFileCreated(file));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDIPFileUpdated(DIPFile file) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.dipFileUpdated(file));
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyDIPFileDeleted(String dipId, List<String> path, String fileId) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      wrapper.addToList(observer.dipFileDeleted(dipId, path, fileId));
    }
    return wrapper;
  }
}
