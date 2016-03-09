/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.JsonUtils;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.storage.Binary;
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
    // indexOtherMetadata(aip);

  }

  private void indexAIP(final AIP aip) {
    boolean safemode = false;
    indexAIP(aip, safemode);
  }

  private void indexAIP(final AIP aip, boolean safemode) {
    try {
      SolrInputDocument aipDoc = SolrUtils.aipToSolrInputDocument(aip, model, safemode);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
      commit(RodaConstants.INDEX_AIP);

      LOGGER.trace("Adding AIP: {}", aipDoc);
    } catch (SolrException | SolrServerException | IOException | RequestNotValidException | GenericException
      | NotFoundException | AuthorizationDeniedException e) {
      if (!safemode) {
        LOGGER.error("Error indexing AIP, trying safe mode", e);
        safemode = true;
        indexAIP(aip, safemode);
      } else {
        LOGGER.error("Could not index created AIP", e);
      }

    }
  }

  private void indexPreservationsEvents(final AIP aip) {

    CloseableIterable<PreservationMetadata> preservationMetadata = null;
    try {
      boolean includeRepresentations = true;
      preservationMetadata = model.listPreservationMetadata(aip.getId(), includeRepresentations);
      for (PreservationMetadata pm : preservationMetadata) {
        if (pm.getType().equals(PreservationMetadataType.EVENT)) {
          try {

            StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(pm);
            Binary binary = model.getStorage().getBinary(filePath);

            SolrInputDocument premisEventDocument = SolrUtils.premisToSolr(aip.getId(), pm.getRepresentationId(),
              pm.getId(), binary);
            LOGGER.trace("{}", premisEventDocument);

            try {
              List<LinkingIdentifier> agents = PremisV3Utils.extractAgentsFromEvent(binary);
              for (LinkingIdentifier id : agents) {
                premisEventDocument.addField(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER,
                  JsonUtils.getJsonFromObject(id));
              }
            } catch (org.roda.core.data.v2.validation.ValidationException e) {
              LOGGER.warn("Error setting linking agent field: {}", e.getMessage());
            }

            try {
              List<LinkingIdentifier> sources = PremisV3Utils.extractObjectFromEvent(binary);
              for (LinkingIdentifier id : sources) {
                premisEventDocument.addField(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER,
                  JsonUtils.getJsonFromObject(id));
              }
            } catch (org.roda.core.data.v2.validation.ValidationException e) {
              LOGGER.warn("Error setting linking source field: {}", e.getMessage());
            }

            try {
              List<LinkingIdentifier> outcomes = PremisV3Utils.extractObjectFromEvent(binary);
              for (LinkingIdentifier id : outcomes) {
                premisEventDocument.addField(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER,
                  JsonUtils.getJsonFromObject(id));
              }
            } catch (org.roda.core.data.v2.validation.ValidationException e) {
              LOGGER.warn("Error setting linking outcome field: {}", e.getMessage());
            }

            index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);

          } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
            | AuthorizationDeniedException e) {
            LOGGER.error("Could not index premis event", e);
          }
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Could not index preservation events", e);
    } finally {
      IOUtils.closeQuietly(preservationMetadata);
    }

    commit(RodaConstants.INDEX_PRESERVATION_EVENTS);
  }

  private void indexOtherMetadata(final AIP aip, boolean commit) {
    // TODO index other metadata
  }

  private void indexRepresentations(final AIP aip) {

    for (Representation representation : aip.getRepresentations()) {
      CloseableIterable<File> allFiles = null;
      try {
        SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(representation);
        index.add(RodaConstants.INDEX_REPRESENTATION, representationDocument);
        final boolean recursive = true;
        allFiles = model.listFilesUnder(aip.getId(), representation.getId(), recursive);
        for (File file : allFiles) {
          boolean commit = false;
          boolean recursiveIndexFile = false;
          indexFile(file, commit, recursiveIndexFile);
        }
        allFiles.close();

      } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException e) {
        LOGGER.error("Could not index representation", e);
      } finally {
        IOUtils.closeQuietly(allFiles);
      }
    }

    commit(RodaConstants.INDEX_REPRESENTATION, RodaConstants.INDEX_FILE);
  }

  private void indexFile(File file, boolean commit, boolean recursive) {
    Binary premisFile = getFilePremisFile(file);
    String fulltext = getFileFulltext(file);

    SolrInputDocument fileDocument = SolrUtils.fileToSolrDocument(file, premisFile, fulltext);
    try {
      index.add(RodaConstants.INDEX_FILE, fileDocument);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index file: " + file, e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<File> allFiles = model.listFilesUnder(file, true);
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

  private Binary getFilePremisFile(File file) {
    Binary premisFile = null;
    try {
      premisFile = model.retrievePreservationFile(file);
    } catch (NotFoundException e) {
      LOGGER.trace("On indexing representations, did not find PREMIS for file: {}", file);
    } catch (RODAException e) {
      LOGGER.warn("On indexing representations, error loading PREMIS for file: " + file, e);
    }
    return premisFile;
  }

  private String getFileFulltext(File file) {
    String fulltext = null;
    InputStream inputStream = null;
    try {
      Binary fulltextBinary = model.retrieveOtherMetadataBinary(file.getAipId(), file.getRepresentationId(),
        file.getPath(), file.getId(), TikaFullTextPlugin.FILE_SUFFIX, TikaFullTextPlugin.OTHER_METADATA_TYPE);
      inputStream = fulltextBinary.getContent().createInputStream();
      fulltext = IOUtils.toString(inputStream);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      LOGGER.warn("Error getting fulltext for file: " + file, e);
    } catch (NotFoundException e) {
      LOGGER.trace("Fulltext not found for file: {}", file);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return fulltext;
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
    deleteDocumentFromIndex(RodaConstants.INDEX_REPRESENTATION, IdUtils.getRepresentationId(aipId, representationId),
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
    fileDeleted(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
    fileCreated(file);

  }

  @Override
  public void fileDeleted(String aipId, String representationId, List<String> fileDirectoryPath, String fileId) {
    String id = IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId);
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
      StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationMetadata);
      Binary binary = model.getStorage().getBinary(storagePath);
      SolrInputDocument premisFileDocument = SolrUtils.premisToSolr(preservationMetadata.getAipId(),
        preservationMetadata.getRepresentationId(), preservationMetadata.getId(), binary);
      PreservationMetadataType type = preservationMetadata.getType();
      if (type.equals(PreservationMetadataType.EVENT)) {
        try {
          List<LinkingIdentifier> agents = PremisV3Utils.extractAgentsFromEvent(binary);
          for (LinkingIdentifier id : agents) {
            premisFileDocument.addField(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER,
              JsonUtils.getJsonFromObject(id));
          }
        } catch (org.roda.core.data.v2.validation.ValidationException e) {
          LOGGER.warn("Error setting linking agent field: " + e.getMessage());
        }
        try {
          List<LinkingIdentifier> sources = PremisV3Utils.extractObjectFromEvent(binary);
          for (LinkingIdentifier id : sources) {
            premisFileDocument.addField(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER,
              JsonUtils.getJsonFromObject(id));
          }
        } catch (org.roda.core.data.v2.validation.ValidationException e) {
          LOGGER.warn("Error setting linking source field: " + e.getMessage());
        }
        try {
          List<LinkingIdentifier> outcomes = PremisV3Utils.extractObjectFromEvent(binary);
          for (LinkingIdentifier id : outcomes) {
            premisFileDocument.addField(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER,
              JsonUtils.getJsonFromObject(id));
          }
        } catch (org.roda.core.data.v2.validation.ValidationException e) {
          LOGGER.warn("Error setting linking outcome field: " + e.getMessage());
        }
        premisFileDocument.addField("content_type", "parentDocument");
        index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisFileDocument);
        index.commit(RodaConstants.INDEX_PRESERVATION_EVENTS);
      } else if (type.equals(PreservationMetadataType.AGENT)) {
        index.add(RodaConstants.INDEX_PRESERVATION_AGENTS, premisFileDocument);
        index.commit(RodaConstants.INDEX_PRESERVATION_AGENTS);
      }
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
      indexOtherMetadata(model.retrieveAIP(otherMetadataBinary.getAipId()), true);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when other metadata created on retrieving the full AIP", e);
    }
  }

  @Override
  public void jobCreatedOrUpdated(Job job) {
    boolean forceCommit = false;
    addDocumentToIndex(RodaConstants.INDEX_JOB, SolrUtils.jobToSolrDocument(job), "Error creating Job", forceCommit);
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
  public void jobReportCreatedOrUpdated(Report jobReport) {
    boolean forceCommit = false;
    addDocumentToIndex(RodaConstants.INDEX_JOB_REPORT, SolrUtils.jobReportToSolrDocument(jobReport),
      "Error creating Job Report", forceCommit);
  }

  @Override
  public void jobReportDeleted(String jobReportId) {
    boolean forceCommit = false;
    deleteDocumentFromIndex(RodaConstants.INDEX_JOB_REPORT, jobReportId,
      "Error deleting Job Report(id=" + jobReportId + ")", forceCommit);
  }

  private void commit(String... collections) {

    boolean waitFlush = false;
    boolean waitSearcher = true;
    boolean softCommit = true;

    for (String collection : collections) {
      try {
        index.commit(collection, waitFlush, waitSearcher, softCommit);
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Error commiting into collection: " + collection, e);
      }
    }

  }

}
