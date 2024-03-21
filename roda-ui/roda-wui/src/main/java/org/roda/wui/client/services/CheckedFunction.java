package org.roda.wui.client.services;

import org.roda.core.data.exceptions.RODAException;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
  R apply(T t) throws RODAException;
}
