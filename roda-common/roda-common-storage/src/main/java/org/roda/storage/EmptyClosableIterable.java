package org.roda.storage;

import java.io.IOException;
import java.util.Iterator;

public class EmptyClosableIterable<T> implements ClosableIterable<T> {

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
