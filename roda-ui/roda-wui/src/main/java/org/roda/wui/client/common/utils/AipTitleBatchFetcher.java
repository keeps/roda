/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.services.Services;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Timer;

/**
 * Session-scoped singleton that batches AIP title lookups by UUID and caches
 * results with a TTL. Callers register a {@link CellTable} to be redrawn once
 * the batch resolves so that {@code Column.getValue()} can re-run against the
 * warm cache and return the final SafeHtml.
 */
public class AipTitleBatchFetcher {

  private static long cacheTtlMs = 10_000L;
  private static final int BATCH_DELAY_MS = 50;
  private static final int CLEANUP_INTERVAL_MS = 60 * 1000;

  private static AipTitleBatchFetcher instance;

  private static final class CacheEntry {
    final String title;
    final String level;
    final long timestamp;

    CacheEntry(String title, String level) {
      this.title = title;
      this.level = level;
      this.timestamp = System.currentTimeMillis();
    }

    boolean isExpired() {
      return System.currentTimeMillis() - timestamp > cacheTtlMs;
    }
  }

  private final Map<String, CacheEntry> cache = new HashMap<>();
  private final Set<String> pendingUuids = new HashSet<>();
  private final Set<CellTable<IndexedAIP>> pendingTables = new HashSet<>();
  private Timer batchTimer;
  private Timer cleanupTimer;

  private AipTitleBatchFetcher() {
  }

  public static AipTitleBatchFetcher getInstance() {
    if (instance == null) {
      instance = new AipTitleBatchFetcher();
    }
    return instance;
  }

  public static void setCacheTtlMs(long ttlMs) {
    cacheTtlMs = ttlMs;
  }

  /** Returns true only if a non-expired entry exists for this UUID. */
  public boolean isCached(String uuid) {
    CacheEntry e = cache.get(uuid);
    return e != null && !e.isExpired();
  }

  /** Returns the cached title, or {@code null} if absent or expired. */
  public String getCachedTitle(String uuid) {
    CacheEntry e = cache.get(uuid);
    return (e != null && !e.isExpired()) ? e.title : null;
  }

  /** Returns the cached description level, or {@code null} if absent or expired. */
  public String getCachedLevel(String uuid) {
    CacheEntry e = cache.get(uuid);
    return (e != null && !e.isExpired()) ? e.level : null;
  }

  /**
   * Queues any uncached UUIDs for the next batch fetch and registers
   * {@code table} to be redrawn once the batch completes. Safe to call
   * repeatedly from {@code Column.getValue()}.
   */
  public void requestTitles(List<String> uuids, CellTable<IndexedAIP> table) {
    pendingTables.add(table);

    for (String uuid : uuids) {
      if (!isCached(uuid)) {
        pendingUuids.add(uuid);
      }
    }

    if (batchTimer == null && !pendingUuids.isEmpty()) {
      batchTimer = new Timer() {
        @Override
        public void run() {
          executeBatch();
        }
      };
      batchTimer.schedule(BATCH_DELAY_MS);
    }
  }

  private void executeBatch() {
    batchTimer = null;

    final List<String> toFetch = new ArrayList<>(pendingUuids);
    pendingUuids.clear();
    final Set<CellTable<IndexedAIP>> tablesToRedraw = new HashSet<>(pendingTables);
    pendingTables.clear();

    if (toFetch.isEmpty()) {
      tablesToRedraw.forEach(CellTable::redraw);
      return;
    }

    Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID, toFetch));
    FindRequest findRequest = FindRequest.getBuilder(filter, true)
      .withSublist(new Sublist(0, toFetch.size()))
      .withFieldsToReturn(Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL))
      .build();

    new Services("Fetch AIP titles", "get")
      .aipResource(s -> s.find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((result, throwable) -> {
        if (throwable == null && result != null) {
          for (IndexedAIP aip : result.getResults()) {
            cache.put(aip.getUUID(), new CacheEntry(aip.getTitle() != null ? aip.getTitle() : "", aip.getLevel()));
          }
          ensureCleanupTimer();
        }
        tablesToRedraw.forEach(CellTable::redraw);
      });
  }

  private void ensureCleanupTimer() {
    if (cleanupTimer == null) {
      cleanupTimer = new Timer() {
        @Override
        public void run() {
          evictExpired();
        }
      };
      cleanupTimer.scheduleRepeating(CLEANUP_INTERVAL_MS);
    }
  }

  private void evictExpired() {
    cache.entrySet().removeIf(e -> e.getValue().isExpired());
    if (cache.isEmpty() && cleanupTimer != null) {
      cleanupTimer.cancel();
      cleanupTimer = null;
    }
  }
}
