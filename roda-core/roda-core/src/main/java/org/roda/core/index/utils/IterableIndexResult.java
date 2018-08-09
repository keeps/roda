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

import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.user.User;

import com.codahale.metrics.Histogram;

/**
 * Does search in the index, using the Solr.find() method, and if configured
 * removes duplicate objects (via uuid comparison) thus providing iterator
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */

public class IterableIndexResult<T extends IsIndexed> implements CloseableIterable<T> {
  private static int PAGE_SIZE = -1;
  private static int RETRIES = -1;
  private static int SLEEP_BETWEEN_RETRIES = -1;

  private static Histogram HISTOGRAM;

  private final IndexResultIterator<T> iterator;

  public IterableIndexResult(final SolrClient solrClient, final Class<T> returnClass, final Filter filter,
    final User user, final boolean justActive, final List<String> fieldsToReturn) {
    iterator = new IndexResultIterator<>(solrClient, returnClass, filter, user, justActive, fieldsToReturn);

    if (PAGE_SIZE > 0) {
      iterator.setPageSize(PAGE_SIZE);
    }

    if (RETRIES > 0) {
      iterator.setRetries(RETRIES);
    }

    if (SLEEP_BETWEEN_RETRIES > 0) {
      iterator.setSleepBetweenRetries(SLEEP_BETWEEN_RETRIES);
    }

    if (HISTOGRAM != null) {
      iterator.setHistogram(HISTOGRAM);
    }
  }

  @Override
  public Iterator<T> iterator() {
    return iterator;
  }

  @Override
  public void close() throws IOException {
    // do nothing
  }

  public static void injectSearchPageSize(int pageSize) {
    PAGE_SIZE = pageSize;
  }

  public static void injectNumberOfRetries(int retries) {
    RETRIES = retries;
  }

  public static void injectSleepBetweenRetries(int sleepTime) {
    SLEEP_BETWEEN_RETRIES = sleepTime;
  }

  public static void injectHistogram(Histogram histogram) {
    HISTOGRAM = histogram;
  }

  /**
   * @see IndexResultIterator#getTotalCount()
   */
  public long getTotalCount() {
    return iterator.getTotalCount();
  }

}
