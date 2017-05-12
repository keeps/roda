/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
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

public class IterableIndexResult<T extends IsIndexed> implements Iterable<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(IterableIndexResult.class);
  private static final int PAGE_SIZE = RodaConstants.DEFAULT_PAGINATION_VALUE;

  private SolrClient solrClient;
  private Class<T> returnClass;
  private Filter filter;
  private Sorter sorter;
  private Sublist sublist;
  private Facets facets;
  private User user;
  private boolean justActive;
  private List<String> fieldsToReturn;

  private boolean removeDuplicates = true;
  private Set<String> uniqueUUIDs = new HashSet<>();
  private IndexResult<T> indexResult = null;
  private List<T> indexResultObjects;
  private int currentObject = 0;
  private int currentObjectInPartialList = 0;
  private long totalObjects = -1;

  public IterableIndexResult(final SolrClient solrClient, final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final Facets facets, final boolean removeDuplicates, final List<String> fieldsToReturn) {
    this(solrClient, returnClass, filter, sorter, facets, null, true, removeDuplicates, fieldsToReturn);
  }

  public IterableIndexResult(final SolrClient solrClient, final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final Facets facets, final User user, final boolean justActive, final boolean removeDuplicates,
    final List<String> fieldsToReturn) {
    this.solrClient = solrClient;
    this.returnClass = returnClass;
    this.filter = filter;
    this.sorter = sorter;
    this.facets = facets;
    // TODO implement sublist support
    this.sublist = new Sublist(0, PAGE_SIZE);
    this.user = user;
    this.justActive = justActive;
    this.removeDuplicates = removeDuplicates;
    this.fieldsToReturn = fieldsToReturn;
    getResults(this.sublist);
  }

  private void getResults(final Sublist sublist) {
    try {
      indexResult = SolrUtils.find(solrClient, returnClass, filter, sorter, sublist, facets, user, justActive,
        fieldsToReturn);
      if (totalObjects == -1) {
        totalObjects = indexResult.getTotalCount();
      }

      if (removeDuplicates) {
        indexResultObjects = new ArrayList<>();
        for (T obj : indexResult.getResults()) {
          if (!uniqueUUIDs.contains(obj.getUUID())) {
            indexResultObjects.add(obj);
            uniqueUUIDs.add(obj.getUUID());
          } else {
            totalObjects -= 1;
          }
        }
      } else {
        indexResultObjects = indexResult.getResults();
      }
    } catch (GenericException | RequestNotValidException e) {
      // just set index result to null & let iterator return proper values
      indexResult = null;
      LOGGER.error("Error while retrieving partial list of results", e);
    }
  }

  public List<FacetFieldResult> getFacetResults() {
    return indexResult != null ? indexResult.getFacetResults() : Collections.emptyList();
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      @Override
      public boolean hasNext() {
        return indexResult != null && currentObject < totalObjects;
      }

      @Override
      public T next() {
        try {
          final T t = indexResultObjects.get(currentObjectInPartialList);
          currentObject += 1;
          currentObjectInPartialList += 1;
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("({} of {}) Returning object of class '{}' with id '{}'", currentObject, totalObjects,
              returnClass.getSimpleName(), t.getUUID());
          }

          // see if a new page needs to be obtained
          if (currentObjectInPartialList == indexResultObjects.size()) {
            getResults(sublist.setFirstElementIndex(sublist.getFirstElementIndex() + PAGE_SIZE));
            currentObjectInPartialList = 0;
          }

          return t;
        } catch (IndexOutOfBoundsException e) {
          LOGGER.error(
            "Error while processing next element. filter='{}'; sorter='{}'; sublist='{}'; justActive='{}'; removeDuplicates='{}'; "
              + "currentObjectInPartialList='{}'; currentObject='{}'; totalObjects='{}'",
            filter, sorter, sublist, justActive, removeDuplicates, currentObjectInPartialList, currentObject,
            totalObjects, e);
          throw new NoSuchElementException("Error while processing next element");
        }
      }
    };
  }

  public long getTotalObjects() {
    return totalObjects;
  }

}
