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
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;

public abstract class ModelObservable {
  private final List<ModelObserver> observers;

  public ModelObservable() {
    super();
    this.observers = new ArrayList<ModelObserver>();
  }

  public void addModelObserver(ModelObserver observer) {
    observers.add(observer);
  }

  public void removeModelObserver(ModelObserver observer) {
    observers.remove(observer);
  }

  public void notifyAipCreated(AIP aip) {
    for (ModelObserver observer : observers) {
      observer.aipCreated(aip);
    }
  }

  public void notifyAipUpdated(AIP aip) {
    for (ModelObserver observer : observers) {
      observer.aipUpdated(aip);
    }
  }

  public void notifyAipMoved(AIP aip, String oldParentId, String newParentId) {
    for (ModelObserver observer : observers) {
      observer.aipMoved(aip, oldParentId, newParentId);
    }
  }

  public void notifyAipStateUpdated(AIP aip) {
    for (ModelObserver observer : observers) {
      observer.aipStateUpdated(aip);
    }
  }

  public void notifyAipDeleted(String aipId) {
    for (ModelObserver observer : observers) {
      observer.aipDeleted(aipId, true);
    }
  }

  public void notifyDescriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    for (ModelObserver observer : observers) {
      observer.descriptiveMetadataCreated(descriptiveMetadata);
    }
  }

  public void notifyDescriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    for (ModelObserver observer : observers) {
      observer.descriptiveMetadataUpdated(descriptiveMetadata);
    }
  }

  public void notifyDescriptiveMetadataDeleted(String aipId, String representationId,
    String descriptiveMetadataBinaryId) {
    for (ModelObserver observer : observers) {
      observer.descriptiveMetadataDeleted(aipId, representationId, descriptiveMetadataBinaryId);
    }
  }

  public void notifyRepresentationCreated(Representation representation) {
    for (ModelObserver observer : observers) {
      observer.representationCreated(representation);
    }
  }

  public void notifyRepresentationUpdated(Representation representation) {
    for (ModelObserver observer : observers) {
      observer.representationUpdated(representation);
    }
  }

  public void notifyRepresentationDeleted(String aipId, String representationId) {
    for (ModelObserver observer : observers) {
      observer.representationDeleted(aipId, representationId, true);
    }
  }

  public void notifyFileCreated(File file) {
    for (ModelObserver observer : observers) {
      observer.fileCreated(file);
    }
  }

  public void notifyFileUpdated(File file) {
    for (ModelObserver observer : observers) {
      observer.fileUpdated(file);
    }
  }

  public void notifyFileDeleted(String aipId, String representationId, List<String> fileDirectoryPath, String fileId) {
    for (ModelObserver observer : observers) {
      observer.fileDeleted(aipId, representationId, fileDirectoryPath, fileId, true);
    }
  }

  public void notifyLogEntryCreated(LogEntry entry) {
    for (ModelObserver observer : observers) {
      observer.logEntryCreated(entry);
    }
  }

  public void notifyUserCreated(User user) {
    for (ModelObserver observer : observers) {
      observer.userCreated(user);
    }
  }

  public void notifyUserUpdated(User user) {
    for (ModelObserver observer : observers) {
      observer.userUpdated(user);
    }
  }

  public void notifyUserDeleted(String userID) {
    for (ModelObserver observer : observers) {
      observer.userDeleted(userID);
    }
  }

  public void notifyGroupCreated(Group group) {
    for (ModelObserver observer : observers) {
      observer.groupCreated(group);
    }
  }

  public void notifyGroupUpdated(Group group) {
    for (ModelObserver observer : observers) {
      observer.groupUpdated(group);
    }
  }

  public void notifyGroupDeleted(String groupID) {
    for (ModelObserver observer : observers) {
      observer.groupDeleted(groupID);
    }
  }

  public void notifyPreservationMetadataCreated(PreservationMetadata preservationMetadataBinary) {
    for (ModelObserver observer : observers) {
      observer.preservationMetadataCreated(preservationMetadataBinary);
    }
  }

  public void notifyPreservationMetadataUpdated(PreservationMetadata preservationMetadataBinary) {
    for (ModelObserver observer : observers) {
      observer.preservationMetadataUpdated(preservationMetadataBinary);
    }
  }

  public void notifyPreservationMetadataDeleted(PreservationMetadata pm) {
    for (ModelObserver observer : observers) {
      observer.preservationMetadataDeleted(pm);
    }
  }

  public void notifyOtherMetadataCreated(OtherMetadata otherMetadataBinary) {
    for (ModelObserver observer : observers) {
      observer.otherMetadataCreated(otherMetadataBinary);
    }
  }

  public void notifyJobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    for (ModelObserver observer : observers) {
      observer.jobCreatedOrUpdated(job, reindexJobReports);
    }
  }

  public void notifyJobDeleted(String jobId) {
    for (ModelObserver observer : observers) {
      observer.jobDeleted(jobId);
    }
  }

  public void notifyJobReportCreatedOrUpdated(Report jobReport, Job job) {
    for (ModelObserver observer : observers) {
      observer.jobReportCreatedOrUpdated(jobReport, job);
    }
  }

  public void notifyJobReportDeleted(String jobReportId) {
    for (ModelObserver observer : observers) {
      observer.jobReportDeleted(jobReportId);
    }
  }

  public void notifyAipPermissionsUpdated(AIP aip) {
    for (ModelObserver observer : observers) {
      observer.aipPermissionsUpdated(aip);
    }
  }

  public void notifyTransferredResourceDeleted(String transferredResourceID) {
    for (ModelObserver observer : observers) {
      observer.transferredResourceDeleted(transferredResourceID);
    }
  }

  public void notifyRiskCreatedOrUpdated(Risk risk, int incidences, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.riskCreatedOrUpdated(risk, incidences, commit);
    }
  }

  public void notifyRiskDeleted(String riskId, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.riskDeleted(riskId, commit);
    }
  }

  public void notifyRiskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.riskIncidenceCreatedOrUpdated(riskIncidence, commit);
    }
  }

  public void notifyRiskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.riskIncidenceDeleted(riskIncidenceId, commit);
    }
  }

  public void notifyFormatCreatedOrUpdated(Format format, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.formatCreatedOrUpdated(format, commit);
    }
  }

  public void notifyFormatDeleted(String formatId, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.formatDeleted(formatId, commit);
    }
  }

  public void notifyNotificationCreatedOrUpdated(Notification notification) {
    for (ModelObserver observer : observers) {
      observer.notificationCreatedOrUpdated(notification);
    }
  }

  public void notifyNotificationDeleted(String notificationId) {
    for (ModelObserver observer : observers) {
      observer.notificationDeleted(notificationId);
    }
  }

  public void notifyDIPCreated(DIP dip, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.dipCreated(dip, commit);
    }
  }

  public void notifyDIPUpdated(DIP dip, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.dipUpdated(dip, commit);
    }
  }

  public void notifyDIPDeleted(String dipId, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.dipDeleted(dipId, commit);
    }
  }

  public void notifyDIPFileCreated(DIPFile file) {
    for (ModelObserver observer : observers) {
      observer.dipFileCreated(file);
    }
  }

  public void notifyDIPFileUpdated(DIPFile file) {
    for (ModelObserver observer : observers) {
      observer.dipFileUpdated(file);
    }
  }

  public void notifyDIPFileDeleted(String dipId, List<String> path, String fileId) {
    for (ModelObserver observer : observers) {
      observer.dipFileDeleted(dipId, path, fileId);
    }
  }
}
