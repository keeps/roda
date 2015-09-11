package org.roda.index;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.index.utils.SolrUtils;
import org.roda.model.AIP;
import org.roda.model.AgentMetadata;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.File;
import org.roda.model.ModelObserver;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.PreservationMetadata;
import org.roda.storage.StorageServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.v2.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.core.data.v2.User;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class IndexModelObserver implements ModelObserver {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexModelObserver.class);

  private final SolrClient index;
  private final ModelService model;

  public IndexModelObserver(SolrClient index, ModelService model) {
    super();
    this.index = index;
    this.model = model;
  }

  @Override
  public void aipCreated(final AIP aip) {
    indexAIPandSDO(aip);
    indexRepresentations(aip);
    indexPreservationFileObjects(aip);
    indexPreservationsEvents(aip);
  }

  private void indexPreservationsEvents(final AIP aip) {
    final Map<String, List<String>> preservationEventsIds = aip.getPreservationsEventsIds();
    for (Map.Entry<String, List<String>> representationPreservationMap : preservationEventsIds.entrySet()) {
      try {
        for (String fileId : representationPreservationMap.getValue()) {
          EventPreservationObject premisEvent = model.retrieveEventPreservationObject(aip.getId(),
            representationPreservationMap.getKey(), fileId);
          String id = SolrUtils.getId(aip.getId(), representationPreservationMap.getKey(), fileId);
          SolrInputDocument premisEventDocument = SolrUtils.eventPreservationObjectToSolrDocument(id, premisEvent);
          index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
        }
      } catch (SolrServerException | IOException | ModelServiceException e) {
        LOGGER.error("Could not index premis event", e);
      }
    }
    try {
      index.commit(RodaConstants.INDEX_PRESERVATION_EVENTS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commit indexed representations", e);
    }
  }

  private void indexPreservationFileObjects(final AIP aip) {
    final Map<String, List<String>> preservationFileObjectsIds = aip.getPreservationFileObjectsIds();
    for (Map.Entry<String, List<String>> eventPreservationMap : preservationFileObjectsIds.entrySet()) {
      try {
        for (String fileId : eventPreservationMap.getValue()) {
          RepresentationFilePreservationObject premisObject = model.retrieveRepresentationFileObject(aip.getId(),
            eventPreservationMap.getKey(), fileId);
          String id = SolrUtils.getId(aip.getId(), eventPreservationMap.getKey(), fileId);
          SolrInputDocument premisObjectDocument = SolrUtils.representationFilePreservationObjectToSolrDocument(id,
            premisObject);
          index.add(RodaConstants.INDEX_PRESERVATION_OBJECTS, premisObjectDocument);
        }
      } catch (SolrServerException | IOException | ModelServiceException e) {
        LOGGER.error("Could not index premis object", e);
      }
    }
    try {
      index.commit(RodaConstants.INDEX_PRESERVATION_OBJECTS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commit indexed representations", e);
    }
  }

  private void indexRepresentations(final AIP aip) {
    final List<String> representationIds = aip.getRepresentationIds();
    for (String representationId : representationIds) {
      try {
        Representation representation = model.retrieveRepresentation(aip.getId(), representationId);
        SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(representation);
        index.add(RodaConstants.INDEX_REPRESENTATIONS, representationDocument);
      } catch (SolrServerException | IOException | ModelServiceException e) {
        LOGGER.error("Could not index representation", e);
      }
    }
    try {
      index.commit(RodaConstants.INDEX_REPRESENTATIONS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commit indexed representations", e);
    }
  }

  private void indexAIPandSDO(final AIP aip) {
    try {
      SolrInputDocument aipDoc = SolrUtils.aipToSolrInputDocument(aip);
      SolrInputDocument sdoDoc = SolrUtils.aipToSolrInputDocumentAsSDO(aip, model);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
      index.commit(RodaConstants.INDEX_AIP);
      LOGGER.trace("Adding SDO: " + sdoDoc);
      index.add(RodaConstants.INDEX_SDO, sdoDoc);
      index.commit(RodaConstants.INDEX_SDO);
    } catch (SolrServerException | IOException | ModelServiceException | StorageServiceException
      | IndexServiceException e) {
      LOGGER.error("Could not index created AIP", e);
    }
  }

  @Override
  public void aipUpdated(AIP aip) {
    // TODO Is this the best way to update?
    aipDeleted(aip.getId());
    aipCreated(aip);
  }

  // TODO Handle exceptions
  @Override
  public void aipDeleted(String aipId) {
    try {
      index.deleteById(RodaConstants.INDEX_AIP, aipId);
      index.commit(RodaConstants.INDEX_AIP);

      index.deleteById(RodaConstants.INDEX_SDO, aipId);
      index.commit(RodaConstants.INDEX_SDO);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not delete AIP from index", e);
    }

    // TODO delete included representations, descriptive metadata and other
  }

  @Override
  public void descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    // re-index whole AIP
    try {
      aipUpdated(model.retrieveAIP(descriptiveMetadata.getAipId()));
    } catch (ModelServiceException e) {
      LOGGER.error("Error when descriptive metadata created on retrieving the full AIP", e);
    }
  }

  @Override
  public void descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    // re-index whole AIP
    try {
      aipUpdated(model.retrieveAIP(descriptiveMetadata.getAipId()));
    } catch (ModelServiceException e) {
      LOGGER.error("Error when descriptive metadata created on retrieving the full AIP", e);
    }

  }

  @Override
  public void descriptiveMetadataDeleted(String aipId, String descriptiveMetadataBinaryId) {
    // re-index whole AIP
    try {
      aipUpdated(model.retrieveAIP(aipId));
    } catch (ModelServiceException e) {
      LOGGER.error("Error when descriptive metadata created on retrieving the full AIP", e);
    }

  }

  @Override
  public void representationCreated(Representation representation) {

    SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(representation);
    try {
      index.add(RodaConstants.INDEX_REPRESENTATIONS, representationDocument);
      index.commit(RodaConstants.INDEX_REPRESENTATIONS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index created representation", e);
    }

  }

  @Override
  public void representationUpdated(Representation representation) {
    representationDeleted(representation.getAipId(), representation.getId());
    representationCreated(representation);
  }

  @Override
  public void representationDeleted(String aipId, String representationId) {
    try {
      index.deleteById(RodaConstants.INDEX_REPRESENTATIONS, SolrUtils.getId(aipId, representationId));
      index.commit(RodaConstants.INDEX_REPRESENTATIONS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Error deleting representation (aipId=" + aipId + "; representationId=" + representationId + ")");
    }
  }

  @Override
  public void fileCreated(File file) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fileUpdated(File file) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fileDeleted(String aipId, String representationId, String fileId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void logEntryCreated(LogEntry entry) {
    SolrInputDocument logEntryDocument = SolrUtils.logEntryToSolrDocument(entry);
    try {
      index.add(RodaConstants.INDEX_ACTION_LOG, logEntryDocument);
      index.commit(RodaConstants.INDEX_ACTION_LOG);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index LogEntry: " + e.getMessage(), e);
    }
  }

  @Override
  public void sipReportCreated(SIPReport sipReport) {
    SolrInputDocument sipReportDocument = SolrUtils.sipReportToSolrDocument(sipReport);
    try {
      index.add(RodaConstants.INDEX_SIP_REPORT, sipReportDocument);
      index.commit(RodaConstants.INDEX_SIP_REPORT);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index SIPState: " + e.getMessage(), e);
    }
  }

  @Override
  public void sipReportUpdated(SIPReport sipReport) {
    sipReportDeleted(sipReport.getId());
    sipReportCreated(sipReport);
  }

  @Override
  public void sipReportDeleted(String sipReportId) {
    try {
      index.deleteById(RodaConstants.INDEX_SIP_REPORT, sipReportId);
      index.commit(RodaConstants.INDEX_REPRESENTATIONS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Error deleting SIP report (id=" + sipReportId + ")");
    }
  }

  @Override
  public void userCreated(User user) {
    SolrInputDocument userDocument = SolrUtils.rodaMemberToSolrDocument(user);
    try {
      index.add(RodaConstants.INDEX_MEMBERS, userDocument);
      index.commit(RodaConstants.INDEX_MEMBERS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index User ", e);
    }
  }

  @Override
  public void userUpdated(User user) {
    userDeleted(user.getId());
    userCreated(user);
  }

  @Override
  public void userDeleted(String userID) {
    try {
      index.deleteById(RodaConstants.INDEX_MEMBERS, userID);
      index.commit(RodaConstants.INDEX_MEMBERS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Error deleting User (id=" + userID + ")");
    }
  }

  @Override
  public void groupCreated(Group group) {
    SolrInputDocument groupDocument = SolrUtils.rodaMemberToSolrDocument(group);
    try {
      index.add(RodaConstants.INDEX_MEMBERS, groupDocument);
      index.commit(RodaConstants.INDEX_MEMBERS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index Group ", e);
    }
  }

  @Override
  public void groupUpdated(Group group) {
    groupDeleted(group.getId());
    groupCreated(group);
  }

  @Override
  public void groupDeleted(String groupID) {
    try {
      index.deleteById(RodaConstants.INDEX_MEMBERS, groupID);
      index.commit(RodaConstants.INDEX_MEMBERS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Error deleting Group (id=" + groupID + ")");
    }
  }

  @Override
  public void preservationMetadataCreated(PreservationMetadata preservationMetadata) {
    try {
      aipUpdated(model.retrieveAIP(preservationMetadata.getAipId()));
    } catch (ModelServiceException e) {
      LOGGER.error("Error when preservation metadata created on retrieving the full AIP", e);
    }

  }

  @Override
  public void preservationMetadataUpdated(PreservationMetadata preservationMetadata) {
    try {
      aipUpdated(model.retrieveAIP(preservationMetadata.getAipId()));
    } catch (ModelServiceException e) {
      LOGGER.error("Error when preservation metadata updated on retrieving the full AIP", e);
    }
  }

  @Override
  public void preservationMetadataDeleted(String aipId, String representationId, String preservationMetadataBinaryId) {
    try {
      aipUpdated(model.retrieveAIP(aipId));
    } catch (ModelServiceException e) {
      LOGGER.error("Error when descriptive metadata deleted on retrieving the full AIP", e);
    }
  }

  @Override
  public void agentMetadataCreated(AgentMetadata agentMetadata) {
    // TODO handle indexing...
  }

  @Override
  public void agentMetadataUpdated(AgentMetadata agentMetadata) {
    agentMetadataDeleted(agentMetadata.getId());
    agentMetadataCreated(agentMetadata);
    
  }

  @Override
  public void agentMetadataDeleted(String agentMetadataId) {
    //TODO: handle deleting
    
  }
}
