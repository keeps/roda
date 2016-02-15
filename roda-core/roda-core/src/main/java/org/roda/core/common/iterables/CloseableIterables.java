package org.roda.core.common.iterables;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

public final class CloseableIterables {

  private CloseableIterables() {

  }

  public static <T> CloseableIterable<T> concat(List<CloseableIterable<T>> list) {
    checkNotNull(list);
    return new CloseableIterable<T>() {

      @Override
      public void close() throws IOException {
        closeAll(list);
      }

      @Override
      public Iterator<T> iterator() {
        return Iterators.concat(iterators(list));
      }
    };
  }

  /**
   * Returns an iterator over the iterators of the given iterables.
   */
  private static <T> UnmodifiableIterator<Iterator<? extends T>> iterators(
    Iterable<? extends Iterable<? extends T>> iterables) {
    final Iterator<? extends Iterable<? extends T>> iterableIterator = iterables.iterator();
    return new UnmodifiableIterator<Iterator<? extends T>>() {
      @Override
      public boolean hasNext() {
        return iterableIterator.hasNext();
      }

      @Override
      public Iterator<? extends T> next() {
        return iterableIterator.next().iterator();
      }
    };
  }

  private static <T> void checkNotNull(List<CloseableIterable<T>> list) {
    if (list == null) {
      throw new NullPointerException();
    } else {
      for (CloseableIterable<T> it : list) {
        if (it == null) {
          throw new NullPointerException();
        }
      }
    }
  }

  protected static <T> void closeAll(List<CloseableIterable<T>> list) {
    for (CloseableIterable<T> it : list) {
      IOUtils.closeQuietly(it);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> CloseableIterable<T> concat(CloseableIterable<T>... iterables) {
    return concat(Arrays.asList(iterables));
  }

  public static <T> int size(CloseableIterable<T> iterable) {
    int size = Iterables.size(iterable);
    IOUtils.closeQuietly(iterable);
    return size;
  }

  public static <T> CloseableIterable<T> filter(final CloseableIterable<T> unfiltered, Predicate<? super T> predicate) {
    return new CloseableIterable<T>() {

      @Override
      public void close() throws IOException {
        unfiltered.close();
      }

      @Override
      public Iterator<T> iterator() {
        return Iterators.filter(unfiltered.iterator(), predicate);
      }
    };
  }

}
