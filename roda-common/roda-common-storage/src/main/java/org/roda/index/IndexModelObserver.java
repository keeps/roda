/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.common.RodaConstants;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.SIPReport;
import org.roda.core.data.v2.User;
import org.roda.index.utils.SolrUtils;
import org.roda.model.AIP;
import org.roda.model.AgentMetadata;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.File;
import org.roda.model.ModelObserver;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.OtherMetadata;
import org.roda.model.PreservationMetadata;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class IndexModelObserver implements ModelObserver {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexModelObserver.class);

  private final SolrClient index;
  private final ModelService model;
  private final Path configBasePath;

  public IndexModelObserver(SolrClient index, ModelService model, Path configBasePath) {
    super();
    this.index = index;
    this.model = model;
    this.configBasePath = configBasePath;
  }

  @Override
  public void aipCreated(final AIP aip) {
    indexAIPandSDO(aip, configBasePath);
    indexRepresentations(aip);
    indexPreservationFileObjects(aip, configBasePath);
    indexPreservationsEvents(aip, configBasePath);
    indexOtherMetadata(aip);
  }

  private void indexPreservationsEvents(final AIP aip, Path configBasePath) {
    final Map<String, List<String>> preservationEventsIds = aip.getPreservationsEventsIds();
    for (Map.Entry<String, List<String>> representationPreservationMap : preservationEventsIds.entrySet()) {
      try {
        for (String fileId : representationPreservationMap.getValue()) {
          StoragePath filePath = ModelUtils.getPreservationFilePath(aip.getId(), representationPreservationMap.getKey(),
            fileId);
          Binary binary = model.getStorage().getBinary(filePath);

          SolrInputDocument premisEventDocument = SolrUtils.premisToSolr(aip.getId(),
            representationPreservationMap.getKey(), fileId, binary, configBasePath);
          index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
        }
      } catch (SolrServerException | IOException | StorageServiceException | IndexServiceException e) {
        LOGGER.error("Could not index premis event", e);
      }
      try {
        index.commit(RodaConstants.INDEX_PRESERVATION_EVENTS);
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Could not commit indexed representations", e);
      }
    }
  }

  private void indexOtherMetadata(final AIP aip) {
    // TODO...
  }

  private void indexPreservationFileObjects(final AIP aip, Path configBasePath) {
    final Map<String, List<String>> preservationFileObjectsIds = aip.getPreservationFileObjectsIds();
    for (Map.Entry<String, List<String>> eventPreservationMap : preservationFileObjectsIds.entrySet()) {
      try {
        for (String fileId : eventPreservationMap.getValue()) {

          StoragePath filePath = ModelUtils.getPreservationFilePath(aip.getId(), eventPreservationMap.getKey(), fileId);
          Binary binary = model.getStorage().getBinary(filePath);
          SolrInputDocument premisFileDocument = SolrUtils.premisToSolr(aip.getId(), eventPreservationMap.getKey(),
            fileId, binary, configBasePath);
          index.add(RodaConstants.INDEX_PRESERVATION_OBJECTS, premisFileDocument);
        }
      } catch (SolrServerException | IOException | StorageServiceException | IndexServiceException e) {
        LOGGER.error("Could not index premis object", e);
      }
      try {
        index.commit(RodaConstants.INDEX_PRESERVATION_OBJECTS);
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Could not commit indexed representations", e);
      }
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

  private void indexAIPandSDO(final AIP aip, Path configBasePath) {
    try {
      SolrInputDocument aipDoc = SolrUtils.aipToSolrInputDocument(aip);
      SolrInputDocument sdoDoc = SolrUtils.aipToSolrInputDocumentAsSDO(aip, model, configBasePath);
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
      LOGGER.error("Error deleting representation (aipId=" + aipId + "; representationId=" + representationId + ")", e);
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
      LOGGER.error("Error deleting SIP report (id=" + sipReportId + ")", e);
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
      LOGGER.error("Error deleting User (id=" + userID + ")", e);
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
      LOGGER.error("Error deleting Group (id=" + groupID + ")", e);
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
    // TODO: handle deleting

  }

  @Override
  public void otherMetadataCreated(OtherMetadata otherMetadataBinary) {
    /// re-index whole AIP
    try {
      aipUpdated(model.retrieveAIP(otherMetadataBinary.getAipId()));
    } catch (ModelServiceException e) {
      LOGGER.error("Error when other metadata created on retrieving the full AIP", e);
    }

  }
}
