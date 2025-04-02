package org.roda.core.model;

import org.roda.core.common.ReturnWithExceptionsWrapper;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;

import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ModelObservable {
    void addModelObserver(ModelObserver observer);

    void removeModelObserver(ModelObserver observer);

    ReturnWithExceptionsWrapper notifyAipCreated(AIP aip);

    ReturnWithExceptionsWrapper notifyAipUpdated(AIP aip);

    ReturnWithExceptionsWrapper notifyAipUpdatedOnChanged(AIP aip);

    ReturnWithExceptionsWrapper notifyAipDestroyed(AIP aip);

    ReturnWithExceptionsWrapper notifyAipMoved(AIP aip, String oldParentId, String newParentId);

    ReturnWithExceptionsWrapper notifyAipStateUpdated(AIP aip);

    ReturnWithExceptionsWrapper notifyAipInstanceIdUpdated(AIP aip);

    ReturnWithExceptionsWrapper notifyAipDeleted(String aipId);

    ReturnWithExceptionsWrapper notifyDescriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata);

    ReturnWithExceptionsWrapper notifyDescriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata);

    ReturnWithExceptionsWrapper notifyDescriptiveMetadataDeleted(String aipId, String representationId,
                                                                 String descriptiveMetadataBinaryId);

    ReturnWithExceptionsWrapper notifyRepresentationCreated(Representation representation);

    ReturnWithExceptionsWrapper notifyRepresentationUpdated(Representation representation);

    ReturnWithExceptionsWrapper notifyRepresentationDeleted(String aipId, String representationId);

    ReturnWithExceptionsWrapper notifyRepresentationUpdatedOnChanged(Representation representation);

    ReturnWithExceptionsWrapper notifyFileCreated(File file);

    ReturnWithExceptionsWrapper notifyFileUpdated(File file);

    ReturnWithExceptionsWrapper notifyFileDeleted(String aipId, String representationId,
                                                  List<String> fileDirectoryPath, String fileId);

    ReturnWithExceptionsWrapper notifyLogEntryCreated(LogEntry entry);

    ReturnWithExceptionsWrapper notifyUserCreated(User user);

    ReturnWithExceptionsWrapper notifyUserUpdated(User user);

    ReturnWithExceptionsWrapper notifyUserDeleted(String userID);

    ReturnWithExceptionsWrapper notifyGroupCreated(Group group);

    ReturnWithExceptionsWrapper notifyGroupUpdated(Group group);

    ReturnWithExceptionsWrapper notifyGroupDeleted(String groupID);

    ReturnWithExceptionsWrapper notifyPreservationMetadataCreated(
            PreservationMetadata preservationMetadataBinary);

    ReturnWithExceptionsWrapper notifyPreservationMetadataUpdated(
            PreservationMetadata preservationMetadataBinary);

    ReturnWithExceptionsWrapper notifyPreservationMetadataDeleted(PreservationMetadata pm);

    ReturnWithExceptionsWrapper notifyOtherMetadataCreated(OtherMetadata otherMetadataBinary);

    ReturnWithExceptionsWrapper notifyJobCreatedOrUpdated(Job job, boolean reindexJobReports);

    ReturnWithExceptionsWrapper notifyJobDeleted(String jobId);

    ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, Job cachedJob);

    ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, IndexedJob indexedJob);

    ReturnWithExceptionsWrapper notifyJobReportDeleted(String jobReportId);

    ReturnWithExceptionsWrapper notifyAipPermissionsUpdated(AIP aip);

    ReturnWithExceptionsWrapper notifyDipPermissionsUpdated(DIP dip);

    ReturnWithExceptionsWrapper notifyDipInstanceIdUpdated(DIP dip);

    ReturnWithExceptionsWrapper notifyTransferredResourceDeleted(String transferredResourceID);

    ReturnWithExceptionsWrapper notifyRiskCreatedOrUpdated(Risk risk, int incidences, boolean commit);

    ReturnWithExceptionsWrapper notifyRiskDeleted(String riskId, boolean commit);

    ReturnWithExceptionsWrapper notifyRiskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit);

    ReturnWithExceptionsWrapper notifyRiskIncidenceDeleted(String riskIncidenceId, boolean commit);

    ReturnWithExceptionsWrapper notifyRepresentationInformationCreatedOrUpdated(RepresentationInformation ri,
                                                                                boolean commit);

    ReturnWithExceptionsWrapper notifyRepresentationInformationDeleted(String representationInformationId,
                                                                       boolean commit);

    ReturnWithExceptionsWrapper notifyNotificationCreatedOrUpdated(Notification notification);

    ReturnWithExceptionsWrapper notifyNotificationDeleted(String notificationId);

    ReturnWithExceptionsWrapper notifyDIPCreated(DIP dip, boolean commit);

    ReturnWithExceptionsWrapper notifyDIPUpdated(DIP dip, boolean commit);

    ReturnWithExceptionsWrapper notifyDIPDeleted(String dipId, boolean commit);

    ReturnWithExceptionsWrapper notifyDIPFileCreated(DIPFile file);

    ReturnWithExceptionsWrapper notifyDIPFileUpdated(DIPFile file);

    ReturnWithExceptionsWrapper notifyDIPFileDeleted(String dipId, List<String> path, String fileId);

    ReturnWithExceptionsWrapper notifyDisposalConfirmationCreatedOrUpdated(DisposalConfirmation confirmation);

    ReturnWithExceptionsWrapper notifyDisposalConfirmationDeleted(String disposalConfirmationId, boolean commit);
}
