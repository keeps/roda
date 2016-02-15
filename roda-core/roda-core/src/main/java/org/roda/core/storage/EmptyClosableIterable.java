/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.IOException;
import java.util.Iterator;

import org.roda.core.common.iterables.CloseableIterable;

public class EmptyClosableIterable<T> implements CloseableIterable<T> {

  @Override
  public void close() throws IOException {
    // nothing to do
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public T next() {
        return null;
      }
    };
  }
}
