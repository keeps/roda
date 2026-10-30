/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Documents to add to Solr, kept by collection and sent with
 * {@link SolrUtils#create(SolrClient, String, List, Object)}, a single request
 * per collection, when there are {@code core.solr.index.batch.size} of them or
 * about {@code core.solr.index.batch.maxBytes} of their text, whichever comes
 * first, and when the buffer is flushed, instead of one request each (#3773).
 * The size limit keeps the documents held in memory, and the request, about as
 * large as one file document with its full text.
 *
 * A buffer belongs to a loop that indexes many objects, which flushes it when
 * it ends, in a single thread. Once a request fails, nothing else is sent and
 * the documents not sent yet are dropped, as indexing stops at its first Solr
 * failure (#3785).
 */
public class SolrDocumentBuffer<S> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrDocumentBuffer.class);

  private static final int DEFAULT_BATCH_SIZE = 100;
  private static final long DEFAULT_BATCH_MAX_BYTES = 10485760L;
  // the size taken for a value that is not text
  private static final int VALUE_BYTES = 8;

  private final SolrClient index;
  private final S source;
  private final Runnable afterRequest;
  private final int batchSize;
  private final long batchMaxBytes;
  // by collection, in the order they were first added to
  private final Map<String, Pending> documents = new LinkedHashMap<>();
  private boolean failed = false;

  public SolrDocumentBuffer(SolrClient index, S source) {
    this(index, source, () -> {
    });
  }

  /**
   * @param afterRequest
   *          run after each request, whether Solr took its documents or not, as
   *          some may have been added even when it fails
   */
  public SolrDocumentBuffer(SolrClient index, S source, Runnable afterRequest) {
    this.index = index;
    this.source = source;
    this.afterRequest = afterRequest;
    Configuration configuration = RodaCoreFactory.getRodaConfiguration();
    this.batchSize = getBatchSize(configuration);
    this.batchMaxBytes = getBatchMaxBytes(configuration);
  }

  /**
   * Adds the document, and sends the documents of its collection when there are
   * enough of them. Once a request failed, does nothing.
   */
  public ReturnWithExceptions<Void, S> add(String collection, SolrInputDocument document) {
    ReturnWithExceptions<Void, S> ret = new ReturnWithExceptions<>(source);
    if (!failed) {
      Pending pending = documents.computeIfAbsent(collection, c -> new Pending());
      pending.add(document);
      if (pending.documents.size() >= batchSize || pending.bytes >= batchMaxBytes) {
        send(collection, pending).addTo(ret);
      }
    }
    return ret;
  }

  /**
   * Sends the documents not sent yet, collection by collection, up to a failure.
   */
  public ReturnWithExceptions<Void, S> flush() {
    ReturnWithExceptions<Void, S> ret = new ReturnWithExceptions<>(source);
    for (Map.Entry<String, Pending> pending : documents.entrySet()) {
      if (!failed && !pending.getValue().documents.isEmpty()) {
        send(pending.getKey(), pending.getValue()).addTo(ret);
      }
    }
    return ret;
  }

  private ReturnWithExceptions<Void, S> send(String collection, Pending pending) {
    ReturnWithExceptions<Void, S> ret = SolrUtils.create(index, collection, new ArrayList<>(pending.documents), source);
    pending.clear();
    afterRequest.run();
    if (!ret.isEmpty()) {
      failed = true;
      documents.values().forEach(Pending::clear);
    }
    return ret;
  }

  /** About the size of the document, from the length of its text. */
  private static long estimatedBytes(SolrInputDocument document) {
    long bytes = 0;
    for (SolrInputField field : document) {
      for (Object value : field) {
        bytes += estimatedBytes(value);
      }
    }
    return bytes;
  }

  /** Of a value, or of the values of an atomic update ({"set": value}). */
  private static long estimatedBytes(Object value) {
    if (value instanceof CharSequence text) {
      return text.length();
    } else if (value instanceof Map<?, ?> modifier) {
      return modifier.values().stream().mapToLong(SolrDocumentBuffer::estimatedBytes).sum();
    } else if (value instanceof Collection<?> values) {
      return values.stream().mapToLong(SolrDocumentBuffer::estimatedBytes).sum();
    }
    return VALUE_BYTES;
  }

  private static int getBatchSize(Configuration configuration) {
    int batchSize = configuration.getInt(RodaConstants.SOLR_INDEX_BATCH_SIZE, DEFAULT_BATCH_SIZE);
    if (batchSize <= 0) {
      LOGGER.warn("{} must be positive, using {}", RodaConstants.SOLR_INDEX_BATCH_SIZE, DEFAULT_BATCH_SIZE);
      return DEFAULT_BATCH_SIZE;
    }
    return batchSize;
  }

  private static long getBatchMaxBytes(Configuration configuration) {
    long batchMaxBytes = configuration.getLong(RodaConstants.SOLR_INDEX_BATCH_MAX_BYTES, DEFAULT_BATCH_MAX_BYTES);
    if (batchMaxBytes <= 0) {
      LOGGER.warn("{} must be positive, using {}", RodaConstants.SOLR_INDEX_BATCH_MAX_BYTES, DEFAULT_BATCH_MAX_BYTES);
      return DEFAULT_BATCH_MAX_BYTES;
    }
    return batchMaxBytes;
  }

  /**
   * The documents of a collection not sent yet, and about the size of their text.
   */
  private static final class Pending {
    private final List<SolrInputDocument> documents = new ArrayList<>();
    private long bytes = 0;

    private void add(SolrInputDocument document) {
      documents.add(document);
      bytes += estimatedBytes(document);
    }

    private void clear() {
      documents.clear();
      bytes = 0;
    }
  }
}
