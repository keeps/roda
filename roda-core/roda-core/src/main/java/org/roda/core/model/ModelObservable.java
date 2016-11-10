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

  protected void notifyAipCreated(AIP aip) {
    for (ModelObserver observer : observers) {
      observer.aipCreated(aip);
    }
  }

  protected void notifyAipUpdated(AIP aip) {
    for (ModelObserver observer : observers) {
      observer.aipUpdated(aip);
    }
  }

  protected void notifyAipMoved(AIP aip, String oldParentId, String newParentId) {
    for (ModelObserver observer : observers) {
      observer.aipMoved(aip, oldParentId, newParentId);
    }
  }

  protected void notifyAipStateUpdated(AIP aip) {
    for (ModelObserver observer : observers) {
      observer.aipStateUpdated(aip);
    }
  }

  protected void notifyAipDeleted(String aipId) {
    for (ModelObserver observer : observers) {
      observer.aipDeleted(aipId, true);
    }
  }

  protected void notifyDescriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    for (ModelObserver observer : observers) {
      observer.descriptiveMetadataCreated(descriptiveMetadata);
    }
  }

  protected void notifyDescriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    for (ModelObserver observer : observers) {
      observer.descriptiveMetadataUpdated(descriptiveMetadata);
    }
  }

  protected void notifyDescriptiveMetadataDeleted(String aipId, String representationId,
    String descriptiveMetadataBinaryId) {
    for (ModelObserver observer : observers) {
      observer.descriptiveMetadataDeleted(aipId, representationId, descriptiveMetadataBinaryId);
    }
  }

  protected void notifyRepresentationCreated(Representation representation) {
    for (ModelObserver observer : observers) {
      observer.representationCreated(representation);
    }
  }

  protected void notifyRepresentationUpdated(Representation representation) {
    for (ModelObserver observer : observers) {
      observer.representationUpdated(representation);
    }
  }

  protected void notifyRepresentationDeleted(String aipId, String representationId) {
    for (ModelObserver observer : observers) {
      observer.representationDeleted(aipId, representationId, true);
    }
  }

  protected void notifyFileCreated(File file) {
    for (ModelObserver observer : observers) {
      observer.fileCreated(file);
    }
  }

  protected void notifyFileUpdated(File file) {
    for (ModelObserver observer : observers) {
      observer.fileUpdated(file);
    }
  }

  protected void notifyFileDeleted(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) {
    for (ModelObserver observer : observers) {
      observer.fileDeleted(aipId, representationId, fileDirectoryPath, fileId, true);
    }
  }

  protected void notifyLogEntryCreated(LogEntry entry) {
    for (ModelObserver observer : observers) {
      observer.logEntryCreated(entry);
    }
  }

  protected void notifyUserCreated(User user) {
    for (ModelObserver observer : observers) {
      observer.userCreated(user);
    }
  }

  protected void notifyUserUpdated(User user) {
    for (ModelObserver observer : observers) {
      observer.userUpdated(user);
    }
  }

  protected void notifyUserDeleted(String userID) {
    for (ModelObserver observer : observers) {
      observer.userDeleted(userID);
    }
  }

  protected void notifyGroupCreated(Group group) {
    for (ModelObserver observer : observers) {
      observer.groupCreated(group);
    }
  }

  protected void notifyGroupUpdated(Group group) {
    for (ModelObserver observer : observers) {
      observer.groupUpdated(group);
    }
  }

  protected void notifyGroupDeleted(String groupID) {
    for (ModelObserver observer : observers) {
      observer.groupDeleted(groupID);
    }
  }

  protected void notifyPreservationMetadataCreated(PreservationMetadata preservationMetadataBinary) {
    for (ModelObserver observer : observers) {
      observer.preservationMetadataCreated(preservationMetadataBinary);
    }
  }

  protected void notifyPreservationMetadataUpdated(PreservationMetadata preservationMetadataBinary) {
    for (ModelObserver observer : observers) {
      observer.preservationMetadataUpdated(preservationMetadataBinary);
    }
  }

  protected void notifyPreservationMetadataDeleted(PreservationMetadata pm) {
    for (ModelObserver observer : observers) {
      observer.preservationMetadataDeleted(pm);
    }
  }

  protected void notifyOtherMetadataCreated(OtherMetadata otherMetadataBinary) {
    for (ModelObserver observer : observers) {
      observer.otherMetadataCreated(otherMetadataBinary);
    }
  }

  protected void notifyJobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    for (ModelObserver observer : observers) {
      observer.jobCreatedOrUpdated(job, reindexJobReports);
    }
  }

  protected void notifyJobDeleted(String jobId) {
    for (ModelObserver observer : observers) {
      observer.jobDeleted(jobId);
    }
  }

  protected void notifyJobReportCreatedOrUpdated(Report jobReport) {
    for (ModelObserver observer : observers) {
      observer.jobReportCreatedOrUpdated(jobReport);
    }
  }

  protected void notifyJobReportDeleted(String jobReportId) {
    for (ModelObserver observer : observers) {
      observer.jobReportDeleted(jobReportId);
    }
  }

  protected void notifyAipPermissionsUpdated(AIP aip) {
    for (ModelObserver observer : observers) {
      observer.aipPermissionsUpdated(aip);
    }
  }

  protected void notifyTransferredResourceDeleted(String transferredResourceID) {
    for (ModelObserver observer : observers) {
      observer.transferredResourceDeleted(transferredResourceID);
    }
  }

  protected void notifyRiskCreatedOrUpdated(Risk risk, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.riskCreatedOrUpdated(risk, commit);
    }
  }

  protected void notifyRiskDeleted(String riskId, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.riskDeleted(riskId, commit);
    }
  }

  protected void notifyRiskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.riskIncidenceCreatedOrUpdated(riskIncidence, commit);
    }
  }

  protected void notifyRiskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.riskIncidenceDeleted(riskIncidenceId, commit);
    }
  }

  protected void notifyFormatCreatedOrUpdated(Format format, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.formatCreatedOrUpdated(format, commit);
    }
  }

  protected void notifyFormatDeleted(String formatId, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.formatDeleted(formatId, commit);
    }
  }

  protected void notifyNotificationCreatedOrUpdated(Notification notification) {
    for (ModelObserver observer : observers) {
      observer.notificationCreatedOrUpdated(notification);
    }
  }

  protected void notifyNotificationDeleted(String notificationId) {
    for (ModelObserver observer : observers) {
      observer.notificationDeleted(notificationId);
    }
  }

  public void notifyDIPCreated(DIP dip, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.dipCreated(dip, commit);
    }
  }

  protected void notifyDIPUpdated(DIP dip, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.dipUpdated(dip, commit);
    }
  }

  protected void notifyDIPDeleted(String dipId, boolean commit) {
    for (ModelObserver observer : observers) {
      observer.dipDeleted(dipId, commit);
    }
  }

  protected void notifyDIPFileCreated(DIPFile file) {
    for (ModelObserver observer : observers) {
      observer.dipFileCreated(file);
    }
  }

  protected void notifyDIPFileUpdated(DIPFile file) {
    for (ModelObserver observer : observers) {
      observer.dipFileUpdated(file);
    }
  }

  protected void notifyDIPFileDeleted(String dipId, List<String> path, String fileId) {
    for (ModelObserver observer : observers) {
      observer.dipFileDeleted(dipId, path, fileId);
    }
  }
}
