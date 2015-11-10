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
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageServiceException;

public class IndexService {

  private static final Logger LOGGER = Logger.getLogger(IndexService.class);

  private final SolrClient index;
  private final ModelService model;
  private final IndexModelObserver observer;
  private final Path configBasePath;

  public IndexService(SolrClient index, ModelService model, Path configBasePath) {
    super();
    this.index = index;
    this.model = model;
    this.configBasePath = configBasePath;

    observer = new IndexModelObserver(this.index, this.model, this.configBasePath);
    model.addModelObserver(observer);
  }

  public SimpleDescriptionObject getParent(SimpleDescriptionObject sdo) throws IndexServiceException {
    return SolrUtils.retrieve(index, SimpleDescriptionObject.class, sdo.getParentID());
  }

  public List<SimpleDescriptionObject> getAncestors(SimpleDescriptionObject sdo) throws IndexServiceException {
    List<SimpleDescriptionObject> ancestors = new ArrayList<SimpleDescriptionObject>();
    SimpleDescriptionObject parent = null, actual = sdo;

    while (actual != null && actual.getParentID() != null) {
      parent = getParent(actual);
      if (parent != null) {
        ancestors.add(parent);
        actual = parent;
      }
    }

    return ancestors;
  }

  public <T extends Serializable> Long count(Class<T> returnClass, Filter filter) throws IndexServiceException {
    return SolrUtils.count(index, returnClass, filter);
  }

  public <T extends Serializable> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter,
    Sublist sublist) throws IndexServiceException {
    Facets facets = null;
    return SolrUtils.find(index, returnClass, filter, sorter, sublist, facets);

  }

  public <T extends Serializable> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws IndexServiceException {
    return SolrUtils.find(index, returnClass, filter, sorter, sublist, facets);
  }

  public <T extends Serializable> T retrieve(Class<T> returnClass, String... ids) throws IndexServiceException {
    return SolrUtils.retrieve(index, returnClass, ids);
  }

  public void reindexAIPs() throws IndexServiceException {
    ClosableIterable<AIP> aips = null;
    try {
      LOGGER.info(new Date().getTime() + " > Listing AIPs");
      aips = model.listAIPs();
      for (AIP aip : aips) {
        if (aip != null) {
          LOGGER.info(new Date().getTime() + " > Reindexing AIP " + aip.getId());
          reindexAIP(aip);
        } else {
          LOGGER.error(new Date().getTime() + " > An error occurred. See log for more details.");
        }
      }
      LOGGER.info(new Date().getTime() + " > Optimizing indexes");
      optimizeAIPs();
      LOGGER.info(new Date().getTime() + " > Done");
    } catch (ModelServiceException e) {
      throw new IndexServiceException("Error while reindexing AIPs", IndexServiceException.INTERNAL_SERVER_ERROR, e);
    } finally {
      try {
        if (aips != null) {
          aips.close();
        }
      } catch (IOException e) {
        LOGGER.error("Error while while freeing up resources", e);
      }
    }
  }

  public void optimizeAIPs() throws IndexServiceException {
    try {
      index.optimize(RodaConstants.INDEX_AIP);
      index.optimize(RodaConstants.INDEX_SDO);
    } catch (SolrServerException | IOException e) {
      throw new IndexServiceException("Error while optimizing indexes", IndexServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  public void reindexAIP(AIP aip) {
    observer.aipCreated(aip);
  }

  public void reindexActionLogs() throws IndexServiceException {
    ClosableIterable<Resource> actionLogs = null;

    try {
      actionLogs = model.getStorage()
        .listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG));

      for (Resource resource : actionLogs) {
        Binary b = model.getStorage().getBinary(resource.getStoragePath());
        BufferedReader br = new BufferedReader(new InputStreamReader(b.getContent().createInputStream()));

        reindexActionLog(br);
      }
    } catch (StorageServiceException | IOException e) {
      throw new IndexServiceException("Error retrieving/processing logs from storage",
        IndexServiceException.INTERNAL_SERVER_ERROR, e);
    } finally {
      if (actionLogs != null) {
        try {
          actionLogs.close();
        } catch (IOException e) {
          LOGGER.error("Error while while freeing up resources", e);
        }
      }
    }
  }

  private void reindexActionLog(BufferedReader br) throws IOException {
    String line;
    while ((line = br.readLine()) != null) {
      LogEntry entry = ModelUtils.getLogEntry(line);
      if (entry != null) {
        reindexActionLog(entry);
      }
    }
    br.close();
  }

  private void reindexActionLog(LogEntry entry) {
    observer.logEntryCreated(entry);
  }

  public void deleteAllActionLog() throws IndexServiceException {
    clearIndex(RodaConstants.INDEX_ACTION_LOG);
  }

  public void deleteActionLog(Date until) throws SolrServerException, IOException {
    SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(RodaConstants.SOLRDATEFORMAT);
    String dateString = iso8601DateFormat.format(until);
    String query = RodaConstants.LOG_DATETIME + ":[* TO " + dateString + "]";
    index.deleteByQuery(RodaConstants.INDEX_ACTION_LOG, query);
    index.commit(RodaConstants.INDEX_ACTION_LOG);

  }

  public void clearIndex(String indexName) throws IndexServiceException {
    try {
      index.deleteByQuery(indexName, "*:*");
      index.commit(indexName);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Error cleaning up index " + indexName, e);
      throw new IndexServiceException("Error cleaning up index " + indexName,
        IndexServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  public void optimizeIndex(String indexName) throws IndexServiceException {
    try {
      index.optimize(indexName);
    } catch (SolrServerException | IOException e) {
      throw new IndexServiceException("Error while optimizing indexes", IndexServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

}
