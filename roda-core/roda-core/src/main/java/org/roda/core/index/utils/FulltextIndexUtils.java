/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.util.IdUtils;

/**
 * Aggregation of file full-text into representations and AIPs.
 */
public final class FulltextIndexUtils {

  private static final int DEFAULT_AGGREGATE_THRESHOLD_IN_BYTES = 52428800;
  private static final int REAL_TIME_GET_BATCH_SIZE = 500;

  private FulltextIndexUtils() {
    // do nothing
  }

  public record RepresentationFulltext(boolean original, List<String> states, List<String> fulltext) {
    public RepresentationFulltext {
      states = states == null ? Collections.emptyList() : states;
      fulltext = fulltext == null ? Collections.emptyList() : fulltext;
    }
  }

  public static long getAggregateThresholdInBytes() {
    return RodaCoreFactory.getRodaConfigurationAsInt(DEFAULT_AGGREGATE_THRESHOLD_IN_BYTES,
      "core.index.fulltext_aggregate_threshold_in_bytes");
  }

  /**
   * Keeps the first chunks whose total UTF-8 size does not exceed the maximum.
   * Once a chunk does not fit, all further chunks are ignored.
   */
  public static final class FulltextAccumulator {
    private final long maxBytes;
    private final List<String> fulltext = new ArrayList<>();
    private long totalBytes = 0;
    private boolean truncated = false;

    public FulltextAccumulator() {
      this(getAggregateThresholdInBytes());
    }

    public FulltextAccumulator(long maxBytes) {
      this.maxBytes = maxBytes;
    }

    public void add(String chunk) {
      if (!truncated) {
        long chunkBytes = utf8Length(chunk);
        if (totalBytes + chunkBytes > maxBytes) {
          truncated = true;
        } else {
          fulltext.add(chunk);
          totalBytes += chunkBytes;
        }
      }
    }

    public void addAll(List<String> chunks) {
      chunks.forEach(this::add);
    }

    public List<String> getFulltext() {
      return fulltext;
    }

    public long getMaxBytes() {
      return maxBytes;
    }

    public boolean isTruncated() {
      return truncated;
    }
  }

  private static long utf8Length(String text) {
    long length = 0;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c < 0x80) {
        length += 1;
      } else if (c < 0x800) {
        length += 2;
      } else if (Character.isHighSurrogate(c) && i + 1 < text.length()
        && Character.isLowSurrogate(text.charAt(i + 1))) {
        length += 4;
        i++;
      } else {
        length += 3;
      }
    }
    return length;
  }

  /**
   * Selects the full-text of a single representation to be indexed in the AIP:
   * preservation first, then original, then any other. Representations without
   * full-text are skipped and ties are resolved by the given order.
   */
  public static List<String> selectAipFulltext(List<RepresentationFulltext> representations) {
    RepresentationFulltext selected = null;
    for (RepresentationFulltext representation : representations) {
      if (!representation.fulltext().isEmpty() && (selected == null || priority(representation) < priority(selected))) {
        selected = representation;
      }
    }
    return selected == null ? Collections.emptyList() : selected.fulltext();
  }

  private static int priority(RepresentationFulltext representation) {
    if (representation.states().contains(RepresentationState.PRESERVATION)) {
      return 0;
    } else if (representation.original() || representation.states().contains(RepresentationState.ORIGINAL)) {
      return 1;
    } else {
      return 2;
    }
  }

  /**
   * Rebuilds the AIP full-text from its indexed representations. Uses real-time
   * get, so representations indexed but not yet committed are also seen.
   */
  public static List<String> collectAipFulltext(SolrClient index, AIP aip) throws GenericException {
    if (AIPState.DESTROYED.equals(aip.getState()) || aip.getRepresentations().isEmpty()) {
      return Collections.emptyList();
    }

    List<String> representationUUIDs = aip.getRepresentations().stream().map(IdUtils::getRepresentationId).toList();

    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set(CommonParams.FL, String.join(",", RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_ORIGINAL,
      RodaConstants.REPRESENTATION_STATES, RodaConstants.REPRESENTATION_FULLTEXT));

    try {
      Map<String, SolrDocument> docs = new HashMap<>();
      for (SolrDocument doc : index.getById(SolrCollectionRegistry.getIndexName(IndexedRepresentation.class),
        representationUUIDs, params)) {
        docs.put(SolrUtils.objectToString(doc.get(RodaConstants.INDEX_UUID), null), doc);
      }

      List<RepresentationFulltext> representations = new ArrayList<>();
      for (String representationUUID : representationUUIDs) {
        SolrDocument doc = docs.get(representationUUID);
        if (doc != null) {
          representations.add(new RepresentationFulltext(
            Boolean.TRUE.equals(SolrUtils.objectToBoolean(doc.get(RodaConstants.REPRESENTATION_ORIGINAL), false)),
            SolrUtils.objectToListString(doc.get(RodaConstants.REPRESENTATION_STATES)),
            SolrUtils.objectToListString(doc.get(RodaConstants.REPRESENTATION_FULLTEXT))));
        }
      }

      return selectAipFulltext(representations);
    } catch (SolrServerException | SolrException | IOException | NotSupportedException e) {
      throw new GenericException("Could not retrieve the full-text of the representations of AIP " + aip.getId(), e);
    }
  }

  /**
   * Rebuilds a representation full-text from its indexed files, in the given
   * order. Uses real-time get, so files indexed but not yet committed are also
   * seen.
   */
  public static FulltextAccumulator collectRepresentationFulltext(SolrClient index, List<String> fileUUIDs)
    throws GenericException {
    FulltextAccumulator fulltext = new FulltextAccumulator();

    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set(CommonParams.FL, String.join(",", RodaConstants.INDEX_UUID, RodaConstants.FILE_FULLTEXT));

    try {
      String collection = SolrCollectionRegistry.getIndexName(IndexedFile.class);
      for (int i = 0; i < fileUUIDs.size() && !fulltext.isTruncated(); i += REAL_TIME_GET_BATCH_SIZE) {
        List<String> batch = fileUUIDs.subList(i, Math.min(i + REAL_TIME_GET_BATCH_SIZE, fileUUIDs.size()));

        Map<String, String> fileFulltext = new HashMap<>();
        for (SolrDocument doc : index.getById(collection, batch, params)) {
          String text = SolrUtils.objectToString(doc.get(RodaConstants.FILE_FULLTEXT), null);
          if (text != null) {
            fileFulltext.put(SolrUtils.objectToString(doc.get(RodaConstants.INDEX_UUID), null), text);
          }
        }

        for (String fileUUID : batch) {
          String text = fileFulltext.get(fileUUID);
          if (text != null) {
            fulltext.add(text);
          }
        }
      }
    } catch (SolrServerException | SolrException | IOException | NotSupportedException e) {
      throw new GenericException("Could not retrieve the full-text of files", e);
    }

    return fulltext;
  }
}
