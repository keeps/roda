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

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.Risk;
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

  protected void notifyAipDeleted(String aipId) {
    for (ModelObserver observer : observers) {
      observer.aipDeleted(aipId);
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

  protected void notifyDescriptiveMetadataDeleted(String aipId, String descriptiveMetadataBinaryId) {
    for (ModelObserver observer : observers) {
      observer.descriptiveMetadataDeleted(aipId, descriptiveMetadataBinaryId);
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
      observer.representationDeleted(aipId, representationId);
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
      observer.fileDeleted(aipId, representationId, fileDirectoryPath, fileId);
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

  protected void notifyJobCreatedOrUpdated(Job job) {
    for (ModelObserver observer : observers) {
      observer.jobCreatedOrUpdated(job);
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

  protected void notifyRiskCreatedOrUpdated(Risk risk, boolean forceCommit) {
    for (ModelObserver observer : observers) {
      observer.riskCreatedOrUpdated(risk, forceCommit);
    }
  }

  protected void notifyRiskDeleted(String riskId) {
    for (ModelObserver observer : observers) {
      observer.riskDeleted(riskId);
    }
  }
}
