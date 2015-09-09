package org.roda.model;

import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.core.data.v2.User;

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

  public void sipReportCreated(SIPReport sipReport);

  public void sipReportUpdated(SIPReport sipReport);

  public void sipReportDeleted(String sipReportId);

  public void userCreated(User user);

  public void userUpdated(User user);

  public void userDeleted(String userID);

  public void groupCreated(Group group);

  public void groupUpdated(Group group);

  public void groupDeleted(String groupID);

  public void preservationMetadataCreated(PreservationMetadata preservationMetadataBinary);

  public void preservationMetadataUpdated(PreservationMetadata preservationMetadataBinary);

  public void preservationMetadataDeleted(String aipId, String representationId, String preservationMetadataBinaryId);

}
