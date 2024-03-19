package org.roda.wui.client.ingest.transfer;

import org.roda.core.data.exceptions.RODAException;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
  R apply(T t) throws RODAException;
}
