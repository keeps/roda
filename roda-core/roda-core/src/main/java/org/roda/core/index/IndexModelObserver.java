/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationFilePreservationObject;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.AgentMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPluginUtils;
import org.roda.core.storage.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
    indexAIP(aip);
    indexRepresentations(aip);
    // indexPreservationFileObjects(aip);
    indexPreservationsEvents(aip);
    indexOtherMetadata(aip);
  }

  private void indexPreservationsEvents(final AIP aip) {
    final Map<String, List<String>> preservationEventsIds = aip.getPreservationsEventsIds();
    for (Map.Entry<String, List<String>> eventEntry : preservationEventsIds.entrySet()) {
      try {
        for (String fileId : eventEntry.getValue()) {
          StoragePath filePath = ModelUtils.getPreservationFilePath(aip.getId(), eventEntry.getKey(), fileId);
          Binary binary = model.getStorage().getBinary(filePath);

          SolrInputDocument premisEventDocument = SolrUtils.premisToSolr(aip.getId(), eventEntry.getKey(), fileId,
            binary);
          LOGGER.debug(premisEventDocument.toString());
          index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
        }
      } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException e) {
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

  // private void indexPreservationFileObjects(final AIP aip) {
  // final Map<String, List<String>> preservationFileObjectsIds =
  // aip.getPreservationFileObjectsIds();
  // for (Map.Entry<String, List<String>> eventPreservationMap :
  // preservationFileObjectsIds.entrySet()) {
  // try {
  // for (String fileId : eventPreservationMap.getValue()) {
  //
  // StoragePath filePath = ModelUtils.getPreservationFilePath(aip.getId(),
  // eventPreservationMap.getKey(), fileId);
  // Binary binary = model.getStorage().getBinary(filePath);
  // SolrInputDocument premisFileDocument = SolrUtils.premisToSolr(aip.getId(),
  // eventPreservationMap.getKey(),
  // fileId, binary);
  // index.add(RodaConstants.INDEX_PRESERVATION_OBJECTS, premisFileDocument);
  // }
  // } catch (SolrServerException | IOException | RequestNotValidException |
  // GenericException | NotFoundException
  // | AuthorizationDeniedException e) {
  // LOGGER.error("Could not index premis object", e);
  // }
  // try {
  // index.commit(RodaConstants.INDEX_PRESERVATION_OBJECTS);
  // } catch (SolrServerException | IOException e) {
  // LOGGER.error("Could not commit indexed representations", e);
  // }
  // }
  // }

  private void indexRepresentations(final AIP aip) {
    final List<String> representationIds = aip.getRepresentationIds();
    for (String representationId : representationIds) {
      try {
        Representation representation = model.retrieveRepresentation(aip.getId(), representationId);
        SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(representation);
        index.add(RodaConstants.INDEX_REPRESENTATION, representationDocument);

        if (representation.getFileIds() != null && !representation.getFileIds().isEmpty()) {
          for (String fileId : representation.getFileIds()) {
            File file = model.retrieveFile(aip.getId(), representationId, fileId);
            indexFile(file, false);
          }
        }

      } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException e) {
        LOGGER.error("Could not index representation", e);
      }
    }
    try {
      index.commit(RodaConstants.INDEX_REPRESENTATION);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commit indexed representations", e);
    }
    try {
      index.commit(RodaConstants.INDEX_FILE);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commit indexed files", e);
    }
  }

  private void indexFile(File file, boolean commit) {
    RepresentationFilePreservationObject premisFile = null;
    try {
      // TODO remove file id PREMIS suffix
      premisFile = PremisUtils.getPremisFile(model.getStorage(), file.getAipId(), file.getRepresentationId(),
        file.getId() + ".premis.xml");
    } catch (NotFoundException e) {
      LOGGER.warn("On indexing representations, did not find PREMIS for file: " + file);
    } catch (PremisMetadataException | RODAException | IOException e) {
      LOGGER.warn("On indexing representations, error loading PREMIS for file: " + file, e);
    }

    String fulltext = null;
    try {
      OtherMetadata fulltextMetadata = model.retrieveOtherMetadata(file.getAipId(), file.getRepresentationId(),
        file.getId() + TikaFullTextPlugin.OUTPUT_EXT, TikaFullTextPlugin.APP_NAME);
      Binary fulltextBinary = model.getStorage().getBinary(fulltextMetadata.getStoragePath());
      Map<String, String> properties = TikaFullTextPluginUtils
        .extractPropertiesFromResult(fulltextBinary.getContent().createInputStream());
      fulltext = properties.get(RodaConstants.FILE_FULLTEXT);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | ParserConfigurationException
      | IOException | SAXException e) {
      LOGGER.warn("Error getting fulltext for file: " + file, e);
    } catch (NotFoundException e) {
      LOGGER.debug("Fulltext not found for file: " + file);
    }

    SolrInputDocument fileDocument = SolrUtils.fileToSolrDocument(file, premisFile, fulltext);
    try {
      index.add(RodaConstants.INDEX_FILE, fileDocument);
      if (commit) {
        index.commit(RodaConstants.INDEX_FILE);
      }
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index file: " + file, e);
    }
  }

  private void indexAIP(final AIP aip) {
    indexAIP(aip, false);
  }

  private void indexAIP(final AIP aip, boolean safemode) {
    try {
      SolrInputDocument aipDoc = SolrUtils.aipToSolrInputDocument(aip, model, safemode);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
      index.commit(RodaConstants.INDEX_AIP);
      LOGGER.debug("Adding AIP: " + aipDoc);
    } catch (SolrException | SolrServerException | IOException | RequestNotValidException | GenericException
      | NotFoundException | AuthorizationDeniedException e) {
      if (!safemode) {
        LOGGER.error("Error indexing AIP, trying safe mode", e);
        indexAIP(aip, true);
      } else {
        LOGGER.error("Could not index created AIP", e);
      }

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

    // TODO delete included representations, descriptive metadata and other
  }

  @Override
  public void descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    // re-index whole AIP
    try {
      aipUpdated(model.retrieveAIP(descriptiveMetadata.getAipId()));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when descriptive metadata created on retrieving the full AIP", e);
    }
  }

  @Override
  public void descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    // re-index whole AIP
    try {
      aipUpdated(model.retrieveAIP(descriptiveMetadata.getAipId()));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when descriptive metadata created on retrieving the full AIP", e);
    }

  }

  @Override
  public void descriptiveMetadataDeleted(String aipId, String descriptiveMetadataBinaryId) {
    // re-index whole AIP
    try {
      aipUpdated(model.retrieveAIP(aipId));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when descriptive metadata created on retrieving the full AIP", e);
    }

  }

  @Override
  public void representationCreated(Representation representation) {
    addDocumentToIndex(RodaConstants.INDEX_REPRESENTATION, SolrUtils.representationToSolrDocument(representation),
      "Error creating Representation");
  }

  @Override
  public void representationUpdated(Representation representation) {
    representationDeleted(representation.getAipId(), representation.getId());
    representationCreated(representation);
  }

  @Override
  public void representationDeleted(String aipId, String representationId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_REPRESENTATION, SolrUtils.getId(aipId, representationId),
      "Error deleting Representation (aipId=" + aipId + "; representationId=" + representationId + ")");
  }

  @Override
  public void fileCreated(File file) {
    indexFile(file, true);
  }

  @Override
  public void fileUpdated(File file) {
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
      AIP aip = model.retrieveAIP(preservationMetadata.getAipId());
      indexAIP(aip);

      Binary binary = model.getStorage().getBinary(preservationMetadata.getStoragePath());
      SolrInputDocument premisFileDocument = SolrUtils.premisToSolr(preservationMetadata.getAipId(),
        preservationMetadata.getRepresentationID(), preservationMetadata.getId(), binary);

      String type = preservationMetadata.getType();
      if (type.equalsIgnoreCase("event")) {
        index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisFileDocument);
      } else if (type.equalsIgnoreCase("agent")) {
        index.add(RodaConstants.INDEX_PRESERVATION_AGENTS, premisFileDocument);
      }
      // TODO reindex file...

      // aipUpdated(model.retrieveAIP(preservationMetadata.getAipId()));
    } catch (IOException | SolrServerException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      LOGGER.error("Error when preservation metadata created on retrieving the full AIP", e);
    }

  }

  @Override
  public void preservationMetadataUpdated(PreservationMetadata preservationMetadata) {
    try {
      aipUpdated(model.retrieveAIP(preservationMetadata.getAipId()));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when preservation metadata updated on retrieving the full AIP", e);
    }
  }

  @Override
  public void preservationMetadataDeleted(String aipId, String representationId, String preservationMetadataBinaryId) {
    try {
      aipUpdated(model.retrieveAIP(aipId));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
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
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
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

  @Override
  public void jobReportCreated(JobReport jobReport) {
    addDocumentToIndex(RodaConstants.INDEX_JOB_REPORT, SolrUtils.jobReportToSolrDocument(jobReport),
      "Error creating Job Report");

  }

  @Override
  public void jobReportUpdated(JobReport jobReport) {
    jobReportDeleted(jobReport.getId());
    jobReportCreated(jobReport);
  }

  @Override
  public void jobReportDeleted(String jobReportId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_JOB_REPORT, jobReportId,
      "Error deleting Job Report(id=" + jobReportId + ")");
  }

}
