/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.util.List;

import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.JobReport;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.SimpleFile;
import org.roda.core.data.v2.User;

public interface ModelObserver {

  public void aipCreated(AIP aip);

  public void aipUpdated(AIP aip);

  public void aipDeleted(String aipId);

  public void descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadataBinary);

  public void descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadataBinary);

  public void descriptiveMetadataDeleted(String aipId, String descriptiveMetadataBinaryId);

  public void representationCreated(Representation representation);

  public void representationUpdated(Representation representation);

  public void representationDeleted(String aipId, String representationId);

  public void fileCreated(File file);

  public void fileUpdated(File file);

  public void fileDeleted(String aipId, String representationId, String fileId);

  public void logEntryCreated(LogEntry entry);

  public void userCreated(User user);

  public void userUpdated(User user);

  public void userDeleted(String userID);

  public void groupCreated(Group group);

  public void groupUpdated(Group group);

  public void groupDeleted(String groupID);

  public void preservationMetadataCreated(PreservationMetadata preservationMetadataBinary);

  public void preservationMetadataUpdated(PreservationMetadata preservationMetadataBinary);

  public void preservationMetadataDeleted(String aipId, String representationId, String preservationMetadataBinaryId);

  public void agentMetadataCreated(AgentMetadata agentMetadataBinary);

  public void agentMetadataUpdated(AgentMetadata agentMetadataBinary);

  public void agentMetadataDeleted(String agentMetadataBinaryId);

  public void otherMetadataCreated(OtherMetadata otherMetadataBinary);

  public void jobCreated(Job job);

  public void jobUpdated(Job job);

  public void jobDeleted(String jobId);

  public void jobReportCreated(JobReport jobReport);

  public void jobReportUpdated(JobReport jobReport);

  public void jobReportDeleted(String jobReportId);

  public void notifyUpdateFileFormats(List<SimpleFile> updatedFiles);

}
