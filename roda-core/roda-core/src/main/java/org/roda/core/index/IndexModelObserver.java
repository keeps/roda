/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPluginUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
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
    for (PreservationMetadata pm : aip.getMetadata().getPreservationMetadata()) {
      if (pm.getType().equals(PreservationMetadataType.EVENT)) {
        try {

          StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(pm);
          Binary binary = model.getStorage().getBinary(filePath);

          SolrInputDocument premisEventDocument = SolrUtils.premisToSolr(aip.getId(), pm.getRepresentationId(),
            pm.getId(), binary);
          LOGGER.debug(premisEventDocument.toString());
          index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);

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
  }

  private void indexOtherMetadata(final AIP aip) {
    // TODO index other metadata
  }

  private void indexRepresentations(final AIP aip) {

    for (Representation representation : aip.getRepresentations()) {
      try {
        SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(representation);
        index.add(RodaConstants.INDEX_REPRESENTATION, representationDocument);

        ClosableIterable<File> allFiles = model.listAllFiles(aip.getId(), representation.getId());
        for (File file : allFiles) {
          boolean commit = false;
          boolean recursive = true;
          indexFile(file, commit, recursive);
        }
        allFiles.close();

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

  private void indexFile(File file, boolean commit, boolean recursive) {
    Binary premisFile = null;
    try {
      premisFile = model.retrievePreservationFile(file);
    } catch (NotFoundException e) {
      LOGGER.trace("On indexing representations, did not find PREMIS for file: " + file);
    } catch (RODAException e) {
      LOGGER.warn("On indexing representations, error loading PREMIS for file: " + file, e);
    }

    String fulltext = null;
    try {

      Binary fulltextBinary = model.retrieveOtherMetadataBinary(file.getAipId(), file.getRepresentationId(),
        file.getPath(), file.getId(), TikaFullTextPlugin.FILE_SUFFIX, TikaFullTextPlugin.OTHER_METADATA_TYPE);

      Map<String, String> properties = TikaFullTextPluginUtils
        .extractPropertiesFromResult(fulltextBinary.getContent().createInputStream());
      fulltext = properties.get(RodaConstants.FILE_FULLTEXT);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | ParserConfigurationException
      | IOException | SAXException e) {
      LOGGER.warn("Error getting fulltext for file: " + file, e);
    } catch (NotFoundException e) {
      LOGGER.trace("Fulltext not found for file: " + file);
    }

    SolrInputDocument fileDocument = SolrUtils.fileToSolrDocument(file, premisFile, fulltext);
    try {
      index.add(RodaConstants.INDEX_FILE, fileDocument);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index file: " + file, e);
    }

    if (recursive && file.isDirectory()) {
      try {
        ClosableIterable<File> allFiles = model.listAllFiles(file);
        for (File subfile : allFiles) {
          indexFile(subfile, false, false);
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Could not index file sub-resources: " + file, e);
      }
    }

    if (commit) {
      try {
        index.commit(RodaConstants.INDEX_FILE);
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Could not index file: " + file, e);
      }
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
    // XXX check if forcing auto commit is necessary
    boolean forceCommit = true;
    deleteDocumentFromIndex(RodaConstants.INDEX_AIP, aipId, "Error deleting AIP (from " + RodaConstants.INDEX_AIP + ")",
      forceCommit);

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
    // XXX check if forcing auto commit is necessary
    boolean forceCommit = true;
    addDocumentToIndex(RodaConstants.INDEX_REPRESENTATION, SolrUtils.representationToSolrDocument(representation),
      "Error creating Representation", forceCommit);
  }

  @Override
  public void representationUpdated(Representation representation) {
    representationDeleted(representation.getAipId(), representation.getId());
    representationCreated(representation);
  }

  @Override
  public void representationDeleted(String aipId, String representationId) {
    // XXX check if forcing auto commit is necessary
    boolean forceCommit = true;
    deleteDocumentFromIndex(RodaConstants.INDEX_REPRESENTATION, SolrUtils.getId(aipId, representationId),
      "Error deleting Representation (aipId=" + aipId + "; representationId=" + representationId + ")", forceCommit);
  }

  @Override
  public void fileCreated(File file) {
    boolean commit = true;
    boolean recursive = true;
    indexFile(file, commit, recursive);
  }

  @Override
  public void fileUpdated(File file) {
    fileDeleted(file.getAipId(), file.getRepresentationId(), file.getId());
    fileCreated(file);

  }

  @Override
  public void fileDeleted(String aipId, String representationId, String fileId) {
    String id = SolrUtils.getId(aipId, representationId, fileId);
    // XXX check if forcing auto commit is necessary
    boolean forceCommit = true;
    deleteDocumentFromIndex(RodaConstants.INDEX_FILE, id, "Error deleting File (id=" + id + ")", forceCommit);

  }

  @Override
  public void logEntryCreated(LogEntry entry) {
    boolean forceCommit = false;
    addDocumentToIndex(RodaConstants.INDEX_ACTION_LOG, SolrUtils.logEntryToSolrDocument(entry),
      "Error creating Log entry", forceCommit);
  }

  @Override
  public void userCreated(User user) {
    // XXX check if forcing auto commit is necessary
    boolean forceCommit = true;
    addDocumentToIndex(RodaConstants.INDEX_MEMBERS, SolrUtils.rodaMemberToSolrDocument(user), "Error creating User",
      forceCommit);
  }

  @Override
  public void userUpdated(User user) {
    userDeleted(user.getId());
    userCreated(user);
  }

  @Override
  public void userDeleted(String userID) {
    // XXX check if forcing auto commit is necessary
    boolean forceCommit = true;
    deleteDocumentFromIndex(RodaConstants.INDEX_MEMBERS, userID, "Error deleting User (id=" + userID + ")",
      forceCommit);
  }

  @Override
  public void groupCreated(Group group) {
    // XXX check if forcing auto commit is necessary
    boolean forceCommit = true;
    addDocumentToIndex(RodaConstants.INDEX_MEMBERS, SolrUtils.rodaMemberToSolrDocument(group), "Error creating Group",
      forceCommit);
  }

  @Override
  public void groupUpdated(Group group) {
    groupDeleted(group.getId());
    groupCreated(group);
  }

  @Override
  public void groupDeleted(String groupID) {
    // XXX check if forcing auto commit is necessary
    boolean forceCommit = true;
    deleteDocumentFromIndex(RodaConstants.INDEX_MEMBERS, groupID, "Error deleting Group (id=" + groupID + ")",
      forceCommit);
  }

  @Override
  public void preservationMetadataCreated(PreservationMetadata preservationMetadata) {
    try {
      if (preservationMetadata.getAipId() != null) {
        AIP aip = model.retrieveAIP(preservationMetadata.getAipId());
        indexAIP(aip);
      }
      LOGGER.debug("preservationMetadataCreated");
      StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationMetadata);
      Binary binary = model.getStorage().getBinary(storagePath);
      SolrInputDocument premisFileDocument = SolrUtils.premisToSolr(preservationMetadata.getAipId(),
        preservationMetadata.getRepresentationId(), preservationMetadata.getId(), binary);

      PreservationMetadataType type = preservationMetadata.getType();
      if (type.equals(PreservationMetadataType.EVENT)) {
        LOGGER.debug("INDEXING EVENT");
        index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisFileDocument);
        index.commit(RodaConstants.INDEX_PRESERVATION_EVENTS);
      } else if (type.equals(PreservationMetadataType.AGENT)) {
        LOGGER.debug("INDEXING AGENT");
        index.add(RodaConstants.INDEX_PRESERVATION_AGENTS, premisFileDocument);
        index.commit(RodaConstants.INDEX_PRESERVATION_EVENTS);
      }

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
  public void preservationMetadataDeleted(PreservationMetadata preservationMetadata) {
    try {
      if (preservationMetadata.getAipId() != null) {
        aipUpdated(model.retrieveAIP(preservationMetadata.getAipId()));
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when descriptive metadata deleted on retrieving the full AIP", e);
    }
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
    boolean forceCommit = false;
    addDocumentToIndex(RodaConstants.INDEX_JOB, SolrUtils.jobToSolrDocument(job), "Error creating Job", forceCommit);
  }

  @Override
  public void jobUpdated(Job job) {
    jobDeleted(job.getId());
    jobCreated(job);
  }

  @Override
  public void jobDeleted(String jobId) {
    boolean forceCommit = false;
    deleteDocumentFromIndex(RodaConstants.INDEX_JOB, jobId, "Error deleting Job (id=" + jobId + ")", forceCommit);
  }

  private void addDocumentToIndex(String indexName, SolrInputDocument document, String errorLogMessage,
    boolean commit) {
    try {
      index.add(indexName, document);
      if (commit) {
        index.commit(indexName);
      }
    } catch (SolrServerException | IOException e) {
      LOGGER.error(errorLogMessage, e);
    }
  }

  private void deleteDocumentFromIndex(String indexName, String documentId, String errorLogMessage, boolean commit) {
    try {
      index.deleteById(indexName, documentId);
      if (commit) {
        index.commit(indexName);
      }
    } catch (SolrServerException | IOException e) {
      LOGGER.error(errorLogMessage, e);
    }
  }

  @Override
  public void jobReportCreated(JobReport jobReport) {
    boolean forceCommit = false;
    addDocumentToIndex(RodaConstants.INDEX_JOB_REPORT, SolrUtils.jobReportToSolrDocument(jobReport),
      "Error creating Job Report", forceCommit);
  }

  @Override
  public void jobReportUpdated(JobReport jobReport) {
    jobReportDeleted(jobReport.getId());
    jobReportCreated(jobReport);
  }

  @Override
  public void jobReportDeleted(String jobReportId) {
    boolean forceCommit = false;
    deleteDocumentFromIndex(RodaConstants.INDEX_JOB_REPORT, jobReportId,
      "Error deleting Job Report(id=" + jobReportId + ")", forceCommit);
  }

}
