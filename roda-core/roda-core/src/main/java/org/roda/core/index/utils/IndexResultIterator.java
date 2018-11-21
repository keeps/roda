/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.CursorMarkParams;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;

public class IndexResultIterator<T extends IsIndexed> implements Iterator<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexResultIterator.class);

  public static final int DEFAULT_PAGE_SIZE = 1000;
  public static final int DEFAULT_RETRIES = 100;
  public static final int DEFAULT_SLEEP_BETWEEN_RETRIES = 10000;

  private int pageSize = DEFAULT_PAGE_SIZE;
  private int retries = DEFAULT_RETRIES;
  private int sleepBetweenRetries = DEFAULT_SLEEP_BETWEEN_RETRIES;
  private Histogram histogram;

  private IndexResult<T> result = null;
  private int indexInResult = 0;
  private String cursorMark = CursorMarkParams.CURSOR_MARK_START;
  private String nextCursorMark = CursorMarkParams.CURSOR_MARK_START;

  private final SolrClient index;
  private final Class<T> classToRetrieve;
  private final Filter filter;
  private final User user;
  private final boolean justActive;
  private final List<String> fieldsToReturn;

  private T next = null;

  public IndexResultIterator(SolrClient index, Class<T> classToRetrieve, Filter filter, User user, boolean justActive,
    List<String> fieldsToReturn) {
    this.index = index;
    this.classToRetrieve = classToRetrieve;
    this.filter = filter;
    this.user = user;
    this.justActive = justActive;
    this.fieldsToReturn = fieldsToReturn;

    getCurrentAndPrepareNext();
  }

  private T getCurrentAndPrepareNext() {
    T current = next;

    // ensure index result is renewed
    if (result == null || result.getResults().size() == indexInResult) {
      indexInResult = 0;

      Instant start = Instant.now();

      cursorMark = nextCursorMark;
      result = null;
      nextCursorMark = null;
      int availableRetries = retries;

      do {
        try {
          Pair<IndexResult<T>, String> page = SolrUtils.find(index, classToRetrieve, filter, pageSize, cursorMark, user,
            justActive, fieldsToReturn);
          result = page.getFirst();
          nextCursorMark = page.getSecond();

        } catch (GenericException | RequestNotValidException e) {
          if (availableRetries > 0) {
            availableRetries--;
            LOGGER.warn("Error getting next page from Solr, retrying in {}ms...", sleepBetweenRetries);
            try {
              Thread.sleep(sleepBetweenRetries);
            } catch (InterruptedException e1) {
              // do nothing
            }
          } else {
            LOGGER.error("Error getting next page from Solr, no more retries.", e);
            throw new NoSuchElementException("Error getting next item in list: " + e.getMessage());
          }
        }
      } while (result == null);

      Instant end = Instant.now();
      if (histogram != null) {
        histogram.update(Duration.between(start, end).toNanos());
      }

    }

    if (indexInResult < result.getResults().size()) {
      this.next = result.getResults().get(indexInResult++);
    } else {
      this.next = null;
    }

    return current;
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public T next() {
    return getCurrentAndPrepareNext();
  }

  /**
   * @return the pageSize
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * @param pageSize
   *          the pageSize to set
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * @return the retries
   */
  public int getRetries() {
    return retries;
  }

  /**
   * @param retries
   *          the retries to set
   */
  public void setRetries(int retries) {
    this.retries = retries;
  }

  /**
   * @return the sleepBetweenRetries
   */
  public int getSleepBetweenRetries() {
    return sleepBetweenRetries;
  }

  /**
   * @param sleepBetweenRetries
   *          the sleepBetweenRetries to set
   */
  public void setSleepBetweenRetries(int sleepBetweenRetries) {
    this.sleepBetweenRetries = sleepBetweenRetries;
  }

  /**
   * @return the histogram
   */
  public Histogram getHistogram() {
    return histogram;
  }

  /**
   * @param histogram
   *          the histogram to set
   */
  public void setHistogram(Histogram histogram) {
    this.histogram = histogram;
  }

  /**
   * Gets the total count of objects as reported by underlying Solr requests.
   * 
   * @return
   */
  public long getTotalCount() {
    return result != null ? result.getTotalCount() : -1;
  }

}
