/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.services;

import org.roda.core.data.exceptions.RODAException;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
  R apply(T t) throws RODAException;
}
