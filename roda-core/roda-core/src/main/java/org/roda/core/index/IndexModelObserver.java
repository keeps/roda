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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.ReturnWithExceptions;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
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
  public ReturnWithExceptions<Void> aipCreated(final AIP aip) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    try {
      List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);
      ReturnWithExceptions<Void> aipExceptions = indexAIP(aip, ancestors);
      exceptions.addExceptions(aipExceptions.getExceptions());

      ReturnWithExceptions<Void> repExceptions = indexRepresentations(aip, ancestors);
      exceptions.addExceptions(repExceptions.getExceptions());

      ReturnWithExceptions<Void> eventExceptions = indexPreservationsEvents(aip.getId(), null);
      exceptions.addExceptions(eventExceptions.getExceptions());

    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error getting ancestors when creating AIP");
      exceptions.addException(e);
    }

    return exceptions;
  }

  private ReturnWithExceptions<Void> indexAIP(final AIP aip, final List<String> ancestors) {
    return indexAIP(aip, ancestors, false);
  }

  private ReturnWithExceptions<Void> indexAIP(final AIP aip, final List<String> ancestors, boolean safemode) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    try {
      SolrInputDocument aipDoc = SolrUtils.aipToSolrInputDocument(aip, ancestors, model, safemode);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
      LOGGER.trace("Adding AIP: {}", aipDoc);
    } catch (SolrException | SolrServerException | IOException | RequestNotValidException | GenericException
      | NotFoundException | AuthorizationDeniedException e) {
      exceptions.addException(e);
      if (!safemode) {
        LOGGER.error("Error indexing AIP, trying safe mode", e);
        indexAIP(aip, ancestors, true);
      } else {
        LOGGER.error("Cannot index created AIP", e);
      }
    }

    return exceptions;
  }

  public ReturnWithExceptions<Void> indexPreservationsEvents(final String aipId) {
    return indexPreservationsEvents(aipId, null);
  }

  public ReturnWithExceptions<Void> indexPreservationsEvents(final String aipId, final String representationId) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = null;
    try {
      if (representationId == null) {
        boolean includeRepresentations = true;
        preservationMetadata = model.listPreservationMetadata(aipId, includeRepresentations);
      } else {
        preservationMetadata = model.listPreservationMetadata(aipId, representationId);
      }
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadata) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            try {
              indexPreservationEvent(pm);
            } catch (SolrServerException | SolrException | IOException | RequestNotValidException | GenericException
              | NotFoundException | AuthorizationDeniedException e) {
              LOGGER.error("Cannot index premis event", e);
              exceptions.addException(e);
            }
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
          exceptions.addException(opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index preservation events", e);
      exceptions.addException(e);
    } finally {
      IOUtils.closeQuietly(preservationMetadata);
    }

    return exceptions;
  }

  private void indexPreservationEvent(PreservationMetadata pm) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, SolrServerException, IOException {
    StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(pm);
    Binary binary = model.getStorage().getBinary(filePath);
    AIP aip = model.retrieveAIP(pm.getAipId());
    String representationUUID = null;
    String fileUUID = null;

    if (pm.getRepresentationId() != null) {
      representationUUID = IdUtils.getRepresentationId(aip.getId(), pm.getRepresentationId());
    }

    if (pm.getFileId() != null) {
      fileUUID = IdUtils.getFileId(aip.getId(), pm.getRepresentationId(), pm.getFileDirectoryPath(), pm.getFileId());
    }

    SolrInputDocument premisEventDocument = SolrUtils.premisToSolr(pm.getType(), aip, representationUUID, fileUUID,
      binary);
    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  private ReturnWithExceptions<Void> indexRepresentations(final AIP aip, final List<String> ancestors) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    for (Representation representation : aip.getRepresentations()) {
      ReturnWithExceptions<Void> repExceptions = indexRepresentation(aip, representation, ancestors);
      exceptions.addExceptions(repExceptions.getExceptions());
    }
    return exceptions;
  }

  private ReturnWithExceptions<Void> indexRepresentation(final AIP aip, final Representation representation,
    final List<String> ancestors) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    CloseableIterable<OptionalWithCause<File>> allFiles = null;
    try {
      Long sizeInBytes = 0L;
      Long numberOfDataFiles = 0L;

      final boolean recursive = true;
      allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(), recursive);
      for (OptionalWithCause<File> file : allFiles) {
        if (file.isPresent()) {
          boolean recursiveIndexFile = false;
          ReturnWithExceptions<Long> ret = indexFile(aip, file.get(), ancestors, recursiveIndexFile);
          sizeInBytes += ret.getRet();
          exceptions.addExceptions(ret.getExceptions());
        } else {
          LOGGER.error("Cannot index representation file", file.getCause());
          exceptions.addException(file.getCause());
        }
        numberOfDataFiles++;
      }
      IOUtils.closeQuietly(allFiles);

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
        sizeInBytes, numberOfDataFiles, numberOfDocumentationFiles, numberOfSchemaFiles, ancestors);
      index.add(RodaConstants.INDEX_REPRESENTATION, representationDocument);

    } catch (SolrServerException | SolrException | IOException | RequestNotValidException | GenericException
      | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index representation", e);
      exceptions.addException(e);
    } finally {
      IOUtils.closeQuietly(allFiles);
    }

    return exceptions;
  }

  private ReturnWithExceptions<Long> indexFile(AIP aip, File file, List<String> ancestors, boolean recursive) {
    ReturnWithExceptions<Long> exceptions = new ReturnWithExceptions<>();
    Long sizeInBytes = 0L;
    SolrInputDocument fileDocument = SolrUtils.fileToSolrDocument(aip, file, ancestors);

    // Add information from PREMIS
    Binary premisFile = getFilePremisFile(file);
    if (premisFile != null) {
      try {
        SolrInputDocument premisSolrDoc = PremisV3Utils.getSolrDocument(premisFile);
        fileDocument.putAll(premisSolrDoc);
        sizeInBytes = SolrUtils.objectToLong(premisSolrDoc.get(RodaConstants.FILE_SIZE).getValue(), 0L);
      } catch (GenericException e) {
        LOGGER.warn("Could not index file PREMIS information", e);
        exceptions.addException(e);
      }
    }

    // Add full text
    String fulltext = getFileFulltext(file);
    if (fulltext != null) {
      fileDocument.addField(RodaConstants.FILE_FULLTEXT, fulltext);

    }

    try {
      index.add(RodaConstants.INDEX_FILE, fileDocument);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Cannot index file: {}", file, e);
      exceptions.addException(e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(file, true);
        for (OptionalWithCause<File> subfile : allFiles) {
          if (subfile.isPresent()) {
            ReturnWithExceptions<Long> ret = indexFile(aip, subfile.get(), ancestors, false);
            sizeInBytes += ret.getRet();
            exceptions.addExceptions(ret.getExceptions());
          } else {
            LOGGER.error("Cannot index file", subfile.getCause());
            exceptions.addException(subfile.getCause());
          }
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Cannot index file sub-resources: {}", file, e);
        exceptions.addException(e);
      }
    }

    exceptions.setRet(sizeInBytes);
    return exceptions;
  }

  private Binary getFilePremisFile(File file) {
    Binary premisFile = null;
    try {
      premisFile = model.retrievePreservationFile(file);
    } catch (NotFoundException e) {
      LOGGER.trace("On indexing representations, did not find PREMIS for file: {}", file);
    } catch (RODAException e) {
      LOGGER.warn("On indexing representations, error loading PREMIS for file: {}", file, e);
    }
    return premisFile;
  }

  private String getFileFulltext(File file) {
    String fulltext = "";
    InputStream inputStream = null;
    try {
      Binary fulltextBinary = model.retrieveOtherMetadataBinary(file.getAipId(), file.getRepresentationId(),
        file.getPath(), file.getId(), RodaConstants.TIKA_FILE_SUFFIX_FULLTEXT,
        RodaConstants.OTHER_METADATA_TYPE_APACHE_TIKA);
      if (fulltextBinary.getSizeInBytes() < RodaCoreFactory.getRodaConfigurationAsInt(TEN_MB_IN_BYTES,
        "core.index.fulltext_threshold_in_bytes")) {
        inputStream = fulltextBinary.getContent().createInputStream();
        fulltext = IOUtils.toString(inputStream, Charset.forName(RodaConstants.DEFAULT_ENCODING));
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      LOGGER.warn("Error getting fulltext for file: {}", file, e);
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
    aipDeleted(aip.getId(), false);
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
      LOGGER.error("Cannot index file: {}", file, e);
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
        LOGGER.error("Cannot index file sub-resources: {}", file, e);
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
      LOGGER.debug("Reindexing moved aip {}", aip.getId());
      List<String> topAncestors = SolrUtils.getAncestors(newParentId, model);
      SolrInputDocument aipDoc = SolrUtils.updateAIPParentId(aip.getId(), newParentId, topAncestors);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
      updateRepresentationAndFileAncestors(aip, topAncestors);

      LOGGER.debug("Finding descendants of moved aip {}", aip.getId());
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
      List<String> aipFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_PARENT_ID,
        RodaConstants.AIP_HAS_REPRESENTATIONS);
      SolrUtils.execute(index, IndexedAIP.class, filter, aipFields, new IndexRunnable<IndexedAIP>() {

        @Override
        public void run(IndexedAIP item)
          throws RequestNotValidException, GenericException, AuthorizationDeniedException {
          SolrInputDocument descendantDoc;
          try {
            LOGGER.debug("Reindexing aip {} descendant {}", aip.getId(), item.getId());
            // 20161109 hsilva: lets test if descendant exists, otherwise there
            // is not point in trying to updated it in the index
            AIP aip = model.retrieveAIP(item.getId());
            List<String> ancestors = SolrUtils.getAncestors(item.getParentID(), model);
            descendantDoc = SolrUtils.updateAIPAncestors(item.getId(), ancestors);
            index.add(RodaConstants.INDEX_AIP, descendantDoc);

            // update representation and file ancestors information
            if (item.getHasRepresentations()) {
              updateRepresentationAndFileAncestors(aip, ancestors);
            }

          } catch (SolrServerException | IOException | NotFoundException e) {
            LOGGER.error("Error indexing moved AIP {} from {} to {}", aip.getId(), oldParentId, newParentId, e);
          }
        }
      });

    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | SolrServerException
      | IOException | NotFoundException e) {
      LOGGER.error("Error indexing moved AIP {} from {} to {}", aip.getId(), oldParentId, newParentId, e);
    }
  }

  private void updateRepresentationAndFileAncestors(AIP aip, List<String> ancestors) throws RequestNotValidException,
    GenericException, AuthorizationDeniedException, SolrServerException, IOException, NotFoundException {
    for (Representation representation : aip.getRepresentations()) {
      SolrInputDocument descendantRepresentationDoc = SolrUtils
        .updateRepresentationAncestors(IdUtils.getRepresentationId(representation), ancestors);
      index.add(RodaConstants.INDEX_REPRESENTATION, descendantRepresentationDoc);

      CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), representation.getId(),
        true);

      for (OptionalWithCause<File> oFile : allFiles) {
        if (oFile.isPresent()) {
          File file = oFile.get();
          SolrInputDocument descendantFileDoc = SolrUtils.updateFileAncestors(IdUtils.getFileId(file), ancestors);
          index.add(RodaConstants.INDEX_FILE, descendantFileDoc);
        }
      }
    }
  }

  @Override
  public void aipDeleted(String aipId, boolean deleteIncidences) {
    deleteDocumentFromIndex(IndexedAIP.class, aipId);
    deleteDocumentsFromIndex(IndexedRepresentation.class, RodaConstants.REPRESENTATION_AIP_ID, aipId);
    deleteDocumentsFromIndex(IndexedFile.class, RodaConstants.FILE_AIP_ID, aipId);
    deleteDocumentsFromIndex(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId);

    if (deleteIncidences) {
      deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_AIP_ID, aipId);
    }
  }

  @Override
  public ReturnWithExceptions<Void> descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    if (descriptiveMetadata.isFromAIP()) {
      try {
        AIP aip = model.retrieveAIP(descriptiveMetadata.getAipId());
        List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);
        indexAIP(aip, ancestors);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error when descriptive metadata created on retrieving the full AIP", e);
      }
    }

    return exceptions;
  }

  @Override
  public void descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    if (descriptiveMetadata.isFromAIP()) {
      try {
        AIP aip = model.retrieveAIP(descriptiveMetadata.getAipId());
        List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);
        indexAIP(aip, ancestors);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error when descriptive metadata updated on retrieving the full AIP", e);
      }
    }
  }

  @Override
  public void descriptiveMetadataDeleted(String aipId, String representationId, String descriptiveMetadataBinaryId) {
    if (representationId == null) {
      try {
        AIP aip = model.retrieveAIP(aipId);
        List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);
        indexAIP(aip, ancestors);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error when descriptive metadata deleted on retrieving the full AIP", e);
      }
    }
  }

  @Override
  public ReturnWithExceptions<Void> representationCreated(Representation representation) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    try {
      AIP aip = model.retrieveAIP(representation.getAipId());
      List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);

      ReturnWithExceptions<Void> representationExceptions = indexRepresentation(aip, representation, ancestors);
      exceptions.addExceptions(representationExceptions.getExceptions());

      ReturnWithExceptions<Void> eventExceptions = indexPreservationsEvents(aip.getId(), representation.getId());
      exceptions.addExceptions(eventExceptions.getExceptions());
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index representation: {}", representation, e);
    }

    return exceptions;
  }

  @Override
  public void representationUpdated(Representation representation) {
    representationDeleted(representation.getAipId(), representation.getId(), false);
    representationCreated(representation);
  }

  @Override
  public void representationDeleted(String aipId, String representationId, boolean deleteIncidences) {
    String representationUUID = IdUtils.getRepresentationId(aipId, representationId);
    deleteDocumentFromIndex(IndexedRepresentation.class, representationUUID);
    deleteDocumentsFromIndex(IndexedFile.class, RodaConstants.FILE_REPRESENTATION_UUID, representationUUID);
    deleteDocumentsFromIndex(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID,
      representationUUID);

    if (deleteIncidences) {
      deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representationId);
    }
  }

  @Override
  public ReturnWithExceptions<Void> fileCreated(File file) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    boolean recursive = true;
    try {
      AIP aip = model.retrieveAIP(file.getAipId());
      List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);
      ReturnWithExceptions<Long> fileExceptions = indexFile(aip, file, ancestors, recursive);
      exceptions.addExceptions(fileExceptions.getExceptions());
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error indexing file: {}", file, e);
    }

    return exceptions;
  }

  @Override
  public void fileUpdated(File file) {
    fileDeleted(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), false);
    fileCreated(file);
  }

  @Override
  public void fileDeleted(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    boolean deleteIncidences) {
    String uuid = IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId);
    deleteDocumentFromIndex(IndexedFile.class, uuid);

    if (deleteIncidences) {
      deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_FILE_ID, fileId);
    }
  }

  @Override
  public ReturnWithExceptions<Void> logEntryCreated(LogEntry entry) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument logDoc = SolrUtils.logEntryToSolrDocument(entry);

    try {
      index.add(RodaConstants.INDEX_ACTION_LOG, logDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Log entry was not added to index");
      exceptions.addException(e);
    }

    return exceptions;
  }

  @Override
  public void userCreated(User user) {
    addDocumentToIndex(RODAMember.class, user);
  }

  @Override
  public void userUpdated(User user) {
    userDeleted(user.getId());
    userCreated(user);
  }

  @Override
  public void userDeleted(String userID) {
    deleteDocumentFromIndex(RODAMember.class, userID);
  }

  @Override
  public void groupCreated(Group group) {
    addDocumentToIndex(RODAMember.class, group);
  }

  @Override
  public void groupUpdated(Group group) {
    deleteDocumentFromIndex(RODAMember.class, group.getId());
    addDocumentToIndex(RODAMember.class, group);
  }

  @Override
  public void groupDeleted(String groupID) {
    deleteDocumentFromIndex(RODAMember.class, groupID);
  }

  @Override
  public ReturnWithExceptions<Void> preservationMetadataCreated(PreservationMetadata pm) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    try {
      StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(pm);
      Binary binary = model.getStorage().getBinary(storagePath);
      AIP aip = pm.getAipId() != null ? model.retrieveAIP(pm.getAipId()) : null;
      String representationUUID = null;
      String fileUUID = null;

      if (pm.getRepresentationId() != null) {
        representationUUID = IdUtils.getRepresentationId(pm.getAipId(), pm.getRepresentationId());

        if (pm.getFileId() != null) {
          fileUUID = IdUtils.getFileId(pm.getAipId(), pm.getRepresentationId(), pm.getFileDirectoryPath(),
            pm.getFileId());
        }
      }

      SolrInputDocument premisFileDocument = SolrUtils.premisToSolr(pm.getType(), aip, representationUUID, fileUUID,
        binary);
      PreservationMetadataType type = pm.getType();
      if (PreservationMetadataType.EVENT.equals(type)) {
        index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisFileDocument);
      } else if (PreservationMetadataType.AGENT.equals(type)) {
        index.add(RodaConstants.INDEX_PRESERVATION_AGENTS, premisFileDocument);
      }
    } catch (IOException | SolrServerException | SolrException | GenericException | RequestNotValidException
      | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error when preservation metadata created on retrieving the full AIP", e);
      exceptions.addException(e);
    }
    return exceptions;
  }

  @Override
  public void preservationMetadataUpdated(PreservationMetadata preservationMetadata) {
    preservationMetadataCreated(preservationMetadata);
  }

  @Override
  public void preservationMetadataDeleted(PreservationMetadata preservationMetadata) {
    PreservationMetadataType type = preservationMetadata.getType();
    String preservationMetadataId = preservationMetadata.getId();
    if (PreservationMetadataType.EVENT.equals(type)) {
      deleteDocumentFromIndex(IndexedPreservationEvent.class, preservationMetadataId);
    } else if (PreservationMetadataType.AGENT.equals(type)) {
      deleteDocumentFromIndex(IndexedPreservationAgent.class, preservationMetadataId);
    }
  }

  @Override
  public void otherMetadataCreated(OtherMetadata otherMetadataBinary) {
    if (RodaConstants.OTHER_METADATA_TYPE_APACHE_TIKA.equalsIgnoreCase(otherMetadataBinary.getType())
      && RodaConstants.TIKA_FILE_SUFFIX_METADATA.equalsIgnoreCase(otherMetadataBinary.getFileSuffix())) {
      try {
        SolrInputDocument solrFile = SolrUtils.addOtherPropertiesToIndexedFile("tika_", otherMetadataBinary, model,
          index);
        index.add(RodaConstants.INDEX_FILE, solrFile);
      } catch (SolrServerException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException | XPathExpressionException | ParserConfigurationException | SAXException
        | IOException e) {
        LOGGER.error("Error adding other properties to indexed file", e);
      }

    }
  }

  @Override
  public ReturnWithExceptions<Void> jobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument jobDoc = SolrUtils.jobToSolrDocument(job);

    try {
      index.add(RodaConstants.INDEX_JOB, jobDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Job document was not added to index");
      exceptions.addException(e);
    }

    if (reindexJobReports) {
      ReturnWithExceptions<Void> subExceptions = indexJobReports(job);
      exceptions.addExceptions(subExceptions.getExceptions());
    }

    return exceptions;
  }

  private ReturnWithExceptions<Void> indexJobReports(Job job) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    StorageService storage = RodaCoreFactory.getStorageService();
    CloseableIterable<Resource> listResourcesUnderDirectory = null;
    try {
      boolean recursive = true;
      listResourcesUnderDirectory = storage
        .listResourcesUnderDirectory(ModelUtils.getJobReportsStoragePath(job.getId()), recursive);

      if (listResourcesUnderDirectory != null) {
        for (Resource resource : listResourcesUnderDirectory) {
          if (!resource.isDirectory()) {
            try {
              Binary binary = storage.getBinary(resource.getStoragePath());
              InputStream inputStream = binary.getContent().createInputStream();
              Report objectFromJson = JsonUtils.getObjectFromJson(inputStream, Report.class);
              IOUtils.closeQuietly(inputStream);
              ReturnWithExceptions<Void> subExceptions = jobReportCreatedOrUpdated(objectFromJson, job);
              exceptions.addExceptions(subExceptions.getExceptions());
            } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
              | IOException e) {
              LOGGER.error("Error getting report json from binary", e);
              exceptions.addException(e);
            }
          }
        }
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.error("Error reindexing job reports", e);
      exceptions.addException(e);
    } finally {
      IOUtils.closeQuietly(listResourcesUnderDirectory);
    }

    return exceptions;
  }

  @Override
  public void jobDeleted(String jobId) {
    deleteDocumentFromIndex(Job.class, jobId);
  }

  private <T extends IsIndexed> void addDocumentToIndex(Class<T> classToAdd, T instance) {
    try {
      SolrUtils.create(index, classToAdd, instance);
    } catch (SolrException | GenericException e) {
      LOGGER.error("Error adding document to index", e);
    }
  }

  private <T extends IsIndexed> void deleteDocumentFromIndex(Class<T> classToDelete, String... ids) {
    try {
      SolrUtils.delete(index, classToDelete, Arrays.asList(ids));
    } catch (GenericException e) {
      LOGGER.error("Error deleting document from index", e);
    }
  }

  private <T extends IsIndexed> void deleteDocumentsFromIndex(Class<T> classToDelete, String fieldName,
    String fieldValue) {
    try {
      SolrUtils.delete(index, classToDelete, new Filter(new SimpleFilterParameter(fieldName, fieldValue)));
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error deleting from index", e);
    }
  }

  @Override
  public ReturnWithExceptions<Void> jobReportCreatedOrUpdated(Report jobReport, Job job) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument jobReportDoc = SolrUtils.jobReportToSolrDocument(jobReport, job, index);

    try {
      index.add(RodaConstants.INDEX_JOB_REPORT, jobReportDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Job report document was not added to index");
      exceptions.addException(e);
    }

    return exceptions;
  }

  @Override
  public void jobReportDeleted(String jobReportId) {
    deleteDocumentFromIndex(IndexedReport.class, jobReportId);
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

  @Override
  public void dipPermissionsUpdated(DIP dip) {
    try {
      // change DIP
      SolrInputDocument dipDoc = SolrUtils.dipPermissionsUpdateToSolrDocument(dip);
      index.add(RodaConstants.INDEX_DIP, dipDoc);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Cannot do a partial update", e);
    }
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
      LOGGER.error("Cannot index file: {}", file, e);
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
        LOGGER.error("Cannot index file sub-resources: {}", file, e);
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
          if (PreservationMetadataType.EVENT.equals(pm.getType())) {
            try {
              preservationEventPermissionsUpdated(pm, aip.getPermissions(), aip.getState());
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

  private void preservationEventPermissionsUpdated(PreservationMetadata pm, Permissions permissions, AIPState state)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    SolrServerException, IOException {
    SolrInputDocument premisEventDocument = SolrUtils.preservationEventPermissionsUpdateToSolrDocument(pm.getId(),
      pm.getAipId(), permissions, state);

    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  @Override
  public ReturnWithExceptions<Void> riskCreatedOrUpdated(Risk risk, int incidences, boolean commit) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument riskDoc = SolrUtils.riskToSolrDocument(risk, incidences);

    try {
      index.add(RodaConstants.INDEX_RISK, riskDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Risk document was not added to index");
      exceptions.addException(e);
    }

    if (commit) {
      try {
        SolrUtils.commit(index, IndexedRisk.class);
      } catch (GenericException | SolrException e) {
        LOGGER.warn("Commit did not run as expected");
        exceptions.addException(e);
      }
    }

    return exceptions;
  }

  @Override
  public void riskDeleted(String riskId, boolean commit) {
    deleteDocumentFromIndex(IndexedRisk.class, riskId);

    if (commit) {
      try {
        SolrUtils.commit(index, IndexedRisk.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  @Override
  public ReturnWithExceptions<Void> riskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument incidenceDoc = SolrUtils.riskIncidenceToSolrDocument(riskIncidence);

    try {
      index.add(RodaConstants.INDEX_RISK_INCIDENCE, incidenceDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Risk incidence document was not added to index");
      exceptions.addException(e);
    }

    if (commit) {
      try {
        SolrUtils.commit(index, RiskIncidence.class);
      } catch (GenericException | SolrException e) {
        LOGGER.warn("Commit did not run as expected");
        exceptions.addException(e);
      }
    }

    return exceptions;
  }

  @Override
  public void riskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    deleteDocumentFromIndex(RiskIncidence.class, riskIncidenceId);

    if (commit) {
      try {
        SolrUtils.commit(index, RiskIncidence.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  @Override
  public ReturnWithExceptions<Void> formatCreatedOrUpdated(Format format, boolean commit) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument formatDoc = SolrUtils.formatToSolrDocument(format);

    try {
      index.add(RodaConstants.INDEX_FORMAT, formatDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Format document was not added to index");
      exceptions.addException(e);
    }

    if (commit) {
      try {
        SolrUtils.commit(index, Format.class);
      } catch (GenericException | SolrException e) {
        LOGGER.warn("Commit did not run as expected");
        exceptions.addException(e);
      }
    }

    return exceptions;
  }

  @Override
  public void formatDeleted(String formatId, boolean commit) {
    deleteDocumentFromIndex(Format.class, formatId);

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
    deleteDocumentFromIndex(TransferredResource.class, transferredResourceID);
  }

  @Override
  public ReturnWithExceptions<Void> notificationCreatedOrUpdated(Notification notification) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument notificationDoc = SolrUtils.notificationToSolrDocument(notification);

    try {
      index.add(RodaConstants.INDEX_NOTIFICATION, notificationDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Notification document was not added to index");
      exceptions.addException(e);
    }

    return exceptions;
  }

  @Override
  public void notificationDeleted(String notificationId) {
    deleteDocumentFromIndex(Notification.class, notificationId);
  }

  @Override
  public ReturnWithExceptions<Void> dipCreated(DIP dip, boolean commit) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument dipDocument = SolrUtils.dipToSolrDocument(dip);
    try {
      index.add(RodaConstants.INDEX_DIP, dipDocument);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Could not index DIP");
      exceptions.addException(e);
    }

    // index DIP Files
    try {
      final boolean recursive = true;
      CloseableIterable<OptionalWithCause<DIPFile>> allFiles = model.listDIPFilesUnder(dip.getId(), recursive);
      for (OptionalWithCause<DIPFile> file : allFiles) {
        if (file.isPresent()) {
          boolean recursiveIndexFile = false;
          ReturnWithExceptions<Void> subExceptions = indexDIPFile(dip, file.get(), recursiveIndexFile);
          exceptions.addExceptions(subExceptions.getExceptions());
        } else {
          LOGGER.error("Cannot index DIP file", file.getCause());
          exceptions.addException(file.getCause());
        }
      }
      IOUtils.closeQuietly(allFiles);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Could not index DIP files");
      exceptions.addException(e);
    }

    if (commit) {
      try {
        SolrUtils.commit(index, IndexedDIP.class);
        SolrUtils.commit(index, DIPFile.class);
      } catch (GenericException | SolrException e) {
        LOGGER.warn("Commit did not run as expected");
        exceptions.addException(e);
      }
    }

    return exceptions;
  }

  @Override
  public void dipUpdated(DIP dip, boolean commit) {
    dipDeleted(dip.getId(), commit);
    dipCreated(dip, commit);
  }

  @Override
  public void dipDeleted(String dipId, boolean commit) {
    deleteDocumentFromIndex(IndexedDIP.class, dipId);
    deleteDocumentsFromIndex(DIPFile.class, RodaConstants.DIPFILE_DIP_ID, dipId);

    if (commit) {
      try {
        SolrUtils.commit(index, IndexedDIP.class);
        SolrUtils.commit(index, DIPFile.class);
      } catch (GenericException e) {
        LOGGER.warn("Commit did not run as expected");
      }
    }
  }

  private ReturnWithExceptions<Void> indexDIPFile(DIP dip, DIPFile file, boolean recursive) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    SolrInputDocument fileDocument = SolrUtils.dipFileToSolrDocument(dip, file);

    try {
      index.add(RodaConstants.INDEX_DIP_FILE, fileDocument);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Cannot index DIP file: {}", file, e);
      exceptions.addException(e);
    }

    if (recursive && file.isDirectory()) {
      try {
        CloseableIterable<OptionalWithCause<DIPFile>> allFiles = model.listDIPFilesUnder(file, true);
        for (OptionalWithCause<DIPFile> subfile : allFiles) {
          if (subfile.isPresent()) {
            ReturnWithExceptions<Void> subExceptions = indexDIPFile(dip, subfile.get(), false);
            exceptions.addExceptions(subExceptions.getExceptions());
          } else {
            LOGGER.error("Cannot index DIP file", subfile.getCause());
            exceptions.addException(subfile.getCause());
          }
        }
        IOUtils.closeQuietly(allFiles);

      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Cannot index DIP file sub-resources: {}", file, e);
        exceptions.addException(e);
      }
    }

    return exceptions;
  }

  @Override
  public ReturnWithExceptions<Void> dipFileCreated(DIPFile file) {
    ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<>();
    try {
      boolean recursive = true;
      DIP dip = model.retrieveDIP(file.getDipId());
      ReturnWithExceptions<Void> ex = indexDIPFile(dip, file, recursive);
      exceptions.addExceptions(ex.getExceptions());
    } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error indexing DIP file: {}", file, e);
    }

    return exceptions;
  }

  @Override
  public void dipFileUpdated(DIPFile file) {
    dipFileDeleted(file.getDipId(), file.getPath(), file.getId());
    dipFileCreated(file);
  }

  @Override
  public void dipFileDeleted(String dipId, List<String> path, String fileId) {
    String uuid = IdUtils.getDIPFileId(dipId, path, fileId);
    deleteDocumentFromIndex(DIPFile.class, uuid);
  }

}
