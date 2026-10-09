/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.LongFunction;

import org.roda.core.common.iterables.CloseableIterable;

/**
 * Iterates over rows read lazily, one page at a time, by keyset (each page
 * starts after the last key of the previous one), so that only one page is held
 * in memory.
 *
 * @author RODA Development Team
 */
final class PagedIterable<T> implements CloseableIterable<T> {

  /**
   * One page of items.
   *
   * @param items
   *          the page's items, in key order
   * @param lastKey
   *          the key of the last item (where the next page starts)
   * @param last
   *          whether there are no more pages (e.g. the page was not full)
   */
  record Page<T>(List<T> items, long lastKey, boolean last) {
  }

  private final LongFunction<Page<T>> loader;

  /**
   * @param loader
   *          loads the page of items whose keys come after the given key (the
   *          first page is loaded after key 0)
   */
  PagedIterable(LongFunction<Page<T>> loader) {
    this.loader = loader;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<>() {
      private Iterator<T> current = Collections.emptyIterator();
      private long lastKey = 0;
      private boolean exhausted = false;

      @Override
      public boolean hasNext() {
        while (!current.hasNext() && !exhausted) {
          Page<T> page = loader.apply(lastKey);
          current = page.items().iterator();
          lastKey = page.lastKey();
          exhausted = page.last() || page.items().isEmpty();
        }
        return current.hasNext();
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return current.next();
      }
    };
  }

  @Override
  public void close() {
    // nothing held between pages
  }
}
