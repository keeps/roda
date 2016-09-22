/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.DateUtil;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

  private final SolrClient index;
  private final ModelService model;
  private final IndexModelObserver observer;

  public IndexService(SolrClient index, ModelService model) {
    super();
    this.index = index;
    this.model = model;

    observer = new IndexModelObserver(this.index, this.model);
    model.addModelObserver(observer);
  }

  public IndexedAIP getParent(IndexedAIP aip) throws NotFoundException, GenericException {
    return SolrUtils.retrieve(index, IndexedAIP.class, aip.getParentID());
  }

  public List<IndexedAIP> retrieveAncestors(IndexedAIP aip) throws GenericException {
    List<IndexedAIP> ancestors = new ArrayList<IndexedAIP>();
    IndexedAIP parent = null, actual = aip;

    while (actual != null && actual.getParentID() != null) {
      try {
        parent = getParent(actual);
      } catch (NotFoundException e) {
        parent = null;
        LOGGER.warn("Ancestor not found: {}", actual.getParentID());
      }

      if (parent != null && ancestors.contains(parent)) {
        LOGGER.warn("Found a cyclic ancestor relationship: {} and {}", aip.getId(), parent.getId());
        break;
      }

      ancestors.add(parent);
      actual = parent;
    }

    return ancestors;
  }

  public <T extends IsIndexed> Long count(Class<T> returnClass, Filter filter)
    throws GenericException, RequestNotValidException {
    return SolrUtils.count(index, returnClass, filter);
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter, Sublist sublist)
    throws GenericException, RequestNotValidException {
    return SolrUtils.find(index, returnClass, filter, sorter, sublist, Facets.NONE);

  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws GenericException, RequestNotValidException {
    return SolrUtils.find(index, returnClass, filter, sorter, sublist, facets);
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, User user, boolean justActive) throws GenericException, RequestNotValidException {
    return SolrUtils.find(index, returnClass, filter, sorter, sublist, facets, user, justActive);
  }

  public <T extends IsIndexed> String findCSV(final Class<T> returnClass, final Filter filter, final Sorter sorter,
    final Sublist sublist, final Facets facets, final User user, final boolean justActive)
    throws GenericException, RequestNotValidException {
    return SolrUtils.findCSV(index, returnClass, filter, sorter, sublist, facets, user, justActive);
  }

  public <T extends IsIndexed> Long count(Class<T> returnClass, Filter filter, User user, boolean justActive)
    throws GenericException, RequestNotValidException {
    return SolrUtils.count(index, returnClass, filter, user, justActive);
  }

  public <T extends IsIndexed> T retrieve(Class<T> returnClass, String id) throws NotFoundException, GenericException {
    return SolrUtils.retrieve(index, returnClass, id);
  }

  public <T extends IsIndexed> List<T> retrieve(Class<T> returnClass, List<String> ids)
    throws NotFoundException, GenericException {
    return SolrUtils.retrieve(index, returnClass, ids);
  }

  public void reindexAIPs()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<AIP>> aips = null;
    try {
      clearAIPs();
      LOGGER.info("{} > Listing AIPs", new Date().getTime());
      aips = model.listAIPs();
      for (OptionalWithCause<AIP> aip : aips) {
        if (aip.isPresent()) {
          LOGGER.info("{} > Reindexing AIP {}", new Date().getTime(), aip.get().getId());
          reindexAIP(aip.get());
        } else {
          LOGGER.error("{} > An error occurred. See log for more details.", new Date().getTime());
        }
      }
      LOGGER.info("{} > Optimizing indexes", new Date().getTime());

      reindexPreservationAgents();
      commitAIPs();
      optimizeAIPs();
      LOGGER.info("{} > Done", new Date().getTime());
    } finally {
      IOUtils.closeQuietly(aips);
    }
  }

  public void commitAIPs() throws GenericException {
    commit(IndexedAIP.class, IndexedRepresentation.class, IndexedFile.class, IndexedPreservationEvent.class,
      IndexedPreservationAgent.class);
  }

  public void clearAIPs() throws GenericException {
    clearIndex(RodaConstants.INDEX_AIP);
    clearIndex(RodaConstants.INDEX_FILE);
    clearIndex(RodaConstants.INDEX_REPRESENTATION);
    clearIndex(RodaConstants.INDEX_PRESERVATION_EVENTS);
    clearIndex(RodaConstants.INDEX_PRESERVATION_AGENTS);
  }

  public void optimizeAIPs() throws GenericException {
    try {
      index.optimize(RodaConstants.INDEX_AIP);
      index.optimize(RodaConstants.INDEX_FILE);
      index.optimize(RodaConstants.INDEX_REPRESENTATION);
      index.optimize(RodaConstants.INDEX_PRESERVATION_EVENTS);
      index.optimize(RodaConstants.INDEX_PRESERVATION_AGENTS);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Error while optimizing indexes", e);
    }
  }

  public void reindexAIP(AIP aip) {
    observer.aipCreated(aip);
  }

  public void reindexPreservationAgents() {
    try {
      CloseableIterable<OptionalWithCause<PreservationMetadata>> iterable = model.listPreservationAgents();
      for (OptionalWithCause<PreservationMetadata> opm : iterable) {
        observer.preservationMetadataCreated(opm.get());
      }
      IOUtils.closeQuietly(iterable);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | NoSuchElementException e) {
      LOGGER.error("Could not reindex preservation agents");
    }
  }

  public void reindexJob(Job job) {
    observer.jobCreatedOrUpdated(job, true);
  }

  public void reindexJobReport(Report jobReport) {
    observer.jobReportCreatedOrUpdated(jobReport);
  }

  public void reindexRisk(Risk risk) {
    observer.riskCreatedOrUpdated(risk, false);
  }

  public void reindexRisks(StorageService storage) {
    try {
      reindex(storage, Risk.class);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException | IsStillUpdatingException e) {
      LOGGER.error("Error reindexing risks");
    }
  }

  public void reindexRiskIncidence(RiskIncidence riskIncidence) {
    observer.riskIncidenceCreatedOrUpdated(riskIncidence, false);
  }

  public void reindexAgent(Agent agent) {
    observer.agentCreatedOrUpdated(agent, false);
  }

  public void reindexFormat(Format format) {
    observer.formatCreatedOrUpdated(format, false);
  }

  public void reindexFormats(StorageService storage) {
    try {
      reindex(storage, Format.class);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException | IsStillUpdatingException e) {
      LOGGER.error("Error reindexing formats");
    }
  }

  public <T extends IsRODAObject> void reindex(StorageService storage, Class<T> objectClass) throws NotFoundException,
    GenericException, AuthorizationDeniedException, RequestNotValidException, IOException, IsStillUpdatingException {
    CloseableIterable<Resource> listResourcesUnderDirectory = null;
    try {
      if (AIP.class.equals(objectClass)) {
        reindexAIPs();
      } else if (TransferredResource.class.equals(objectClass)) {
        RodaCoreFactory.getTransferredResourcesScanner().updateAllTransferredResources(null, true);
      } else {
        StoragePath containerPath = ModelUtils.getContainerPath(objectClass);
        try {
          listResourcesUnderDirectory = storage.listResourcesUnderContainer(containerPath, false);
          for (Resource resource : listResourcesUnderDirectory) {
            if (!resource.isDirectory()) {
              Binary binary = (Binary) resource;
              InputStream inputStream = binary.getContent().createInputStream();
              String jsonString = IOUtils.toString(inputStream, RodaConstants.DEFAULT_ENCODING);
              T object = JsonUtils.getObjectFromJson(jsonString, objectClass);
              IOUtils.closeQuietly(inputStream);
              reindex(objectClass, object);
            }
          }
        } catch (NoSuchFileException | NotFoundException e) {
          // do nothing
        }
      }
    } finally {
      IOUtils.closeQuietly(listResourcesUnderDirectory);
    }
  }

  public <T extends Serializable> void reindex(StorageService storage, T object) throws NotFoundException,
    GenericException, AuthorizationDeniedException, RequestNotValidException, IOException, IsStillUpdatingException {
    CloseableIterable<Resource> listResourcesUnderDirectory = null;
    try {
      if (AIP.class.equals(object.getClass())) {
        reindexAIP((AIP) object);
      } else if (TransferredResource.class.equals(object.getClass())) {
        TransferredResource resource = (TransferredResource) object;
        String folderUUID = UUID.nameUUIDFromBytes(resource.getParentPath().getBytes()).toString();
        RodaCoreFactory.getTransferredResourcesScanner().updateAllTransferredResources(folderUUID, true);
      } else {
        reindex((Class<T>) object.getClass(), object);
      }
    } finally {
      IOUtils.closeQuietly(listResourcesUnderDirectory);
    }
  }

  public void reindexNotification(Notification notification) {
    observer.notificationCreatedOrUpdated(notification);
  }

  public <T extends Serializable> void reindex(Class<T> objectClass, T object) {
    if (AIP.class.equals(objectClass) || IndexedAIP.class.equals(objectClass)) {
      reindexAIP(AIP.class.cast(object));
    } else if (Agent.class.equals(objectClass)) {
      reindexAgent(Agent.class.cast(object));
    } else if (Format.class.equals(objectClass)) {
      reindexFormat(Format.class.cast(object));
    } else if (Notification.class.equals(objectClass)) {
      reindexNotification(Notification.class.cast(object));
    } else if (Risk.class.equals(objectClass)) {
      reindexRisk(Risk.class.cast(object));
    } else if (RiskIncidence.class.equals(objectClass)) {
      reindexRiskIncidence(RiskIncidence.class.cast(object));
    } else if (LogEntry.class.equals(objectClass)) {
      reindexActionLog(LogEntry.class.cast(object));
    } else if (Job.class.equals(objectClass)) {
      reindexJob(Job.class.cast(object));
    } else {
      LOGGER.error("Error trying to reindex an unconfigured object class: {}", objectClass.getName());
    }
  }

  public void reindexActionLogs()
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    CloseableIterable<Resource> actionLogs = null;

    try {
      boolean recursive = false;
      actionLogs = model.getStorage()
        .listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG), recursive);

      for (Resource resource : actionLogs) {
        if (resource instanceof Binary) {
          Binary b = (Binary) resource;
          BufferedReader br = new BufferedReader(new InputStreamReader(b.getContent().createInputStream()));
          reindexActionLog(br);
        }
      }
    } catch (IOException e) {
      throw new GenericException("Error retrieving/processing logs from storage", e);
    } finally {
      IOUtils.closeQuietly(actionLogs);
    }
  }

  public void reindexActionLog(BufferedReader br) throws GenericException {
    String line;
    try {
      while ((line = br.readLine()) != null) {
        LogEntry entry = JsonUtils.getObjectFromJson(line, LogEntry.class);
        if (entry != null) {
          reindexActionLog(entry);
        }
      }
      br.close();
    } catch (IOException e) {
      throw new GenericException("Error reading log", e);
    }
  }

  private void reindexActionLog(LogEntry entry) {
    observer.logEntryCreated(entry);
  }

  public void deleteAllActionLog() throws GenericException {
    clearIndex(RodaConstants.INDEX_ACTION_LOG);
  }

  public void deleteActionLog(Date until) throws SolrServerException, IOException {
    String dateString = DateUtil.getThreadLocalDateFormat().format(until);
    String query = RodaConstants.LOG_DATETIME + ":[* TO " + dateString + "]";
    index.deleteByQuery(RodaConstants.INDEX_ACTION_LOG, query);
    index.commit(RodaConstants.INDEX_ACTION_LOG);

  }

  public void clearIndex(String indexName) throws GenericException {
    try {
      index.deleteByQuery(indexName, "*:*");
      index.commit(indexName);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Error cleaning up index {}", indexName, e);
      throw new GenericException("Error cleaning up index " + indexName, e);
    }
  }

  public void clearIndexes(List<String> indexNames) throws GenericException {
    for (String indexName : indexNames) {
      clearIndex(indexName);
    }
  }

  public void optimizeIndex(String indexName) throws GenericException {
    try {
      index.optimize(indexName);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Error while optimizing indexes", e);
    }
  }

  public void optimizeIndexes(List<String> indexNames) throws GenericException {
    for (String indexName : indexNames) {
      optimizeIndex(indexName);
    }
  }

  @SafeVarargs
  public final void commit(Class<? extends IsIndexed>... classToCommit) throws GenericException {
    SolrUtils.commit(index, classToCommit);
  }

  public <T extends IsIndexed> List<String> suggest(Class<T> returnClass, String field, String query)
    throws GenericException {
    return SolrUtils.suggest(index, returnClass, field, query);
  }

  public <T extends IsIndexed> void execute(Class<T> classToRetrieve, Filter filter, IndexRunnable<T> indexRunnable)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    SolrUtils.execute(index, classToRetrieve, filter, indexRunnable);
  }

  public <T extends IsIndexed> void delete(Class<T> classToRetrieve, List<String> ids)
    throws GenericException, RequestNotValidException {
    SolrUtils.delete(index, classToRetrieve, ids);
  }

  public <T extends IsIndexed> void deleteSilently(Class<T> classToRetrieve, List<String> ids) {
    try {
      delete(classToRetrieve, ids);
    } catch (GenericException | RequestNotValidException e) {
      // do nothing as we should be quiet
    }
  }

  public <T extends IsIndexed> void delete(Class<T> classToRetrieve, Filter filter)
    throws GenericException, RequestNotValidException {
    SolrUtils.delete(index, classToRetrieve, filter);
  }

  public <T extends IsIndexed> void create(Class<T> classToCreate, T instance)
    throws GenericException, RequestNotValidException {
    SolrUtils.create(index, classToCreate, instance);
  }

}
