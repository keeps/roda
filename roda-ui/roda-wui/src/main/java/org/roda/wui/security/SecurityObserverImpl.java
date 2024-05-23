package org.roda.wui.security;

import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.security.SecurityObserver;
import org.roda.wui.common.ControllerAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SecurityObserverImpl implements SecurityObserver {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityObserverImpl.class);

  @Override
  public ReturnWithExceptions<Void, SecurityObserver> login(User user, LogEntryState state, Object... parameters) {
    ReturnWithExceptions<Void, SecurityObserver> ret = new ReturnWithExceptions<>(this);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    controllerAssistant.registerAction(user, state, parameters);
    return ret;
  }

  @Override
  public ReturnWithExceptions<Void, SecurityObserver> logout(User user, LogEntryState state, Object... parameters) {
    ReturnWithExceptions<Void, SecurityObserver> ret = new ReturnWithExceptions<>(this);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    controllerAssistant.registerAction(user, state, parameters);
    return ret;
  }
}
