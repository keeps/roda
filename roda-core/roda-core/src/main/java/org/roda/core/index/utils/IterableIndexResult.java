/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.solr.client.solrj.SolrClient;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DB.TreeMapSink;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does search in the index, using the Solr.find() method, and if configured
 * removes duplicate objects (via uuid comparison) thus providing iterator
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */

public class IterableIndexResult<T extends IsIndexed> implements CloseableIterable<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(IterableIndexResult.class);
  private static final Sorter SINK_SORTER = new Sorter(new SortParameter(RodaConstants.INDEX_UUID, false));
  private static final int DEFAULT_PAGE_SIZE = 1000;
  private static int PAGE_SIZE = DEFAULT_PAGE_SIZE;

  private SolrClient solrClient;
  private Class<T> returnClass;
  private Filter filter;
  private User user;
  private boolean justActive;
  private List<String> fieldsToReturn;
  private DB db;

  private BTreeMap<String, T> indexObjects;

  private long totalObjects = -1;

  @Deprecated
  public IterableIndexResult(final SolrClient solrClient, final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final Facets facets, final User user, final boolean justActive, final boolean removeDuplicates,
    final List<String> fieldsToReturn) {
    this(solrClient, returnClass, filter, sorter, user, justActive, fieldsToReturn);
  }

  public IterableIndexResult(final SolrClient solrClient, final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final User user, final boolean justActive, final List<String> fieldsToReturn) {
    this.solrClient = solrClient;
    this.returnClass = returnClass;
    this.filter = filter;
    this.user = user;
    this.justActive = justActive;
    this.fieldsToReturn = fieldsToReturn;

    if (sorter == null || Sorter.NONE.equals(sorter) || SINK_SORTER.equals(sorter)) {
      getResults();
    } else {
      getResults(sorter);
    }
  }

  private void getResults() {
    db = DBMaker.tempFileDB().fileMmapEnableIfSupported().cleanerHackEnable().make();
    TreeMapSink<String, T> sink = db.treeMap("myMap", Serializer.STRING, Serializer.JAVA).createFromSink();
    this.totalObjects = getResultsImpl(SINK_SORTER, t -> sink.put(t.getUUID(), t));
    this.indexObjects = sink.create();
  }

  private void getResults(Sorter sorter) {
    db = DBMaker.tempFileDB().fileMmapEnableIfSupported().cleanerHackEnable().make();
    this.indexObjects = db.treeMap("myMap", Serializer.STRING, Serializer.JAVA).create();
    this.totalObjects = getResultsImpl(sorter, t -> indexObjects.put(t.getUUID(), t));
  }

  private long getResultsImpl(Sorter sorter, Consumer<T> putFunction) {
    int startIndex = 0;
    long totalCount = -1;
    String lastUuid = "";

    do {
      // TODO use SOLR export after adding docValues to UUID
      try {
        IndexResult<T> result = SolrUtils.find(solrClient, returnClass, filter, sorter,
          new Sublist(startIndex, PAGE_SIZE), Facets.NONE, user, justActive, fieldsToReturn);

        totalCount = result.getTotalCount();
        startIndex += result.getResults().size();

        for (T element : result.getResults()) {
          if (SINK_SORTER.equals(sorter)) {
            if (lastUuid.compareTo(element.getUUID()) < 0) {
              putFunction.accept(element);
              lastUuid = element.getUUID();
            }
          } else {
            putFunction.accept(element);
          }
        }
      } catch (GenericException | RequestNotValidException e) {
        LOGGER.error("Error find new Solr page when creating iterable index result", e);
      }
    } while (startIndex < totalCount);

    return totalCount;
  }

  @Override
  public Iterator<T> iterator() {
    return indexObjects.valueIterator();
  }

  public long getTotalObjects() {
    return totalObjects;
  }

  @Override
  public void close() throws IOException {
    db.close();
  }

  public static void setPageSize(int pageSize) {
    PAGE_SIZE = pageSize;
  }

}
