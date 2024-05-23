package org.roda.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.roda.core.common.ReturnWithExceptionsWrapper;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SecurityObservable {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityObservable.class);
  private List<SecurityObserver> observers = new ArrayList<>();

  public void addObserver(SecurityObserver securityObserver) {
    observers.add(securityObserver);
  }

  private ReturnWithExceptionsWrapper notifyObserversSafely(Function<SecurityObserver, ReturnWithExceptions<?, ?>> func) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (SecurityObserver observer : observers) {
      try {
        wrapper.addToList(func.apply(observer));
      } catch (Exception e) {
        LOGGER.error("Error invoking method in observer {}", observer.getClass().getSimpleName(), e);
        // do nothing, just want to sandbox observer method invocation
      }
    }
    return wrapper;
  }

  public ReturnWithExceptionsWrapper notifyLogin(final User user, final LogEntryState state,
    final Object... parameters) {
    return notifyObserversSafely(observer -> observer.login(user, state, parameters));
  }

  public ReturnWithExceptionsWrapper notifyLogout(final User user, final LogEntryState state,
    final Object... parameters) {
    return notifyObserversSafely(observer -> observer.logout(user, state, parameters));
  }


}
