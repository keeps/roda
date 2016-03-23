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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.messages.Message;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
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

  private static final int TEN_MB_IN_BYTES = 10485760;

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
            indexPreservationEvent(pm);
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

  }

  private void indexPreservationEvent(PreservationMetadata pm) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, SolrServerException, IOException {
    StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(pm);
    Binary binary = model.getStorage().getBinary(filePath);
    AIP aip = model.retrieveAIP(pm.getAipId());
    SolrInputDocument premisEventDocument = SolrUtils.premisToSolr(pm.getType(), aip, pm.getRepresentationId(),
      pm.getId(), binary);
    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  private void indexOtherMetadata(final AIP aip) {
    // TODO index other metadata
  }

  private void indexRepresentations(final AIP aip) {
    for (Representation representation : aip.getRepresentations()) {
      indexRepresentation(aip, representation);
    }
  }

  private void indexRepresentation(final AIP aip, final Representation representation) {
    CloseableIterable<File> allFiles = null;
    try {
      SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(aip, representation);
      index.add(RodaConstants.INDEX_REPRESENTATION, representationDocument);
      final boolean recursive = true;
      allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(), recursive);
      for (File file : allFiles) {
        boolean recursiveIndexFile = false;
        indexFile(aip, file, recursiveIndexFile);
      }
      allFiles.close();

    } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException e) {
      LOGGER.error("Could not index representation", e);
    } finally {
      IOUtils.closeQuietly(allFiles);
    }
  }

  private void indexFile(AIP aip, File file, boolean recursive) {
    Binary premisFile = getFilePremisFile(file);
    String fulltext = getFileFulltext(file);

    SolrInputDocument fileDocument = SolrUtils.fileToSolrDocument(aip, file, premisFile, fulltext);
    try {
      index.add(RodaConstants.INDEX_FILE, fileDocument);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index file: " + file, e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<File> allFiles = model.listFilesUnder(file, true);
        for (File subfile : allFiles) {
          indexFile(aip, subfile, false);
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Could not index file sub-resources: " + file, e);
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
    String fulltext = "";
    InputStream inputStream = null;
    try {
      Binary fulltextBinary = model.retrieveOtherMetadataBinary(file.getAipId(), file.getRepresentationId(),
        file.getPath(), file.getId(), TikaFullTextPlugin.FILE_SUFFIX_FULLTEXT, TikaFullTextPlugin.OTHER_METADATA_TYPE);
      if (fulltextBinary.getSizeInBytes() < RodaCoreFactory.getRodaConfigurationAsInt(TEN_MB_IN_BYTES,
        "core.index.fulltext_threshold_in_bytes")) {
        inputStream = fulltextBinary.getContent().createInputStream();
        fulltext = IOUtils.toString(inputStream);
      }
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

  @Override
  public void aipActiveFlagUpdated(AIP aip) {
    try {
      // change AIP
      SolrInputDocument aipDoc = SolrUtils.aipActiveFlagUpdateToSolrDocument(aip);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not do a partial update", e);
    }

    // change Representations and Files
    representationsActiveFlagUpdated(aip);
    // change Preservation events
    preservationEventsActiveFlagUpdated(aip);

  }

  private void representationsActiveFlagUpdated(final AIP aip) {
    for (Representation representation : aip.getRepresentations()) {
      representationActiveFlagUpdated(aip, representation);
    }
  }

  private void representationActiveFlagUpdated(final AIP aip, final Representation representation) {
    CloseableIterable<File> allFiles = null;
    try {
      SolrInputDocument repDoc = SolrUtils.representationActiveFlagUpdateToSolrDocument(representation, aip.isActive());
      index.add(RodaConstants.INDEX_REPRESENTATION, repDoc);
      final boolean recursive = true;
      allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(), recursive);
      for (File file : allFiles) {
        boolean recursiveIndexFile = false;
        fileActiveFlagUpdated(aip, file, recursiveIndexFile);
      }

    } catch (SolrServerException | AuthorizationDeniedException | IOException | NotFoundException | GenericException
      | RequestNotValidException e) {
      LOGGER.error("Could not do a partial update", e);
    } finally {
      IOUtils.closeQuietly(allFiles);
    }
  }

  private void fileActiveFlagUpdated(AIP aip, File file, boolean recursive) {
    SolrInputDocument fileDoc = SolrUtils.fileActiveFlagUpdateToSolrDocument(file, aip.isActive());
    try {
      index.add(RodaConstants.INDEX_FILE, fileDoc);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index file: " + file, e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<File> allFiles = model.listFilesUnder(file, true);
        for (File subfile : allFiles) {
          fileActiveFlagUpdated(aip, subfile, false);
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Could not index file sub-resources: " + file, e);
      }
    }
  }

  private void preservationEventsActiveFlagUpdated(final AIP aip) {

    CloseableIterable<PreservationMetadata> preservationMetadata = null;
    try {
      boolean includeRepresentations = true;
      preservationMetadata = model.listPreservationMetadata(aip.getId(), includeRepresentations);
      for (PreservationMetadata pm : preservationMetadata) {
        if (pm.getType().equals(PreservationMetadataType.EVENT)) {
          try {
            preservationEventActiveFlagUpdated(pm, aip.isActive());
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

  }

  private void preservationEventActiveFlagUpdated(PreservationMetadata pm, boolean active)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    SolrServerException, IOException {
    SolrInputDocument premisEventDocument = SolrUtils.preservationEventActiveFlagUpdateToSolrDocument(pm.getId(),
      active);
    premisEventDocument.addField(RodaConstants.PRESERVATION_EVENT_AIP_ID, pm.getAipId());
    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  @Override
  public void aipDeleted(String aipId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_AIP, aipId,
      "Error deleting AIP (from " + RodaConstants.INDEX_AIP + ")");
    deleteDocumentsFromIndex(RodaConstants.INDEX_REPRESENTATION, RodaConstants.REPRESENTATION_AIP_ID, aipId,
      "Error deleting representations (aipId=" + aipId + ")");
    deleteDocumentsFromIndex(RodaConstants.INDEX_FILE, RodaConstants.FILE_AIPID, aipId,
      "Error deleting files (aipId=" + aipId + ")");
    deleteDocumentsFromIndex(RodaConstants.INDEX_PRESERVATION_EVENTS, RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId,
      "Error deleting files (aipId=" + aipId + ")");
  }

  @Override
  public void descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    try {
      indexAIP((model.retrieveAIP(descriptiveMetadata.getAipId())));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when descriptive metadata created on retrieving the full AIP", e);
    }
  }

  @Override
  public void descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    try {
      indexAIP((model.retrieveAIP(descriptiveMetadata.getAipId())));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when descriptive metadata updated on retrieving the full AIP", e);
    }
  }

  @Override
  public void descriptiveMetadataDeleted(String aipId, String descriptiveMetadataBinaryId) {
    try {
      indexAIP((model.retrieveAIP(aipId)));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error when descriptive metadata deleted on retrieving the full AIP", e);
    }
  }

  @Override
  public void representationCreated(Representation representation) {
    try {
      indexRepresentation(model.retrieveAIP(representation.getAipId()), representation);

    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Could not index representation: " + representation, e);
    }

  }

  @Override
  public void representationUpdated(Representation representation) {
    representationDeleted(representation.getAipId(), representation.getId());
    representationCreated(representation);
  }

  @Override
  public void representationDeleted(String aipId, String representationId) {
    String representationUUID = IdUtils.getRepresentationId(aipId, representationId);
    deleteDocumentFromIndex(RodaConstants.INDEX_REPRESENTATION, representationUUID,
      "Error deleting Representation (aipId=" + aipId + "; representationId=" + representationId + ")");

    deleteDocumentsFromIndex(RodaConstants.INDEX_FILE, RodaConstants.FILE_REPRESENTATION_UUID, representationUUID,
      "Error deleting Representation files (aipId=" + aipId + "; representationId=" + representationId + ")");

    deleteDocumentsFromIndex(RodaConstants.INDEX_PRESERVATION_EVENTS,
      RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID, representationUUID,
      "Error deleting files (aipId=" + aipId + "; representationId=" + representationId + ")");
  }

  @Override
  public void fileCreated(File file) {
    boolean recursive = true;
    try {
      indexFile(model.retrieveAIP(file.getAipId()), file, recursive);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error indexing file: " + file, e);
    }
  }

  @Override
  public void fileUpdated(File file) {
    fileDeleted(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
    fileCreated(file);

  }

  @Override
  public void fileDeleted(String aipId, String representationId, List<String> fileDirectoryPath, String fileId) {
    String id = IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId);
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
  public void preservationMetadataCreated(PreservationMetadata pm) {
    try {
      StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(pm);
      Binary binary = model.getStorage().getBinary(storagePath);
      AIP aip = pm.getAipId() != null ? model.retrieveAIP(pm.getAipId()) : null;
      SolrInputDocument premisFileDocument = SolrUtils.premisToSolr(pm.getType(), aip, pm.getRepresentationId(),
        pm.getId(), binary);
      PreservationMetadataType type = pm.getType();
      if (type.equals(PreservationMetadataType.EVENT)) {
        index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisFileDocument);
      } else if (type.equals(PreservationMetadataType.AGENT)) {
        index.add(RodaConstants.INDEX_PRESERVATION_AGENTS, premisFileDocument);
      }
    } catch (IOException | SolrServerException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      LOGGER.error("Error when preservation metadata created on retrieving the full AIP", e);
    }

  }

  @Override
  public void preservationMetadataUpdated(PreservationMetadata preservationMetadata) {
    preservationMetadataCreated(preservationMetadata);
  }

  @Override
  public void preservationMetadataDeleted(PreservationMetadata preservationMetadata) {
    PreservationMetadataType type = preservationMetadata.getType();
    String preservationMetadataId = preservationMetadata.getId();
    if (type.equals(PreservationMetadataType.EVENT)) {
      deleteDocumentFromIndex(RodaConstants.INDEX_PRESERVATION_EVENTS, preservationMetadataId,
        "Error deleting PreservationMetadata event (id=" + preservationMetadataId + ")");
    } else if (type.equals(PreservationMetadataType.AGENT)) {
      deleteDocumentFromIndex(RodaConstants.INDEX_PRESERVATION_AGENTS, preservationMetadataId,
        "Error deleting PreservationMetadata agent (id=" + preservationMetadataId + ")");
    }
  }

  @Override
  public void otherMetadataCreated(OtherMetadata otherMetadataBinary) {
    if (otherMetadataBinary.getType().equalsIgnoreCase(TikaFullTextPlugin.OTHER_METADATA_TYPE)
      && otherMetadataBinary.getFileSuffix().equalsIgnoreCase(TikaFullTextPlugin.FILE_SUFFIX_METADATA)) {
      try {
        SolrInputDocument solrFile = SolrUtils.addOtherPropertiesToIndexedFile("tika_", otherMetadataBinary, model,
          index);
        index.add(RodaConstants.INDEX_FILE, solrFile);
      } catch (SolrServerException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException | XPathExpressionException | ParserConfigurationException | SAXException
        | IOException e) {
        LOGGER.error("Error adding other properties to indexed file: " + e.getMessage(), e);
      }

    }
  }

  @Override
  public void jobCreatedOrUpdated(Job job) {
    addDocumentToIndex(RodaConstants.INDEX_JOB, SolrUtils.jobToSolrDocument(job), "Error creating Job");
  }

  @Override
  public void jobDeleted(String jobId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_JOB, jobId, "Error deleting Job (id=" + jobId + ")");
  }

  private void addDocumentToIndex(String indexName, SolrInputDocument document, String errorLogMessage) {
    try {
      index.add(indexName, document);
    } catch (SolrServerException | IOException e) {
      LOGGER.error(errorLogMessage, e);
    }
  }

  private void deleteDocumentFromIndex(String indexName, String documentId, String errorLogMessage) {
    try {
      index.deleteById(indexName, documentId);
    } catch (SolrServerException | IOException e) {
      LOGGER.error(errorLogMessage, e);
    }
  }

  private void deleteDocumentsFromIndex(String indexName, String fieldName, String fieldValue, String errorLogMessage) {
    try {
      index.deleteByQuery(indexName, fieldName + ":" + fieldValue);
    } catch (SolrServerException | IOException e) {
      LOGGER.error(errorLogMessage, e);
    }
  }

  @Override
  public void jobReportCreatedOrUpdated(Report jobReport) {
    addDocumentToIndex(RodaConstants.INDEX_JOB_REPORT, SolrUtils.jobReportToSolrDocument(jobReport),
      "Error creating Job Report");
  }

  @Override
  public void jobReportDeleted(String jobReportId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_JOB_REPORT, jobReportId,
      "Error deleting Job Report(id=" + jobReportId + ")");
  }

  @Override
  public void aipPermissionsUpdated(AIP aip) {
    try {
      // change AIP
      SolrInputDocument aipDoc = SolrUtils.aipPermissionsUpdateToSolrDocument(aip);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not do a partial update", e);
    }

    // change Representations and Files
    representationsPermissionsUpdated(aip);
    // change Preservation events
    preservationEventsPermissionsUpdated(aip);

  }

  private void representationsPermissionsUpdated(final AIP aip) {
    for (Representation representation : aip.getRepresentations()) {
      representationPermissionsUpdated(aip, representation);
    }
  }

  private void representationPermissionsUpdated(final AIP aip, final Representation representation) {
    CloseableIterable<File> allFiles = null;
    try {
      SolrInputDocument repDoc = SolrUtils.representationPermissionsUpdateToSolrDocument(representation,
        aip.getPermissions());
      index.add(RodaConstants.INDEX_REPRESENTATION, repDoc);
      final boolean recursive = true;
      allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(), recursive);
      for (File file : allFiles) {
        boolean recursiveIndexFile = false;
        filePermissionsUpdated(aip, file, recursiveIndexFile);
      }

    } catch (SolrServerException | AuthorizationDeniedException | IOException | NotFoundException | GenericException
      | RequestNotValidException e) {
      LOGGER.error("Could not do a partial update", e);
    } finally {
      IOUtils.closeQuietly(allFiles);
    }
  }

  private void filePermissionsUpdated(AIP aip, File file, boolean recursive) {
    SolrInputDocument fileDoc = SolrUtils.filePermissionsUpdateToSolrDocument(file, aip.getPermissions());
    try {
      index.add(RodaConstants.INDEX_FILE, fileDoc);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not index file: " + file, e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<File> allFiles = model.listFilesUnder(file, true);
        for (File subfile : allFiles) {
          filePermissionsUpdated(aip, subfile, false);
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Could not index file sub-resources: " + file, e);
      }
    }
  }

  private void preservationEventsPermissionsUpdated(final AIP aip) {

    CloseableIterable<PreservationMetadata> preservationMetadata = null;
    try {
      boolean includeRepresentations = true;
      preservationMetadata = model.listPreservationMetadata(aip.getId(), includeRepresentations);
      for (PreservationMetadata pm : preservationMetadata) {
        if (pm.getType().equals(PreservationMetadataType.EVENT)) {
          try {
            preservationEventPermissionsUpdated(pm, aip.getPermissions());
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

  }

  private void preservationEventPermissionsUpdated(PreservationMetadata pm, Permissions permissions)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    SolrServerException, IOException {
    SolrInputDocument premisEventDocument = SolrUtils.preservationEventPermissionsUpdateToSolrDocument(pm.getId(),
      permissions);
    premisEventDocument.addField(RodaConstants.PRESERVATION_EVENT_AIP_ID, pm.getAipId());
    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  public void riskCreatedOrUpdated(Risk risk) {
    addDocumentToIndex(RodaConstants.INDEX_RISK, SolrUtils.riskToSolrDocument(risk), "Error creating Risk");
  }

  public void riskDeleted(String riskId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_RISK, riskId, "Error deleting Risk (id=" + riskId + ")");
  }

  public void agentCreatedOrUpdated(Agent agent) {
    addDocumentToIndex(RodaConstants.INDEX_AGENT, SolrUtils.agentToSolrDocument(agent), "Error creating Agent");
  }

  public void agentDeleted(String agentId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_AGENT, agentId, "Error deleting Agent (id=" + agentId + ")");
  }

  public void formatCreatedOrUpdated(Format format) {
    addDocumentToIndex(RodaConstants.INDEX_FORMAT, SolrUtils.formatToSolrDocument(format), "Error creating Format");
  }

  public void formatDeleted(String formatId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_FORMAT, formatId, "Error deleting Format (id=" + formatId + ")");
  }

  @Override
  public void transferredResourceDeleted(String transferredResourceID) {
    deleteDocumentFromIndex(RodaConstants.INDEX_TRANSFERRED_RESOURCE, transferredResourceID,
      "Error deleting Transferred Resource(id=" + transferredResourceID + ")");
  }

  public void messageCreatedOrUpdated(Message message) {
    addDocumentToIndex(RodaConstants.INDEX_MESSAGE, SolrUtils.messageToSolrDocument(message), "Error creating Message");
  }

  public void messageDeleted(String messageId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_MESSAGE, messageId, "Error deleting Message (id=" + messageId + ")");
  }

}
