/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.iterables;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

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

  public static <A, B> CloseableIterable<B> concat(final CloseableIterable<A> list,
    final Function<A, CloseableIterable<B>> listFunction) {

    CloseableIterable<CloseableIterable<B>> iterable = new CloseableIterable<CloseableIterable<B>>() {

      @Override
      public void close() throws IOException {
        list.close();
      }

      @Override
      public Iterator<CloseableIterable<B>> iterator() {
        final Iterator<A> iterator = list.iterator();
        return new Iterator<CloseableIterable<B>>() {

          @Override
          public boolean hasNext() {
            return iterator.hasNext();
          }

          @Override
          public CloseableIterable<B> next() {
            A next = iterator.next();
            return listFunction.apply(next);
          }
        };
      }
    };
    return concat(iterable);
  }

  public static <T> CloseableIterable<T> concat(CloseableIterable<CloseableIterable<T>> list) {
    final Iterator<CloseableIterable<T>> main = list.iterator();

    return new CloseableIterable<T>() {

      CloseableIterable<T> current = null;

      @Override
      public void close() throws IOException {
        if (current != null) {
          current.close();
        }
        list.close();
      }

      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          Iterator<T> currentIt = null;

          @Override
          public boolean hasNext() {
            boolean hasNext = false;
            if (current != null) {
              if (currentIt.hasNext()) {
                hasNext = true;
              } else {
                IOUtils.closeQuietly(current);
                hasNext = fastForward();
              }
            } else {
              hasNext = fastForward();
            }
            return hasNext;
          }

          private boolean fastForward() {
            boolean hasNext = false;
            while (main.hasNext()) {
              current = main.next();
              currentIt = current.iterator();
              if (currentIt.hasNext()) {
                hasNext = true;
                break;
              } else {
                IOUtils.closeQuietly(current);
              }
            }
            return hasNext;
          }

          @Override
          public T next() {
            return hasNext() ? currentIt.next() : null;
          }
        };
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

  public static <T> boolean isEmpty(CloseableIterable<T> it) {
    boolean empty = Iterables.isEmpty(it);
    IOUtils.closeQuietly(it);
    return empty;
  }

  public static <T> CloseableIterable<T> fromList(final List<T> list) {
    return new CloseableIterable<T>() {

      @Override
      public void close() throws IOException {
        // do nothing
      }

      @Override
      public Iterator<T> iterator() {
        return list.iterator();
      }
    };
  }

  public static <T> CloseableIterable<T> empty() {
    return new CloseableIterable<T>() {

      @Override
      public void close() throws IOException {
        // nothing to do
      }

      @Override
      public Iterator<T> iterator() {
        return Collections.emptyIterator();
      }
    };
  }

}
