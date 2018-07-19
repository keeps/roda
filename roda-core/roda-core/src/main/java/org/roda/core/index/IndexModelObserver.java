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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
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
import org.roda.core.index.schema.collections.AIPCollection;
import org.roda.core.index.schema.collections.DIPFileCollection;
import org.roda.core.index.schema.collections.FileCollection;
import org.roda.core.index.schema.collections.JobReportCollection;
import org.roda.core.index.schema.collections.PreservationEventCollection;
import org.roda.core.index.schema.collections.RepresentationCollection;
import org.roda.core.index.schema.collections.RiskCollection;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
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
      if (ret.isEmpty()) {
        indexRepresentations(aip, ancestors).addTo(ret);
        if (ret.isEmpty()) {
          indexPreservationsEvents(aip.getId(), null).addTo(ret);
        }
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
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

    SolrUtils.create2(index, (ModelObserver) this, IndexedAIP.class, aip, new AIPCollection.Info(ancestors, safemode))
      .addTo(ret);

    // if there was an error indexing, try in safe mode
    if (!ret.isEmpty()) {
      if (!safemode) {
        LOGGER.error("Error indexing AIP, trying safe mode", ret.getExceptions().get(0));
        indexAIP(aip, ancestors, true).addTo(ret);
      } else {
        LOGGER.error("Cannot index created AIP", ret.getExceptions().get(0));
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

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = (representationId == null)
      ? model.listPreservationMetadata(aipId, true)
      : model.listPreservationMetadata(aipId, representationId)) {

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
      | IOException e) {
      LOGGER.error("Cannot index preservation events", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexPreservationEvent(PreservationMetadata pm) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    AIP aip = null;
    try {
      aip = model.retrieveAIP(pm.getAipId());
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error indexing preservation events", e);
      ret.add(e);
    }

    SolrUtils.create2(index, (ModelObserver) this, IndexedPreservationEvent.class, pm,
      new PreservationEventCollection.Info(aip)).addTo(ret);

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
    Long numberOfDataFolders = 0L;

    try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
      representation.getId(), true)) {
      for (OptionalWithCause<File> file : allFiles) {
        if (file.isPresent()) {
          sizeInBytes += indexFile(aip, file.get(), ancestors, false).addTo(ret).getReturnedObject();

          if (file.get().isDirectory()) {
            numberOfDataFolders++;
          } else {
            numberOfDataFiles++;
          }
        } else {
          LOGGER.error("Cannot index representation file", file.getCause());
          ret.add(file.getCause());
        }
      }

      // TODO support safemode
      boolean safemode = false;

      RepresentationCollection.Info info = new RepresentationCollection.Info(aip, ancestors, sizeInBytes,
        numberOfDataFiles, numberOfDataFolders, safemode);
      SolrUtils.create2(index, (ModelObserver) this, IndexedRepresentation.class, representation, info).addTo(ret);
    } catch (IOException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index representation", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Long, ModelObserver> indexFile(AIP aip, File file, List<String> ancestors,
    boolean recursive) {
    ReturnWithExceptions<Long, ModelObserver> ret = new ReturnWithExceptions<>(this);

    Long sizeInBytes = 0L;

    FileCollection.Info info = new FileCollection.Info(aip, ancestors);
    SolrUtils.create2(index, (ModelObserver) this, IndexedFile.class, file, info).addTo(ret);

    sizeInBytes = (Long) info.getAccumulators().get(RodaConstants.FILE_SIZE);

    if (ret.isEmpty()) {
      if (recursive && file.isDirectory()) {
        try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(file, true)) {
          for (OptionalWithCause<File> subfile : allFiles) {
            if (subfile.isPresent()) {
              sizeInBytes += indexFile(aip, subfile.get(), ancestors, false).addTo(ret).getReturnedObject();
            } else {
              LOGGER.error("Cannot index file", subfile.getCause());
              ret.add(subfile.getCause());
            }
          }
        } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
          | IOException e) {
          LOGGER.error("Cannot index file sub-resources: {}", file, e);
          ret.add(e);
        }
      }
    } else {
      LOGGER.error("Cannot index file: {}", file);
    }

    ret.setReturnedObject(sizeInBytes);
    return ret;
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

    // change AIP
    SolrUtils
      .update(index, IndexedAIP.class, aip.getId(),
        Collections.singletonMap(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(aip.getState())), (ModelObserver) this)
      .addTo(ret);

    if (ret.isEmpty()) {
      // change Representations, Files & Preservation events
      representationsStateUpdated(aip).addTo(ret);
      preservationEventsStateUpdated(aip).addTo(ret);
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
      SolrUtils.update(index, IndexedRepresentation.class, IdUtils.getRepresentationId(representation),
        Collections.singletonMap(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(aip.getState())), (ModelObserver) this)
        .addTo(ret);

      if (ret.isEmpty()) {
        for (OptionalWithCause<File> file : allFiles) {
          if (file.isPresent()) {
            fileStateUpdated(aip, file.get(), false).addTo(ret);
          } else {
            LOGGER.error("Cannot do a partial update on File", file.getCause());
            ret.add(file.getCause());
          }
        }
      } else {
        LOGGER.error("Cannot index representation: {}", representation);
      }
    } catch (AuthorizationDeniedException | IOException | NotFoundException | GenericException
      | RequestNotValidException e) {
      LOGGER.error("Cannot do a partial update", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> fileStateUpdated(AIP aip, File file, boolean recursive) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    SolrUtils
      .update(index, IndexedFile.class, IdUtils.getFileId(file),
        Collections.singletonMap(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(aip.getState())), (ModelObserver) this)
      .addTo(ret);

    if (ret.isEmpty()) {
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
          | IOException e) {
          LOGGER.error("Cannot index file sub-resources: {}", file, e);
          ret.add(e);
        }
      }
    } else {
      LOGGER.error("Cannot index file: {}", file);
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
            preservationEventStateUpdated(pm, aip.getState()).addTo(ret);
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
          ret.add(opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IOException e) {
      LOGGER.error("Cannot index preservation events", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> preservationEventStateUpdated(PreservationMetadata pm,
    AIPState state) {
    Map<String, Object> fieldsToUpdate = new HashMap<>();
    fieldsToUpdate.put(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(state));
    fieldsToUpdate.put(RodaConstants.PRESERVATION_EVENT_AIP_ID, pm.getAipId());
    fieldsToUpdate.put(RodaConstants.INDEX_ID, pm.getId());
    return SolrUtils.update(index, IndexedPreservationEvent.class, IdUtils.getPreservationId(pm), fieldsToUpdate,
      (ModelObserver) this);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipMoved(AIP aip, String oldParentId, String newParentId) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      LOGGER.debug("Reindexing moved aip {}", aip.getId());
      List<String> topAncestors = SolrUtils.getAncestors(newParentId, model);

      Map<String, Object> updatedFields = new HashMap<>();
      updatedFields.put(RodaConstants.AIP_PARENT_ID, newParentId);
      updatedFields.put(RodaConstants.AIP_ANCESTORS, topAncestors);
      SolrUtils.update(index, IndexedAIP.class, aip.getId(), updatedFields, (ModelObserver) this).addTo(ret);

      if (ret.isEmpty()) {
        updateRepresentationAndFileAncestors(aip, topAncestors).addTo(ret);

        LOGGER.debug("Finding descendants of moved aip {}", aip.getId());
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()),
          new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.FALSE.toString()));
        List<String> aipFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_PARENT_ID,
          RodaConstants.AIP_HAS_REPRESENTATIONS);

        List<IndexedAIP> items = new ArrayList<>();
        try (IterableIndexResult<IndexedAIP> childrenResults = new IterableIndexResult<>(index, IndexedAIP.class,
          filter, null, false, aipFields)) {
          childrenResults.forEach(items::add);
        }

        for (IndexedAIP item : items) {
          try {
            LOGGER.debug("Reindexing aip {} descendant {}", aip.getId(), item.getId());
            List<String> ancestors = SolrUtils.getAncestors(item.getParentID(), model);
            SolrUtils.update(index, IndexedAIP.class, aip.getId(),
              Collections.singletonMap(RodaConstants.AIP_ANCESTORS, ancestors), (ModelObserver) this).addTo(ret);

            // update representation and file ancestors information
            if (item.getHasRepresentations()) {
              AIP aipModel = model.retrieveAIP(item.getId());
              updateRepresentationAndFileAncestors(aipModel, ancestors).addTo(ret);
            }
          } catch (NotFoundException e) {
            LOGGER.error("Error indexing moved AIP {} from {} to {}", aip.getId(), oldParentId, newParentId, e);
            ret.add(e);
          }
        }
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      LOGGER.error("Error indexing moved AIP {} from {} to {}", aip.getId(), oldParentId, newParentId, e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> updateRepresentationAndFileAncestors(AIP aip,
    List<String> ancestors) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    for (Representation representation : aip.getRepresentations()) {
      SolrUtils.update(index, IndexedRepresentation.class, IdUtils.getRepresentationId(representation),
        Collections.singletonMap(RodaConstants.REPRESENTATION_ANCESTORS, ancestors), (ModelObserver) this).addTo(ret);

      if (ret.isEmpty()) {
        try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
          representation.getId(), true)) {
          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();
              SolrUtils.update(index, IndexedFile.class, IdUtils.getFileId(file),
                Collections.singletonMap(RodaConstants.FILE_ANCESTORS, ancestors), (ModelObserver) this).addTo(ret);
            }
          }
        } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException
          | NotFoundException e) {
          LOGGER.error("Error updating file ancestors", e);
          ret.add(e);
        }
      }
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipDeleted(String aipId, boolean deleteIncidences) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    deleteDocumentFromIndex(IndexedAIP.class, aipId).addTo(ret);
    deleteDocumentsFromIndex(IndexedRepresentation.class, RodaConstants.REPRESENTATION_AIP_ID, aipId).addTo(ret);
    deleteDocumentsFromIndex(IndexedFile.class, RodaConstants.FILE_AIP_ID, aipId).addTo(ret);
    deleteDocumentsFromIndex(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId).addTo(ret);

    if (deleteIncidences) {
      deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_AIP_ID, aipId).addTo(ret);
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
        indexAIP(aip, ancestors).addTo(ret);
      } else {
        Representation representation = model.retrieveRepresentation(descriptiveMetadata.getAipId(),
          descriptiveMetadata.getRepresentationId());
        indexRepresentation(aip, representation, ancestors).addTo(ret);
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
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
        indexAIP(aip, ancestors).addTo(ret);
      } else {
        Representation representation = model.retrieveRepresentation(descriptiveMetadata.getAipId(),
          descriptiveMetadata.getRepresentationId());
        indexRepresentation(aip, representation, ancestors).addTo(ret);
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
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
        indexAIP(aip, ancestors).addTo(ret);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
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
      if (ret.isEmpty()) {
        indexPreservationsEvents(aip.getId(), representation.getId()).addTo(ret);

        if (aip.getRepresentations().size() == 1) {
          SolrUtils.update(index, IndexedAIP.class, aip.getId(),
            Collections.singletonMap(RodaConstants.AIP_HAS_REPRESENTATIONS, true), (ModelObserver) this).addTo(ret);
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Cannot index representation: {}", representation, e);
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

    String representationUUID = IdUtils.getRepresentationId(aipId, representationId);
    deleteDocumentFromIndex(IndexedRepresentation.class, representationUUID).addTo(ret);
    deleteDocumentsFromIndex(IndexedFile.class, RodaConstants.FILE_REPRESENTATION_UUID, representationUUID).addTo(ret);
    deleteDocumentsFromIndex(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID,
      representationUUID).addTo(ret);

    if (deleteIncidences) {
      deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representationId)
        .addTo(ret);
    }

    try {
      AIP aip = model.retrieveAIP(aipId);
      if (aip.getRepresentations().size() == 0) {
        SolrUtils.update(index, IndexedAIP.class, aip.getId(),
          Collections.singletonMap(RodaConstants.AIP_HAS_REPRESENTATIONS, false), (ModelObserver) this).addTo(ret);
      }
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Cannot update hasRepresentations flag on AIP", e);
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
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
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

    String uuid = IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId);
    deleteDocumentFromIndex(IndexedFile.class, uuid).addTo(ret);

    if (deleteIncidences) {
      deleteDocumentsFromIndex(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_FILE_ID, fileId).addTo(ret);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> logEntryCreated(LogEntry entry) {
    return SolrUtils.create2(index, this, LogEntry.class, entry);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> userCreated(User user) {
    return addDocumentToIndex(RODAMember.class, user);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> userUpdated(User user) {
    ReturnWithExceptions<Void, ModelObserver> ret = userDeleted(user.getId());
    userCreated(user).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> userDeleted(String userId) {
    return deleteDocumentFromIndex(RODAMember.class, IdUtils.getUserId(userId));
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> groupCreated(Group group) {
    return addDocumentToIndex(RODAMember.class, group);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> groupUpdated(Group group) {
    ReturnWithExceptions<Void, ModelObserver> ret = groupDeleted(group.getId());
    groupCreated(group).addTo(ret);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> groupDeleted(String groupId) {
    return deleteDocumentFromIndex(RODAMember.class, IdUtils.getGroupId(groupId));
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> preservationMetadataCreated(PreservationMetadata pm) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    PreservationMetadataType type = pm.getType();
    if (PreservationMetadataType.EVENT.equals(type)) {
      SolrUtils.create2(index, (ModelObserver) this, IndexedPreservationEvent.class, pm).addTo(ret);
    } else if (PreservationMetadataType.AGENT.equals(type)) {
      SolrUtils.create2(index, (ModelObserver) this, IndexedPreservationAgent.class, pm).addTo(ret);
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

    PreservationMetadataType type = preservationMetadata.getType();
    String preservationMetadataId = preservationMetadata.getId();
    if (PreservationMetadataType.EVENT.equals(type)) {
      deleteDocumentFromIndex(IndexedPreservationEvent.class, preservationMetadataId).addTo(ret);
    } else if (PreservationMetadataType.AGENT.equals(type)) {
      deleteDocumentFromIndex(IndexedPreservationAgent.class, preservationMetadataId).addTo(ret);
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
        SolrUtils.create(index, RodaConstants.INDEX_FILE, solrFile, (ModelObserver) this).addTo(ret);
      } catch (SolrServerException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException | XPathExpressionException | ParserConfigurationException | SAXException
        | IOException e) {
        LOGGER.error("Error adding other properties to indexed file", e);
        ret.add(e);
      }
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> jobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    SolrUtils.create2(index, (ModelObserver) this, Job.class, job).addTo(ret);

    if (ret.isEmpty() && reindexJobReports) {
      indexJobReports(job).addTo(ret);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexJobReports(Job job) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    StorageService storage = model.getStorage();
    try (CloseableIterable<Resource> listResourcesUnderDirectory = storage
      .listResourcesUnderDirectory(ModelUtils.getJobReportsStoragePath(job.getId()), true)) {

      if (listResourcesUnderDirectory != null) {
        for (Resource resource : listResourcesUnderDirectory) {
          if (!resource.isDirectory()) {
            try (
              InputStream inputStream = storage.getBinary(resource.getStoragePath()).getContent().createInputStream()) {
              Report objectFromJson = JsonUtils.getObjectFromJson(inputStream, Report.class);
              jobReportCreatedOrUpdated(objectFromJson, job).addTo(ret);
            } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
              | IOException e) {
              LOGGER.error("Error getting report json from binary", e);
              ret.add(e);
            }
          }
        }
      }
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("Error reindexing job reports", e);
      ret.add(e);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> jobDeleted(String jobId) {
    return deleteDocumentFromIndex(Job.class, jobId);
  }

  private <T extends IsIndexed, M extends IsModelObject> ReturnWithExceptions<Void, ModelObserver> addDocumentToIndex(
    Class<T> classToAdd, M instance) {
    return addDocumentToIndex(classToAdd, instance, false);
  }

  private <T extends IsIndexed, M extends IsModelObject> ReturnWithExceptions<Void, ModelObserver> addDocumentToIndex(
    Class<T> classToAdd, M instance, boolean commit) {
    return SolrUtils.create(index, classToAdd, instance, this, commit);
  }

  private <T extends IsIndexed> ReturnWithExceptions<Void, ModelObserver> deleteDocumentFromIndex(
    Class<T> classToDelete, String... ids) {
    return deleteDocumentFromIndex(classToDelete, false, ids);
  }

  private <T extends IsIndexed> ReturnWithExceptions<Void, ModelObserver> deleteDocumentFromIndex(
    Class<T> classToDelete, boolean commit, String... ids) {
    return SolrUtils.delete(index, classToDelete, Arrays.asList(ids), this, commit);
  }

  private <T extends IsIndexed> ReturnWithExceptions<Void, ModelObserver> deleteDocumentsFromIndex(
    Class<T> classToDelete, String fieldName, String fieldValue) {
    return SolrUtils.delete(index, classToDelete, new Filter(new SimpleFilterParameter(fieldName, fieldValue)), this);
  }

  private <T extends IsIndexed> ReturnWithExceptions<Void, ModelObserver> deleteDocumentsFromIndex(
    Class<T> classToDelete, String fieldName, String fieldValue, boolean commit) {
    return SolrUtils.delete(index, classToDelete, new Filter(new SimpleFilterParameter(fieldName, fieldValue)), this,
      commit);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> jobReportCreatedOrUpdated(Report jobReport, Job job) {
    return SolrUtils.create2(index, this, IndexedReport.class, jobReport, new JobReportCollection.Info(jobReport, job));
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> jobReportDeleted(String jobReportId) {
    return deleteDocumentFromIndex(IndexedReport.class, jobReportId);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> aipPermissionsUpdated(AIP aip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    // change AIP
    SolrUtils.update(index, IndexedAIP.class, aip.getId(),
      SolrUtils.getPermissionsAsPreCalculatedFields(aip.getPermissions()), (ModelObserver) this).addTo(ret);

    if (ret.isEmpty()) {
      // change Representations, Files and Preservation events
      representationsPermissionsUpdated(aip).addTo(ret);
      preservationEventsPermissionsUpdated(aip).addTo(ret);
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipPermissionsUpdated(DIP dip) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    SolrUtils.update(index, IndexedDIP.class, dip.getId(),
      SolrUtils.getPermissionsAsPreCalculatedFields(dip.getPermissions()), (ModelObserver) this).addTo(ret);

    if (ret.isEmpty()) {
      try (CloseableIterable<OptionalWithCause<DIPFile>> allFiles = model.listDIPFilesUnder(dip.getId(), true)) {

        for (OptionalWithCause<DIPFile> dipFile : allFiles) {
          if (dipFile.isPresent()) {
            SolrUtils.update(index, DIPFile.class, IdUtils.getDIPFileId(dipFile.get()),
              SolrUtils.getPermissionsAsPreCalculatedFields(dip.getPermissions()), (ModelObserver) this).addTo(ret);
          } else {
            LOGGER.error("Cannot do a partial update on DIP file", dipFile.getCause());
            ret.add(dipFile.getCause());
          }
        }
      } catch (AuthorizationDeniedException | IOException | NotFoundException | GenericException
        | RequestNotValidException e) {
        LOGGER.error("Cannot do a partial update", e);
        ret.add(e);
      }
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

    SolrUtils.update(index, IndexedRepresentation.class, IdUtils.getRepresentationId(representation),
      SolrUtils.getPermissionsAsPreCalculatedFields(aip.getPermissions()), (ModelObserver) this).addTo(ret);

    if (ret.isEmpty()) {
      try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
        representation.getId(), true)) {

        for (OptionalWithCause<File> file : allFiles) {
          if (file.isPresent()) {
            SolrUtils.update(index, IndexedFile.class, IdUtils.getFileId(file.get()),
              SolrUtils.getPermissionsAsPreCalculatedFields(aip.getPermissions()), (ModelObserver) this).addTo(ret);
          } else {
            LOGGER.error("Cannot do a partial update on file", file.getCause());
            ret.add(file.getCause());
          }
        }

      } catch (AuthorizationDeniedException | IOException | NotFoundException | GenericException
        | RequestNotValidException e) {
        LOGGER.error("Cannot do a partial update", e);
        ret.add(e);
      }
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
            preservationEventPermissionsUpdated(pm, aip.getPermissions(), aip.getState()).addTo(ret);
          }
        } else {
          LOGGER.error("Cannot index premis event", opm.getCause());
          ret.add(opm.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IOException e) {
      LOGGER.error("Cannot index preservation events", e);
      ret.add(e);
    }

    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> preservationEventPermissionsUpdated(PreservationMetadata pm,
    Permissions permissions, AIPState state) {
    Map<String, Object> updateFields = new HashMap<>();
    updateFields.putAll(SolrUtils.getPermissionsAsPreCalculatedFields(permissions));
    updateFields.put(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(state));
    updateFields.put(RodaConstants.PRESERVATION_EVENT_AIP_ID, pm.getAipId());
    updateFields.put(RodaConstants.PRESERVATION_EVENT_ID, pm.getId());
    return SolrUtils.update(index, IndexedPreservationEvent.class, IdUtils.getPreservationId(pm), updateFields, this);

  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> riskCreatedOrUpdated(Risk risk, int incidences, boolean commit) {
    return SolrUtils.create2(index, (ModelObserver) this, IndexedRisk.class, risk,
      new RiskCollection.Info(risk, incidences));
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> riskDeleted(String riskId, boolean commit) {
    return SolrUtils.delete(index, IndexedRisk.class, Arrays.asList(riskId), this, commit);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> riskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence,
    boolean commit) {
    return SolrUtils.create(index, RiskIncidence.class, riskIncidence, this, commit);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> riskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    return SolrUtils.delete(index, RiskIncidence.class, Arrays.asList(riskIncidenceId), this, commit);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> representationInformationCreatedOrUpdated(
    RepresentationInformation ri, boolean commit) {
    return SolrUtils.create(index, RepresentationInformation.class, ri, this, commit);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> representationInformationDeleted(String representationInformationId,
    boolean commit) {
    return SolrUtils.delete(index, RepresentationInformation.class, Arrays.asList(representationInformationId), this,
      commit);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> formatCreatedOrUpdated(Format format, boolean commit) {
    return SolrUtils.create(index, Format.class, format, this, commit);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> formatDeleted(String formatId, boolean commit) {
    return SolrUtils.delete(index, Format.class, Arrays.asList(formatId), this, commit);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> transferredResourceDeleted(String transferredResourceID) {
    return deleteDocumentFromIndex(TransferredResource.class, transferredResourceID);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> notificationCreatedOrUpdated(Notification notification) {
    return addDocumentToIndex(Notification.class, notification);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> notificationDeleted(String notificationId) {
    return deleteDocumentFromIndex(Notification.class, notificationId);
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipCreated(DIP dip, boolean commit) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    SolrUtils.create2(index, (ModelObserver) this, IndexedDIP.class, dip).addTo(ret);

    if (ret.isEmpty()) {
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
          } catch (GenericException e) {
            LOGGER.warn("Commit did not run as expected");
            ret.add(e);
          }
        }
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
        | IOException e) {
        LOGGER.error("Could not index DIP files", e);
        ret.add(e);
      }
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
    ReturnWithExceptions<Void, ModelObserver> ret = deleteDocumentFromIndex(IndexedDIP.class, commit, dipId);
    if (ret.isEmpty()) {
      deleteDocumentsFromIndex(DIPFile.class, RodaConstants.DIPFILE_DIP_ID, dipId, commit).addTo(ret);
    }
    return ret;
  }

  private ReturnWithExceptions<Void, ModelObserver> indexDIPFile(DIP dip, DIPFile file, boolean recursive) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);

    SolrUtils.create2(index, (ModelObserver) this, DIPFile.class, file, new DIPFileCollection.Info(dip)).addTo(ret);

    if (recursive && file.isDirectory() && ret.isEmpty()) {
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
        | IOException e) {
        LOGGER.error("Cannot index DIP file sub-resources: {}", file, e);
        ret.add(e);
      }
    }

    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, ModelObserver> dipFileCreated(DIPFile file) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>(this);
    try {
      DIP dip = model.retrieveDIP(file.getDipId());
      indexDIPFile(dip, file, true).addTo(ret);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
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
    return deleteDocumentFromIndex(DIPFile.class, IdUtils.getDIPFileId(dipId, path, fileId));
  }

}
