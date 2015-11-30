/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.SIPReport;
import org.roda.core.data.v2.SimpleFile;
import org.roda.core.data.v2.User;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.AIP;
import org.roda.core.model.AgentMetadata;
import org.roda.core.model.DescriptiveMetadata;
import org.roda.core.model.File;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.OtherMetadata;
import org.roda.core.model.PreservationMetadata;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageServiceException;
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

        if (representation.getFileIds() != null && representation.getFileIds().size() > 0) {
          for (String fileId : representation.getFileIds()) {
            File file = model.retrieveFile(aip.getId(), representationId, fileId);
            if(file.getFileFormat()==null){
              LOGGER.error("FILE FORMAT NULL");
            }else{
              if(file.getFileFormat().getMimeType()==null){
                LOGGER.error("MIME TYPE NULL");
              }else{
                LOGGER.error("MIMETYPE: "+file.getFileFormat().getMimeType());
              }
            }
            
            SolrInputDocument fileDocument = SolrUtils.fileToSolrDocument(file);
            index.add(RodaConstants.INDEX_FILE, fileDocument);
          }
        }

      } catch (SolrServerException | IOException | ModelServiceException e) {
        LOGGER.error("Could not index representation", e);
      }
    }
    try {
      index.commit(RodaConstants.INDEX_REPRESENTATIONS);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commit indexed representations", e);
    }
    try {
      index.commit(RodaConstants.INDEX_FILE);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commit indexed files", e);
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
    deleteDocumentFromIndex(RodaConstants.INDEX_AIP, aipId,
      "Error deleting AIP (from " + RodaConstants.INDEX_AIP + ")");

    deleteDocumentFromIndex(RodaConstants.INDEX_SDO, aipId,
      "Error deleting AIP (from " + RodaConstants.INDEX_SDO + ")");

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
    addDocumentToIndex(RodaConstants.INDEX_REPRESENTATIONS, SolrUtils.representationToSolrDocument(representation),
      "Error creating Representation");
  }

  @Override
  public void representationUpdated(Representation representation) {
    representationDeleted(representation.getAipId(), representation.getId());
    representationCreated(representation);
  }

  @Override
  public void representationDeleted(String aipId, String representationId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_REPRESENTATIONS, SolrUtils.getId(aipId, representationId),
      "Error deleting Representation (aipId=" + aipId + "; representationId=" + representationId + ")");
  }

  @Override
  public void fileCreated(SimpleFile file) {
    addDocumentToIndex(RodaConstants.INDEX_FILE, SolrUtils.fileToSolrDocument(file), "Error creating File");
  }

  @Override
  public void fileUpdated(SimpleFile file) {
    fileDeleted(file.getAipId(), file.getRepresentationId(), file.getId());
    fileCreated(file);

  }

  @Override
  public void fileDeleted(String aipId, String representationId, String fileId) {
    String id = SolrUtils.getId(aipId, representationId, fileId);
    deleteDocumentFromIndex(RodaConstants.INDEX_FILE, id, "Error deleting File (id=" + id + ")");

  }

  @Override
  public void logEntryCreated(LogEntry entry) {
    addDocumentToIndex(RodaConstants.INDEX_ACTION_LOG, SolrUtils.logEntryToSolrDocument(entry),
      "Error creating Log entry");
  }

  @Override
  public void sipReportCreated(SIPReport sipReport) {
    addDocumentToIndex(RodaConstants.INDEX_SIP_REPORT, SolrUtils.sipReportToSolrDocument(sipReport),
      "Error creating SIP report");
  }

  @Override
  public void sipReportUpdated(SIPReport sipReport) {
    sipReportDeleted(sipReport.getId());
    sipReportCreated(sipReport);
  }

  @Override
  public void sipReportDeleted(String sipReportId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_SIP_REPORT, sipReportId,
      "Error deleting SIP report (id=" + sipReportId + ")");
  }

  @Override
  public void userCreated(User user) {
    addDocumentToIndex(RodaConstants.INDEX_MEMBERS, SolrUtils.rodaMemberToSolrDocument(user), "Error creating User");
  }

  @Override
  public void userUpdated(User user) {
    userDeleted(user.getId());
    userCreated(user);
  }

  @Override
  public void userDeleted(String userID) {
    deleteDocumentFromIndex(RodaConstants.INDEX_MEMBERS, userID, "Error deleting User (id=" + userID + ")");
  }

  @Override
  public void groupCreated(Group group) {
    addDocumentToIndex(RodaConstants.INDEX_MEMBERS, SolrUtils.rodaMemberToSolrDocument(group), "Error creating Group");
  }

  @Override
  public void groupUpdated(Group group) {
    groupDeleted(group.getId());
    groupCreated(group);
  }

  @Override
  public void groupDeleted(String groupID) {
    deleteDocumentFromIndex(RodaConstants.INDEX_MEMBERS, groupID, "Error deleting Group (id=" + groupID + ")");
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
    try {
      indexOtherMetadata(model.retrieveAIP(otherMetadataBinary.getAipId()));
    } catch (ModelServiceException e) {
      LOGGER.error("Error when other metadata created on retrieving the full AIP", e);
    }
  }

  @Override
  public void jobCreated(Job job) {
    addDocumentToIndex(RodaConstants.INDEX_JOB, SolrUtils.jobToSolrDocument(job), "Error creating Job");
  }

  @Override
  public void jobUpdated(Job job) {
    jobDeleted(job.getId());
    jobCreated(job);
  }

  @Override
  public void jobDeleted(String jobId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_JOB, jobId, "Error deleting Job (id=" + jobId + ")");

  }

  private void addDocumentToIndex(String indexName, SolrInputDocument document, String errorLogMessage) {
    try {
      index.add(indexName, document);
      index.commit(indexName);
    } catch (SolrServerException | IOException e) {
      LOGGER.error(errorLogMessage, e);
    }
  }

  private void deleteDocumentFromIndex(String indexName, String documentId, String errorLogMessage) {
    try {
      index.deleteById(indexName, documentId);
      index.commit(indexName);
    } catch (SolrServerException | IOException e) {
      LOGGER.error(errorLogMessage, e);
    }
  }
}
