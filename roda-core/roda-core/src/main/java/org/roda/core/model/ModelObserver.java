/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.util.List;

import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.ip.*;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
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

public interface ModelObserver {

  public ReturnWithExceptions<Void, ModelObserver> aipCreated(AIP aip);

  public ReturnWithExceptions<Void, ModelObserver> aipUpdated(AIP aip);

  public ReturnWithExceptions<Void, ModelObserver> aipStateUpdated(AIP aip);

  public ReturnWithExceptions<Void, ModelObserver> aipMoved(AIP aip, String oldParentId, String newParentId);

  public ReturnWithExceptions<Void, ModelObserver> aipDeleted(String aipId, boolean deleteIncidences);

  public ReturnWithExceptions<Void, ModelObserver> descriptiveMetadataCreated(
    DescriptiveMetadata descriptiveMetadataBinary);

  public ReturnWithExceptions<Void, ModelObserver> descriptiveMetadataUpdated(
    DescriptiveMetadata descriptiveMetadataBinary);

  public ReturnWithExceptions<Void, ModelObserver> descriptiveMetadataDeleted(String aipId, String representationId,
    String descriptiveMetadataBinaryId);

  public ReturnWithExceptions<Void, ModelObserver> representationCreated(Representation representation);

  public ReturnWithExceptions<Void, ModelObserver> representationUpdated(Representation representation);

  public ReturnWithExceptions<Void, ModelObserver> representationDeleted(String aipId, String representationId,
    boolean deleteIncidences);

  public ReturnWithExceptions<Void, ModelObserver> fileCreated(File file);

  public ReturnWithExceptions<Void, ModelObserver> fileUpdated(File file);

  public ReturnWithExceptions<Void, ModelObserver> fileDeleted(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, boolean deleteIncidences);

  public ReturnWithExceptions<Void, ModelObserver> logEntryCreated(LogEntry entry);

  public ReturnWithExceptions<Void, ModelObserver> userCreated(User user);

  public ReturnWithExceptions<Void, ModelObserver> userUpdated(User user);

  public ReturnWithExceptions<Void, ModelObserver> userDeleted(String userID);

  public ReturnWithExceptions<Void, ModelObserver> groupCreated(Group group);

  public ReturnWithExceptions<Void, ModelObserver> groupUpdated(Group group);

  public ReturnWithExceptions<Void, ModelObserver> groupDeleted(String groupID);

  public ReturnWithExceptions<Void, ModelObserver> preservationMetadataCreated(
    PreservationMetadata preservationMetadataBinary);

  public ReturnWithExceptions<Void, ModelObserver> preservationMetadataUpdated(
    PreservationMetadata preservationMetadataBinary);

  public ReturnWithExceptions<Void, ModelObserver> preservationMetadataDeleted(
    PreservationMetadata preservationMetadataBinary);

  public ReturnWithExceptions<Void, ModelObserver> otherMetadataCreated(OtherMetadata otherMetadataBinary);

  public ReturnWithExceptions<Void, ModelObserver> jobCreatedOrUpdated(Job job, boolean reindexJobReports);

  public ReturnWithExceptions<Void, ModelObserver> jobDeleted(String jobId);

  public ReturnWithExceptions<Void, ModelObserver> jobReportCreatedOrUpdated(Report jobReport, Job cachedJob);

  public ReturnWithExceptions<Void, ModelObserver> jobReportDeleted(String jobReportId);

  public ReturnWithExceptions<Void, ModelObserver> aipPermissionsUpdated(AIP aip);

  public ReturnWithExceptions<Void, ModelObserver> dipPermissionsUpdated(DIP dip);

  public ReturnWithExceptions<Void, ModelObserver> transferredResourceDeleted(String transferredResourceID);

  public ReturnWithExceptions<Void, ModelObserver> riskCreatedOrUpdated(Risk risk, int incidences, boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> riskDeleted(String riskId, boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> riskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence,
    boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> riskIncidenceDeleted(String riskIncidenceId, boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> representationInformationCreatedOrUpdated(
    RepresentationInformation ri, boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> representationInformationDeleted(String representationInformationId,
    boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> notificationCreatedOrUpdated(Notification notification);

  public ReturnWithExceptions<Void, ModelObserver> notificationDeleted(String notificationId);

  public ReturnWithExceptions<Void, ModelObserver> dipCreated(DIP dip, boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> dipUpdated(DIP dip, boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> dipDeleted(String dipId, boolean commit);

  public ReturnWithExceptions<Void, ModelObserver> dipFileCreated(DIPFile file);

  public ReturnWithExceptions<Void, ModelObserver> dipFileUpdated(DIPFile file);

  public ReturnWithExceptions<Void, ModelObserver> dipFileDeleted(String dipId, List<String> path, String fileId);
}
