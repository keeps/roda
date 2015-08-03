package org.roda.storage;

import java.io.Closeable;

public interface ClosableIterable<T> extends Closeable, Iterable<T> {
}
