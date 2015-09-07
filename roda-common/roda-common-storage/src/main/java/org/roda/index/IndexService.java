package org.roda.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.index.utils.SolrUtils;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Resource;
import org.roda.storage.StorageServiceException;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

public class IndexService {

  private final Logger logger = Logger.getLogger(getClass());

  private final SolrClient index;
  private final ModelService model;
  private final IndexModelObserver observer;

  public IndexService(SolrClient index, ModelService model) {
    super();
    this.index = index;
    this.model = model;

    observer = new IndexModelObserver(index, model);
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

  // FIXME perhaps transform sysout into logger logging
  public void reindexAIPs() throws IndexServiceException {
    ClosableIterable<AIP> aips = null;
    try {
      System.out.println(new Date().getTime() + " > Listing AIPs");
      aips = model.listAIPs();
      for (AIP aip : aips) {
        if (aip != null) {
          System.out.println(new Date().getTime() + " > Reindexing AIP " + aip.getId());
          reindexAIP(aip);
        } else {
          System.err.println(new Date().getTime() + " > An error occurred. See log for more details.");
        }
      }
      System.out.println(new Date().getTime() + " > Optimizing indexes");
      optimizeAIPs();
      System.out.println(new Date().getTime() + " > Done");
    } catch (ModelServiceException e) {
      throw new IndexServiceException("Error while reindexing AIPs", IndexServiceException.INTERNAL_SERVER_ERROR, e);
    } finally {
      try {
        if (aips != null) {
          aips.close();
        }
      } catch (IOException e) {
        logger.error("Error while while freeing up resources", e);
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

        String line;
        while ((line = br.readLine()) != null) {
          LogEntry entry = ModelUtils.getLogEntry(line);
          if (entry != null) {
            reindexActionLog(entry);
          }
        }
      }
    } catch (StorageServiceException | IOException e) {
      throw new IndexServiceException("Error retrieving/processing logs from storage",
        IndexServiceException.INTERNAL_SERVER_ERROR, e);
    } finally {
      if (actionLogs != null) {
        try {
          actionLogs.close();
        } catch (IOException e) {
          logger.error("Error while while freeing up resources", e);
        }
      }
    }
  }

  private void reindexActionLog(LogEntry entry) {
    observer.logEntryCreated(entry);
  }

  public void deleteAllActionLog() throws IndexServiceException {
    clearIndex(RodaConstants.INDEX_ACTION_LOG);
  }

  public void clearIndex(String indexName) throws IndexServiceException {
    try {
      index.deleteByQuery(indexName, "*:*");
      index.commit(indexName);
    } catch (SolrServerException | IOException e) {
      logger.error("Error cleaning up index " + indexName, e);
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
