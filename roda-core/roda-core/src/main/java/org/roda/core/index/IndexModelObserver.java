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
import java.util.ArrayList;
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
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
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
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.IterableIndexResult;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexModelObserver.class);

  private static final int TEN_MB_IN_BYTES = 10485760;

  private final SolrClient index;
  private final ModelService model;

  public IndexModelObserver(SolrClient index, ModelService model) {
    super();
    this.index = index;
    this.model = model;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipCreated(final AIP aip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);
      indexAIP(aip, ancestors).addTo(ret);
      indexRepresentations(aip, ancestors).addTo(ret);
      indexPreservationsEvents(aip.getId(), null).addTo(ret);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | SolrException e) {
      LOGGER.error("Error getting ancestors when creating AIP", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexAIP(final AIP aip, final List<String> ancestors) {
    return indexAIP(aip, ancestors, false);
  }

  private ReturnWithExceptions<Void, ModelObserver> indexAIP(final AIP aip, final List<String> ancestors,
    boolean safemode) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      SolrInputDocument aipDoc = SolrUtils.aipToSolrInputDocument(aip, ancestors, model, safemode);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
      LOGGER.trace("Adding AIP: {}", aipDoc);
    } catch (SolrException | SolrServerException | IOException | RequestNotValidException | GenericException
      | NotFoundException | AuthorizationDeniedException e) {
      ret.add(e);

      if (!safemode) {
        LOGGER.error("Error indexing AIP, trying safe mode", e);
        indexAIP(aip, ancestors, true).addTo(ret);
      } else {
        LOGGER.error("Cannot index created AIP", e);
      }
    }

    return ret;
  }

  public ReturnWithExceptions<Void, ModelObserver> indexPreservationsEvents(final String aipId) {
    return indexPreservationsEvents(aipId, null);
  }

  public ReturnWithExceptions<Void, ModelObserver> indexPreservationsEvents(final String aipId,
    final String representationId) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = null;
    try {
      if (representationId == null) {
        preservationMetadata = model.listPreservationMetadata(aipId, true);
      } else {
        preservationMetadata = model.listPreservationMetadata(aipId, representationId);
      }

      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadata) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            indexPreservationEvent(pm).addTo(ret);
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
          ret.add(opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | SolrException e) {
      LOGGER.error("Cannot index preservation events", e);
      ret.add(e);
    } finally {
      IOUtils.closeQuietly(preservationMetadata);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexPreservationEvent(PreservationMetadata pm) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
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
    } catch (SolrException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException | SolrServerException | IOException e) {
      LOGGER.error("Error when indexing preservation event {}", pm.getId(), e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexRepresentations(final AIP aip, final List<String> ancestors) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    for (Representation representation : aip.getRepresentations()) {
      indexRepresentation(aip, representation, ancestors).addTo(ret);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexRepresentation(final AIP aip,
    final Representation representation, final List<String> ancestors) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    Long sizeInBytes = 0L;
    Long numberOfDataFiles = 0L;

    try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
      representation.getId(), true)) {
      for (OptionalWithCause<File> file : allFiles) {
        if (file.isPresent()) {
          ReturnWithExceptions<Long, ModelObserver> fileExceptions = indexFile(aip, file.get(), ancestors, false);
          sizeInBytes += fileExceptions.getReturnedObject();
          ret.add(fileExceptions.getExceptions());
        } else {
          LOGGER.error("Cannot index representation file", file.getCause());
          ret.add(file.getCause());
        }

        numberOfDataFiles++;
      }

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
        sizeInBytes, numberOfDataFiles, numberOfDocumentationFiles, numberOfSchemaFiles, ancestors, model, false);
      index.add(RodaConstants.INDEX_REPRESENTATION, representationDocument);
    } catch (SolrServerException | SolrException | IOException | RequestNotValidException | GenericException
      | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index representation", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Long, ModelObserver> indexFile(AIP aip, File file, List<String> ancestors,
    boolean recursive) {
    ReturnWithExceptions<Long, ModelObserver> ret = new ReturnWithExceptions<>(this);
    Long sizeInBytes = 0L;
    SolrInputDocument fileDocument = SolrUtils.fileToSolrDocument(aip, file, ancestors);

    // Add information from PREMIS
    Binary premisFile = getFilePremisFile(file);
    if (premisFile != null) {
      try {
        SolrInputDocument premisSolrDoc = PremisV3Utils.getSolrDocument(premisFile);
        fileDocument.putAll(premisSolrDoc);
        sizeInBytes = SolrUtils.objectToLong(premisSolrDoc.get(RodaConstants.FILE_SIZE).getValue(), 0L);
      } catch (GenericException | SolrException e) {
        LOGGER.warn("Could not index file PREMIS information", e);
        ret.add(e);
      }
    }

    // Add full text
    String fulltext = getFileFulltext(file);
    if (fulltext != null) {
      fileDocument.addField(RodaConstants.FILE_FULLTEXT, fulltext);
    }

    try {
      index.add(RodaConstants.INDEX_FILE, fileDocument);

      if (recursive && file.isDirectory()) {
        try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(file, true)) {
          for (OptionalWithCause<File> subfile : allFiles) {
            if (subfile.isPresent()) {
              ReturnWithExceptions<Long, ModelObserver> fileExceptions = indexFile(aip, subfile.get(), ancestors,
                false);
              sizeInBytes += fileExceptions.getReturnedObject();
              ret.add(fileExceptions.getExceptions());
            } else {
              LOGGER.error("Cannot index file", subfile.getCause());
              ret.add(subfile.getCause());
            }
          }
        } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
          | SolrException e) {
          LOGGER.error("Cannot index file sub-resources: {}", file, e);
          ret.add(e);
        }
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Cannot index file: {}", file, e);
      ret.add(e);
    }

    ret.setReturnedObject(sizeInBytes);
    return ret;
  }

  private Binary getFilePremisFile(File file) {
    Binary premisFile = null;
    try {
      premisFile = model.retrievePreservationFile(file);
    } catch (NotFoundException e) {
      LOGGER.trace("On indexing representations, did not find PREMIS for file: {}", file, e);
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
      LOGGER.trace("Fulltext not found for file: {}", file, e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return fulltext;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipUpdated(AIP aip) {
    // TODO Is this the best way to update?
    ReturnWithExceptions<Void, ModelObserver> ret = aipDeleted(aip.getId(), false);
    aipCreated(aip).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipStateUpdated(AIP aip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      // change AIP
      SolrInputDocument aipDoc = SolrUtils.aipStateUpdateToSolrDocument(aip);
      index.add(RodaConstants.INDEX_AIP, aipDoc);

      // change Representations, Files & Preservation events
      representationsStateUpdated(aip).addTo(ret);
      preservationEventsStateUpdated(aip).addTo(ret);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Cannot do a partial update", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> representationsStateUpdated(final AIP aip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    for (Representation representation : aip.getRepresentations()) {
      representationStateUpdated(aip, representation).addTo(ret);
    }
    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> representationStateUpdated(final AIP aip,
    final Representation representation) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
      representation.getId(), true)) {
      SolrInputDocument repDoc = SolrUtils.representationStateUpdateToSolrDocument(representation, aip.getState());
      index.add(RodaConstants.INDEX_REPRESENTATION, repDoc);

      for (OptionalWithCause<File> file : allFiles) {
        if (file.isPresent()) {
          fileStateUpdated(aip, file.get(), false).addTo(ret);
        } else {
          LOGGER.error("Cannot do a partial update on File", file.getCause());
          ret.add(file.getCause());
        }
      }
    } catch (SolrServerException | AuthorizationDeniedException | IOException | NotFoundException | GenericException
      | RequestNotValidException | SolrException e) {
      LOGGER.error("Cannot do a partial update", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> fileStateUpdated(AIP aip, File file, boolean recursive) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      SolrInputDocument fileDoc = SolrUtils.fileStateUpdateToSolrDocument(file, aip.getState());
      index.add(RodaConstants.INDEX_FILE, fileDoc);

      if (recursive && file.isDirectory()) {
        try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(file, true)) {
          for (OptionalWithCause<File> subfile : allFiles) {
            if (subfile.isPresent()) {
              fileStateUpdated(aip, subfile.get(), false).addTo(ret);
            } else {
              LOGGER.error("Cannot index file sub-resources", subfile.getCause());
              ret.add(subfile.getCause());
            }
          }
        } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
          | SolrException e) {
          LOGGER.error("Cannot index file sub-resources: {}", file, e);
          ret.add(e);
        }
      }
    } catch (SolrServerException | IOException | SolrException e) {
      LOGGER.error("Cannot index file: {}", file, e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> preservationEventsStateUpdated(final AIP aip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = model
      .listPreservationMetadata(aip.getId(), true)) {
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadata) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            try {
              preservationEventStateUpdated(pm, aip.getState());
            } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
              | AuthorizationDeniedException e) {
              LOGGER.error("Cannot index premis event", e);
              ret.add(e);
            }
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
          ret.add(opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | SolrException | IOException e) {
      LOGGER.error("Cannot index preservation events", e);
      ret.add(e);
    }

    return ret;
  }

  private void preservationEventStateUpdated(PreservationMetadata pm, AIPState state) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, SolrServerException, IOException {
    SolrInputDocument premisEventDocument = SolrUtils.preservationEventStateUpdateToSolrDocument(pm.getId(),
      pm.getAipId(), state);
    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipMoved(AIP aip, String oldParentId, String newParentId) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      LOGGER.debug("Reindexing moved aip {}", aip.getId());
      List<String> topAncestors = SolrUtils.getAncestors(newParentId, model);
      SolrInputDocument aipDoc = SolrUtils.updateAIPParentId(aip.getId(), newParentId, topAncestors);
      index.add(RodaConstants.INDEX_AIP, aipDoc);
      updateRepresentationAndFileAncestors(aip, topAncestors).addTo(ret);

      LOGGER.debug("Finding descendants of moved aip {}", aip.getId());
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()),
        new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.FALSE.toString()));
      List<String> aipFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_PARENT_ID,
        RodaConstants.AIP_HAS_REPRESENTATIONS);

      List<IndexedAIP> items = new ArrayList<>();
      new IterableIndexResult<>(index, IndexedAIP.class, filter, Sorter.NONE, Facets.NONE, null, false, true, aipFields)
        .forEach(items::add);

      for (IndexedAIP item : items) {
        SolrInputDocument descendantDoc;
        try {
          LOGGER.debug("Reindexing aip {} descendant {}", aip.getId(), item.getId());
          List<String> ancestors = SolrUtils.getAncestors(item.getParentID(), model);
          descendantDoc = SolrUtils.updateAIPAncestors(item.getId(), ancestors);
          index.add(RodaConstants.INDEX_AIP, descendantDoc);

          // update representation and file ancestors information
          if (item.getHasRepresentations()) {
            AIP aipModel = model.retrieveAIP(item.getId());
            updateRepresentationAndFileAncestors(aipModel, ancestors).addTo(ret);
          }
        } catch (SolrServerException | IOException | NotFoundException | SolrException e) {
          LOGGER.error("Error indexing moved AIP {} from {} to {}", aip.getId(), oldParentId, newParentId, e);
          ret.add(e);
        }
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | SolrServerException
      | IOException | NotFoundException | SolrException e) {
      LOGGER.error("Error indexing moved AIP {} from {} to {}", aip.getId(), oldParentId, newParentId, e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> updateRepresentationAndFileAncestors(AIP aip,
    List<String> ancestors) throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    SolrServerException, IOException, NotFoundException {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      for (Representation representation : aip.getRepresentations()) {
        SolrInputDocument descendantRepresentationDoc = SolrUtils
          .updateRepresentationAncestors(IdUtils.getRepresentationId(representation), ancestors);
        index.add(RodaConstants.INDEX_REPRESENTATION, descendantRepresentationDoc);

        try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
          representation.getId(), true)) {
          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();
              SolrInputDocument descendantFileDoc = SolrUtils.updateFileAncestors(IdUtils.getFileId(file), ancestors);
              index.add(RodaConstants.INDEX_FILE, descendantFileDoc);
            }
          }
        } catch (SolrException | RequestNotValidException | GenericException | AuthorizationDeniedException
          | SolrServerException | IOException | NotFoundException e) {
          LOGGER.error("Error updating file ancestors", e);
          ret.add(e);
        }
      }
    } catch (SolrException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | SolrServerException | IOException e) {
      LOGGER.error("Error updating representation ancestors", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipDeleted(String aipId, boolean deleteIncidences) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(IndexedAIP.class, aipId);
      deleteDocumentsFromIndex(IndexedRepresentation.class, RodaConstants.REPRESENTATION_AIP_ID, aipId);
      deleteDocumentsFromIndex(IndexedFile.class, RodaConstants.FILE_AIP_ID, aipId);
      deleteDocumentsFromIndex(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId);

      if (deleteIncidences) {
        deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_AIP_ID, aipId);
      }
    } catch (SolrException | RODAException e) {
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      AIP aip = model.retrieveAIP(descriptiveMetadata.getAipId());
      List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);

      if (descriptiveMetadata.isFromAIP()) {
        ret = indexAIP(aip, ancestors);
      } else {
        Representation representation = model.retrieveRepresentation(descriptiveMetadata.getAipId(),
          descriptiveMetadata.getRepresentationId());
        ret = indexRepresentation(aip, representation, ancestors);
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | SolrException e) {
      LOGGER.error("Failed to index AIP or representation when creating descriptive metadata", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      AIP aip = model.retrieveAIP(descriptiveMetadata.getAipId());
      List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);

      if (descriptiveMetadata.isFromAIP()) {
        ret = indexAIP(aip, ancestors);
      } else {
        Representation representation = model.retrieveRepresentation(descriptiveMetadata.getAipId(),
          descriptiveMetadata.getRepresentationId());
        ret = indexRepresentation(aip, representation, ancestors);
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | SolrException e) {
      LOGGER.error("Failed to index AIP or representation when updating descriptive metadata", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> descriptiveMetadataDeleted(String aipId, String representationId,
    String descriptiveMetadataBinaryId) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    if (representationId == null) {
      try {
        AIP aip = model.retrieveAIP(aipId);
        List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);
        ret = indexAIP(aip, ancestors);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
        | SolrException e) {
        LOGGER.error("Error when descriptive metadata deleted on retrieving the full AIP", e);
        ret.add(e);
      }
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> representationCreated(Representation representation) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      AIP aip = model.retrieveAIP(representation.getAipId());
      List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);

      indexRepresentation(aip, representation, ancestors).addTo(ret);
      indexPreservationsEvents(aip.getId(), representation.getId()).addTo(ret);

      if (aip.getRepresentations().size() == 1) {
        SolrInputDocument doc = SolrUtils.updateAIPHasRepresentations(aip.getId(), true);
        index.add(RodaConstants.INDEX_AIP, doc);
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index representation: {}", representation, e);
      ret.add(e);
    } catch (SolrServerException | IOException | SolrException e) {
      LOGGER.error("Cannot update hasRepresentations flag on AIP", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> representationUpdated(Representation representation) {
    ReturnWithExceptions<Void, ModelObserver> ret = representationDeleted(representation.getAipId(),
      representation.getId(), false);
    representationCreated(representation).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> representationDeleted(String aipId, String representationId,
    boolean deleteIncidences) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      String representationUUID = IdUtils.getRepresentationId(aipId, representationId);
      deleteDocumentFromIndex(IndexedRepresentation.class, representationUUID);
      deleteDocumentsFromIndex(IndexedFile.class, RodaConstants.FILE_REPRESENTATION_UUID, representationUUID);
      deleteDocumentsFromIndex(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID,
        representationUUID);

      if (deleteIncidences) {
        deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representationId);
      }

      try {
        AIP aip = model.retrieveAIP(aipId);
        if (aip.getRepresentations().size() == 0) {
          SolrInputDocument doc = SolrUtils.updateAIPHasRepresentations(aipId, false);
          index.add(RodaConstants.INDEX_AIP, doc);
        }
      } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
        | SolrServerException | IOException e) {
        LOGGER.error("Cannot update hasRepresentations flag on AIP", e);
        ret.add(e);
      }
    } catch (SolrException | RODAException e) {
      LOGGER.error("Cannot delete representation from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> fileCreated(File file) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      AIP aip = model.retrieveAIP(file.getAipId());
      List<String> ancestors = SolrUtils.getAncestors(aip.getParentId(), model);
      indexFile(aip, file, ancestors, true).addTo(ret);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | SolrException e) {
      LOGGER.error("Error indexing file: {}", file, e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> fileUpdated(File file) {
    ReturnWithExceptions<Void, ModelObserver> ret = fileDeleted(file.getAipId(), file.getRepresentationId(),
      file.getPath(), file.getId(), false);
    fileCreated(file).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> fileDeleted(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, boolean deleteIncidences) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      String uuid = IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId);
      deleteDocumentFromIndex(IndexedFile.class, uuid);

      if (deleteIncidences) {
        deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_FILE_ID, fileId);
      }
    } catch (SolrException | RODAException e) {
      LOGGER.error("Error deleting file from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> logEntryCreated(LogEntry entry) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument logDoc = SolrUtils.logEntryToSolrDocument(entry);

    try {
      index.add(RodaConstants.INDEX_ACTION_LOG, logDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Log entry was not added to index");
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> userCreated(User user) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      addDocumentToIndex(RODAMember.class, user);
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error adding user on index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> userUpdated(User user) {
    ReturnWithExceptions<Void, ModelObserver> ret = userDeleted(user.getId());
    userCreated(user).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> userDeleted(String userID) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(RODAMember.class, userID);
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error deleting user from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> groupCreated(Group group) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      addDocumentToIndex(RODAMember.class, group);
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error adding group on index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> groupUpdated(Group group) {
    ReturnWithExceptions<Void, ModelObserver> ret = groupDeleted(group.getId());
    groupCreated(group).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> groupDeleted(String groupID) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(RODAMember.class, groupID);
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error deleting group from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> preservationMetadataCreated(PreservationMetadata pm) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
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
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> preservationMetadataUpdated(
    PreservationMetadata preservationMetadata) {
    return preservationMetadataCreated(preservationMetadata);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> preservationMetadataDeleted(
    PreservationMetadata preservationMetadata) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      PreservationMetadataType type = preservationMetadata.getType();
      String preservationMetadataId = preservationMetadata.getId();
      if (PreservationMetadataType.EVENT.equals(type)) {
        deleteDocumentFromIndex(IndexedPreservationEvent.class, preservationMetadataId);
      } else if (PreservationMetadataType.AGENT.equals(type)) {
        deleteDocumentFromIndex(IndexedPreservationAgent.class, preservationMetadataId);
      }
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error deleting preservation metadata from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> otherMetadataCreated(OtherMetadata otherMetadataBinary) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    if (RodaConstants.OTHER_METADATA_TYPE_APACHE_TIKA.equalsIgnoreCase(otherMetadataBinary.getType())
      && RodaConstants.TIKA_FILE_SUFFIX_METADATA.equalsIgnoreCase(otherMetadataBinary.getFileSuffix())) {
      try {
        SolrInputDocument solrFile = SolrUtils.addOtherPropertiesToIndexedFile("tika_", otherMetadataBinary, model,
          index);
        index.add(RodaConstants.INDEX_FILE, solrFile);
      } catch (SolrServerException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException | XPathExpressionException | ParserConfigurationException | SAXException
        | IOException | SolrException e) {
        LOGGER.error("Error adding other properties to indexed file", e);
        ret.add(e);
      }
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> jobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument jobDoc = SolrUtils.jobToSolrDocument(job);

    try {
      index.add(RodaConstants.INDEX_JOB, jobDoc);

      if (reindexJobReports) {
        indexJobReports(job).addTo(ret);
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Job document was not added to index", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexJobReports(Job job) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    StorageService storage = RodaCoreFactory.getStorageService();
    try (CloseableIterable<Resource> listResourcesUnderDirectory = storage
      .listResourcesUnderDirectory(ModelUtils.getJobReportsStoragePath(job.getId()), true)) {

      if (listResourcesUnderDirectory != null) {
        for (Resource resource : listResourcesUnderDirectory) {
          if (!resource.isDirectory()) {
            try {
              Binary binary = storage.getBinary(resource.getStoragePath());
              InputStream inputStream = binary.getContent().createInputStream();
              Report objectFromJson = JsonUtils.getObjectFromJson(inputStream, Report.class);
              IOUtils.closeQuietly(inputStream);

              jobReportCreatedOrUpdated(objectFromJson, job).addTo(ret);
            } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
              | IOException | SolrException e) {
              LOGGER.error("Error getting report json from binary", e);
              ret.add(e);
            }
          }
        }
      }
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | SolrException | IOException e) {
      LOGGER.error("Error reindexing job reports", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> jobDeleted(String jobId) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(Job.class, jobId);
    } catch (RODAException e) {
      LOGGER.error("Error deleting job from index", e);
      ret.add(e);
    }

    return ret;
  }

  private <T extends IsIndexed> void addDocumentToIndex(Class<T> classToAdd, T instance) throws RODAException {
    try {
      SolrUtils.create(index, classToAdd, instance);
    } catch (GenericException e) {
      LOGGER.error("Error adding document to index", e);
      throw e;
    }
  }

  private <T extends IsIndexed> void deleteDocumentFromIndex(Class<T> classToDelete, String... ids)
    throws RODAException {
    try {
      SolrUtils.delete(index, classToDelete, Arrays.asList(ids));
    } catch (GenericException e) {
      LOGGER.error("Error deleting document from index", e);
      throw e;
    }
  }

  private <T extends IsIndexed> void deleteDocumentsFromIndex(Class<T> classToDelete, String fieldName,
    String fieldValue) throws RODAException {
    try {
      SolrUtils.delete(index, classToDelete, new Filter(new SimpleFilterParameter(fieldName, fieldValue)));
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error deleting from index", e);
      throw e;
    }
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> jobReportCreatedOrUpdated(Report jobReport, Job job) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument jobReportDoc = SolrUtils.jobReportToSolrDocument(jobReport, job, index);

    try {
      index.add(RodaConstants.INDEX_JOB_REPORT, jobReportDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Job report document was not added to index");
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> jobReportDeleted(String jobReportId) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(IndexedReport.class, jobReportId);
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error deleting job report from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipPermissionsUpdated(AIP aip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      // change AIP
      SolrInputDocument aipDoc = SolrUtils.aipPermissionsUpdateToSolrDocument(aip);
      index.add(RodaConstants.INDEX_AIP, aipDoc);

      // change Representations, Files and Preservation events
      ret = representationsPermissionsUpdated(aip);
      preservationEventsPermissionsUpdated(aip).addTo(ret);
    } catch (SolrServerException | IOException | SolrException e) {
      LOGGER.error("Cannot do a partial update", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipPermissionsUpdated(DIP dip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      // change DIP
      SolrInputDocument dipDoc = SolrUtils.dipPermissionsUpdateToSolrDocument(dip);
      index.add(RodaConstants.INDEX_DIP, dipDoc);
    } catch (SolrServerException | IOException | SolrException e) {
      LOGGER.error("Cannot do a partial update", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> representationsPermissionsUpdated(final AIP aip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    for (Representation representation : aip.getRepresentations()) {
      representationPermissionsUpdated(aip, representation).addTo(ret);
    }
    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> representationPermissionsUpdated(final AIP aip,
    final Representation representation) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
      representation.getId(), true)) {
      SolrInputDocument repDoc = SolrUtils.representationPermissionsUpdateToSolrDocument(representation,
        aip.getPermissions());
      index.add(RodaConstants.INDEX_REPRESENTATION, repDoc);

      for (OptionalWithCause<File> file : allFiles) {
        if (file.isPresent()) {
          filePermissionsUpdated(aip, file.get(), false).addTo(ret);
        } else {
          LOGGER.error("Cannot do a partial update on file", file.getCause());
          ret.add(file.getCause());
        }
      }
    } catch (SolrServerException | AuthorizationDeniedException | IOException | NotFoundException | GenericException
      | RequestNotValidException | SolrException e) {
      LOGGER.error("Cannot do a partial update", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> filePermissionsUpdated(AIP aip, File file, boolean recursive) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      SolrInputDocument fileDoc = SolrUtils.filePermissionsUpdateToSolrDocument(file, aip.getPermissions());
      index.add(RodaConstants.INDEX_FILE, fileDoc);

      if (recursive && file.isDirectory()) {
        try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(file, true)) {
          for (OptionalWithCause<File> subfile : allFiles) {
            if (subfile.isPresent()) {
              filePermissionsUpdated(aip, subfile.get(), false).addTo(ret);
            } else {
              LOGGER.error("Cannot index file sub-resources file", subfile.getCause());
              ret.add(subfile.getCause());
            }
          }
        } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
          LOGGER.error("Cannot index file sub-resources: {}", file, e);
          ret.add(e);
        }
      }
    } catch (SolrServerException | IOException | SolrException e) {
      LOGGER.error("Cannot index file: {}", file, e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> preservationEventsPermissionsUpdated(final AIP aip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = model
      .listPreservationMetadata(aip.getId(), true)) {
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadata) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (PreservationMetadataType.EVENT.equals(pm.getType())) {
            try {
              preservationEventPermissionsUpdated(pm, aip.getPermissions(), aip.getState());
            } catch (SolrServerException | IOException | RequestNotValidException | GenericException | NotFoundException
              | AuthorizationDeniedException | SolrException e) {
              LOGGER.error("Cannot index premis event", e);
              ret.add(e);
            }
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
          ret.add(opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | SolrException | IOException e) {
      LOGGER.error("Cannot index preservation events", e);
      ret.add(e);
    }

    return ret;
  }

  private void preservationEventPermissionsUpdated(PreservationMetadata pm, Permissions permissions, AIPState state)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    SolrServerException, IOException {
    SolrInputDocument premisEventDocument = SolrUtils.preservationEventPermissionsUpdateToSolrDocument(pm.getId(),
      pm.getAipId(), permissions, state);
    index.add(RodaConstants.INDEX_PRESERVATION_EVENTS, premisEventDocument);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> riskCreatedOrUpdated(Risk risk, int incidences, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument riskDoc = SolrUtils.riskToSolrDocument(risk, incidences);

    try {
      index.add(RodaConstants.INDEX_RISK, riskDoc);

      if (commit) {
        try {
          SolrUtils.commit(index, IndexedRisk.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Risk document was not added to index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> riskDeleted(String riskId, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(IndexedRisk.class, riskId);

      if (commit) {
        try {
          SolrUtils.commit(index, IndexedRisk.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (RODAException | SolrException e) {
      LOGGER.warn("Error deleting risk from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> riskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence,
    boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument incidenceDoc = SolrUtils.riskIncidenceToSolrDocument(riskIncidence);

    try {
      index.add(RodaConstants.INDEX_RISK_INCIDENCE, incidenceDoc);

      if (commit) {
        try {
          SolrUtils.commit(index, RiskIncidence.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Risk incidence document was not added to index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> riskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(RiskIncidence.class, riskIncidenceId);

      if (commit) {
        try {
          SolrUtils.commit(index, RiskIncidence.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (RODAException | SolrException e) {
      LOGGER.warn("Error deleting risk incidence from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> representationInformationCreatedOrUpdated(
    RepresentationInformation ri, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument doc = SolrUtils.representationInformationToSolrDocument(ri);

    try {
      index.add(RodaConstants.INDEX_REPRESENTATION_INFORMATION, doc);

      if (commit) {
        try {
          SolrUtils.commit(index, RepresentationInformation.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("RepresentationInformation document was not added to index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> representationInformationDeleted(String representationInformationId,
    boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(RepresentationInformation.class, representationInformationId);

      if (commit) {
        try {
          SolrUtils.commit(index, RepresentationInformation.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (RODAException | SolrException e) {
      LOGGER.warn("Error deleting representation information from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> formatCreatedOrUpdated(Format f, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument formatDoc = SolrUtils.formatToSolrDocument(f);

    try {
      index.add(RodaConstants.INDEX_FORMAT, formatDoc);

      if (commit) {
        try {
          SolrUtils.commit(index, Format.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Format document was not added to index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> formatDeleted(String formatId, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(Format.class, formatId);

      if (commit) {
        try {
          SolrUtils.commit(index, Format.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (RODAException | SolrException e) {
      LOGGER.warn("Error deleting format from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> transferredResourceDeleted(String transferredResourceID) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(TransferredResource.class, transferredResourceID);
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error deleting transferred resource from index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> notificationCreatedOrUpdated(Notification notification) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument notificationDoc = SolrUtils.notificationToSolrDocument(notification);

    try {
      index.add(RodaConstants.INDEX_NOTIFICATION, notificationDoc);
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Notification document was not added to index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> notificationDeleted(String notificationId) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      deleteDocumentFromIndex(Notification.class, notificationId);
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error deleting notificationfrom index", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipCreated(DIP dip, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument dipDocument = SolrUtils.dipToSolrDocument(dip);
    try {
      index.add(RodaConstants.INDEX_DIP, dipDocument);

      // index DIP Files
      try (CloseableIterable<OptionalWithCause<DIPFile>> allFiles = model.listDIPFilesUnder(dip.getId(), true)) {
        for (OptionalWithCause<DIPFile> file : allFiles) {
          if (file.isPresent()) {
            indexDIPFile(dip, file.get(), false).addTo(ret);
          } else {
            LOGGER.error("Cannot index DIP file", file.getCause());
            ret.add(file.getCause());
          }
        }

        if (commit) {
          try {
            SolrUtils.commit(index, IndexedDIP.class);
            SolrUtils.commit(index, DIPFile.class);
          } catch (GenericException | SolrException e) {
            LOGGER.warn("Commit did not run as expected");
            ret.add(e);
          }
        }
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Could not index DIP files", e);
        ret.add(e);
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Could not index DIP", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipUpdated(DIP dip, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = dipDeleted(dip.getId(), commit);
    dipCreated(dip, commit).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipDeleted(String dipId, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    try {
      deleteDocumentFromIndex(IndexedDIP.class, dipId);
      deleteDocumentsFromIndex(DIPFile.class, RodaConstants.DIPFILE_DIP_ID, dipId);

      if (commit) {
        try {
          SolrUtils.commit(index, IndexedDIP.class);
          SolrUtils.commit(index, DIPFile.class);
        } catch (GenericException | SolrException e) {
          LOGGER.warn("Commit did not run as expected", e);
          ret.add(e);
        }
      }
    } catch (RODAException e) {
      LOGGER.warn("Error deleting DIP and/or its DIP files from index", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexDIPFile(DIP dip, DIPFile file, boolean recursive) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrInputDocument fileDocument = SolrUtils.dipFileToSolrDocument(dip, file);

    try {
      index.add(RodaConstants.INDEX_DIP_FILE, fileDocument);

      if (recursive && file.isDirectory()) {
        try (CloseableIterable<OptionalWithCause<DIPFile>> allFiles = model.listDIPFilesUnder(file, true)) {
          for (OptionalWithCause<DIPFile> subfile : allFiles) {
            if (subfile.isPresent()) {
              indexDIPFile(dip, subfile.get(), false).addTo(ret);
            } else {
              LOGGER.error("Cannot index DIP file", subfile.getCause());
              ret.add(subfile.getCause());
            }
          }
        } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
          | SolrException e) {
          LOGGER.error("Cannot index DIP file sub-resources: {}", file, e);
          ret.add(e);
        }
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Cannot index DIP file: {}", file, e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipFileCreated(DIPFile file) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      DIP dip = model.retrieveDIP(file.getDipId());
      indexDIPFile(dip, file, true).addTo(ret);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | SolrException e) {
      LOGGER.error("Error indexing DIP file: {}", file, e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipFileUpdated(DIPFile file) {
    ReturnWithExceptions<Void, ModelObserver> ret = dipFileDeleted(file.getDipId(), file.getPath(), file.getId());
    dipFileCreated(file).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipFileDeleted(String dipId, List<String> path, String fileId) {
    ReturnWithExceptions<Void, ModelObserver> exceptions = new ReturnWithExceptions<>(this);
    String uuid = IdUtils.getDIPFileId(dipId, path, fileId);
    try {
      deleteDocumentFromIndex(DIPFile.class, uuid);
    } catch (RODAException | SolrException e) {
      LOGGER.error("Error deleting DIP file from index", e);
      exceptions.add(e);
    }

    return exceptions;
  }

}
