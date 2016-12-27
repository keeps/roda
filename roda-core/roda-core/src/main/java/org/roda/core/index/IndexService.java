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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.DateUtil;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ReturnWithExceptions;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

  private final SolrClient solrClient;
  private final ModelService model;
  private final IndexModelObserver observer;

  public IndexService(SolrClient index, ModelService model) {
    super();
    this.solrClient = index;
    this.model = model;

    observer = new IndexModelObserver(this.getSolrClient(), this.model);
    model.addModelObserver(observer);
  }

  public IndexedAIP getParent(IndexedAIP aip) throws NotFoundException, GenericException {
    return SolrUtils.retrieve(getSolrClient(), IndexedAIP.class, aip.getParentID());
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
    return SolrUtils.count(getSolrClient(), returnClass, filter);
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter, Sublist sublist)
    throws GenericException, RequestNotValidException {
    return SolrUtils.find(getSolrClient(), returnClass, filter, sorter, sublist, Facets.NONE);
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws GenericException, RequestNotValidException {
    return SolrUtils.find(getSolrClient(), returnClass, filter, sorter, sublist, facets);
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, User user, boolean justActive) throws GenericException, RequestNotValidException {
    return SolrUtils.find(getSolrClient(), returnClass, filter, sorter, sublist, facets, user, justActive);
  }

  public <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> returnClass, final Filter filter) {
    return findAll(returnClass, filter, new Sorter(new SortParameter(RodaConstants.INDEX_UUID, true)), true);
  }

  public <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> returnClass, final Filter filter,
    final boolean removeDuplicates) {
    return findAll(returnClass, filter, new Sorter(new SortParameter(RodaConstants.INDEX_UUID, true)),
      removeDuplicates);
  }

  public <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> returnClass, final Filter filter,
    final boolean justActive, final boolean removeDuplicates) {
    return findAll(returnClass, filter, new Sorter(new SortParameter(RodaConstants.INDEX_UUID, true)), Sublist.ALL,
      null, justActive, removeDuplicates);
  }

  public <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> returnClass, final Filter filter,
    final Sorter sorter) {
    return findAll(returnClass, filter, sorter, true);
  }

  public <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final boolean removeDuplicates) {
    return findAll(returnClass, filter, sorter, Sublist.ALL, null, true, removeDuplicates);
  }

  public <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final Sublist sublist, final User user, final boolean justActive,
    final boolean removeDuplicates) {
    return new IterableIndexResult<>(getSolrClient(), returnClass, filter, sorter, sublist, Facets.NONE, user,
      justActive, removeDuplicates);
  }

  public <T extends IsIndexed> Long count(Class<T> returnClass, Filter filter, User user, boolean justActive)
    throws GenericException, RequestNotValidException {
    return SolrUtils.count(getSolrClient(), returnClass, filter, user, justActive);
  }

  public <T extends IsIndexed> T retrieve(Class<T> returnClass, String id) throws NotFoundException, GenericException {
    return SolrUtils.retrieve(getSolrClient(), returnClass, id);
  }

  public <T extends IsIndexed> List<T> retrieve(Class<T> returnClass, List<String> ids)
    throws NotFoundException, GenericException {
    return SolrUtils.retrieve(getSolrClient(), returnClass, ids);
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

      try {
        reindexPreservationAgents();
      } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error reindexing preservation agents");
      }

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
      getSolrClient().optimize(RodaConstants.INDEX_AIP);
      getSolrClient().optimize(RodaConstants.INDEX_FILE);
      getSolrClient().optimize(RodaConstants.INDEX_REPRESENTATION);
      getSolrClient().optimize(RodaConstants.INDEX_PRESERVATION_EVENTS);
      getSolrClient().optimize(RodaConstants.INDEX_PRESERVATION_AGENTS);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Error while optimizing indexes", e);
    }
  }

  public ReturnWithExceptions<Void> reindexAIP(AIP aip) {
    return observer.aipCreated(aip);
  }

  public ReturnWithExceptions<Void> reindexRepresentation(Representation rep) {
    return observer.representationCreated(rep);
  }

  public ReturnWithExceptions<Void> reindexFile(File file) {
    return observer.fileCreated(file);
  }

  public ReturnWithExceptions<Void> reindexDIP(DIP dip) {
    return observer.dipCreated(dip, false);
  }

  public ReturnWithExceptions<Void> reindexDIPFile(DIPFile file) {
    return observer.dipFileCreated(file);
  }

  public void reindexPreservationAgents()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<PreservationMetadata>> iterable = model.listPreservationAgents();
    reindexPreservationAgents(iterable);
  }

  public void reindexPreservationAgents(CloseableIterable<OptionalWithCause<PreservationMetadata>> iterable)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    for (OptionalWithCause<PreservationMetadata> opm : iterable) {
      observer.preservationMetadataCreated(opm.get());
    }
    IOUtils.closeQuietly(iterable);
  }

  public ReturnWithExceptions<Void> reindexJob(Job job) {
    return observer.jobCreatedOrUpdated(job, true);
  }

  public ReturnWithExceptions<Void> reindexJobReport(Report jobReport) {
    return observer.jobReportCreatedOrUpdated(jobReport);
  }

  public ReturnWithExceptions<Void> reindexRisk(Risk risk) {
    return observer.riskCreatedOrUpdated(risk, false);
  }

  public void reindexRisks(StorageService storage) {
    try {
      reindexAll(storage, Risk.class);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException | IsStillUpdatingException e) {
      LOGGER.error("Error reindexing risks");
    }
  }

  public ReturnWithExceptions<Void> reindexRiskIncidence(RiskIncidence riskIncidence) {
    return observer.riskIncidenceCreatedOrUpdated(riskIncidence, false);
  }

  public ReturnWithExceptions<Void> reindexFormat(Format format) {
    return observer.formatCreatedOrUpdated(format, false);
  }

  public void reindexFormats(StorageService storage) {
    try {
      reindexAll(storage, Format.class);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException | IsStillUpdatingException e) {
      LOGGER.error("Error reindexing formats");
    }
  }

  public ReturnWithExceptions<Void> reindexNotification(Notification notification) {
    return observer.notificationCreatedOrUpdated(notification);
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

  public SimpleJobPluginInfo reindexActionLog(BufferedReader br, SimpleJobPluginInfo jobPluginInfo)
    throws GenericException {
    String line;
    try {
      while ((line = br.readLine()) != null) {
        jobPluginInfo.incrementObjectsCount();

        try {
          LogEntry entry = JsonUtils.getObjectFromJson(line, LogEntry.class);
          if (entry != null) {
            reindexActionLog(entry);
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          }

        } catch (GenericException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
        }
      }
      br.close();
    } catch (IOException e) {
      throw new GenericException("Error reading log", e);
    } finally {
      return jobPluginInfo;
    }
  }

  private ReturnWithExceptions<Void> reindexActionLog(LogEntry entry) {
    return observer.logEntryCreated(entry);
  }

  public void deleteAllActionLog() throws GenericException {
    clearIndex(RodaConstants.INDEX_ACTION_LOG);
  }

  public void deleteActionLog(Date until) throws SolrServerException, IOException {
    String dateString = DateUtil.getThreadLocalDateFormat().format(until);
    String query = RodaConstants.LOG_DATETIME + ":[* TO " + dateString + "]";
    getSolrClient().deleteByQuery(RodaConstants.INDEX_ACTION_LOG, query);
    getSolrClient().commit(RodaConstants.INDEX_ACTION_LOG);
  }

  public <T extends IsRODAObject> void reindexAll(StorageService storage, Class<T> objectClass)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException, IOException,
    IsStillUpdatingException {
    if (AIP.class.equals(objectClass)) {
      reindexAIPs();
    } else if (TransferredResource.class.equals(objectClass)) {
      RodaCoreFactory.getTransferredResourcesScanner().updateTransferredResources(Optional.empty(), true);
    } else {
      StoragePath containerPath = ModelUtils.getContainerPath(objectClass);
      CloseableIterable<Resource> listResourcesUnderDirectory = null;
      try {
        listResourcesUnderDirectory = storage.listResourcesUnderContainer(containerPath, false);
        for (Resource resource : listResourcesUnderDirectory) {
          if (!resource.isDirectory()) {
            Binary binary = (Binary) resource;
            InputStream inputStream = binary.getContent().createInputStream();
            String jsonString = IOUtils.toString(inputStream, RodaConstants.DEFAULT_ENCODING);
            T object = JsonUtils.getObjectFromJson(jsonString, objectClass);
            IOUtils.closeQuietly(inputStream);
            reindex(object);
          }
        }
      } catch (NoSuchFileException | NotFoundException e) {
        // do nothing
      } finally {
        IOUtils.closeQuietly(listResourcesUnderDirectory);
      }
    }
  }

  public <T extends Serializable> ReturnWithExceptions<Void> reindex(T object) {
    Class<T> objectClass = (Class<T>) object.getClass();

    if (AIP.class.equals(objectClass) || IndexedAIP.class.equals(objectClass)) {
      return reindexAIP(AIP.class.cast(object));
    } else if (Format.class.equals(objectClass)) {
      return reindexFormat(Format.class.cast(object));
    } else if (Notification.class.equals(objectClass)) {
      return reindexNotification(Notification.class.cast(object));
    } else if (Risk.class.equals(objectClass) || IndexedRisk.class.equals(objectClass)) {
      return reindexRisk(Risk.class.cast(object));
    } else if (RiskIncidence.class.equals(objectClass)) {
      return reindexRiskIncidence(RiskIncidence.class.cast(object));
    } else if (LogEntry.class.equals(objectClass)) {
      return reindexActionLog(LogEntry.class.cast(object));
    } else if (Job.class.equals(objectClass)) {
      return reindexJob(Job.class.cast(object));
    } else if (Representation.class.equals(objectClass) || IndexedRepresentation.class.equals(objectClass)) {
      return reindexRepresentation(Representation.class.cast(object));
    } else if (File.class.equals(objectClass) || IndexedFile.class.equals(objectClass)) {
      return reindexFile(File.class.cast(object));
    } else if (DIP.class.equals(objectClass) || IndexedDIP.class.equals(objectClass)) {
      return reindexDIP(DIP.class.cast(object));
    } else if (DIPFile.class.equals(objectClass)) {
      return reindexDIPFile(DIPFile.class.cast(object));
    } else {
      LOGGER.error("Error trying to reindex an unconfigured object class: {}", objectClass.getName());
      ReturnWithExceptions<Void> exceptions = new ReturnWithExceptions<Void>();
      exceptions.addException(
        new RODAException("Error trying to reindex an unconfigured object class: " + objectClass.getName()));
      return exceptions;
    }
  }

  public void clearIndex(String indexName) throws GenericException {
    try {
      getSolrClient().deleteByQuery(indexName, "*:*");
      getSolrClient().commit(indexName);
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
      getSolrClient().optimize(indexName);
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
    SolrUtils.commit(getSolrClient(), classToCommit);
  }

  public <T extends IsIndexed> List<String> suggest(Class<T> returnClass, String field, String query, User user,
    boolean allowPartial, boolean justActive) throws GenericException {
    return SolrUtils.suggest(getSolrClient(), returnClass, field, query, justActive, user, allowPartial);
  }

  public <T extends IsIndexed> void execute(Class<T> classToRetrieve, Filter filter, IndexRunnable<T> indexRunnable)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    SolrUtils.execute(getSolrClient(), classToRetrieve, filter, indexRunnable);
  }

  public <T extends IsIndexed> void delete(Class<T> classToRetrieve, List<String> ids)
    throws GenericException, RequestNotValidException {
    SolrUtils.delete(getSolrClient(), classToRetrieve, ids);
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
    SolrUtils.delete(getSolrClient(), classToRetrieve, filter);
  }

  public <T extends IsIndexed> void create(Class<T> classToCreate, T instance)
    throws GenericException, RequestNotValidException {
    SolrUtils.create(getSolrClient(), classToCreate, instance);
  }

  public SolrClient getSolrClient() {
    return solrClient;
  }

  public <T extends IsIndexed> CloseableIterable<OptionalWithCause<T>> list(Class<T> listClass)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    int counter = RodaCoreFactory.getIndexService().count(listClass, Filter.ALL).intValue();
    IndexResult<T> resources = RodaCoreFactory.getIndexService().find(listClass, Filter.ALL, Sorter.NONE,
      new Sublist(0, counter));
    Iterator<T> it = resources.getResults().iterator();

    CloseableIterable<OptionalWithCause<T>> resourceIterable = new CloseableIterable<OptionalWithCause<T>>() {
      @Override
      public Iterator<OptionalWithCause<T>> iterator() {
        return new Iterator<OptionalWithCause<T>>() {

          @Override
          public boolean hasNext() {
            return it.hasNext();
          }

          @Override
          public OptionalWithCause<T> next() {
            return OptionalWithCause.of(it.next());
          }
        };
      }

      @Override
      public void close() throws IOException {
      }
    };

    return resourceIterable;
  }
}
