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
 * Session-scoped singleton that batches AIP title lookups by UUID using a
 * stale-while-revalidate strategy: cached values are returned immediately while
 * a background re-fetch is always queued. {@link CellTable#redraw()} is called
 * only when a fetched value differs from the cached one, so the display
 * self-corrects after any Solr update without flickering on unchanged data.
 */
public class AipTitleBatchFetcher {

  private static final int BATCH_DELAY_MS = 50;

  private static AipTitleBatchFetcher instance;

  private static final class CacheEntry {
    final String title;
    final String level;

    CacheEntry(String title, String level) {
      this.title = title;
      this.level = level;
    }
  }

  private final Map<String, CacheEntry> cache = new HashMap<>();
  private final Set<String> pendingUuids = new HashSet<>();
  private final Set<CellTable<IndexedAIP>> pendingTables = new HashSet<>();
  private Timer batchTimer;

  private AipTitleBatchFetcher() {
  }

  public static AipTitleBatchFetcher getInstance() {
    if (instance == null) {
      instance = new AipTitleBatchFetcher();
    }
    return instance;
  }

  /** Returns true if a cached entry exists for this UUID. */
  public boolean isCached(String uuid) {
    return cache.containsKey(uuid);
  }

  /** Returns the cached title, or {@code null} if not yet fetched. */
  public String getCachedTitle(String uuid) {
    CacheEntry e = cache.get(uuid);
    return e != null ? e.title : null;
  }

  /** Returns the cached description level, or {@code null} if not yet fetched. */
  public String getCachedLevel(String uuid) {
    CacheEntry e = cache.get(uuid);
    return e != null ? e.level : null;
  }

  /**
   * Queues {@code uuids} for a background re-fetch and registers {@code table}
   * to be redrawn if any fetched value differs from the cached one. Always
   * called from {@code Column.getValue()} — even on cache hits — so that stale
   * data is corrected automatically after Solr updates.
   */
  public void requestTitles(List<String> uuids, CellTable<IndexedAIP> table) {
    pendingTables.add(table);
    pendingUuids.addAll(uuids);

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
          boolean anyChanged = false;
          for (IndexedAIP aip : result.getResults()) {
            String newTitle = aip.getTitle() != null ? aip.getTitle() : "";
            String newLevel = aip.getLevel();
            CacheEntry existing = cache.get(aip.getUUID());
            if (existing == null || !newTitle.equals(existing.title) || !equalOrBothNull(newLevel, existing.level)) {
              anyChanged = true;
            }
            cache.put(aip.getUUID(), new CacheEntry(newTitle, newLevel));
          }
          if (anyChanged) {
            tablesToRedraw.forEach(CellTable::redraw);
          }
        }
      });
  }

  private static boolean equalOrBothNull(String a, String b) {
    return a == null ? b == null : a.equals(b);
  }
}
