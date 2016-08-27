/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common;

import java.lang.reflect.Method;
import java.util.Date;

import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.user.User;

public class ControllerAssistant {
  private final Date startDate;
  private final Method enclosingMethod;

  public ControllerAssistant() {
    this.startDate = new Date();
    this.enclosingMethod = this.getClass().getEnclosingMethod();
  }

  public void checkGroup(final User user, final String group) throws AuthorizationDeniedException {
    try {
      UserUtility.checkGroup(user, group);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LOG_ENTRY_STATE.UNAUTHORIZED);
      throw e;
    }
  }

  public void checkRoles(final User user) throws AuthorizationDeniedException {
    try {
      UserUtility.checkRoles(user, this.getClass());
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LOG_ENTRY_STATE.UNAUTHORIZED);
      throw e;
    }
  }

  public void checkRoles(final User user, final Class<?> classToReturn) throws AuthorizationDeniedException {
    try {
      UserUtility.checkRoles(user, this.getClass(), classToReturn);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LOG_ENTRY_STATE.UNAUTHORIZED);
      throw e;
    }
  }

  public void registerAction(final User user, final String relatedObjectId, final LOG_ENTRY_STATE state,
    final Object... parameters) {
    final long duration = new Date().getTime() - startDate.getTime();
    ControllerAssistantUtils.registerAction(user, this.enclosingMethod.getDeclaringClass().getName(),
      this.enclosingMethod.getName(), relatedObjectId, duration, state, parameters);
  }

  public void registerAction(final User user, final LOG_ENTRY_STATE state, final Object... parameters) {
    registerAction(user, null, state, parameters);
  }

  public void registerAction(final User user, final LOG_ENTRY_STATE state) {
    registerAction(user, (String) null, state);
  }
}
