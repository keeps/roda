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
