/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.security;

import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface SecurityObserver {
  ReturnWithExceptions<Void, SecurityObserver> login(final User user, final LogEntryState state,
    final Object... parameters);

  ReturnWithExceptions<Void, SecurityObserver> logout(final User user, final LogEntryState state,
    final Object... parameters);
}
