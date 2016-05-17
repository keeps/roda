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
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
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
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;
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
        LOGGER.error("Cannot index created AIP", e);
      }

    }
  }

  private void indexPreservationsEvents(final AIP aip) {

    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = null;
    try {
      boolean includeRepresentations = true;
      preservationMetadata = model.listPreservationMetadata(aip.getId(), includeRepresentations);
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadata) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            try {
              indexPreservationEvent(pm);
            } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
              | AuthorizationDeniedException e) {
              LOGGER.error("Cannot index premis event", e);
            }
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index preservation events", e);
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
    CloseableIterable<OptionalWithCause<File>> allFiles = null;
    try {
      Long sizeInBytes = 0L;
      Long numberOfDataFiles = 0L;

      final boolean recursive = true;
      allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(), recursive);
      for (OptionalWithCause<File> file : allFiles) {
        if (file.isPresent()) {
          boolean recursiveIndexFile = false;
          sizeInBytes += indexFile(aip, file.get(), recursiveIndexFile);
        } else {
          LOGGER.error("Cannot index representation file", file.getCause());
        }
        numberOfDataFiles++;
      }
      allFiles.close();

      // Calculate number of documentation and schema files
      StorageService storage = model.getStorage();
      Long numberOfDocumentationFiles;
      try {
        Directory documentationDirectory = model.getDocumentationDirectory(aip.getId(), representation.getId());
        numberOfDocumentationFiles = storage.countResourcesUnderDirectory(documentationDirectory.getStoragePath(),
          true);
      } catch (NotFoundException e) {
        numberOfDocumentationFiles = 0L;
      }

      Long numberOfSchemaFiles;
      try {
        Directory schemasDirectory = model.getSchemasDirectory(aip.getId(), representation.getId());
        numberOfSchemaFiles = storage.countResourcesUnderDirectory(schemasDirectory.getStoragePath(), true);
      } catch (NotFoundException e) {
        numberOfSchemaFiles = 0L;
      }

      SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(aip, representation,
        sizeInBytes, numberOfDataFiles, numberOfDocumentationFiles, numberOfSchemaFiles);
      index.add(RodaConstants.INDEX_REPRESENTATION, representationDocument);

    } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index representation", e);
    } finally {
      IOUtils.closeQuietly(allFiles);
    }
  }

  // private void indexFile(OptionalWithCause<AIP> aip, OptionalWithCause<File>
  // file, boolean recursive) {
  // if (aip.isPresent() && file.isPresent()) {
  // indexFile(aip.get(), file.get(), recursive);
  // }
  // }

  private Long indexFile(AIP aip, File file, boolean recursive) {

    Long sizeInBytes = 0L;

    SolrInputDocument fileDocument = SolrUtils.fileToSolrDocument(aip, file);

    // Add information from PREMIS
    Binary premisFile = getFilePremisFile(file);
    if (premisFile != null) {
      // TODO get entry point from PREMIS or remove it
      // doc.addField(RodaConstants.FILE_ISENTRYPOINT, file.isEntryPoint());
      try {
        SolrInputDocument premisSolrDoc = PremisV3Utils.getSolrDocument(premisFile);
        fileDocument.putAll(premisSolrDoc);
        sizeInBytes = SolrUtils.objectToLong(premisSolrDoc.get(RodaConstants.FILE_SIZE).getValue(), 0L);
      } catch (GenericException e) {
        LOGGER.warn("Could not index file PREMIS information", e);
      }
    }

    // Add full text
    String fulltext = getFileFulltext(file);
    if (fulltext != null) {
      fileDocument.addField(RodaConstants.FILE_FULLTEXT, fulltext);

    }

    try {
      index.add(RodaConstants.INDEX_FILE, fileDocument);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Cannot index file: " + file, e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(file, true);
        for (OptionalWithCause<File> subfile : allFiles) {
          if (subfile.isPresent()) {
            sizeInBytes += indexFile(aip, subfile.get(), false);
          } else {
            LOGGER.error("Cannot index file", subfile.getCause());
          }
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Cannot index file sub-resources: " + file, e);
      }
    }

    return sizeInBytes;
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
  public void aipStateUpdated(AIP aip) {
    try {
      // change AIP
      SolrInputDocument aipDoc = SolrUtils.aipStateUpdateToSolrDocument(aip);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Cannot do a partial update", e);
    }

    // change Representations and Files
    representationsStateUpdated(aip);
    // change Preservation events
    preservationEventsStateUpdated(aip);

  }

  private void representationsStateUpdated(final AIP aip) {
    for (Representation representation : aip.getRepresentations()) {
      representationStateUpdated(aip, representation);
    }
  }

  private void representationStateUpdated(final AIP aip, final Representation representation) {
    CloseableIterable<OptionalWithCause<File>> allFiles = null;
    try {
      SolrInputDocument repDoc = SolrUtils.representationStateUpdateToSolrDocument(representation, aip.getState());
      index.add(RodaConstants.INDEX_REPRESENTATION, repDoc);
      final boolean recursive = true;
      allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(), recursive);
      for (OptionalWithCause<File> file : allFiles) {
        if (file.isPresent()) {
          boolean recursiveIndexFile = false;
          fileStateUpdated(aip, file.get(), recursiveIndexFile);
        } else {
          LOGGER.error("Cannot do a partial update on File", file.getCause());
        }
      }

    } catch (SolrServerException | AuthorizationDeniedException | IOException | NotFoundException | GenericException
      | RequestNotValidException e) {
      LOGGER.error("Cannot do a partial update", e);
    } finally {
      IOUtils.closeQuietly(allFiles);
    }
  }

  private void fileStateUpdated(AIP aip, File file, boolean recursive) {
    SolrInputDocument fileDoc = SolrUtils.fileStateUpdateToSolrDocument(file, aip.getState());
    try {
      index.add(RodaConstants.INDEX_FILE, fileDoc);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Cannot index file: " + file, e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(file, true);
        for (OptionalWithCause<File> subfile : allFiles) {
          if (subfile.isPresent()) {
            fileStateUpdated(aip, subfile.get(), false);
          } else {
            LOGGER.error("Cannot index file sub-resources", subfile.getCause());
          }
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Cannot index file sub-resources: " + file, e);
      }
    }
  }

  private void preservationEventsStateUpdated(final AIP aip) {

    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = null;
    try {
      boolean includeRepresentations = true;
      preservationMetadata = model.listPreservationMetadata(aip.getId(), includeRepresentations);
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadata) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            try {
              preservationEventStateUpdated(pm, aip.getState());
            } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
              | AuthorizationDeniedException e) {
              LOGGER.error("Cannot index premis event", e);
            }
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index preservation events", e);
    } finally {
      IOUtils.closeQuietly(preservationMetadata);
    }

  }

  private void preservationEventStateUpdated(PreservationMetadata pm, AIPState state) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, SolrServerException, IOException {
    SolrInputDocument premisEventDocument = SolrUtils.preservationEventStateUpdateToSolrDocument(pm.getId(),
      pm.getAipId(), state);

    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  @Override
  public void aipMoved(AIP aip, String oldParentId, String newParentId) {

    try {
      LOGGER.debug("Reindexing moved aip " + aip.getId());
      SolrInputDocument aipDoc = SolrUtils.updateAIPParentId(aip.getId(), newParentId, model);
      index.add(RodaConstants.INDEX_AIP, aipDoc);

      LOGGER.debug("Finding descendents of moved aip " + aip.getId());
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
      SolrUtils.execute(index, IndexedAIP.class, filter, new IndexRunnable<IndexedAIP>() {

        @Override
        public void run(IndexedAIP item)
          throws RequestNotValidException, GenericException, AuthorizationDeniedException {
          SolrInputDocument descendantDoc;
          try {
            LOGGER.debug("Reindexing aip " + aip.getId() + " descendent " + item.getId());
            descendantDoc = SolrUtils.updateAIPAncestors(item.getId(), item.getParentID(), model);
            index.add(RodaConstants.INDEX_AIP, descendantDoc);
          } catch (SolrServerException | IOException e) {
            LOGGER.error("Error indexing moved AIP " + aip.getId() + " from " + oldParentId + " to " + newParentId, e);
          }
        }
      });

    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | SolrServerException
      | IOException e) {
      LOGGER.error("Error indexing moved AIP " + aip.getId() + " from " + oldParentId + " to " + newParentId, e);
    }
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
      "Error deleting preservation events (aipId=" + aipId + ")");
    deleteDocumentsFromIndex(RodaConstants.INDEX_RISK_INCIDENCE, RodaConstants.RISK_INCIDENCE_AIP_ID, aipId,
      "Error deleting risk incidences (aipId=" + aipId + ")");
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
      LOGGER.error("Cannot index representation: " + representation, e);
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

    deleteDocumentsFromIndex(RodaConstants.INDEX_RISK_INCIDENCE, RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID,
      representationUUID,
      "Error deleting risk incidences (aipId=" + aipId + "; representationId=" + representationId + ")");
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

    deleteDocumentsFromIndex(RodaConstants.INDEX_RISK_INCIDENCE, RodaConstants.RISK_INCIDENCE_FILE_ID, fileId,
      "Error deleting risk incidences (aipId=" + aipId + "; representationId=" + representationId + "; fileId=" + fileId
        + ")");
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
    } catch (SolrServerException | SolrException | IOException e) {
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
    } catch (SolrServerException | SolrException | IOException e) {
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
      LOGGER.error("Cannot do a partial update", e);
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
    CloseableIterable<OptionalWithCause<File>> allFiles = null;
    try {
      SolrInputDocument repDoc = SolrUtils.representationPermissionsUpdateToSolrDocument(representation,
        aip.getPermissions());
      index.add(RodaConstants.INDEX_REPRESENTATION, repDoc);
      final boolean recursive = true;
      allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(), recursive);
      for (OptionalWithCause<File> file : allFiles) {
        if (file.isPresent()) {
          boolean recursiveIndexFile = false;
          filePermissionsUpdated(aip, file.get(), recursiveIndexFile);
        } else {
          LOGGER.error("Cannot do a partial update on file", file.getCause());
        }
      }

    } catch (SolrServerException | AuthorizationDeniedException | IOException | NotFoundException | GenericException
      | RequestNotValidException e) {
      LOGGER.error("Cannot do a partial update", e);
    } finally {
      IOUtils.closeQuietly(allFiles);
    }
  }

  private void filePermissionsUpdated(AIP aip, File file, boolean recursive) {
    SolrInputDocument fileDoc = SolrUtils.filePermissionsUpdateToSolrDocument(file, aip.getPermissions());
    try {
      index.add(RodaConstants.INDEX_FILE, fileDoc);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Cannot index file: " + file, e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(file, true);
        for (OptionalWithCause<File> subfile : allFiles) {
          if (subfile.isPresent()) {
            filePermissionsUpdated(aip, subfile.get(), false);
          } else {
            LOGGER.error("Cannot index file sub-resources file", subfile.getCause());
          }
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Cannot index file sub-resources: " + file, e);
      }
    }
  }

  private void preservationEventsPermissionsUpdated(final AIP aip) {

    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = null;
    try {
      boolean includeRepresentations = true;
      preservationMetadata = model.listPreservationMetadata(aip.getId(), includeRepresentations);
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadata) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            try {
              preservationEventPermissionsUpdated(pm, aip.getPermissions());
            } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
              | AuthorizationDeniedException e) {
              LOGGER.error("Cannot index premis event", e);
            }
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index preservation events", e);
    } finally {
      IOUtils.closeQuietly(preservationMetadata);
    }

  }

  private void preservationEventPermissionsUpdated(PreservationMetadata pm, Permissions permissions)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    SolrServerException, IOException {
    SolrInputDocument premisEventDocument = SolrUtils.preservationEventPermissionsUpdateToSolrDocument(pm.getId(),
      pm.getAipId(), permissions);

    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  public void riskCreatedOrUpdated(Risk risk, boolean commit) {
    addDocumentToIndex(RodaConstants.INDEX_RISK, SolrUtils.riskToSolrDocument(risk), "Error creating Risk");

    if (commit) {
      try {
        SolrUtils.commit(index, IndexedRisk.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  public void riskDeleted(String riskId, boolean commit) {
    deleteDocumentFromIndex(RodaConstants.INDEX_RISK, riskId, "Error deleting Risk (id=" + riskId + ")");

    if (commit) {
      try {
        SolrUtils.commit(index, IndexedRisk.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  public void riskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit) {
    addDocumentToIndex(RodaConstants.INDEX_RISK_INCIDENCE, SolrUtils.riskIncidenceToSolrDocument(riskIncidence),
      "Error creating Risk Incidence");

    if (commit) {
      try {
        SolrUtils.commit(index, RiskIncidence.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  public void riskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    deleteDocumentFromIndex(RodaConstants.INDEX_RISK_INCIDENCE, riskIncidenceId,
      "Error deleting Risk Incidence (id=" + riskIncidenceId + ")");

    if (commit) {
      try {
        SolrUtils.commit(index, RiskIncidence.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  public void agentCreatedOrUpdated(Agent agent, boolean commit) {
    addDocumentToIndex(RodaConstants.INDEX_AGENT, SolrUtils.agentToSolrDocument(agent), "Error creating Agent");

    if (commit) {
      try {
        SolrUtils.commit(index, Risk.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  public void agentDeleted(String agentId, boolean commit) {
    deleteDocumentFromIndex(RodaConstants.INDEX_AGENT, agentId, "Error deleting Agent (id=" + agentId + ")");

    if (commit) {
      try {
        SolrUtils.commit(index, Agent.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  public void formatCreatedOrUpdated(Format format, boolean commit) {
    addDocumentToIndex(RodaConstants.INDEX_FORMAT, SolrUtils.formatToSolrDocument(format), "Error creating Format");

    if (commit) {
      try {
        SolrUtils.commit(index, Risk.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  public void formatDeleted(String formatId, boolean commit) {
    deleteDocumentFromIndex(RodaConstants.INDEX_FORMAT, formatId, "Error deleting Format (id=" + formatId + ")");

    if (commit) {
      try {
        SolrUtils.commit(index, Format.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  @Override
  public void transferredResourceDeleted(String transferredResourceID) {
    deleteDocumentFromIndex(RodaConstants.INDEX_TRANSFERRED_RESOURCE, transferredResourceID,
      "Error deleting Transferred Resource(id=" + transferredResourceID + ")");
  }

  public void notificationCreatedOrUpdated(Notification notification) {
    addDocumentToIndex(RodaConstants.INDEX_NOTIFICATION, SolrUtils.notificationToSolrDocument(notification),
      "Error creating NotificationId");
  }

  public void notificationDeleted(String notificationId) {
    deleteDocumentFromIndex(RodaConstants.INDEX_NOTIFICATION, notificationId,
      "Error deleting Notification (id=" + notificationId + ")");
  }

}
