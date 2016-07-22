package org.roda.wui.common;

import java.util.Date;

import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.user.RodaUser;
import java.lang.reflect.Method;

public class ControllerAssistant {
  private final Date startDate;
  private final Method enclosingMethod;

  public ControllerAssistant() {
    this.startDate = new Date();
    this.enclosingMethod = this.getClass().getEnclosingMethod();
  }

  public void checkRoles(RodaUser user) throws AuthorizationDeniedException {
    try {
      UserUtility.checkRoles(user, this.getClass());
    } catch (AuthorizationDeniedException e) {
      registerAction(user, LOG_ENTRY_STATE.UNAUTHORIZED);
      throw e;
    }
  }

  public void registerAction(RodaUser user, String aipId, LOG_ENTRY_STATE state, Object... parameters) {
    long duration = new Date().getTime() - startDate.getTime();
    RodaCoreService.registerAction(user, this.enclosingMethod.getDeclaringClass().getName(),
      this.enclosingMethod.getName(), aipId, duration, state, parameters);
  }

  public void registerAction(RodaUser user, LOG_ENTRY_STATE state, Object... parameters) {
    registerAction(user, null, state, parameters);
  }

  public void registerAction(RodaUser user, LOG_ENTRY_STATE state) {
    registerAction(user, (String) null, state);
  }
}
